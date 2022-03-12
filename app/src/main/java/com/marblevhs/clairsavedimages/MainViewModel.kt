package com.marblevhs.clairsavedimages


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.marblevhs.clairsavedimages.data.Album
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.imageRepo.Repo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject


class MainViewModel (private val repo: Repo): ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repo: Repo): ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repo = repo) as T
        }
    }




    private var albumState = Album.CLAIR
    private var imageInstance = LocalImage(
        id = "",
        ownerId = "",
        album = "",
        width = 1,
        height = 1,
        thumbnailUrl = "",
        fullSizeUrl = "")
    var firstImagesInit = true
    var firstFavouritesInit = true


    private val detailsDefaultState = ImageDetailsUiState.Success(isLiked = false, isFav = false,
        image = imageInstance)
    private val _detailsUiState = MutableStateFlow<ImageDetailsUiState>(detailsDefaultState)
    val detailsUiState: StateFlow<ImageDetailsUiState> = _detailsUiState.asStateFlow()

    private val detailsExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> _detailsUiState.value = ImageDetailsUiState.Error(throwable)}
    private val detailsCoroutineScope =
        CoroutineScope(SupervisorJob() + detailsExceptionHandler)

    private val listDefaultState = ImageListUiState.InitLoadingState
    private val _listUiState = MutableStateFlow<ImageListUiState>(listDefaultState)
    val listUiState: StateFlow<ImageListUiState> = _listUiState.asStateFlow()


    private val listExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> _listUiState.value = ImageListUiState.Error(throwable)}
    private val listCoroutineScope =
        CoroutineScope(SupervisorJob() + listExceptionHandler)

    private val favouritesDefaultState = FavouritesListUiState.Success(emptyList(), rev = 1)
    private val _favouritesUiState = MutableStateFlow<FavouritesListUiState>(favouritesDefaultState)
    val favouritesUiState: StateFlow<FavouritesListUiState> = _favouritesUiState.asStateFlow()

    private val favouritesExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> _favouritesUiState.value = FavouritesListUiState.Error(throwable)}
    private val favouritesCoroutineScope =
        CoroutineScope(SupervisorJob() + favouritesExceptionHandler)

    fun newImageSelected(image: LocalImage){
        detailsCoroutineScope.launch {
            _detailsUiState.value = ImageDetailsUiState.LoadingState
            val isLiked = repo.getIsLiked(itemId = image.id, ownerId = image.ownerId)
            val isFav = repo.getIsFav(itemId = image.id, ownerId = image.ownerId)
            imageInstance = image
            _detailsUiState.value = ImageDetailsUiState.Success(isLiked = isLiked, isFav = isFav,image = image)
        }
    }



    private val _query = MutableStateFlow("")
    private val query: StateFlow<String> = _query.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val images: StateFlow<PagingData<LocalImage>> = query
        .flatMapLatest { queryString -> repo.getImagesPaging(queryString).cachedIn(listCoroutineScope) }
        .stateIn(listCoroutineScope, SharingStarted.Eagerly, PagingData.empty())

    private fun setQuery(query: String, rev: Int) {
        _query.tryEmit(query)
        _listUiState.value = ImageListUiState.Success(rev = rev, album = albumState.albumId)
    }

    fun initImages(rev: Int){
        if(firstImagesInit) {
            listCoroutineScope.launch {
                var queryString = ""
                queryString += if(albumState.albumId == "saved"){
                    "saved"
                } else {
                    "public"
                }
                queryString += if(rev == 1){
                    "_descendant"
                } else {
                    "_ascendant"
                }
                setQuery(query = queryString, rev = rev)
            }
        }
    }



    fun switchAlbumState(rev: Int){
        albumState = if(albumState == Album.CLAIR){
            Album.PUBLIC
        } else{
            Album.CLAIR
        }
        loadImages(rev = rev)
    }

    fun loadImages(rev: Int){
        listCoroutineScope.launch {
            var queryString = ""
                queryString += if(albumState.albumId == "saved"){
                    "saved"
                } else {
                    "public"
                }
                queryString += if(rev == 1){
                    "_descendant"
                } else {
                    "_ascendant"
                }
                setQuery(query = queryString, rev = rev)

        }
    }

    fun likeButtonClicked(){
        detailsCoroutineScope.launch {
            var isLiked = repo.getIsLiked(itemId = imageInstance.id, ownerId = imageInstance.ownerId)
            val isFav = repo.getIsFav(imageInstance.id, imageInstance.ownerId)
            if(isLiked){
                launch { repo.deleteLike(itemId = imageInstance.id, ownerId = imageInstance.ownerId) }
                isLiked = !isLiked
                _detailsUiState.value =
                    ImageDetailsUiState.Success(isLiked = isLiked, isFav = isFav, image =  imageInstance)
            } else {
                launch { repo.addLike(itemId = imageInstance.id, ownerId = imageInstance.ownerId) }
                isLiked = !isLiked
                _detailsUiState.value =
                    ImageDetailsUiState.Success(isLiked = isLiked, isFav = isFav, image =  imageInstance)
            }
        }
    }

    fun favouritesButtonClicked(){
        detailsCoroutineScope.launch {
            val isLiked = repo.getIsLiked(itemId = imageInstance.id, ownerId = imageInstance.ownerId)
            var isFav = repo.getIsFav(imageInstance.id, imageInstance.ownerId)
            if(isFav){
                launch { repo.deleteFav(imageInstance) }
                isFav = !isFav
                _detailsUiState.value =
                    ImageDetailsUiState.Success(isLiked = isLiked, isFav = isFav, image =  imageInstance)
            } else {
                launch { repo.addFav(imageInstance) }
                isFav = !isFav
                _detailsUiState.value =
                    ImageDetailsUiState.Success(isLiked = isLiked, isFav = isFav, image =  imageInstance)
            }
        }
    }

    fun initFavs(revUi: Int){
        var rev = revUi
        if(firstFavouritesInit) {
            favouritesCoroutineScope.launch {
                val favs = repo.getFavs(rev)
                _favouritesUiState.value = FavouritesListUiState.Success(images = favs, rev = rev)
                firstFavouritesInit = false
            }
        } else {
            rev = (favouritesUiState.value as FavouritesListUiState.Success).rev
            favouritesCoroutineScope.launch {
                val favs = repo.getFavs(rev = rev)
                _favouritesUiState.value = FavouritesListUiState.Success(images = favs, rev = rev)
            }
        }
    }

    fun loadFavs(rev: Int){
        favouritesCoroutineScope.launch {
            val favs = repo.getFavs(rev)
            delay(200)
            _favouritesUiState.value = FavouritesListUiState.Success(images = favs, rev = rev)

        }
    }

}


sealed class ImageListUiState {
    data class Success(val rev: Int, val album: String): ImageListUiState()
    data class Error(val exception: Throwable): ImageListUiState()
    object InitLoadingState : ImageListUiState()
}

sealed class FavouritesListUiState {
    data class Success(val images: List<LocalImage>, val rev: Int): FavouritesListUiState()
    data class Error(val exception: Throwable): FavouritesListUiState()
}

sealed class ImageDetailsUiState {
    data class Success(val isLiked: Boolean, val isFav: Boolean, val image: LocalImage): ImageDetailsUiState()
    data class Error(val exception: Throwable): ImageDetailsUiState()
    object LoadingState : ImageDetailsUiState()
}


