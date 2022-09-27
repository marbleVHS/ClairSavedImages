package com.marblevhs.clairsavedimages.imageDetails

import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.mock.repositories.TestImagesRepoImpl
import com.marblevhs.clairsavedimages.repositories.ImagesRepo
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImageDetailsViewModelTest {

    private val repo = mockk<ImagesRepo>()
    private lateinit var viewmodel: ImageDetailsViewModel
    private val image = LocalImage(
        id = "457253072",
        ownerId = "213769512",
        album = "saved",
        width = 864,
        height = 1080,
        thumbnailUrl = "https://sun1-30.userapi.com/impg/pzzg5Uhyvi9R9dzD2mbMVN6_9NtLtiUc0iNisw/gQpduNKmuH4.jpg?size=200x250&quality=96&sign=9296edce83a9ca8cf5429a652c74a31a&c_uniq_tag=CCpldcRsjjBnWGu9Kov08iS_hmJdwKIbB6wkN2CMD6M&type=album",
        fullSizeUrl = "https://sun1-30.userapi.com/impg/pzzg5Uhyvi9R9dzD2mbMVN6_9NtLtiUc0iNisw/gQpduNKmuH4.jpg?size=864x1080&quality=96&sign=b945370c44b9b0213b6c6a5b51d2c58f&c_uniq_tag=33u8-DhR5MQtQLq_VZtj7RuQQyysUTtoi1WTsjhqums&type=album",
    )

    @Before
    fun setup() {
        viewmodel = ImageDetailsViewModel(TestImagesRepoImpl())

    }

    @Test
    fun `should update ui state when new image is selected`() = runTest {
        assert(viewmodel.detailsUiState.value is ImageDetailsUiState.LoadingState)
        viewmodel.newImageSelected(image)
        assert(viewmodel.detailsUiState
            .dropWhile { it is ImageDetailsUiState.LoadingState }
            .first() is ImageDetailsUiState.Success)
    }


}