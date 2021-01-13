package com.franscar.instabus.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.franscar.instabus.data.BusStationsRepository

class MapViewModel(app: Application) : AndroidViewModel(app) {
    val busStationsData = BusStationsRepository(app).busStationsData
}