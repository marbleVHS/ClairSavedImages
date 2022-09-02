package com.marblevhs.clairsavedimages.imageDetails


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.databinding.ImageDetailsFragmentBinding
import com.marblevhs.clairsavedimages.extensions.appComponent
import com.ortiz.touchview.OnTouchImageViewListener
import kotlinx.coroutines.launch
import javax.inject.Inject


class ImageDetailsFragment : Fragment(R.layout.image_details_fragment) {


    @Inject
    lateinit var viewModelFactory: ImageDetailsViewModel.Factory


    private val binding by viewBinding(ImageDetailsFragmentBinding::bind)
    private val viewModel: ImageDetailsViewModel by viewModels { viewModelFactory }


    private val args: ImageDetailsFragmentArgs by navArgs()

    companion object {
        fun newInstance() = ImageDetailsFragment()
    }


    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        if (savedInstanceState == null) {
            viewModel.newImageSelected(args.chosenImage)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detailsUiState.collect {
                    when (it) {
                        is ImageDetailsUiState.Success -> updateUi(
                            it.image,
                            it.isLiked,
                            it.isFav
                        )
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
        binding.ivSelectedImage.setZoom(2f)
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


    private fun updateUi(image: LocalImage, isLiked: Boolean, isFav: Boolean) {
        if (image.id != "") {
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