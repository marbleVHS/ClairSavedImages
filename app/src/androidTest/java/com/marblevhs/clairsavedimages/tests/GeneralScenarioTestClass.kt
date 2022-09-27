package com.marblevhs.clairsavedimages.tests


import android.Manifest
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.screens.*
import org.junit.Rule
import org.junit.Test


class GeneralScenarioTestClass : TestCase() {

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

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

        step("open images screen") {
            NavBarScreen {
                picturesNavMenuButton {
                    isVisible()
                    isClickable()
                    click()
                }

            }
        }

        step("open details screen") {
            ImagesListScreen {
                rvImages {
                    scrollTo(2)
                    childAt<ImagesItem>(2) {
                        isVisible()
                        isClickable()
                        click()
                    }
                }
            }
        }

        step("add new favourite") {
            ImageDetailsScreen {
                favouritesButton {
                    isVisible()
                    isClickable()
                    click()
                }
                pressBack()
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

        step("open last favourite") {
            FavouritesListScreen {
                rvImages.childAt<FavouritesItem>(0) {
                    isVisible()
                    isClickable()
                    click()
                }
            }
        }

        step("delete last favourite") {
            ImageDetailsScreen {
                favouritesButton {
                    isVisible()
                    isClickable()
                    isChecked()
                    click()
                }
                pressBack()
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