package com.marblevhs.clairsavedimages

import androidx.lifecycle.ViewModel
import com.marblevhs.clairsavedimages.data.Image
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class MainViewModel: ViewModel() {

     val selectedImageFlow = MutableSharedFlow<Image>(1)

    fun newImageSelected(image: Image){
        CoroutineScope(SupervisorJob()).launch {
            selectedImageFlow.emit(image)
        }
    }

}