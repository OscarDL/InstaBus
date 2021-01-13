package com.franscar.instabus.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.franscar.instabus.data.BusStationsRepository

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    val busStationsData = BusStationsRepository(app).busStationsData
}