package com.marblevhs.clairsavedimages.runners

import android.app.Application
import android.content.Context
import com.marblevhs.clairsavedimages.TestMainApp
import io.qameta.allure.android.runners.AllureAndroidJUnitRunner

class MockedTestRunner : AllureAndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, TestMainApp::class.java.name, context)
    }

}