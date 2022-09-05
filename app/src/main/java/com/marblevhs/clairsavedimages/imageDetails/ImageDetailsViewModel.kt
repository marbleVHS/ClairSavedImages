package com.marblevhs.clairsavedimages.imageDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.repositories.ImagesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
                val imageBelongingToLists = coroutineScope {
                    val isLiked =
                        async { imagesRepo.getIsLiked(itemId = image.id, ownerId = image.ownerId) }
                    val isFav =
                        async { imagesRepo.getIsFav(itemId = image.id, ownerId = image.ownerId) }
                    return@coroutineScope mapOf(
                        "isLiked" to isLiked.await(),
                        "isFav" to isFav.await()
                    )
                }
                imageInstance = image
                _detailsUiState.value =
                    ImageDetailsUiState.Success(
                        isLiked = imageBelongingToLists["isLiked"] ?: false,
                        isFav = imageBelongingToLists["isFav"] ?: false,
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
                var isLiked =
                    imagesRepo.getIsLiked(
                        itemId = imageInstance.id,
                        ownerId = imageInstance.ownerId
                    )
                val isFav = imagesRepo.getIsFav(imageInstance.id, imageInstance.ownerId)
                if (isLiked) {
                    launch {
                        imagesRepo.deleteLike(
                            itemId = imageInstance.id,
                            ownerId = imageInstance.ownerId
                        )
                    }
                    isLiked = !isLiked
                    _detailsUiState.value =
                        ImageDetailsUiState.Success(
                            isLiked = isLiked,
                            isFav = isFav,
                            image = imageInstance
                        )
                } else {
                    launch {
                        imagesRepo.addLike(
                            itemId = imageInstance.id,
                            ownerId = imageInstance.ownerId
                        )
                    }
                    isLiked = !isLiked
                    _detailsUiState.value =
                        ImageDetailsUiState.Success(
                            isLiked = isLiked,
                            isFav = isFav,
                            image = imageInstance
                        )
                }
            } catch (e: Exception) {
                _detailsUiState.value = ImageDetailsUiState.Error(e)
            }
        }
    }

    fun favouritesButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isLiked =
                    imagesRepo.getIsLiked(
                        itemId = imageInstance.id,
                        ownerId = imageInstance.ownerId
                    )
                var isFav = imagesRepo.getIsFav(imageInstance.id, imageInstance.ownerId)
                if (isFav) {
                    launch { imagesRepo.deleteFav(imageInstance) }
                    isFav = !isFav
                    _detailsUiState.value =
                        ImageDetailsUiState.Success(
                            isLiked = isLiked,
                            isFav = isFav,
                            image = imageInstance
                        )
                } else {
                    launch { imagesRepo.addFav(imageInstance) }
                    isFav = !isFav
                    _detailsUiState.value =
                        ImageDetailsUiState.Success(
                            isLiked = isLiked,
                            isFav = isFav,
                            image = imageInstance
                        )
                }
            } catch (e: Exception) {
                _detailsUiState.value = ImageDetailsUiState.Error(e)
            }
        }
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