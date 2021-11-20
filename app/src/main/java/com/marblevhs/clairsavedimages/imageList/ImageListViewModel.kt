package com.marblevhs.clairsavedimages.imageList

import androidx.lifecycle.ViewModel
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.imageRepo.Repo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class ImageListViewModel : ViewModel() {

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private val defaultState = ImageListUiState.Success(emptyList())
    private val _uiState = MutableStateFlow<ImageListUiState>(defaultState)
    val uiState: StateFlow<ImageListUiState> = _uiState
    private val repo = Repo()

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