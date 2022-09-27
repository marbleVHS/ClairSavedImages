package com.marblevhs.clairsavedimages.screens

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.favouritesList.FavouritesListFragment
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.toolbar.KToolbar
import org.hamcrest.Matcher

class FavouritesItem(parent: Matcher<View>) : KRecyclerItem<FavouritesItem>(parent) {
    val image = KImageView(parent) { withId(R.id.ivImage) }
}

object FavouritesListScreen : KScreen<FavouritesListScreen>() {

    override val layoutId: Int = R.layout.favourites_list_fragment
    override val viewClass: Class<*> = FavouritesListFragment::class.java

    val toolbar = KToolbar { withId(R.id.toolbar) }
    val rvImages =
        KRecyclerView({ withId(R.id.rvImages) }, itemTypeBuilder = { itemType(::FavouritesItem) })

}