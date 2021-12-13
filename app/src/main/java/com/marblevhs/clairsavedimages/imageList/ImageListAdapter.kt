package com.marblevhs.clairsavedimages.imageList



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.data.Image

class ImageListAdapter(private val onClick: (Image) -> Unit): ListAdapter<Image, ImageListAdapter.ViewHolder>(
    ImageDiffCallback
){


    class ViewHolder(view: View,  val onClick: (Image) -> Unit): RecyclerView.ViewHolder(view){
        val ivCatImage: ImageView = view.findViewById(R.id.ivCatImage)
        private var currentImage: Image? = null


        fun bind(image: Image){
            currentImage = image

            var size = image.sizes[image.sizes.size - 1]
            for (i in image.sizes.indices){
                if(image.sizes[i].type == "p"){
                    size = image.sizes[i]
                }
            }
            ivCatImage.load(size.imageUrl) {
                crossfade(enable = true)
                placeholder(R.drawable.ic_download_progress)
                error(R.drawable.ic_download_error)
            }
        }

        init {
            itemView.setOnClickListener {
                currentImage?.let {
                    onClick(it)
                }
            }
        }

    }

    object ImageDiffCallback: DiffUtil.ItemCallback<Image>() {
        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem.id == newItem.id
        }

    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = getItem(position)
        holder.bind(image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_view, parent, false)
//        Log.i("RESP", "Creating view holder")
        return ViewHolder(view, onClick)
    }


}