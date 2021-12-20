package com.marblevhs.clairsavedimages.imageDetails


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import com.ortiz.touchview.OnTouchImageViewListener
import kotlinx.coroutines.flow.collect

class ImageDetailsFragment : Fragment() {

    private var binding: ImageDetailsFragmentBinding? = null
    private var shortAnimationDuration: Int = 200
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

        lifecycleScope.launchWhenStarted{
            viewModel.detailsUiState.replayCache
            viewModel.detailsUiState.collect{
                when (it) {
                    is ImageDetailsUiState.Success -> updateUi(it.image, it.isLiked)
                    is ImageDetailsUiState.Error -> {
                        setLoading(isLoading = false)
                        Toast.makeText(activity, "Network error! Check your connection and try again.", Toast.LENGTH_LONG).show()
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
        binding?.zoomInButton?.setOnClickListener{ zoomInButtonClicked() }
        binding?.ivSelectedImage?.setOnTouchImageViewListener(object : OnTouchImageViewListener {
            val imageView = binding?.ivSelectedImage
            override fun onMove() {
                if(imageView != null){
                    if(imageView.isZoomed){
                        crossfadeToInvisible()
                    } else {
                        crossfadeToVisible()
                    }
                }
            }
        })
    }

    private fun likeButtonClicked(){
        viewModel.likeButtonClicked()
    }

    private fun zoomInButtonClicked(){
        val imageView = binding?.ivSelectedImage
        imageView?.setZoom(2f)
        crossfadeToInvisible()
    }

    private fun crossfadeToVisible() {
        binding?.likeButton?.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
        binding?.zoomInButton?.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
    }

    private fun crossfadeToInvisible(){
        binding?.zoomInButton?.animate()?.alpha(0f)?.setDuration(shortAnimationDuration.toLong())
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding?.zoomInButton?.visibility = View.GONE
                }
            })
        binding?.likeButton?.animate()?.alpha(0f)?.setDuration(shortAnimationDuration.toLong())
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding?.zoomInButton?.visibility = View.GONE
                }
            })
    }



    private fun updateUi(image: Image, isLiked: Boolean) {
        if(image.id != "") {
            setLoading(false)
            binding?.ivSelectedImage?.load(image.sizes[image.sizes.size - 1].imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_download_progress)
                error(R.drawable.ic_download_error)
            }
            updateIsLiked(isLiked)
            binding?.likeButton?.isClickable = true
        }
    }

    private fun updateIsLiked(isLiked: Boolean){
        if(isLiked){
            binding?.likeButton?.setImageResource(R.drawable.ic_baseline_favorite_36)
        } else {
            binding?.likeButton?.setImageResource(R.drawable.ic_baseline_favorite_border_36)
        }
    }


}