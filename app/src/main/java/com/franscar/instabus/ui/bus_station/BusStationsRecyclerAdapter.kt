package com.franscar.instabus.ui.bus_station

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.franscar.instabus.R
import com.franscar.instabus.data.images.UserImage
import java.io.File


class BusStationsRecyclerAdapter(
        private val context: Context, private val userImages: MutableList<UserImage>, private val itemListener:
        UserImagesItemListener): RecyclerView.Adapter<BusStationsRecyclerAdapter.ViewHolder>() {

    private var removedPosition: Int = 0
    private lateinit var removedItem: UserImage

    override fun getItemCount() = userImages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.bus_station_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userImage = userImages[position]
        with(holder) {
            userImageName.text = userImage.title
            userImageDate.text = userImage.date

            if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO) {
                userImageName.setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            Glide.with(holder.itemView.context)
                .load(File(userImage.image))
                .transform(CenterCrop(context))
                .into(holder.userImageIcon)

            /*if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO)
                userImageIcon.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            */

            holder.itemView.setOnClickListener {
                itemListener.onUserImageItemClick(userImage)
            }
        }
    }

    fun removeItem(position: Int) {
        userImages.removeAt(position)
        notifyItemRemoved(position)
    }

    fun restoreItem(position: Int, userImage: UserImage) {
        userImages.add(position, userImage)
        notifyItemInserted(position)
    }

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        var image = bitmap
        if (degrees != 0) {
            val matrix = Matrix()
            matrix.setRotate(
                    degrees.toFloat(),
                    image.width.toFloat() / 2,
                    image.height.toFloat() / 2
            )
            val newImage = Bitmap.createBitmap(
                    image, 0, 0, image.width,
                    image.height, matrix, true
            )
            if (image != newImage) {
                image.recycle()
                image = newImage
            }
        }
        return image
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userImageIcon: ImageView = itemView.findViewById(R.id.userImageIcon)
        val userImageName: TextView = itemView.findViewById(R.id.userImageName)
        val userImageDate: TextView = itemView.findViewById(R.id.userImageDate)
    }

    interface UserImagesItemListener {
        fun onUserImageItemClick(userImage: UserImage)
    }
}