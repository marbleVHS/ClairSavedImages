package com.marblevhs.clairsavedimages.imageDetails


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.load
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.MainViewModel.ImageDetailsUiState
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.databinding.ImageDetailsFragmentBinding
import com.marblevhs.clairsavedimages.extensions.appComponent
import com.ortiz.touchview.OnTouchImageViewListener
import kotlinx.coroutines.launch
import javax.inject.Inject


class ImageDetailsFragment : Fragment(R.layout.image_details_fragment) {

    @Inject
    lateinit var viewModelFactory: MainViewModel.Factory

    private val binding by viewBinding(ImageDetailsFragmentBinding::bind)
    private var shortAnimationDuration: Int = 0
    private var bottomNavView: BottomNavigationView? = null
    private val viewModel: MainViewModel by activityViewModels { viewModelFactory }

    companion object {
        fun newInstance() = ImageDetailsFragment()
    }


    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }


    override fun onPause() {
        super.onPause()
        bottomNavView?.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomNavView = activity?.findViewById(R.id.bottomNavBar)
        initListeners()
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detailsUiState.collect {
                    when (it) {
                        is ImageDetailsUiState.Success -> updateUi(it.image, it.isLiked, it.isFav)
                        is ImageDetailsUiState.Error -> {
                            setLoading(isLoading = false)
                            Snackbar.make(
                                binding.root as View,
                                "Network error! Check your connection and try again.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            binding.likeButton.isClickable = false
                            binding.favouritesButton.isClickable = false
                            Log.e("RESP", it.exception.message ?: "0")
                        }
                        is ImageDetailsUiState.LoadingState -> setLoading(isLoading = true)
                    }
                }
            }
        }

    }

    override fun onResume() {
        bottomNavView?.visibility = View.GONE
        super.onResume()
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.detailsLoader.visibility = View.VISIBLE
            binding.ivSelectedImage.visibility = View.INVISIBLE
            binding.likeButton.visibility = View.INVISIBLE
            binding.zoomInButton.visibility = View.INVISIBLE
            binding.favouritesButton.visibility = View.INVISIBLE
        } else {
            binding.detailsLoader.visibility = View.GONE
            binding.ivSelectedImage.visibility = View.VISIBLE
            binding.likeButton.visibility = View.VISIBLE
            binding.zoomInButton.visibility = View.VISIBLE
            binding.favouritesButton.visibility = View.VISIBLE
        }

    }


    private fun initListeners() {
        binding.likeButton.setOnClickListener { likeButtonClicked() }
        binding.zoomInButton.setOnClickListener { zoomInButtonClicked() }
        binding.favouritesButton.setOnClickListener { favouritesButtonClicked() }
        binding.ivSelectedImage.setOnTouchImageViewListener(object : OnTouchImageViewListener {
            val imageView = binding.ivSelectedImage
            override fun onMove() {

                if (imageView.currentZoom > 1.1) {
                    fadeToInvisible()
                } else {
                    fadeToVisible()
                }

            }
        })
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).showSystemBars()
    }

    private fun likeButtonClicked() {
        viewModel.likeButtonClicked()
    }

    private fun zoomInButtonClicked() {
        val imageView = binding.ivSelectedImage
        imageView.setZoom(2f)
        fadeToInvisible()
    }

    private fun favouritesButtonClicked() {
        viewModel.favouritesButtonClicked()
    }

    private fun fadeToVisible() {
        (activity as MainActivity).showSystemBars()
        binding.favouritesButton.visibility = View.VISIBLE
        binding.zoomInButton.visibility = View.VISIBLE
        binding.likeButton.visibility = View.VISIBLE
    }

    private fun fadeToInvisible() {
        (activity as MainActivity).hideSystemBars()
        binding.favouritesButton.visibility = View.INVISIBLE
        binding.zoomInButton.visibility = View.INVISIBLE
        binding.likeButton.visibility = View.INVISIBLE
    }


    private var curImage: LocalImage = LocalImage(
        id = "",
        ownerId = "",
        album = "",
        width = 1,
        height = 1,
        thumbnailUrl = "",
        fullSizeUrl = ""
    )

    private fun updateUi(image: LocalImage, isLiked: Boolean, isFav: Boolean) {
        if (image.id != "") {
            curImage = image
            binding.ivSelectedImage.load(image.fullSizeUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_download_progress)
                error(R.drawable.ic_download_error)
            }
            updateIsLiked(isLiked)
            updateIsFav(isFav)
            binding.likeButton.isClickable = true
            binding.favouritesButton.isClickable = true
            setLoading(false)
        }
    }

    private fun updateIsFav(isFav: Boolean) {
        binding.favouritesButton.isChecked = isFav
    }

    private fun updateIsLiked(isLiked: Boolean) {
        binding.likeButton.isChecked = isLiked
    }


}