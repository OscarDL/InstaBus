package com.franscar.instabus.ui.bus_station

import android.content.ClipData.Item
import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.franscar.instabus.R
import com.franscar.instabus.data.images.UserImage
import com.google.android.material.snackbar.Snackbar


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
                userImageIcon.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            }

            holder.itemView.setOnClickListener {
                itemListener.onUserImageItemClick(userImage)
            }
        }
    }

    fun removeItem(position: Int) {
        removedPosition = position
        removedItem = userImages[position]

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