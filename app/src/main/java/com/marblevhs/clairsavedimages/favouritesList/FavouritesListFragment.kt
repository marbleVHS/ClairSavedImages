package com.marblevhs.clairsavedimages.favouritesList

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.marblevhs.clairsavedimages.FavouritesListUiState
import com.marblevhs.clairsavedimages.MainApp
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.databinding.FavouritesListFragmentBinding
import com.marblevhs.clairsavedimages.di.AppComponent
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.appComponent: AppComponent
    get() = when(this){
        is MainApp -> appComponent
        else -> this.applicationContext.appComponent
    }

class FavouritesListFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels{ viewModelFactory }
    private var binding: FavouritesListFragmentBinding? = null
    private var bottomNavView: BottomNavigationView? = null
    private var revUi: Int = 1
    private val adapter: FavouritesListAdapter = FavouritesListAdapter() { image -> adapterOnClick(image) }


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
        binding = FavouritesListFragmentBinding.inflate(layoutInflater)
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleSystemInsets(view)
        bottomNavView?.visibility = View.VISIBLE
        viewModel.initFavs(revUi)
        binding?.rvImages?.adapter = adapter
        binding?.rvImages?.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        initListeners()
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favouritesUiState.collect {
                    when (it) {
                        is FavouritesListUiState.Success -> {
                            updateUi(it.images, it.rev)
                        }
                        is FavouritesListUiState.Error -> showError(it.exception.message)
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




    private fun adapterOnClick(image: LocalImage) {
        viewModel.newImageSelected(image)
        findNavController().navigate(FavouritesListFragmentDirections.actionOpenImageDetailsFavouritesList())
    }

    private fun showError(errorMessage: String?){
        Toast.makeText(activity, "Network error", Toast.LENGTH_SHORT).show()
        Log.e("RESP", errorMessage ?: "0")
    }

    private fun updateUi(images: List<LocalImage>, rev: Int){
        revUi = rev
        adapter.submitList(images)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initListeners(){
        binding?.appbar?.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_sort -> {
                    revUi = if(revUi == 1){
                        0
                    } else {
                        1
                    }
                    adapter.submitList(emptyList())
                    viewModel.loadFavs(revUi)
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
        fun newInstance() = FavouritesListFragment()
    }


}