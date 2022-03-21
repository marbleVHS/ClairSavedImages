package com.marblevhs.clairsavedimages


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.navigation.NavigationBarView
import com.marblevhs.clairsavedimages.extensions.appComponent
import com.marblevhs.clairsavedimages.imageList.ImageListFragmentDirections
import com.marblevhs.clairsavedimages.loginScreen.LoginFragmentDirections
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: MainViewModel.Factory

    lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels { viewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.appComponent.inject(this)
        DynamicColors.applyIfAvailable(this)
        setContentView(R.layout.main_activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.NavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavBar.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_SELECTED
        bottomNavBar.setupWithNavController(navController)
        if (savedInstanceState == null) {
            viewModel.getIsLogged()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.isLoggedFlow.collectLatest {
                    updateIsLogged(it)
                }
            }
        }
    }

    private fun updateIsLogged(isLogged: Boolean) {
        if (isLogged) {
            if (navController.currentDestination?.equals(navController.graph.findNode(R.id.loginFragment)) == true) {
                navController.navigate(LoginFragmentDirections.actionLoginFragmentToImageListFragment())
            }
        } else {
            if (navController.currentDestination?.equals(navController.graph.findNode(R.id.loginFragment)) != true) {
                navController.navigate(ImageListFragmentDirections.actionImageListFragmentToLoginFragment())
            }
        }
    }

    fun clearLoginData() {
        viewModel.clearLoginData()
    }

    fun getAccessToken() {
        VK.login(
            this,
            arrayListOf(
                VKScope.WALL,
                VKScope.PHOTOS,
                VKScope.FRIENDS,
                VKScope.GROUPS,
                VKScope.OFFLINE
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                viewModel.saveAccessToken(token.accessToken)
            }

            override fun onLoginFailed(authException: VKAuthException) {

            }

        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
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

