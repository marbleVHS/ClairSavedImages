package com.marblevhs.clairsavedimages.favouritesList


import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.LocalImage

class FavouritesListAdapter(
    private val onClick: (LocalImage, String?) -> Unit
) :
    ListAdapter<LocalImage, FavouritesListAdapter.ViewHolder>(
        ImageDiffCallback
    ) {


    class ViewHolder(view: View, val onClick: (LocalImage, String?) -> Unit) :
        RecyclerView.ViewHolder(view) {
        private val ivImage: ImageView = view.findViewById(R.id.ivImage)
        private val constraintLayout: ConstraintLayout = view.findViewById(R.id.ConstraintLayout)
        private val cardView: CardView = view.findViewById(R.id.cardView)
        private var currentImage: LocalImage? = null
        private var memoryCacheKey: String? = null
        val set = ConstraintSet()

        fun bind(image: LocalImage) {
            currentImage = image
//            TODO:
//            ViewCompat.setTransitionName(ivImage, "image_${currentImage!!.id}")
            val ratio = String.format("%d:%d", image.width, image.height)
            set.clone(constraintLayout)
            set.setDimensionRatio(cardView.id, ratio)
            set.applyTo(constraintLayout)
            ivImage.load(image.thumbnailUrl) {
                bitmapConfig(Bitmap.Config.ARGB_8888)
                allowHardware(false)
                crossfade(enable = true)
                placeholder(R.drawable.ic_download_progress)
                error(R.drawable.ic_download_error)
                listener(onSuccess = { _, result -> memoryCacheKey = result.memoryCacheKey?.key })
            }
        }

        init {
            itemView.setOnClickListener {
                currentImage?.let {
//                    TODO:
//                    val extras = FragmentNavigatorExtras(
//                        ivImage to "image_${currentImage!!.id}")
                    onClick(it, memoryCacheKey)
                }
            }
        }

    }

    object ImageDiffCallback : DiffUtil.ItemCallback<LocalImage>() {
        override fun areContentsTheSame(oldItem: LocalImage, newItem: LocalImage): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: LocalImage, newItem: LocalImage): Boolean {
            return oldItem.id == newItem.id
        }

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = getItem(position)
        holder.bind(image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_card_view, parent, false)
        return ViewHolder(view, onClick)
    }


}