package com.franscar.instabus.ui.home

import android.annotation.SuppressLint
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
import com.franscar.instabus.data.BusStation

class HomeRecyclerAdapter(
    private val context: Context, private val busStations: List<BusStation>, private val itemListener:
  BusStationsItemListener): RecyclerView.Adapter<HomeRecyclerAdapter.ViewHolder>() {
    override fun getItemCount(): Int { return busStations.size }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.home_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val busStation = busStations[position]
        with(holder) {
            busStationStreet.text = busStation.street_name
            busStationDistance.text = "${((busStation.distance * 1000).toInt()).toString().replaceFirst("^0+", "")}m away"

            if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO) {
                busStationStreet.setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO) {
                busStationIcon.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            }

            holder.itemView.setOnClickListener {
                itemListener.onBusStationItemClick(busStation)
            }
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val busStationIcon: ImageView = itemView.findViewById(R.id.busStationIcon)
        val busStationStreet: TextView = itemView.findViewById(R.id.busStationStreet)
        val busStationDistance: TextView = itemView.findViewById(R.id.busStationDistance)
    }

    interface BusStationsItemListener {
        fun onBusStationItemClick(busStation: BusStation)
    }
}