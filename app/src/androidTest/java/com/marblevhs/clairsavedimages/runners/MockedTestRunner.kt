package com.marblevhs.clairsavedimages.runners

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.marblevhs.clairsavedimages.TestMainApp

class MockedTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, TestMainApp::class.java.name, context)
    }

}