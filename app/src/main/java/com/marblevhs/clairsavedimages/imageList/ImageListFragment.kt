package com.marblevhs.clairsavedimages.imageList

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.marblevhs.clairsavedimages.ImageListUiState
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.databinding.ImageListFragmentBinding
import com.marblevhs.clairsavedimages.imageDetails.ImageDetailsFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class ImageListFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private var binding: ImageListFragmentBinding? = null
    private val adapter: ImageListAdapter = ImageListAdapter() { flower -> adapterOnClick(flower) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = ImageListFragmentBinding.inflate(layoutInflater)
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.rvImages?.adapter = adapter
        binding?.rvImages?.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding?.rvImages?.Recycler()?.setViewCacheSize(300)
        initListeners()
        viewModel.initImages()
        lifecycleScope.launchWhenStarted{
            viewModel.listUiState.collect {
                when (it) {
                    is ImageListUiState.Success -> updateUi(it.images)
                    is ImageListUiState.Error -> {
                        binding?.listLoader?.visibility = View.INVISIBLE
                        binding?.swipeRefreshLayout?.isRefreshing = false
                        Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show()
                        Log.e("RESP", it.exception.message ?: "0")
                    }
                    is ImageListUiState.InitLoadingState -> {
                        binding?.listLoader?.visibility = View.VISIBLE
                    }
                    is ImageListUiState.RefreshLoadingState -> {
                        binding?.swipeRefreshLayout?.isRefreshing = true
                    }
                }
            }

        }
        super.onViewCreated(view, savedInstanceState)

    }

    private fun adapterOnClick(image: Image) {
        viewModel.newImageSelected(image)
        activity?.supportFragmentManager?.beginTransaction()?.replace(
            R.id.container, ImageDetailsFragment.newInstance())?.addToBackStack(null)?.commit()
    }

    private fun updateUi(images: List<Image>){
        binding?.listLoader?.visibility = View.INVISIBLE
        binding?.swipeRefreshLayout?.isRefreshing = false
        adapter.submitList(images)
    }

    private fun initListeners(){
        val swipeRefreshLayout = binding?.swipeRefreshLayout
        swipeRefreshLayout?.setOnRefreshListener{
            viewModel.loadImages()
//            swipeRefreshLayout.isRefreshing = false
        }
    }

    companion object {
        fun newInstance() = ImageListFragment()
    }


}