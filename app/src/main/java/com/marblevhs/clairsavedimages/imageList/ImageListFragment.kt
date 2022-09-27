package com.marblevhs.clairsavedimages.imageList

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigator
import androidx.paging.LoadState
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.NavBarFragmentDirections
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.Album
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.databinding.ImageListFragmentBinding
import com.marblevhs.clairsavedimages.utils.appComponent
import com.marblevhs.clairsavedimages.utils.toPx
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class ImageListFragment : Fragment(R.layout.image_list_fragment) {


    @Inject
    lateinit var viewModelFactory: ImageListViewModel.Factory

    private val viewModel: ImageListViewModel by viewModels { viewModelFactory }
    private val binding by viewBinding(ImageListFragmentBinding::bind)
    private val adapter: ImageListAdapter =
        ImageListAdapter { image, memoryCacheKey, extras ->
            adapterOnClick(
                image,
                memoryCacheKey,
                extras
            )
        }
    private var albumIdUi: String = Album.DEBUG.albumId

    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.saveLatestSeenImage()
        }
        NotificationManagerCompat.from(requireContext()).cancel(R.string.new_image_notification_id)
        handleSystemInsets(binding.toolbar)
//        TODO:
//        binding.rvImages.apply {
//            postponeEnterTransition()
//            viewTreeObserver.addOnPreDrawListener {
//                startPostponedEnterTransition()
//                true
//            }
//        }
        binding.rvImages.adapter = adapter.withLoadStateHeaderAndFooter(
            header = ImageListLoaderStateAdapter(),
            footer = ImageListLoaderStateAdapter()
        )
        binding.rvImages.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        initListeners()
        assetColorSchemeToSwipeRefreshLayout()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { collectUiState() }
                launch { collectImagesList() }
                launch { collectLoadState() }
            }
        }
    }

    private suspend fun collectUiState() {
        viewModel.listUiState.collect {
            when (it) {
                is ImageListUiState.Success -> {
                    albumIdUi = it.albumId
                }
                is ImageListUiState.Error -> showError(it.exception)
                is ImageListUiState.InitLoadingState -> {}

            }
        }
    }

    private suspend fun collectImagesList() {
        viewModel.imagesFlow.collectLatest {
            adapter.submitData(it)
        }
    }

    private suspend fun collectLoadState() {
        adapter.loadStateFlow.collectLatest { loadStates ->
            if (loadStates.refresh == LoadState.Loading) {
                if (!viewModel.firstImagesInit) {
                    binding.swipeRefreshLayout.isRefreshing = true
                    binding.listLoader.visibility = View.GONE
                } else {
                    binding.listLoader.visibility = View.VISIBLE
                }
            } else {
                updateTopbarTitle(albumIdUi)
                viewModel.firstImagesInit = false
                binding.swipeRefreshLayout.isRefreshing = false
                binding.listLoader.visibility = View.GONE
                if (loadStates.refresh is LoadState.Error) {
                    showError((loadStates.refresh as LoadState.Error).error)
                }
            }
        }
    }

    private fun handleSystemInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val sysBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = sysBarInsets.top,
                left = sysBarInsets.left,
                right = sysBarInsets.right,
                bottom = 8.toPx.toInt()
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

    private fun adapterOnClick(
        image: LocalImage,
        memoryCacheKey: String? = null,
        extras: FragmentNavigator.Extras
    ) {
        (activity as MainActivity).navController
            .navigate(
                NavBarFragmentDirections.actionNavBarFragmentToImageDetailsFragment(
                    image,
                    memoryCacheKey
                ), extras
            )
    }

    private fun showError(exception: Throwable) {
        binding.swipeRefreshLayout.isRefreshing = false
        binding.listLoader.visibility = View.GONE
        Snackbar.make(
            binding.coordinatorLayout as View,
            "Images aren't available",
            Snackbar.LENGTH_SHORT
        )
            .show()
        Log.e("RESP", exception.localizedMessage ?: "no message? :<")
    }


    private fun updateTopbarTitle(albumId: String) {
        if (albumId == "saved") {
            binding.toolbar.title = "Claire saved pictures"
        } else {
            binding.toolbar.title = "Claire public pictures"
        }
    }

    private fun initListeners() {
        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_sort -> {
                    viewModel.switchRev()
                    true
                }
                R.id.menu_refresh -> {
                    adapter.refresh()
                    true
                }
                R.id.menu_switch -> {
                    viewModel.switchAlbumState()
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