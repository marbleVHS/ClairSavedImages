package com.marblevhs.clairsavedimages.imageDetails



import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.marblevhs.clairsavedimages.*
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.databinding.ImageDetailsFragmentBinding
import com.marblevhs.clairsavedimages.di.AppComponent
import com.ortiz.touchview.OnTouchImageViewListener
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.appComponent: AppComponent
    get() = when(this){
        is MainApp -> appComponent
        else -> this.applicationContext.appComponent
    }

class ImageDetailsFragment : Fragment() {

    private var binding: ImageDetailsFragmentBinding? = null
    private var shortAnimationDuration: Int = 0
    private var bottomNavView: BottomNavigationView? = null
    private val viewModel: MainViewModel by activityViewModels{ viewModelFactory }
    companion object {
        fun newInstance() = ImageDetailsFragment()
    }

    @Inject
    lateinit var viewModelFactory: MainViewModel.Factory

    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = ImageDetailsFragmentBinding.inflate(layoutInflater)
        return binding?.root!!
    }

    override fun onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment: Boolean) {
        super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment)
        bottomNavView?.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomNavView = activity?.findViewById(R.id.bottomNavBar)
        bottomNavView?.visibility = View.GONE
        initListeners()
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.detailsUiState.replayCache
                viewModel.detailsUiState.collect {
                    when (it) {
                        is ImageDetailsUiState.Success -> updateUi(it.image, it.isLiked, it.isFav)
                        is ImageDetailsUiState.Error -> {
                            setLoading(isLoading = false)
                            Snackbar.make(
                                binding?.root as View,
                                "Network error! Check your connection and try again.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            binding?.likeButton?.isClickable = false
                            binding?.favouritesButton?.isClickable = false
                            Log.e("RESP", it.exception.message ?: "0")
                        }
                        is ImageDetailsUiState.LoadingState -> setLoading(isLoading = true)
                    }
                }
            }
        }

    }

    private fun setLoading(isLoading: Boolean){
        if(isLoading){
            binding?.detailsLoader?.visibility = View.VISIBLE
            binding?.ivSelectedImage?.visibility = View.INVISIBLE
            binding?.likeButton?.visibility = View.INVISIBLE
            binding?.zoomInButton?.visibility = View.INVISIBLE
            binding?.favouritesButton?.visibility = View.INVISIBLE
        } else {
            binding?.detailsLoader?.visibility = View.GONE
            binding?.ivSelectedImage?.visibility = View.VISIBLE
            binding?.likeButton?.visibility = View.VISIBLE
            binding?.zoomInButton?.visibility = View.VISIBLE
            binding?.favouritesButton?.visibility = View.VISIBLE
        }

    }


    private fun initListeners(){
        binding?.likeButton?.setOnClickListener { likeButtonClicked() }
        binding?.zoomInButton?.setOnClickListener { zoomInButtonClicked() }
        binding?.favouritesButton?.setOnClickListener { favouritesButtonClicked() }
        binding?.ivSelectedImage?.setOnTouchImageViewListener(object : OnTouchImageViewListener {
            val imageView = binding?.ivSelectedImage
            override fun onMove() {
                if(imageView != null){
                    if(imageView.currentZoom > 1.1){
                        fadeToInvisible()
                    } else {
                        fadeToVisible()
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
        fadeToInvisible()
    }

    private fun favouritesButtonClicked(){
        viewModel.favouritesButtonClicked()
    }

    private fun fadeToVisible() {
        binding?.zoomInButton?.apply {
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
        binding?.likeButton?.apply {
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
        binding?.favouritesButton?.apply {
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
    }

    private fun fadeToInvisible(){
        binding?.zoomInButton?.apply {
            visibility = View.VISIBLE
            animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
        binding?.likeButton?.apply {
            visibility = View.VISIBLE
            animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
        binding?.favouritesButton?.apply {
            visibility = View.VISIBLE
            animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
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
        if(image.id != "") {
            curImage = image
            binding?.ivSelectedImage?.load(image.fullSizeUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_download_progress)
                error(R.drawable.ic_download_error)
            }
            updateIsLiked(isLiked)
            updateIsFav(isFav)
            binding?.likeButton?.isClickable = true
            binding?.favouritesButton?.isClickable = true
            setLoading(false)
        }
    }

    private fun updateIsFav(isFav: Boolean){
        binding?.favouritesButton?.isChecked = isFav
    }

    private fun updateIsLiked(isLiked: Boolean){
        binding?.likeButton?.isChecked = isLiked
    }


}