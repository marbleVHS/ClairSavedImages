package com.marblevhs.clairsavedimages.screens

import com.kaspersky.kaspresso.screens.KScreen
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.loginScreen.LoginFragment
import io.github.kakaocup.kakao.text.KButton

object LoginScreen : KScreen<LoginScreen>() {

    override val layoutId: Int = R.layout.login_fragment
    override val viewClass: Class<*> = LoginFragment::class.java

    val loginButton = KButton { withText("Login via VK") }

}