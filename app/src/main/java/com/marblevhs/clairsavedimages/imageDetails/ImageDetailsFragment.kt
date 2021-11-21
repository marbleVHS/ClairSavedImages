package com.marblevhs.clairsavedimages.imageDetails

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.databinding.ImageDetailsFragmentBinding
import com.marblevhs.clairsavedimages.databinding.ImageListFragmentBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ImageDetailsFragment : Fragment() {

    private var binding: ImageDetailsFragmentBinding? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private var isLiked = false

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
                   updateUi(it)
               }
        }




    }

    private fun initListeners(){
        binding?.likeButton?.setOnClickListener { likeButtonClicked() }
    }

    private fun likeButtonClicked(){
        if(isLiked){
            binding?.likeButton?.setImageResource(R.drawable.baseline_favorite_border_white_36)
            isLiked = !isLiked
        } else {
            binding?.likeButton?.setImageResource(R.drawable.baseline_favorite_white_36)
            isLiked = !isLiked
        }
    }

    private fun updateUi(image: Image) {
        binding?.ivSelectedImage?.load(image.sizes[image.sizes.size - 1].imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_download_progress)
            error(R.drawable.ic_download_error)
        }
    }
}