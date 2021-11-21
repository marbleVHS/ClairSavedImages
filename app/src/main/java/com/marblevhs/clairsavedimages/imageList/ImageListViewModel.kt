package com.marblevhs.clairsavedimages.imageList

import androidx.lifecycle.ViewModel
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.imageRepo.Repo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

class ImageListViewModel : ViewModel() {

    private val defaultState = ImageListUiState.Success(emptyList())
    private val _uiState = MutableStateFlow<ImageListUiState>(defaultState)
    val uiState: StateFlow<ImageListUiState> = _uiState
    private val repo = Repo()
    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext,
                                    throwable -> _uiState.value = ImageListUiState.Error(throwable) }
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    fun loadImages(){
        _uiState.value = ImageListUiState.LoadingState()
        coroutineScope.launch {
            _uiState.value = ImageListUiState.Success(repo.getImages())
        }

    }


}


sealed class ImageListUiState {
    data class Success(val images: List<Image>): ImageListUiState()
    data class Error(val exception: Throwable): ImageListUiState()
    class LoadingState: ImageListUiState()
}

