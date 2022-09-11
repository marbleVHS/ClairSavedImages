package com.marblevhs.clairsavedimages.screens

import com.kaspersky.kaspresso.screens.KScreen
import com.marblevhs.clairsavedimages.profileScreen.SignOutConfirmationDialogFragment
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView

object SignOutDialogScreen : KScreen<SignOutDialogScreen>() {

    override val layoutId: Int? = null
    override val viewClass: Class<*> = SignOutConfirmationDialogFragment::class.java

    val SignOutButton = KButton { withText("Sign out") }
    val CancelButton = KButton { withText("Cancel") }
    val title = KTextView { withText("Are you sure?") }

}