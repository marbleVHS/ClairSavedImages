package com.marblevhs.clairsavedimages


import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.marblevhs.clairsavedimages.databinding.MainActivityBinding
import com.marblevhs.clairsavedimages.extensions.appComponent
import com.marblevhs.clairsavedimages.loginScreen.LoginActivityResultCallback
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: MainViewModel.Factory

    private var VkLoginActivityResultLauncher: ActivityResultLauncher<Collection<VKScope>>? = null
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels { viewModelFactory }
    private val binding by viewBinding(MainActivityBinding::bind)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.appComponent.inject(this)
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.main_activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setupBottomNavigation()
        if (savedInstanceState == null) {
            viewModel.getIsLogged()
        }
        VkLoginActivityResultLauncher =
            VK.login(this, LoginActivityResultCallback(viewModel))
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.isLoggedFlow.collectLatest {
                    updateIsLogged(it)
                }
            }
        }
    }


    private fun setupBottomNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.NavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavBar.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_SELECTED
        binding.bottomNavBar.setupWithNavController(navController)
    }


    private fun updateIsLogged(isLogged: Boolean) {
        if (isLogged) {
            if (navController.currentDestination?.equals(navController.findDestination(R.id.loginFragment)) == true) {
                navController.setGraph(R.navigation.nav_graph)
            }
        } else {
            if (navController.currentDestination?.equals(navController.findDestination(R.id.loginFragment)) != true) {
                val navOptions =
                    NavOptions.Builder().setPopUpTo(R.id.nav_graph, inclusive = true).build()
                navController.navigate(
                    resId = R.id.login_screen,
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

