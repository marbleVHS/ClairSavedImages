package com.marblevhs.clairsavedimages.profileScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marblevhs.clairsavedimages.data.UserProfile
import com.marblevhs.clairsavedimages.repositories.ProfileRepo
import com.marblevhs.clairsavedimages.repositories.SettingsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel(
    private val profileRepo: ProfileRepo,
    private val settingsRepo: SettingsRepo
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val profileRepo: ProfileRepo,
        private val settingsRepo: SettingsRepo
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(profileRepo = profileRepo, settingsRepo = settingsRepo) as T
        }
    }


    private val profileDefaultState = ProfileUiState.InitLoadingState

    private val _profileUiState = MutableStateFlow<ProfileUiState>(profileDefaultState)
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    val notificationToggleState = settingsRepo.getNotificationsToggleFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun initProfile() {
        if (profileUiState.value !is ProfileUiState.Success) {
            viewModelScope.launch {
                try {
                    _profileUiState.value = ProfileUiState.Success(profileRepo.getProfile())
                } catch (e: Exception) {
                    _profileUiState.value = ProfileUiState.Error(exception = e)
                }
            }
        }

    }

    fun notificationSwitchInvoked(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.setNotificationToggle(value)
        }
    }


}

sealed class ProfileUiState {
    data class Success(val userProfile: UserProfile) :
        ProfileUiState()

    data class Error(val exception: Throwable) : ProfileUiState()
    object InitLoadingState : ProfileUiState()
}