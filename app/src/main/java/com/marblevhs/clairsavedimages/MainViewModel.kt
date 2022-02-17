package com.marblevhs.clairsavedimages


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.imageRepo.Repo
import com.marblevhs.clairsavedimages.secrets.Secrets
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


class MainViewModel (private val repo: Repo): ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repo: Repo): ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repo = repo) as T
        }
    }




    private var albumState = Album.CLAIR
    private var imageInstance = LocalImage(id = "",
        width = 1,
        height = 1,
        thumbnailUrl = "",
        fullSizeUrl = "")
    private var firstInit = true


    private val detailsDefaultState = ImageDetailsUiState.Success(isLiked = false,
        image = imageInstance)
    private val _detailsUiState = MutableStateFlow<ImageDetailsUiState>(detailsDefaultState)
    val detailsUiState: StateFlow<ImageDetailsUiState> = _detailsUiState.asStateFlow()

    private val detailsExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> _detailsUiState.value = ImageDetailsUiState.Error(throwable)}
    private val detailsCoroutineScope =
        CoroutineScope(SupervisorJob() + detailsExceptionHandler)

    private val listDefaultState = ImageListUiState.Success(emptyList(), rev = 1, album = "saved")
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
            val isLiked = repo.getIsLiked(itemId = image.id, ownerId = albumState.ownerId)
            imageInstance = image
            _detailsUiState.value = ImageDetailsUiState.Success(isLiked = isLiked,image)
        }
    }

    fun initImages(rev: Int){
        if(firstInit) {
            _listUiState.value = ImageListUiState.InitLoadingState
            listCoroutineScope.launch {
                val images = repo.getImages(
                    ownerId = albumState.ownerId,
                    albumId = albumState.albumId,
                    rev = rev)
                val localImages: MutableList<LocalImage> = emptyList<LocalImage>().toMutableList()
                images.forEach {

                    val localImage = LocalImage(id = it.id,
                        width = it.sizes[it.sizes.size - 1].width,
                        height = it.sizes[it.sizes.size - 1].height,
                        thumbnailUrl = it.sizes[6].imageUrl,
                        fullSizeUrl = it.sizes[it.sizes.size - 1].imageUrl)
                    localImages.add(localImage)
                }
                _listUiState.value = ImageListUiState.Success(images = localImages, rev = rev, album = albumState.albumId)
                firstInit = false
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
        _listUiState.value = ImageListUiState.RefreshLoadingState
        listCoroutineScope.launch {
            val images = repo.getImages(
                ownerId = albumState.ownerId,
                albumId = albumState.albumId,
                rev = rev)
            val localImages: MutableList<LocalImage> = emptyList<LocalImage>().toMutableList()
            images.forEach {
                val localImage = LocalImage(id = it.id,
                    width = it.sizes[it.sizes.size - 1].width,
                    height = it.sizes[it.sizes.size - 1].height,
                    thumbnailUrl = it.sizes[6].imageUrl,
                    fullSizeUrl = it.sizes[it.sizes.size - 1].imageUrl)
                localImages.add(localImage)
            }
            _listUiState.value = ImageListUiState.Success(
                images = localImages,
                rev = rev,
                album = albumState.albumId
            )
        }
    }

    fun likeButtonClicked(){
        detailsCoroutineScope.launch {
            var isLiked = repo.getIsLiked(itemId = imageInstance.id, ownerId = albumState.ownerId)
            if(isLiked){
                launch { repo.deleteLike(itemId = imageInstance.id, ownerId = albumState.ownerId) }
                isLiked = !isLiked
                _detailsUiState.value =
                    ImageDetailsUiState.Success(isLiked = isLiked, imageInstance)
            } else {
                launch { repo.addLike(itemId = imageInstance.id, ownerId = albumState.ownerId) }
                isLiked = !isLiked
                _detailsUiState.value =
                    ImageDetailsUiState.Success(isLiked = isLiked, imageInstance)
            }
        }
    }

    fun favouritesButtonClicked(){

    }

}



enum class Album(val ownerId: String, val albumId: String){
    CLAIR(Secrets.CLAIR_ID, "saved"),
    PUBLIC(Secrets.PUBLIC_ID, "wall")
}

sealed class ImageListUiState {
    data class Success(val images: List<LocalImage>, val rev: Int, val album: String): ImageListUiState()
    data class Error(val exception: Throwable): ImageListUiState()
    object InitLoadingState : ImageListUiState()
    object RefreshLoadingState : ImageListUiState()
}

sealed class FavouritesListUiState {
    data class Success(val images: List<LocalImage>, val rev: Int): FavouritesListUiState()
    data class Error(val exception: Throwable): FavouritesListUiState()
    object InitLoadingState : FavouritesListUiState()
    object RefreshLoadingState : FavouritesListUiState()
}

sealed class ImageDetailsUiState {
    data class Success(val isLiked: Boolean, val image: LocalImage): ImageDetailsUiState()
    data class Error(val exception: Throwable): ImageDetailsUiState()
    object LoadingState : ImageDetailsUiState()
}


