package com.marblevhs.clairsavedimages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.imageList.ImageListFragment

class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ImageListFragment.newInstance())
            .commit()

    }


}