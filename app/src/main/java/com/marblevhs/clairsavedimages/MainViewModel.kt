package com.marblevhs.clairsavedimages


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marblevhs.clairsavedimages.imageRepo.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainViewModel(private val repo: Repo) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repo: Repo) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repo = repo) as T
        }
    }


    fun getIsLogged() {
        viewModelScope.launch(Dispatchers.IO) {
            val isLogged = async { repo.getIsLogged() }
            _isLoggedFlow.value = isLogged.await()
        }
    }

    fun clearLoginData() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.clearLoginData()
            getIsLogged()
        }
    }

    fun saveAccessToken(accessKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.saveAccessToken(accessKey)
            getIsLogged()
        }
    }

    private val _isLoggedFlow = MutableStateFlow(value = false)
    val isLoggedFlow: StateFlow<Boolean> = _isLoggedFlow.asStateFlow()

    fun getDefaultNightMode() {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultNightMode = async { repo.getDefaultNightMode() }
            _defaultNightMode.value = defaultNightMode.await()
        }
    }



    fun setDefaultNightMode(defaultNightMode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.setDefaultNightMode(defaultNightMode)
            getDefaultNightMode()
        }
    }

    private val _defaultNightMode = MutableStateFlow(value = -1)
    val defaultNightMode: StateFlow<Int> = _defaultNightMode.asStateFlow()













}









