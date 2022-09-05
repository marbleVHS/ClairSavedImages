package com.marblevhs.clairsavedimages.favouritesList

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.NavBarFragmentDirections
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.databinding.FavouritesListFragmentBinding
import com.marblevhs.clairsavedimages.utils.appComponent
import com.marblevhs.clairsavedimages.utils.toPx
import kotlinx.coroutines.launch
import javax.inject.Inject


class FavouritesListFragment : Fragment(R.layout.favourites_list_fragment) {

    @Inject
    lateinit var viewModelFactory: FavouritesListViewModel.Factory


    private val viewModel: FavouritesListViewModel by viewModels { viewModelFactory }
    private val binding by viewBinding(FavouritesListFragmentBinding::bind)
    private var revUi: Int = 1
    private val adapter = FavouritesListAdapter { image -> adapterOnClick(image) }


    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleSystemInsets(view)
        viewModel.initFavs(revUi)
        binding.rvImages.adapter = adapter
        binding.rvImages.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        initListeners()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favouritesUiState.collect {
                    when (it) {
                        is FavouritesListUiState.Success -> {
                            updateUi(it.images, it.rev)
                        }
                        is FavouritesListUiState.InitLoadingState -> {
                            binding.favouritesLoader.visibility = View.VISIBLE
                        }
                        is FavouritesListUiState.Error -> showError(it.exception.message)
                    }
                }
            }
        }

    }

    private fun handleSystemInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val sysBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(
                top = sysBarInsets.top,
                left = sysBarInsets.left,
                right = sysBarInsets.right,
                bottom = 8.toPx.toInt()
            )
            insets
        }
    }


    private fun adapterOnClick(image: LocalImage) {
        (activity as MainActivity).navController
            .navigate(NavBarFragmentDirections.actionNavBarFragmentToImageDetailsFragment(image))
    }

    private fun showError(errorMessage: String?) {
        binding.favouritesLoader.visibility = View.GONE
        Snackbar.make(
            (binding.coordinatorLayout as View),
            "Unexpected error :(",
            Snackbar.LENGTH_SHORT
        ).show()
        Log.e("RESP", errorMessage ?: "0")
    }

    private fun updateUi(images: List<LocalImage>, rev: Int) {
        revUi = rev
        adapter.submitList(images)
        binding.favouritesLoader.visibility = View.GONE
    }


    private fun initListeners() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_sort -> {
                    revUi = if (revUi == 1) {
                        0
                    } else {
                        1
                    }
                    adapter.submitList(emptyList())
                    viewModel.loadFavs(revUi)
                    true
                }
                R.id.menu_refresh -> {
                    viewModel.loadFavs(revUi)
                    true
                }
                else -> false
            }
        }
    }


    companion object {
        fun newInstance() = FavouritesListFragment()
    }


}