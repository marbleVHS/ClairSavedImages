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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.marblevhs.clairsavedimages.ImageDetailsUiState
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.data.Size
import com.marblevhs.clairsavedimages.databinding.ImageDetailsFragmentBinding
import com.ortiz.touchview.OnTouchImageViewListener


class ImageDetailsFragment : Fragment() {

    private var binding: ImageDetailsFragmentBinding? = null
    private var shortAnimationDuration: Int = 0
    private var bottomNavView: BottomNavigationView? = null
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
//        TODO: implement
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


    private var curImage: Image = Image("", sizes = listOf(Size(type = "x", "https://thiscatdoesnotexist.com/")), height = 1, width = 1)

    private fun updateUi(image: Image, isLiked: Boolean) {
        if(image.id != "") {
            curImage = image
            binding?.ivSelectedImage?.load(image.sizes[image.sizes.size - 1].imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_download_progress)
                error(R.drawable.ic_download_error)
            }
            updateIsLiked(isLiked)
            binding?.likeButton?.isClickable = true
            setLoading(false)
        }
    }

    private fun updateIsLiked(isLiked: Boolean){
        binding?.likeButton?.isChecked = isLiked
    }


}