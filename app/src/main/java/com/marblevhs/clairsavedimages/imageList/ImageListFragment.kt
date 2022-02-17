package com.marblevhs.clairsavedimages.imageList

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.marblevhs.clairsavedimages.ImageListUiState
import com.marblevhs.clairsavedimages.MainApp
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.databinding.ImageListFragmentBinding
import com.marblevhs.clairsavedimages.di.AppComponent

import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.appComponent: AppComponent
    get() = when(this){
        is MainApp -> appComponent
        else -> this.applicationContext.appComponent
    }

class ImageListFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels{ viewModelFactory }
    private var binding: ImageListFragmentBinding? = null
    private var bottomNavView: BottomNavigationView? = null
    private var revUi: Int = 1
    private var shortAnimationDuration: Int = 0
    private val adapter: ImageListAdapter = ImageListAdapter() { image -> adapterOnClick(image) }

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
        binding = ImageListFragmentBinding.inflate(layoutInflater)
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        handleSystemInsets(view)
        fixSwipeRefreshLayout()
        bottomNavView?.visibility = View.VISIBLE
        viewModel.initImages(revUi)
        binding?.rvImages?.adapter = adapter
        binding?.rvImages?.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        initListeners()
        binding?.swipeRefreshLayout?.setProgressViewOffset(true, 80.toPx.toInt(), 130.toPx.toInt())
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.listUiState.collect {
                    when (it) {
                        is ImageListUiState.Success -> updateUi(it.images, it.rev, it.album)
                        is ImageListUiState.Error -> showError(it.exception.message)
                        is ImageListUiState.InitLoadingState -> {
                            binding?.listLoader?.visibility = View.VISIBLE
                        }
                        is ImageListUiState.RefreshLoadingState -> {
                            binding?.swipeRefreshLayout?.isRefreshing = true
                        }
                    }
                }
            }

        }

    }

    private fun handleSystemInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view){ _ , insets ->
            val sysBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding?.appbar?.updatePadding(
                top = sysBarInsets.top,
                left = sysBarInsets.left,
                right = sysBarInsets.right
            )
            insets
        }
    }


    private fun fixSwipeRefreshLayout(){
        val recyclerView = binding?.rvImages
        val swipeRefreshLayout = binding?.swipeRefreshLayout
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val topRowVerticalPosition =
                    if (recyclerView.childCount == 0) 0 else recyclerView.getChildAt(
                        0
                    ).top
                swipeRefreshLayout?.isEnabled = topRowVerticalPosition >= 0
            }

        })
    }

    private fun adapterOnClick(image: LocalImage) {
        viewModel.newImageSelected(image)
        findNavController().navigate(ImageListFragmentDirections.actionOpenImageDetailsImageList())
    }

    private fun showError(errorMessage: String?){
        binding?.listLoader?.visibility = View.INVISIBLE
        binding?.swipeRefreshLayout?.isRefreshing = false
        Toast.makeText(activity, "Network error", Toast.LENGTH_SHORT).show()
        Log.e("RESP", errorMessage ?: "0")
    }

    private fun updateUi(images: List<LocalImage>, rev: Int, album: String){
        revUi = rev
        binding?.listLoader?.visibility = View.INVISIBLE
        binding?.swipeRefreshLayout?.isRefreshing = false
        if (album == "saved"){
            binding?.appbar?.title = "Claire saved pictures"
        } else {
            binding?.appbar?.title = "Claire public pictures"
        }
        adapter.submitList(images)
    }

    private fun initListeners(){
        val swipeRefreshLayout = binding?.swipeRefreshLayout
        swipeRefreshLayout?.setOnRefreshListener{
            viewModel.loadImages(revUi)
        }
        binding?.appbar?.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_sort -> {
                    revUi = if(revUi == 1){
                        0

                    } else {
                        1

                    }
                    viewModel.loadImages(revUi)
                    true
                }
                R.id.menu_refresh -> {
                    viewModel.loadImages(revUi)
                    true
                }
                R.id.menu_switch -> {
                    viewModel.switchAlbumState(rev = revUi)
                    true
                }
                R.id.info -> {
                    true
                }
                else -> false
            }
        }
    }
    private val Number.toPx get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics)

    companion object {
        fun newInstance() = ImageListFragment()
    }


}