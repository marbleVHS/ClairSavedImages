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

    private val _query = MutableStateFlow("-1")
    private val query: StateFlow<String> = _query.asStateFlow()

    private var albumState: Album = Album.CLAIR


    @OptIn(ExperimentalCoroutinesApi::class)
    val images: StateFlow<PagingData<LocalImage>> = query
        .flatMapLatest { queryString ->
            repo.getImagesPaging(queryString).cachedIn(viewModelScope)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PagingData.empty())

    private suspend fun setQuery(queryString: String, rev: Int) {
        _query.emit(queryString)
        _listUiState.value = ImageListUiState.Success(rev = rev, album = albumState.albumId)
    }

    fun initImages(rev: Int) {
        if (firstImagesInit) {
            viewModelScope.launch(Dispatchers.IO) {
                var queryString = ""
                queryString += if (albumState.albumId == "saved") {
                    "saved"
                } else {
                    "public"
                }
                queryString += if (rev == 1) {
                    "_descendant"
                } else {
                    "_ascendant"
                }
                setQuery(queryString = queryString, rev = rev)
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
            var queryString = ""
            queryString += if (albumState.albumId == "saved") {
                "saved"
            } else {
                "public"
            }
            queryString += if (rev == 1) {
                "_descendant"
            } else {
                "_ascendant"
            }
            try {
                setQuery(queryString = queryString, rev = rev)
            } catch (e: Exception) {
                _listUiState.emit(ImageListUiState.Error(exception = e))
            }

        }
    }

    sealed class ImageListUiState {
        data class Success(val rev: Int, val album: String) : ImageListUiState()
        data class Error(val exception: Throwable) : ImageListUiState()
        object InitLoadingState : ImageListUiState()
    }

}