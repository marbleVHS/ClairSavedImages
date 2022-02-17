package com.marblevhs.clairsavedimages.favouritesList

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
//        viewModel.initImages(revUi)
//        TODO:dfsdfsfd
        binding?.rvImages?.adapter = adapter
        binding?.rvImages?.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding?.swipeRefreshLayout?.setProgressViewOffset(true, 80.toPx.toInt(), 100.toPx.toInt())
        fixSwipeRefreshLayout()
        initListeners()
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favouritesUiState.collect {
                    when (it) {
                        is FavouritesListUiState.Success -> {} /*updateUi(it.images, it.rev)*/
                        is FavouritesListUiState.Error -> showError(it.exception.message)
                        is FavouritesListUiState.InitLoadingState -> {
                            binding?.listLoader?.visibility = View.VISIBLE
                        }
                        is FavouritesListUiState.RefreshLoadingState -> {
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

    private fun adapterOnClick(image: LocalImage) {
//        viewModel.newImageSelected(image)
        findNavController().navigate(FavouritesListFragmentDirections.actionOpenImageDetailsFavouritesList())
    }

    private fun showError(errorMessage: String?){
        binding?.listLoader?.visibility = View.INVISIBLE
        binding?.swipeRefreshLayout?.isRefreshing = false
        Toast.makeText(activity, "Network error", Toast.LENGTH_SHORT).show()
        Log.e("RESP", errorMessage ?: "0")
    }

    private fun updateUi(images: List<LocalImage>, rev: Int){
        revUi = rev
        binding?.listLoader?.visibility = View.INVISIBLE
        binding?.swipeRefreshLayout?.isRefreshing = false
        adapter.submitList(images)
    }

    private fun initListeners(){
        val swipeRefreshLayout = binding?.swipeRefreshLayout
        swipeRefreshLayout?.setOnRefreshListener{
//            viewModel.loadImages(revUi)
//            TODO:dfsfdsf
            swipeRefreshLayout.isRefreshing = false
        }
        binding?.appbar?.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_sort -> {
                    if(revUi == 1){
                        revUi = 0
                    } else {
                        revUi = 1
                    }
//                    viewModel.loadImages(revUi)
//                    TODO:dfsfsdf
                    binding?.rvImages?.scrollToPosition(0)
                    true
                }
                R.id.menu_refresh -> {
//                    viewModel.loadImages(revUi)
//                    TODO:dfsfdsdfsdf
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
        fun newInstance() = FavouritesListFragment()
    }


}