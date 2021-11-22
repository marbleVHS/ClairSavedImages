package com.marblevhs.clairsavedimages

import androidx.lifecycle.ViewModel
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.data.Size
import com.marblevhs.clairsavedimages.imageRepo.Repo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel: ViewModel() {
    private val repo = Repo()
    private var imageId = ""

    private val detailsDefaultState = ImageDetailsUiState.Success(isLiked = false,
        image = Image("", listOf(Size(type = "x", "https://thiscatdoesnotexist.com/"))))
    private val _detailsUiState = MutableStateFlow<ImageDetailsUiState>(detailsDefaultState)
    val detailsUiState: StateFlow<ImageDetailsUiState> = _detailsUiState

    private val detailsExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable -> _detailsUiState.value = ImageDetailsUiState.Error(throwable)}
    private val detailsCoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + detailsExceptionHandler)

    private val listDefaultState = ImageListUiState.Success(emptyList())
    private val _listUiState = MutableStateFlow<ImageListUiState>(listDefaultState)
    val listUiState: StateFlow<ImageListUiState> = _listUiState

    private val listExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable -> _listUiState.value = ImageListUiState.Error(throwable)}
    private val listCoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + listExceptionHandler)



    fun newImageSelected(image: Image){
        detailsCoroutineScope.launch {
            _detailsUiState.emit(ImageDetailsUiState.LoadingState)
            val isLiked = repo.getIsLiked(image.id)
            imageId = image.id
            _detailsUiState.value = ImageDetailsUiState.Success(isLiked = isLiked,image)
        }
    }

    fun loadImages(){
        _listUiState.value = ImageListUiState.LoadingState
        listCoroutineScope.launch {
            _listUiState.value = ImageListUiState.Success(repo.getImages())
        }
    }

    private fun loadIsLiked(imageId: String){
        detailsCoroutineScope.launch {
            val isLiked = repo.getIsLiked(imageId)
            _detailsUiState.emit(ImageDetailsUiState.IsLikedChanged(isLiked))
        }
    }

    fun likeButtonClicked(){
        detailsCoroutineScope.launch {
            repo.addLike(itemId = imageId)
            loadIsLiked(imageId = imageId)
        }
    }

}

sealed class ImageListUiState {
    data class Success(val images: List<Image>): ImageListUiState()
    data class Error(val exception: Throwable): ImageListUiState()
    object LoadingState : ImageListUiState()
}


sealed class ImageDetailsUiState {
    data class Success(val isLiked: Boolean, val image: Image): ImageDetailsUiState()
    data class Error(val exception: Throwable): ImageDetailsUiState()
    data class IsLikedChanged(val isLiked: Boolean): ImageDetailsUiState()
    object LoadingState : ImageDetailsUiState()
}


