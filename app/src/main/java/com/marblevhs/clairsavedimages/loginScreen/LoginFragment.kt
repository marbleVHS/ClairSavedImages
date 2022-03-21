package com.marblevhs.clairsavedimages.loginScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.databinding.*

class LoginFragment : Fragment() {

    private var binding: LoginFragmentBinding? = null
    private var bottomNavView: BottomNavigationView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginFragmentBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomNavView = activity?.findViewById(R.id.bottomNavBar)
        bottomNavView?.visibility = View.INVISIBLE
        binding?.loginButton?.setOnClickListener {
            (activity as MainActivity).getAccessToken()
        }
    }

    override fun onDetach() {
        super.onDetach()
        bottomNavView?.visibility = View.VISIBLE
    }

    companion object {
        fun newInstance() = LoginFragment()
    }
}