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
import androidx.preference.PreferenceManager
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

    override fun getItemCount() = userImages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val layout =
            if (!prefs.getBoolean("enable_pictures_grid", false))
                R.layout.bus_station_list_item
            else
                R.layout.bus_station_grid_item

        return ViewHolder(inflater.inflate(layout, parent, false))
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

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userImageIcon: ImageView = itemView.findViewById(R.id.userImageIcon)
        val userImageName: TextView = itemView.findViewById(R.id.userImageName)
        val userImageDate: TextView = itemView.findViewById(R.id.userImageDate)
    }

    interface UserImagesItemListener {
        fun onUserImageItemClick(userImage: UserImage)
    }
}