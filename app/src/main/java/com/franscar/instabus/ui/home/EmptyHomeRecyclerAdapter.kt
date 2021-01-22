package com.franscar.instabus.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.franscar.instabus.R

class EmptyHomeRecyclerAdapter(private val context: Context): RecyclerView.Adapter<EmptyHomeRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.busStationStreet)
        val desc: TextView = itemView.findViewById(R.id.busStationDistance)
        val icon: ImageView = itemView.findViewById(R.id.busStationIcon)

        init {
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "Please wait until data is loaded.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.home_list_item, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = null
        holder.desc.text = null
        holder.icon.setImageResource(R.drawable.ic_bus_56dp)

        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO) {
            holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }

    override fun getItemCount(): Int {
        return 16
    }
}