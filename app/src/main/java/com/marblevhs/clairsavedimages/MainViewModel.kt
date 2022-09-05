package com.marblevhs.clairsavedimages


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marblevhs.clairsavedimages.repositories.ProfileRepo
import com.marblevhs.clairsavedimages.repositories.SettingsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainViewModel(
    private val settingsRepo: SettingsRepo,
    private val profileRepo: ProfileRepo
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val settingsRepo: SettingsRepo,
        private val profileRepo: ProfileRepo
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(settingsRepo = settingsRepo, profileRepo = profileRepo) as T
        }
    }


    fun clearLoginData() {
        viewModelScope.launch(Dispatchers.IO) {
            profileRepo.clearLoginData()
        }
    }

    fun saveAccessToken(accessKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            profileRepo.saveAccessToken(accessKey)
        }
    }


    val isLoggedFlow: StateFlow<Boolean> =
        profileRepo.getIsLoggedFlow().stateIn(viewModelScope, SharingStarted.Eagerly, false)


    fun setDefaultNightMode(defaultNightMode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.setDefaultNightMode(defaultNightMode)
        }
    }


    val defaultNightModeFlow: StateFlow<Int> =
        settingsRepo.getDefaultNightModeFlow().stateIn(viewModelScope, SharingStarted.Eagerly, -1)


}









