package com.marblevhs.clairsavedimages


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marblevhs.clairsavedimages.monoRepo.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainViewModel(private val repo: Repo) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repo: Repo) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repo = repo) as T
        }
    }


    fun clearLoginData() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.clearLoginData()
        }
    }

    fun saveAccessToken(accessKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.saveAccessToken(accessKey)
        }
    }


    val isLoggedFlow: StateFlow<Boolean> =
        repo.getIsLoggedFlow().stateIn(viewModelScope, SharingStarted.Eagerly, false)


    fun setDefaultNightMode(defaultNightMode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.setDefaultNightMode(defaultNightMode)
        }
    }


    val defaultNightModeFlow: StateFlow<Int> =
        repo.getDefaultNightModeFlow().stateIn(viewModelScope, SharingStarted.Eagerly, -1)


}









