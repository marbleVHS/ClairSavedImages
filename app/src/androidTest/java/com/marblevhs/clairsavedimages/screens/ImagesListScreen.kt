package com.marblevhs.clairsavedimages.screens

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.imageList.ImageListFragment
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.toolbar.KToolbar
import org.hamcrest.Matcher


class ImagesItem(parent: Matcher<View>) : KRecyclerItem<ImagesItem>(parent) {
    val image = KImageView(parent) { withId(R.id.ivImage) }
}

object ImagesListScreen : KScreen<ImagesListScreen>() {

    override val layoutId: Int = R.layout.image_list_fragment
    override val viewClass: Class<*> = ImageListFragment::class.java

    val toolbar = KToolbar { withId(R.id.toolbar) }
    val rvImages =
        KRecyclerView({ withId(R.id.rvImages) }, itemTypeBuilder = { itemType(::ImagesItem) })

}