package com.marblevhs.clairsavedimages.imageList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.marblevhs.clairsavedimages.data.Album
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.monoRepo.Repo
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


    var firstImagesInit = true


    private val albumState: MutableStateFlow<Album> = MutableStateFlow(Album.DEBUG)
    private val rev = MutableStateFlow(1)


    @OptIn(ExperimentalCoroutinesApi::class)
    val imagesFlow: StateFlow<PagingData<LocalImage>> =
        albumState.combine(rev) { album, rev -> Pair(album, rev) }
            .flatMapLatest { queryPair ->
                repo.getImagesPaging(album = queryPair.first, rev = queryPair.second)
                    .cachedIn(viewModelScope)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, PagingData.empty())

    val listUiState: StateFlow<ImageListUiState> =
        albumState.combine(rev) { album, rev ->
            ImageListUiState.Success(rev, album.albumId)
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, ImageListUiState.InitLoadingState)

    fun switchAlbumState() {
        albumState.value = if (albumState.value == Album.DEBUG) {
            Album.PUBLIC
        } else {
            Album.DEBUG
        }
    }


    fun switchRev() {
        rev.value = if (rev.value == 1) {
            0
        } else {
            1
        }
    }

    fun saveLatestSeenImage() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateLastImage()
        }
    }


}

sealed class ImageListUiState {
    data class Success(val rev: Int, val albumId: String) : ImageListUiState()
    data class Error(val exception: Throwable) : ImageListUiState()
    object InitLoadingState : ImageListUiState()
}