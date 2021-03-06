package com.marblevhs.clairsavedimages.loginScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.databinding.LoginFragmentBinding

class LoginFragment : Fragment() {

    private var binding: LoginFragmentBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginFragmentBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.loginButton?.setOnClickListener {
            (activity as MainActivity).getAccessToken()
        }
    }



    companion object {
        fun newInstance() = LoginFragment()
    }
}