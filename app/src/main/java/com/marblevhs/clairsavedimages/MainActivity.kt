package com.marblevhs.clairsavedimages



import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.navigation.NavigationBarView



class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyIfAvailable(this)
        setContentView(R.layout.main_activity)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.NavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavBar.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_SELECTED
        bottomNavBar.setupWithNavController(navController)
    }



    fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }


}

