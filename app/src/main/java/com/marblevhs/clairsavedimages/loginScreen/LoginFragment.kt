package com.marblevhs.clairsavedimages.loginScreen

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.databinding.LoginFragmentBinding
import com.marblevhs.clairsavedimages.utils.appComponent
import javax.inject.Inject

class LoginFragment : Fragment(R.layout.login_fragment) {

    @Inject
    lateinit var vkLoginManager: VKLoginManager

    private val binding by viewBinding(LoginFragmentBinding::bind)

    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginButton.setOnClickListener {
            vkLoginManager.login()
        }

    }


    companion object {
        fun newInstance() = LoginFragment()
    }
}