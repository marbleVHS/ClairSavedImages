package com.marblevhs.clairsavedimages.imageList

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.marblevhs.clairsavedimages.ImageListUiState
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.databinding.ImageListFragmentBinding


class ImageListFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private var binding: ImageListFragmentBinding? = null
    private var bottomNavView: BottomNavigationView? = null
    private var revUi: Int = 1
    private val adapter: ImageListAdapter = ImageListAdapter() { image -> adapterOnClick(image) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = ImageListFragmentBinding.inflate(layoutInflater)
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handleSystemInsets(view)
        fixSwipeRefreshLayout()
        bottomNavView?.visibility = View.VISIBLE
        viewModel.initImages(revUi)
        binding?.rvImages?.adapter = adapter
        binding?.rvImages?.layoutManager =
            StaggeredGridLayoutManager(3, GridLayoutManager.VERTICAL)
        initListeners()
        lifecycleScope.launchWhenStarted{
            viewModel.listUiState.collect {
                when (it) {
                    is ImageListUiState.Success -> updateUi(it.images, it.rev)
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
        binding?.appbar?.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_sort -> {
                    if(revUi == 1){
                        revUi = 0
                    } else {
                        revUi = 1
                    }
                    viewModel.loadImages(revUi)
                    binding?.rvImages?.scrollToPosition(0)
                    true
                }
                R.id.menu_refresh -> {
                    viewModel.loadImages(revUi)
                    true
                }
                R.id.info -> {
                    true
                }
                else -> false
            }
        }
        binding?.swipeRefreshLayout?.setProgressViewOffset(true, 80.toPx.toInt(), 100.toPx.toInt())
        super.onViewCreated(view, savedInstanceState)

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
                    if (recyclerView == null || recyclerView.getChildCount() === 0) 0 else recyclerView.getChildAt(
                        0
                    ).getTop()
                swipeRefreshLayout?.setEnabled(topRowVerticalPosition >= 0)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun adapterOnClick(image: Image) {
        viewModel.newImageSelected(image)
        findNavController().navigate(ImageListFragmentDirections.actionOpenImageDetailsImageList())
    }

    private fun showError(errorMessage: String?){
        binding?.listLoader?.visibility = View.INVISIBLE
        binding?.swipeRefreshLayout?.isRefreshing = false
        Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show()
        Log.e("RESP", errorMessage ?: "0")
    }

    private fun updateUi(images: List<Image>, rev: Int){
        revUi = rev
        binding?.listLoader?.visibility = View.INVISIBLE
        binding?.swipeRefreshLayout?.isRefreshing = false
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
                    if(revUi == 1){
                        revUi = 0
                    } else {
                        revUi = 1
                    }
                    viewModel.loadImages(revUi)
                    binding?.rvImages?.scrollToPosition(0)
                    true
                }
                R.id.menu_refresh -> {
                    viewModel.loadImages(revUi)
                    true
                }
                R.id.info -> {
                    true
                }
                else -> false
            }
        }
    }
    val Number.toPx get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics)

    companion object {
        fun newInstance() = ImageListFragment()
    }


}