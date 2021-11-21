package com.marblevhs.clairsavedimages.imageDetails

import androidx.lifecycle.ViewModel
import com.marblevhs.clairsavedimages.data.Image

class ImageDetailsViewModel: ViewModel() {




}

sealed class ImageDetailsUiState {
    data class Success(val image: Image): ImageDetailsUiState()
    data class Error(val exception: Throwable): ImageDetailsUiState()
    object LoadingState : ImageDetailsUiState()
}
