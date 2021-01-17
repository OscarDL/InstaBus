package com.franscar.instabus.ui.camera

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class CameraViewModel: ViewModel() {
    var imageSrc = MutableLiveData<File>()
}