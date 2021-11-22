package com.marblevhs.clairsavedimages.imageDetails

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.marblevhs.clairsavedimages.ImageDetailsUiState
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.databinding.ImageDetailsFragmentBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ImageDetailsFragment : Fragment() {

    private var binding: ImageDetailsFragmentBinding? = null
    private val viewModel: MainViewModel by activityViewModels()
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

        lifecycleScope.launch{
            viewModel.detailsUiState.collect{
                when (it) {
                    is ImageDetailsUiState.Success -> updateUi(it.image, it.isLiked)
                    is ImageDetailsUiState.IsLikedChanged -> updateIsLiked(it.isLiked)
                    is ImageDetailsUiState.Error -> {
                        setLoading(isLoading = false)
                        binding?.likeButton?.isClickable = false
                        Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show()
                        Log.e("RESP", it.exception.message ?: "0")
                    }
                    is ImageDetailsUiState.LoadingState -> setLoading(isLoading = true)
                }
            }
        }

    }

    private fun setLoading(isLoading: Boolean){
        if(isLoading){
            binding?.detailsLoader?.visibility = View.VISIBLE
            binding?.ivSelectedImage?.visibility = View.INVISIBLE
            binding?.likeButton?.visibility = View.INVISIBLE
        } else {
            binding?.detailsLoader?.visibility = View.GONE
            binding?.ivSelectedImage?.visibility = View.VISIBLE
            binding?.likeButton?.visibility = View.VISIBLE
        }

    }


    private fun initListeners(){
        binding?.likeButton?.setOnClickListener { likeButtonClicked() }
    }

    private fun likeButtonClicked(){
        viewModel.likeButtonClicked()
    }

    private fun updateUi(image: Image, isLiked: Boolean) {
        binding?.likeButton?.isClickable = true
        if(image.id != "") {
            setLoading(false)
            binding?.ivSelectedImage?.load(image.sizes[image.sizes.size - 1].imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_download_progress)
                error(R.drawable.ic_download_error)
            }
            updateIsLiked(isLiked)
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