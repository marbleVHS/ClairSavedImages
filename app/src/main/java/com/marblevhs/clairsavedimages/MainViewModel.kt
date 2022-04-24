package com.marblevhs.clairsavedimages


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.imageRepo.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainViewModel(private val repo: Repo) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repo: Repo) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repo = repo) as T
        }
    }


    fun getIsLogged() {
        viewModelScope.launch(Dispatchers.IO) {
            val isLogged = async { repo.getIsLogged() }
            _isLoggedFlow.value = isLogged.await()
        }
    }

    fun clearLoginData() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.clearLoginData()
            getIsLogged()
        }
    }

    fun saveAccessToken(accessKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.saveAccessToken(accessKey)
            getIsLogged()
        }
    }

    private val _isLoggedFlow = MutableStateFlow(value = false)
    val isLoggedFlow: StateFlow<Boolean> = _isLoggedFlow.asStateFlow()

    fun getDefaultNightMode() {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultNightMode = async { repo.getDefaultNightMode() }
            _defaultNightMode.value = defaultNightMode.await()
        }
    }



    fun setDefaultNightMode(defaultNightMode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.setDefaultNightMode(defaultNightMode)
            getDefaultNightMode()
        }
    }

    private val _defaultNightMode = MutableStateFlow(value = -1)
    val defaultNightMode: StateFlow<Int> = _defaultNightMode.asStateFlow()


    private var imageInstance = LocalImage(
        id = "",
        ownerId = "",
        album = "",
        width = 1,
        height = 1,
        thumbnailUrl = "",
        fullSizeUrl = ""
    )

    private val detailsDefaultState = ImageDetailsUiState.LoadingState
    private val _detailsUiState = MutableStateFlow<ImageDetailsUiState>(detailsDefaultState)
    val detailsUiState: StateFlow<ImageDetailsUiState> = _detailsUiState.asStateFlow()

    fun newImageSelected(image: LocalImage) {
        viewModelScope.launch(Dispatchers.Default) {
            _detailsUiState.value = ImageDetailsUiState.LoadingState
            try {
                val imageBelongingToLists = coroutineScope {
                    val isLiked =
                        async { repo.getIsLiked(itemId = image.id, ownerId = image.ownerId) }
                    val isFav =
                        async { repo.getIsFav(itemId = image.id, ownerId = image.ownerId) }
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
                    repo.getIsLiked(itemId = imageInstance.id, ownerId = imageInstance.ownerId)
                val isFav = repo.getIsFav(imageInstance.id, imageInstance.ownerId)
                if (isLiked) {
                    launch {
                        repo.deleteLike(
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
                        repo.addLike(
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
                    repo.getIsLiked(itemId = imageInstance.id, ownerId = imageInstance.ownerId)
                var isFav = repo.getIsFav(imageInstance.id, imageInstance.ownerId)
                if (isFav) {
                    launch { repo.deleteFav(imageInstance) }
                    isFav = !isFav
                    _detailsUiState.value =
                        ImageDetailsUiState.Success(
                            isLiked = isLiked,
                            isFav = isFav,
                            image = imageInstance
                        )
                } else {
                    launch { repo.addFav(imageInstance) }
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


    sealed class ImageDetailsUiState {
        data class Success(val isLiked: Boolean, val isFav: Boolean, val image: LocalImage) :
            ImageDetailsUiState()

        data class Error(val exception: Throwable) : ImageDetailsUiState()
        object LoadingState : ImageDetailsUiState()
    }


}









