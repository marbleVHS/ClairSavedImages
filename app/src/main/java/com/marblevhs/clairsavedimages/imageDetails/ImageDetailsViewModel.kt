package com.marblevhs.clairsavedimages.imageDetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.repositories.ImagesRepo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ImageDetailsViewModel(private val imagesRepo: ImagesRepo) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val imagesRepo: ImagesRepo) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ImageDetailsViewModel(imagesRepo = imagesRepo) as T
        }
    }

    private var imageInstance = LocalImage(
        id = "",
        ownerId = "",
        album = "",
        width = 1,
        height = 1,
        thumbnailUrl = "",
        fullSizeUrl = ""
    )


    fun newImageSelected(image: LocalImage) {
        viewModelScope.launch(Dispatchers.IO) {
            _detailsUiState.value = ImageDetailsUiState.LoadingState
            try {
                imageInstance = image
                val isLiked = coroutineScope {
                    withContext(Dispatchers.IO) { isCurrentImageLiked() }
                }
                val isFav = coroutineScope {
                    withContext(Dispatchers.IO) { isCurrentImageFav() }
                }
                _detailsUiState.value =
                    ImageDetailsUiState.Success(
                        isLiked = isLiked,
                        isFav = isFav,
                        image = image
                    )
            } catch (e: Exception) {
                _detailsUiState.value = ImageDetailsUiState.Error(e)
            }

        }
    }


    fun likeButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isLiked = async { isCurrentImageLiked() }
                val isFav = async { isCurrentImageFav() }
                if (isLiked.await()) {
                    launch {
                        imagesRepo.deleteLike(
                            itemId = imageInstance.id,
                            ownerId = imageInstance.ownerId
                        )
                    }
                } else {
                    launch {
                        imagesRepo.addLike(
                            itemId = imageInstance.id,
                            ownerId = imageInstance.ownerId
                        )
                    }
                }
                _detailsUiState.value =
                    ImageDetailsUiState.Success(
                        isLiked = !isLiked.await(),
                        isFav = isFav.await(),
                        image = imageInstance
                    )
            } catch (e: Exception) {
                Log.e("RESP", e.stackTrace.toString())
                _detailsUiState.value = ImageDetailsUiState.Error(e)
            }
        }
    }

    fun favouritesButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isFav = isCurrentImageFav()
                val isLiked = isCurrentImageLiked()
                if (isFav) {
                    launch { imagesRepo.deleteFav(imageInstance) }
                } else {
                    launch { imagesRepo.addFav(imageInstance) }
                }
                _detailsUiState.value =
                    ImageDetailsUiState.Success(
                        isLiked = isLiked,
                        isFav = !isFav,
                        image = imageInstance
                    )
            } catch (e: Exception) {
                _detailsUiState.value = ImageDetailsUiState.Error(e)
            }
        }
    }

    private suspend fun isCurrentImageLiked(): Boolean {
        val isLiked = when (detailsUiState.value) {
            is ImageDetailsUiState.Success -> {
                (detailsUiState.value as ImageDetailsUiState.Success).isLiked
            }
            else -> {
                imagesRepo.getIsLiked(
                    imageInstance.id,
                    imageInstance.ownerId
                )
            }
        }
        return isLiked
    }

    private suspend fun isCurrentImageFav(): Boolean {
        val isFav = when (detailsUiState.value) {
            is ImageDetailsUiState.Success -> {
                (detailsUiState.value as ImageDetailsUiState.Success).isFav
            }
            else -> {
                imagesRepo.getIsFav(
                    imageInstance.id,
                    imageInstance.ownerId
                )
            }
        }
        return isFav
    }

    private val detailsDefaultState = ImageDetailsUiState.LoadingState
    private val _detailsUiState = MutableStateFlow<ImageDetailsUiState>(detailsDefaultState)
    val detailsUiState: StateFlow<ImageDetailsUiState> = _detailsUiState.asStateFlow()


}

sealed class ImageDetailsUiState {
    data class Success(val isLiked: Boolean, val isFav: Boolean, val image: LocalImage) :
        ImageDetailsUiState()

    data class Error(val exception: Throwable) : ImageDetailsUiState()
    object LoadingState : ImageDetailsUiState()
}