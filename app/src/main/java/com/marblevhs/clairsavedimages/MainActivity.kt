package com.marblevhs.clairsavedimages


import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.color.DynamicColors
import com.marblevhs.clairsavedimages.extensions.appComponent
import com.marblevhs.clairsavedimages.loginScreen.LoginActivityResultCallback
import com.marblevhs.clairsavedimages.loginScreen.LoginFragmentDirections
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: MainViewModel.Factory

    private var VkLoginActivityResultLauncher: ActivityResultLauncher<Collection<VKScope>>? = null
    lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels { viewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.appComponent.inject(this)
        viewModel.getDefaultNightMode()
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.main_activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.NavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        if (savedInstanceState == null) {
            viewModel.getIsLogged()
        }
        VkLoginActivityResultLauncher =
            VK.login(this, LoginActivityResultCallback(viewModel))
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    viewModel.isLoggedFlow.collectLatest {
                        updateIsLogged(it)
                    }
                }
                launch {
                    viewModel.defaultNightMode.collectLatest {
                        updateDefaultNightMode(it)
                    }
                }
            }
        }
    }


    private fun updateIsLogged(isLogged: Boolean) {
        if (isLogged) {
            if (navController.currentDestination?.id == R.id.loginFragment) {
                navController.navigate(LoginFragmentDirections.actionLoginFragmentToNavBarFragment())
            }
        } else {
            if (navController.currentDestination?.id != R.id.loginFragment) {
                val navOptions =
                    NavOptions.Builder().setPopUpTo(R.id.nav_graph, inclusive = true).build()
                navController.navigate(
                    resId = R.id.loginFragment,
                    args = null,
                    navOptions = navOptions
                )
            }
        }
    }


    fun getAccessToken() {
        VkLoginActivityResultLauncher?.launch(
            arrayListOf(
                VKScope.WALL,
                VKScope.PHOTOS,
                VKScope.OFFLINE
            )
        )
    }

    fun clearAccessToken() {
        viewModel.clearLoginData()
    }

    fun updateDefaultNightMode(defaultNightMode: Int = MODE_NIGHT_YES) {
        AppCompatDelegate.setDefaultNightMode(defaultNightMode)
    }


    fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    fun showSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }


}

