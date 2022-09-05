package com.marblevhs.clairsavedimages


import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.work.*
import com.google.android.material.color.DynamicColors
import com.marblevhs.clairsavedimages.loginScreen.LoginActivityResultCallback
import com.marblevhs.clairsavedimages.loginScreen.LoginFragmentDirections
import com.marblevhs.clairsavedimages.utils.appComponent
import com.marblevhs.clairsavedimages.workers.FetchingWorker
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: MainViewModel.Factory

    private var vkLoginActivityResultLauncher: ActivityResultLauncher<Collection<VKScope>>? = null
    lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels { viewModelFactory }
    private val windowInsetsController by lazy {
        WindowCompat.getInsetsController(window, window.decorView)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.appComponent.inject(this)
        if (savedInstanceState == null) {
            requestNotificationPermission()
            startFetchingWorker()
        }
        setupEdgeToEdgeBehavior()
        vkLoginActivityResultLauncher =
            VK.login(this, LoginActivityResultCallback(viewModel, applicationContext))
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.main_activity)
        initializeNavigation()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    viewModel.isLoggedFlow.collectLatest {
                        updateIsLogged(it)
                    }
                }
                launch {
                    viewModel.defaultNightModeFlow.collectLatest {
                        updateDefaultNightMode(it)
                    }
                }
            }
        }

    }

    private fun initializeNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.NavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupEdgeToEdgeBehavior() {
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.POST_NOTIFICATIONS"
            ) == PermissionChecker.PERMISSION_DENIED
        ) {
            val requestPermissionLauncher =
                registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {}
            requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
        }
    }

    private fun startFetchingWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work =
            PeriodicWorkRequestBuilder<FetchingWorker>(
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS,
                PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS
            )
                .setConstraints(constraints)
                .build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            "Fetching work",
            ExistingPeriodicWorkPolicy.REPLACE,
            work
        )

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


    fun startVkLoginActivity() {
        vkLoginActivityResultLauncher?.launch(
            arrayListOf(
                VKScope.WALL,
                VKScope.PHOTOS,
                VKScope.OFFLINE
            )
        )
    }

    fun clearAccessToken() {
        VK.logout()
        viewModel.clearLoginData()
    }

    private fun updateDefaultNightMode(defaultNightMode: Int = MODE_NIGHT_FOLLOW_SYSTEM) {
        setDefaultNightMode(defaultNightMode)
    }


    fun hideSystemBars() {
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    fun showSystemBars() {

        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }


}

