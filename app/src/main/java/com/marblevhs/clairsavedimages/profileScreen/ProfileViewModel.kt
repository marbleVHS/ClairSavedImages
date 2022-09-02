package com.marblevhs.clairsavedimages.profileScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marblevhs.clairsavedimages.data.UserProfile
import com.marblevhs.clairsavedimages.monoRepo.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel(private val repo: Repo) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repo: Repo) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(repo = repo) as T
        }
    }


    private val profileDefaultState = ProfileUiState.InitLoadingState

    private val _profileUiState = MutableStateFlow<ProfileUiState>(profileDefaultState)
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    fun initProfile() {
        if (profileUiState.value !is ProfileUiState.Success) {
            viewModelScope.launch {
                try {
                    _profileUiState.value = ProfileUiState.Success(repo.getProfile())
                } catch (e: Exception) {
                    _profileUiState.value = ProfileUiState.Error(exception = e)
                }
            }
        }

    }
    


}

sealed class ProfileUiState {
    data class Success(val userProfile: UserProfile) : ProfileUiState()
    data class Error(val exception: Throwable) : ProfileUiState()
    object InitLoadingState : ProfileUiState()
}