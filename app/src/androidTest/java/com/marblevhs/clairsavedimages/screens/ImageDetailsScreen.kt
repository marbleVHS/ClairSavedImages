package com.marblevhs.clairsavedimages.screens

import com.kaspersky.kaspresso.screens.KScreen
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.imageDetails.ImageDetailsFragment
import io.github.kakaocup.kakao.check.KCheckBox
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.text.KButton

object ImageDetailsScreen : KScreen<ImageDetailsScreen>() {

    override val layoutId: Int = R.layout.image_details_fragment
    override val viewClass: Class<*> = ImageDetailsFragment::class.java

    val likeButton = KButton { withId(R.id.likeButton) }
    val zoomInButton = KCheckBox { withId(R.id.zoomInButton) }
    val favouritesButton = KCheckBox { withId(R.id.favouritesButton) }
    val selectedImageView = KImageView { withId(R.id.ivSelectedImage) }

}