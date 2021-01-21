package com.franscar.instabus.ui.shared

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.franscar.instabus.data.BusStation
import com.franscar.instabus.data.BusStationsRepository
import com.franscar.instabus.data.images.UserImage

class SharedViewModel(app: Application) : AndroidViewModel(app) {
    val busStationsData = BusStationsRepository(app).busStationsData
    val selectedBusStation = MutableLiveData<BusStation>()
    val selectedImage = MutableLiveData<UserImage>()
    var canGetImages = true
}