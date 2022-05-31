package com.marblevhs.clairsavedimages.profileScreen

import android.content.Context
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
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.load
import coil.transform.CircleCropTransformation
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.UserProfile
import com.marblevhs.clairsavedimages.databinding.ProfileFragmentBinding
import com.marblevhs.clairsavedimages.extensions.appComponent
import kotlinx.coroutines.launch
import javax.inject.Inject


class ProfileFragment : Fragment(R.layout.profile_fragment) {

    @Inject
    lateinit var viewModelFactory: ProfileViewModel.Factory

    @Inject
    lateinit var mainViewModelFactory: MainViewModel.Factory

    private val binding by viewBinding(ProfileFragmentBinding::bind)
    private val viewModel: ProfileViewModel by viewModels { viewModelFactory }
    private val mainViewModel: MainViewModel by activityViewModels { mainViewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        context.appComponent.inject(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleSystemInsets(view)
        initListeners()
        viewModel.initProfile()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.profileUiState.collect {
                        when (it) {
                            is ProfileUiState.Success ->
                                updateUi(it.userProfile)
                            is ProfileUiState.InitLoadingState -> {
                                binding.ivError.visibility = View.INVISIBLE
                                binding.progressBar.visibility = View.VISIBLE
                                binding.tvProfileName.visibility = View.INVISIBLE
                                binding.ivProfileImage.visibility = View.INVISIBLE
                                binding.buttonLogOut.visibility = View.INVISIBLE
                                binding.tvTheme.visibility = View.INVISIBLE
                                binding.ThemeChooser.visibility = View.INVISIBLE
                            }
                            is ProfileUiState.Error ->
                                showError(it.exception.localizedMessage)
                        }
                    }
                }
                launch {
                    mainViewModel.defaultNightMode.collect {
                        when (it) {
                            NightMode.SYSTEM.value -> {
                                binding.rbSystemDefault.isChecked = true
                            }
                            NightMode.LIGHT.value -> {
                                binding.rbLight.isChecked = true
                            }
                            NightMode.DARK.value -> {
                                binding.rbDark.isChecked = true
                            }

                        }
                    }
                }
            }
        }
    }


    private fun updateUi(userProfile: UserProfile) {
        binding.ivProfileImage.load(userProfile.profilePicUrl) {
            crossfade(enable = true)
            transformations(CircleCropTransformation())
            placeholder(R.drawable.ic_download_progress)
            error(R.drawable.ic_download_error)
        }
        val fullName = "${userProfile.firstName} ${userProfile.lastName}"
        binding.tvProfileName.text = fullName
        binding.ThemeChooser.visibility = View.VISIBLE
        binding.tvTheme.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
        binding.ivError.visibility = View.INVISIBLE
        binding.tvProfileName.visibility = View.VISIBLE
        binding.ivProfileImage.visibility = View.VISIBLE
        binding.buttonLogOut.visibility = View.VISIBLE
    }

    private fun showError(error: String?) {
        if (error != null) {
            Log.e("RESP", error)
        }
        binding.ivError.visibility = View.VISIBLE
        binding.ThemeChooser.visibility = View.VISIBLE
        binding.tvTheme.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
        binding.tvProfileName.visibility = View.INVISIBLE
        binding.ivProfileImage.visibility = View.INVISIBLE
        binding.buttonLogOut.visibility = View.INVISIBLE
    }

    private fun handleSystemInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val sysBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appbarLayout.updatePadding(
                top = sysBarInsets.top,
                left = sysBarInsets.left,
                right = sysBarInsets.right
            )
            insets
        }
    }

    enum class NightMode(val value: Int) {
        LIGHT(1),
        DARK(2),
        SYSTEM(-1)
    }

    private fun initListeners() {
        binding.buttonLogOut.setOnClickListener {
            SignOutConfirmationDialogFragment().show(
                childFragmentManager,
                SignOutConfirmationDialogFragment.TAG
            )
        }
        binding.rbLight.setOnClickListener {
            mainViewModel.setDefaultNightMode(NightMode.LIGHT.value)
        }
        binding.rbDark.setOnClickListener {
            mainViewModel.setDefaultNightMode(NightMode.DARK.value)
        }
        binding.rbSystemDefault.setOnClickListener {
            mainViewModel.setDefaultNightMode(NightMode.SYSTEM.value)
        }
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}