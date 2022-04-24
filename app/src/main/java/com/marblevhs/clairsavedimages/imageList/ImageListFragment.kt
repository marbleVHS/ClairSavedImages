package com.marblevhs.clairsavedimages.imageList

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.NavBarFragmentDirections
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.databinding.ImageListFragmentBinding
import com.marblevhs.clairsavedimages.extensions.appComponent
import com.marblevhs.clairsavedimages.imageList.ImageListViewModel.ImageListUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class ImageListFragment : Fragment(R.layout.image_list_fragment) {

    @Inject
    lateinit var mainViewModelFactory: MainViewModel.Factory

    @Inject
    lateinit var viewModelFactory: ImageListViewModel.Factory

    private val mainViewModel: MainViewModel by activityViewModels { mainViewModelFactory }
    private val viewModel: ImageListViewModel by viewModels { viewModelFactory }
    private val binding by viewBinding(ImageListFragmentBinding::bind)
    private var revUi: Int = 1
    private var albumUi: String = "saved"
    private val adapter: ImageListAdapter = ImageListAdapter() { image -> adapterOnClick(image) }


    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initImages(revUi)
        handleSystemInsets(view)
        binding.rvImages.adapter = adapter.withLoadStateHeaderAndFooter(
            header = ImageListLoaderStateAdapter(),
            footer = ImageListLoaderStateAdapter()
        )
        binding.rvImages.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        initListeners()
        assetColorSchemeToSwipeRefreshLayout()
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.listUiState.collect {
                        when (it) {
                            is ImageListUiState.Success -> {
                                updateUi(it.rev, it.album)
                            }
                            is ImageListUiState.Error -> showError(it.exception.message)
                            else -> {}
                        }
                    }
                }
            }
            launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.images.collectLatest {
                        adapter.submitData(it)
                    }
                }
            }
            launch {
                adapter.loadStateFlow.collectLatest { loadStates ->
                    if (loadStates.refresh == LoadState.Loading) {
                        if (!viewModel.firstImagesInit) {
                            binding.swipeRefreshLayout.isRefreshing = true
                            binding.listLoader.visibility = View.GONE
                        } else if (mainViewModel.isLoggedFlow.value) {
                            binding.listLoader.visibility = View.VISIBLE
                            viewModel.firstImagesInit = false
                        }
                    } else {
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.listLoader.visibility = View.GONE
                        if (loadStates.refresh is LoadState.Error) {
                            showError((loadStates.refresh as LoadState.Error).error.message)
                        }
                    }

                }
            }
        }

    }

    private fun handleSystemInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val sysBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appbar.updatePadding(
                top = sysBarInsets.top,
                left = sysBarInsets.left,
                right = sysBarInsets.right
            )
            insets
        }
    }


    private fun assetColorSchemeToSwipeRefreshLayout() {
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.swipeRefreshLayout.setColorSchemeResources(R.color.md_theme_dark_onBackground)
                binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.md_theme_dark_background)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.swipeRefreshLayout.setColorSchemeResources(R.color.md_theme_light_onBackground)
                binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.md_theme_light_background)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.swipeRefreshLayout.setColorSchemeResources(R.color.md_theme_light_onBackground)
                binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.md_theme_light_background)
            }
        }
    }

    private fun adapterOnClick(image: LocalImage) {
        mainViewModel.newImageSelected(image)
        (activity as MainActivity).navController
            .navigate(NavBarFragmentDirections.actionNavBarFragmentToImageDetailsFragment())
    }

    private fun showError(errorMessage: String?) {
        binding.swipeRefreshLayout.isRefreshing = false
        binding.listLoader.visibility = View.GONE
        Snackbar.make(binding.coordinatorLayout as View, "Network error", Snackbar.LENGTH_SHORT)
            .show()
        Log.e("RESP", errorMessage ?: "0")
    }


    private fun updateUi(rev: Int, album: String) {
        revUi = rev
        albumUi = album
        if (album == "saved") {
            binding.appbar.title = "Claire saved pictures"
        } else {
            binding.appbar.title = "Claire public pictures"
        }
    }

    private fun initListeners() {
        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
        binding.appbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_sort -> {
                    revUi = if (revUi == 1) {
                        0

                    } else {
                        1

                    }
                    viewModel.loadImages(revUi)
                    true
                }
                R.id.menu_refresh -> {
                    adapter.refresh()
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


    companion object {
        fun newInstance() = ImageListFragment()
    }


}