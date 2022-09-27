package com.marblevhs.clairsavedimages.imageDetails


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.igreenwood.loupe.Loupe
import com.igreenwood.loupe.extensions.setOnScaleChangedListener
import com.igreenwood.loupe.extensions.setOnViewTranslateListener
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.databinding.ImageDetailsFragmentBinding
import com.marblevhs.clairsavedimages.utils.appComponent
import com.marblevhs.clairsavedimages.utils.toPx
import kotlinx.coroutines.launch
import javax.inject.Inject


class ImageDetailsFragment : Fragment(R.layout.image_details_fragment) {


    @Inject
    lateinit var viewModelFactory: ImageDetailsViewModel.Factory


    private val binding by viewBinding(ImageDetailsFragmentBinding::bind)
    private val viewModel: ImageDetailsViewModel by viewModels { viewModelFactory }
    private lateinit var loupe: Loupe

    private val args: ImageDetailsFragmentArgs by navArgs()

    companion object {
        fun newInstance() = ImageDetailsFragment()
    }


    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        TODO:
//        val transition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.shared_image)
//        sharedElementEnterTransition = transition
//        sharedElementReturnTransition = transition
//        super.onCreate(savedInstanceState)
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        TODO:
//        ViewCompat.setTransitionName(binding.ivSelectedImage, "image_${args.chosenImage.id}")
        handleSystemInsets(binding.zoomInButton)
        initListeners()
        if (savedInstanceState == null) {
            viewModel.newImageSelected(args.chosenImage)
        }

        loupe = Loupe.create(binding.ivSelectedImage, binding.container as ViewGroup) {
            setOnViewTranslateListener(
                onStart = {
                    fadeToInvisible()
                    Log.e("RESP", loupe.scale.toString())
                },
                onRestore = {
                    fadeToVisible()
                    Log.e("RESP", loupe.scale.toString())
                },
                onDismiss = { (activity as MainActivity).navController.navigateUp() }
            )

            setOnScaleChangedListener { scaleFactor, focusX, focusY ->
                val currentZoom = scaleFactor / loupe.minScale
                if (currentZoom > 1.2f) {
                    fadeToInvisible()
                } else {
                    fadeToVisible()
                }
            }

        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.detailsUiState.collect {
                    when (it) {
                        is ImageDetailsUiState.Success -> updateUi(
                            it.image,
                            it.isLiked,
                            it.isFav,
                            args.memoryCacheKey
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
                        is ImageDetailsUiState.LoadingState -> {
                            setLoading(isLoading = true)
                            val placeholderMemoryCacheKey = args.memoryCacheKey
                            binding.ivSelectedImage.load(args.chosenImage.fullSizeUrl) {
                                if (placeholderMemoryCacheKey == null) {
                                    placeholder(R.drawable.image_placeholder)
                                } else {
                                    placeholderMemoryCacheKey(placeholderMemoryCacheKey)
                                }
                                error(R.drawable.ic_download_error)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleSystemInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val sysBarInsets =
                insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
            val newMargin = 16.toPx.toInt() + sysBarInsets.bottom
            val newLayoutParams = v.layoutParams as ConstraintLayout.LayoutParams
            newLayoutParams.bottomMargin = newMargin
            v.layoutParams = newLayoutParams
            insets
        }
    }


    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.likeButton.visibility = View.INVISIBLE
            binding.zoomInButton.visibility = View.INVISIBLE
            binding.favouritesButton.visibility = View.INVISIBLE
        } else {
            binding.likeButton.visibility = View.VISIBLE
            binding.zoomInButton.visibility = View.VISIBLE
            binding.favouritesButton.visibility = View.VISIBLE
        }

    }


    private fun initListeners() {
        binding.likeButton.setOnClickListener { likeButtonClicked() }
        binding.zoomInButton.setOnClickListener { zoomInButtonClicked() }
        binding.favouritesButton.setOnClickListener { favouritesButtonClicked() }
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).showSystemBars()
    }

    private fun likeButtonClicked() {
        viewModel.likeButtonClicked()
    }

    private fun zoomInButtonClicked() {
        (activity as MainActivity).hideSystemBars()
    }

    private fun favouritesButtonClicked() {
        viewModel.favouritesButtonClicked()
    }

    private fun fadeToVisible() {
//        TODO: doesn't work with Loupe
//        (activity as MainActivity).showSystemBars()
        binding.favouritesButton.visibility = View.VISIBLE
        binding.zoomInButton.visibility = View.VISIBLE
        binding.likeButton.visibility = View.VISIBLE
    }

    private fun fadeToInvisible() {
//        TODO: doesn't work with Loupe
//        (activity as MainActivity).hideSystemBars()
        binding.favouritesButton.visibility = View.INVISIBLE
        binding.zoomInButton.visibility = View.INVISIBLE
        binding.likeButton.visibility = View.INVISIBLE
    }


    private fun updateUi(
        image: LocalImage,
        isLiked: Boolean,
        isFav: Boolean,
        placeholderMemoryCacheKey: String? = null
    ) {
        if (image.id != "") {
            binding.ivSelectedImage.load(image.fullSizeUrl) {
                if (placeholderMemoryCacheKey == null) {
                    placeholder(R.drawable.ic_download_progress)
                } else {
                    placeholderMemoryCacheKey(placeholderMemoryCacheKey)
                }
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