package com.marblevhs.clairsavedimages.imageList

import android.os.Bundle
import android.text.TextUtils.replace
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.databinding.ImageListFragmentBinding
import com.marblevhs.clairsavedimages.imageDetails.ImageDetailsFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class ImageListFragment : Fragment() {

    private val viewModel: ImageListViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
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
//    inflater.inflate(R.layout.image_list_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if(savedInstanceState == null){
            binding?.rvImages?.adapter = adapter
            binding?.rvImages?.layoutManager =
                StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            binding?.rvImages?.Recycler()?.setViewCacheSize(300)
            initListeners()
            lifecycleScope.launch{
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiState.collect {
                        when (it) {
                            is ImageListUiState.Success -> updateUi(it.images)
                            is ImageListUiState.LoadingState -> {
                                binding?.progressBar?.visibility = View.VISIBLE
                            }
                            is ImageListUiState.Error -> {
                                binding?.progressBar?.visibility = View.INVISIBLE
                                Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show()
                                Log.e("RESP", it.exception.message ?: "0")
                            }
                        }
                    }
                }
            }
        }

        super.onViewCreated(view, savedInstanceState)

    }

    private fun adapterOnClick(image: Image) {
        CoroutineScope(Job() + Dispatchers.Default).launch{
            mainViewModel.selectedImageFlow.emit(image)
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.container, ImageDetailsFragment.newInstance())?.addToBackStack(null)?.commit()
        }
    }

    private fun updateUi(images: List<Image>){
        binding?.progressBar?.visibility = View.INVISIBLE
        adapter.submitList(images)
    }

    private fun initListeners(){
        binding?.btLoad?.setOnClickListener{
            viewModel.loadImages()
        }
    }

    companion object {
        fun newInstance() = ImageListFragment()
    }


}