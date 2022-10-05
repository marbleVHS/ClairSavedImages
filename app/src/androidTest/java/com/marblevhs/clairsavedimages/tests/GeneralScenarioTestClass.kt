package com.marblevhs.clairsavedimages.tests


import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.kaspersky.components.alluresupport.withAllureSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.screens.*
import com.marblevhs.clairsavedimages.workers.FetchingWorker
import io.github.kakaocup.kakao.switch.SwitchableActions
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test


class GeneralScenarioTestClass : TestCase(
    kaspressoBuilder = Kaspresso.Builder.withAllureSupport()
) {
    private lateinit var context: Context

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun sampleTest() = before {
        context = ApplicationProvider.getApplicationContext()
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

        step("add new favourite and exit details screen") {
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

        step("delete last favourite and exit details screen") {
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

        step("check Fetching worker") {
            ProfileScreen {
                notificationToggle {
                    isVisible()
                    isClickable()
                    swipeSwitchThumb(SwitchableActions.Direction.RIGHT)
                }
            }
            val worker = TestListenableWorkerBuilder<FetchingWorker>(context).build()
            runBlocking {
                val result = worker.doWork()
                Assert.assertThat(result, Matchers.`is`(ListenableWorker.Result.success()))
            }
            device.screenshots.take("check the notification")
            ProfileScreen {
                notificationToggle {
                    isVisible()
                    isClickable()
                    swipeSwitchThumb(SwitchableActions.Direction.LEFT)
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