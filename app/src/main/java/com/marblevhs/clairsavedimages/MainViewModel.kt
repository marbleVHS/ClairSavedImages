package com.marblevhs.clairsavedimages

import androidx.lifecycle.ViewModel
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.data.Size
import com.marblevhs.clairsavedimages.imageRepo.Repo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel: ViewModel() {
    private val repo = Repo()
    private var imageInstance = Image("", listOf(Size(type = "x", "https://thiscatdoesnotexist.com/")))
    private var firstInit = true
    private val detailsDefaultState = ImageDetailsUiState.Success(isLiked = false,
        image = Image("", listOf(Size(type = "x", "https://thiscatdoesnotexist.com/"))))
    private val _detailsUiState = MutableStateFlow<ImageDetailsUiState>(detailsDefaultState)
    val detailsUiState: StateFlow<ImageDetailsUiState> = _detailsUiState.asStateFlow()

    private val detailsExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> _detailsUiState.value = ImageDetailsUiState.Error(throwable)}
    private val detailsCoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + detailsExceptionHandler)

    private val listDefaultState = ImageListUiState.Success(emptyList())
    private val _listUiState = MutableStateFlow<ImageListUiState>(listDefaultState)
    val listUiState: StateFlow<ImageListUiState> = _listUiState.asStateFlow()

    private val listExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> _listUiState.value = ImageListUiState.Error(throwable)}
    private val listCoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + listExceptionHandler)



    fun newImageSelected(image: Image){
        detailsCoroutineScope.launch {
            _detailsUiState.value = ImageDetailsUiState.LoadingState
            val isLiked = repo.getIsLiked(image.id)
            imageInstance = image
            _detailsUiState.value = ImageDetailsUiState.Success(isLiked = isLiked,image)
        }
    }

    fun initImages(){
        if(firstInit) {
            _listUiState.value = ImageListUiState.InitLoadingState
            listCoroutineScope.launch {
                _listUiState.value = ImageListUiState.Success(repo.getImages())
            }
            firstInit = false
        }
    }

    fun loadImages(){
        _listUiState.value = ImageListUiState.RefreshLoadingState
        listCoroutineScope.launch {
            _listUiState.value = ImageListUiState.Success(repo.getImages())
        }
    }

    fun likeButtonClicked(){
        detailsCoroutineScope.launch {
            var isLiked = repo.getIsLiked(imageInstance.id)
            if(isLiked){
                detailsCoroutineScope.launch { repo.deleteLike(itemId = imageInstance.id) }
                isLiked = !isLiked
                _detailsUiState.value =
                    ImageDetailsUiState.Success(isLiked = isLiked, imageInstance)
            } else {
                detailsCoroutineScope.launch { repo.addLike(itemId = imageInstance.id) }
                isLiked = !isLiked
                _detailsUiState.value =
                    ImageDetailsUiState.Success(isLiked = isLiked, imageInstance)
            }
        }
    }

}

sealed class ImageListUiState {
    data class Success(val images: List<Image>): ImageListUiState()
    data class Error(val exception: Throwable): ImageListUiState()
    object InitLoadingState : ImageListUiState()
    object RefreshLoadingState : ImageListUiState()
}


sealed class ImageDetailsUiState {
    data class Success(val isLiked: Boolean, val image: Image): ImageDetailsUiState()
    data class Error(val exception: Throwable): ImageDetailsUiState()
    object LoadingState : ImageDetailsUiState()
}


