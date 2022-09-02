package com.marblevhs.clairsavedimages.screens

import com.kaspersky.kaspresso.screens.KScreen
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.profileScreen.ProfileFragment
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.text.KButton

object ProfileScreen : KScreen<ProfileScreen>() {

    override val layoutId: Int = R.layout.profile_fragment
    override val viewClass: Class<*> = ProfileFragment::class.java

    val lightRbButton = KButton { withId(R.id.rbLight) }
    val darkRbButton = KButton { withId(R.id.rbDark) }
    val systemDefaultRbButton = KButton { withId(R.id.rbSystemDefault) }
    val logOutButton = KButton { withId(R.id.buttonLogOut) }
    val errorLogOutButton = KButton { withId(R.id.buttonErrorLogOut) }
    val loader = KView { withId(R.id.profileLoader) }
    val errorImage = KImageView { withId(R.id.ivProfileError) }


}