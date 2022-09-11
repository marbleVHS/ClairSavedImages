package com.marblevhs.clairsavedimages.tests


import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.screens.LoginScreen
import com.marblevhs.clairsavedimages.screens.NavBarScreen
import com.marblevhs.clairsavedimages.screens.ProfileScreen
import com.marblevhs.clairsavedimages.screens.SignOutDialogScreen
import org.junit.Rule
import org.junit.Test


class SampleTestClass : TestCase() {

//    @get:Rule
//    val runtimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
//        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.READ_EXTERNAL_STORAGE
//    )

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun sampleTest() = before {

    }.after {

    }.run {

        step("login") {
            LoginScreen {
                loginButton {
                    isVisible()
                    isClickable()
                    click()
                }
            }
        }

        step("open pictures screen") {
            NavBarScreen {
                picturesNavMenuButton {
                    isVisible()
                    isClickable()
                    click()
                }

            }
        }
        step("open favourites screen") {
            NavBarScreen {
                favouritesNavMenuButton {
                    isVisible()
                    isClickable()
                    click()
                }
            }
        }
        step("open profile screen") {
            NavBarScreen {
                profileNavMenuButton {
                    isVisible()
                    isClickable()
                    click()
                }
            }
        }

        step("change themes a little") {
            ProfileScreen {
                darkRbButton {
                    isVisible()
                    isClickable()
                    click()
                }

                lightRbButton {
                    isVisible()
                    isClickable()
                    click()
                }
                systemDefaultRbButton {
                    isVisible()
                    isClickable()
                    click()
                }
            }
        }

        step("open the log out dialog") {
            ProfileScreen {
                errorLogOutButton {
                    isGone()
                }
                logOutButton {
                    isVisible()
                    isClickable()
                    click()
                    device.screenshots.take("check the dialog")
                }
            }
        }

        step("confirm the log out") {
            SignOutDialogScreen {
                title {
                    isVisible()
                    containsText("Are you sure?")
                }
                CancelButton {
                    isVisible()
                    isClickable()
                }
                SignOutButton {
                    isVisible()
                    isClickable()
                    click()
                }
            }
        }

    }

}