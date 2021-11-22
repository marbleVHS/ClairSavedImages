package com.marblevhs.clairsavedimages.imageDetails

import androidx.lifecycle.ViewModel
import com.marblevhs.clairsavedimages.imageRepo.Repo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ImageDetailsViewModel: ViewModel() {
    private val defaultState = ImageDetailsUiState.Success(isLiked = false)
    private val _uiState = MutableStateFlow<ImageDetailsUiState>(defaultState)
    val uiState: StateFlow<ImageDetailsUiState> = _uiState
    private val repo = Repo()
    private var isLiked = false
    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext,
                                    throwable -> }
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    fun loadIsLiked(imageId: String){
        coroutineScope.launch {
            isLiked = repo.getIsLiked(imageId)
            _uiState.value = ImageDetailsUiState.Success(isLiked = isLiked)
        }
    }

    fun likeButtonClicked(imageId: String){

            coroutineScope.launch {
                repo.addLike(itemId = imageId)
                loadIsLiked(imageId = imageId)
            }

    }

}

sealed class ImageDetailsUiState {
    data class Success(val isLiked: Boolean): ImageDetailsUiState()
    data class Error(val exception: Throwable): ImageDetailsUiState()
}
