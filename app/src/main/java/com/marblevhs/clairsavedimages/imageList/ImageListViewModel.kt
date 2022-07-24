package com.marblevhs.clairsavedimages.imageList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.marblevhs.clairsavedimages.data.Album
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.imageRepo.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImageListViewModel(private val repo: Repo) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repo: Repo) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ImageListViewModel(repo = repo) as T
        }
    }


    private val listDefaultState = ImageListUiState.InitLoadingState
    private val _listUiState = MutableStateFlow<ImageListUiState>(listDefaultState)
    val listUiState: StateFlow<ImageListUiState> = _listUiState.asStateFlow()


    var firstImagesInit = true

    private var albumState: Album = Album.CLAIR

    private val query = MutableStateFlow(Pair(albumState, 1))


    @OptIn(ExperimentalCoroutinesApi::class)
    val images: StateFlow<PagingData<LocalImage>> = query
        .flatMapLatest { queryPair ->
            repo.getImagesPaging(album = queryPair.first, rev = queryPair.second)
                .cachedIn(viewModelScope)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PagingData.empty())
        .also { viewModelScope.launch { repo.updateLastImage() } }

    private suspend fun setQuery(queryPair: Pair<Album, Int>) {
        query.emit(queryPair)
        _listUiState.value =
            ImageListUiState.Success(rev = queryPair.second, album = albumState.albumId)
    }

    fun initImages(rev: Int) {
        if (firstImagesInit) {
            viewModelScope.launch(Dispatchers.IO) {
                val query = Pair<Album, Int>(albumState, rev)
                setQuery(query)
            }
        }
    }


    fun switchAlbumState(rev: Int) {
        albumState = if (albumState == Album.CLAIR) {
            Album.PUBLIC
        } else {
            Album.CLAIR
        }
        loadImages(rev = rev)
    }

    fun loadImages(rev: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setQuery(Pair(albumState, rev))
            } catch (e: Exception) {
                _listUiState.emit(ImageListUiState.Error(exception = e))
            }

        }
    }


}

sealed class ImageListUiState {
    data class Success(val rev: Int, val album: String) : ImageListUiState()
    data class Error(val exception: Throwable) : ImageListUiState()
    object InitLoadingState : ImageListUiState()
}