package com.marblevhs.clairsavedimages

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.marblevhs.clairsavedimages.databinding.FragmentNavBarBinding


class NavBarFragment : Fragment(R.layout.fragment_nav_bar) {

    private val binding by viewBinding(FragmentNavBarBinding::bind)
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigation()
    }



    private fun setupBottomNavigation() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavBar.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_SELECTED
        binding.bottomNavBar.setupWithNavController(navController)
    }

    companion object {
        fun newInstance() = NavBarFragment()
    }
}