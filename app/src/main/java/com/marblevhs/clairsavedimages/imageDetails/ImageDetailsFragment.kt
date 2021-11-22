package com.marblevhs.clairsavedimages.imageDetails

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.databinding.ImageDetailsFragmentBinding
import com.marblevhs.clairsavedimages.imageList.ImageListUiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ImageDetailsFragment : Fragment() {

    private var binding: ImageDetailsFragmentBinding? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: ImageDetailsViewModel by viewModels()
    private var imageId: String = ""
    companion object {
        fun newInstance() = ImageDetailsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = ImageDetailsFragmentBinding.inflate(layoutInflater)
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()

        lifecycleScope.launch {
            mainViewModel.selectedImageFlow.collect {
                imageId = it.id
                updateImage(it)
                viewModel.loadIsLiked(imageId)
            }
        }
        lifecycleScope.launch{
            viewModel.uiState.collect{
                when (it) {
                    is ImageDetailsUiState.Success -> updateIsLiked(it.isLiked)
                    is ImageDetailsUiState.Error -> {
                        Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show()
                        Log.e("RESP", it.exception.message ?: "0")
                    }
                }
            }
        }



    }



    private fun initListeners(){
        binding?.likeButton?.setOnClickListener { likeButtonClicked() }
    }

    private fun likeButtonClicked(){
        viewModel.likeButtonClicked(imageId)
    }

    private fun updateImage(image: Image) {
        binding?.ivSelectedImage?.load(image.sizes[image.sizes.size - 1].imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_download_progress)
            error(R.drawable.ic_download_error)
        }
    }

    private fun updateIsLiked(isLiked: Boolean){
        if(isLiked){
            binding?.likeButton?.setImageResource(R.drawable.baseline_favorite_white_36)
        } else {
            binding?.likeButton?.setImageResource(R.drawable.baseline_favorite_border_white_36)
        }
    }


}