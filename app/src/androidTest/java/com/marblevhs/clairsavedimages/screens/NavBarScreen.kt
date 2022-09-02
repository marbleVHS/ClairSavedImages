package com.marblevhs.clairsavedimages.screens

import com.kaspersky.kaspresso.screens.KScreen
import com.marblevhs.clairsavedimages.NavBarFragment
import com.marblevhs.clairsavedimages.R
import io.github.kakaocup.kakao.text.KButton

object NavBarScreen : KScreen<NavBarScreen>() {

    override val layoutId: Int = R.layout.fragment_nav_bar
    override val viewClass: Class<*> = NavBarFragment::class.java

    val picturesNavMenuButton = KButton { withId(R.id.imageListFragment) }
    val favouritesNavMenuButton = KButton { withId(R.id.favouritesListFragment) }
    val profileNavMenuButton = KButton { withId(R.id.profileFragment) }

}