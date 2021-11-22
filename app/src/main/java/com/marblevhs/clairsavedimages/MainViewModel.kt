package com.marblevhs.clairsavedimages

import androidx.lifecycle.ViewModel
import com.marblevhs.clairsavedimages.data.Image
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class MainViewModel: ViewModel() {

    private val _selectedImageFlow = MutableSharedFlow<Image>(1)
    val selectedImageFlow: SharedFlow<Image> = _selectedImageFlow

    fun newImageSelected(image: Image){
        CoroutineScope(SupervisorJob()).launch {
            _selectedImageFlow.emit(image)
        }
    }

}