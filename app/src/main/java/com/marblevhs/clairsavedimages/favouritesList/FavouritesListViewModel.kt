package com.marblevhs.clairsavedimages.favouritesList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.imageRepo.Repo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class FavouritesListViewModel(private val repo: Repo) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repo: Repo) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavouritesListViewModel(repo = repo) as T
        }
    }

    private var firstFavouritesInit = true

    private val favouritesDefaultState = FavouritesListUiState.InitLoadingState

    private val _favouritesUiState = MutableStateFlow<FavouritesListUiState>(favouritesDefaultState)
    val favouritesUiState: StateFlow<FavouritesListUiState> = _favouritesUiState.asStateFlow()


    fun initFavs(revUi: Int) {
        var rev = revUi
        if (firstFavouritesInit) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val favs = coroutineScope {
                        val favsAsync = async { repo.getFavs(rev) }
                        delay(250)
                        return@coroutineScope favsAsync.await()
                    }
                    _favouritesUiState.value =
                        FavouritesListUiState.Success(images = favs, rev = rev)
                } catch (e: Exception) {
                    _favouritesUiState.value = FavouritesListUiState.Error(e)
                }
                firstFavouritesInit = false
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                if (favouritesUiState.value is FavouritesListUiState.Success) {
                    rev = (favouritesUiState.value as FavouritesListUiState.Success).rev
                }
                try {
                    val favs = coroutineScope {
                        val favsAsync = async { repo.getFavs(rev) }
                        delay(250)
                        return@coroutineScope favsAsync.await()
                    }
                    _favouritesUiState.value =
                        FavouritesListUiState.Success(images = favs, rev = rev)
                } catch (e: Exception) {
                    _favouritesUiState.value = FavouritesListUiState.Error(e)
                }
                firstFavouritesInit = false
            }
        }
    }

    fun loadFavs(rev: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val favs = repo.getFavs(rev)
            delay(200)
            _favouritesUiState.value = FavouritesListUiState.Success(images = favs, rev = rev)
        }
    }



}

sealed class FavouritesListUiState {
    data class Success(val images: List<LocalImage>, val rev: Int) : FavouritesListUiState()
    data class Error(val exception: Throwable) : FavouritesListUiState()
    object InitLoadingState : FavouritesListUiState()
}