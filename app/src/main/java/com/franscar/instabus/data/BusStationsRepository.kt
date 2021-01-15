package com.franscar.instabus.data

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.franscar.instabus.utilities.FileHelper
import com.franscar.instabus.utilities.WEB_SERVICE_URL
import com.franscar.instabus.utilities.refreshData
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class BusStationsRepository(val app: Application) {
    val busStationsData = MutableLiveData<List<BusStation>>()

    init {
        refreshData()
    }

    @WorkerThread
    private suspend fun getData() {
        if (networkAvailable() && refreshData) {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(WEB_SERVICE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val service = retrofit.create(BusStationsService::class.java)
            val serviceData = service.getBusStationsData().body()?.data?.nearstations
            Log.i("ResponseData", serviceData.toString())
            busStationsData.postValue(serviceData ?: emptyList())
            Log.i("BusStationsRepository", "PULLING_DATA_FROM_WEB")
            if (serviceData != null) {
                saveDataToCache(serviceData)
                Log.i("BusStationsRepository", "SAVED_DATA_TO_CACHE")
            }
            refreshData = false
        } else {
            val data = readDataFromCache()
            if (data.isEmpty()) {
                return
            } else {
                busStationsData.postValue(data)
                Log.i("BusStationsRepository", "READING_DATA_FROM_CACHE")
            }
        }
    }

    @Suppress("DEPRECATION")
    fun networkAvailable(): Boolean {
        val connectivityManager = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
    }

    private fun refreshData() {
        CoroutineScope(Dispatchers.IO).launch {
            getData()
        }
    }

    private fun saveDataToCache(busStationsData: List<BusStation>) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val listType = Types.newParameterizedType(List::class.java, BusStation::class.java)
        val adapter: JsonAdapter<List<BusStation>> = moshi.adapter(listType)

        val json = adapter.toJson(busStationsData)
        FileHelper.saveToTextFile(app, json)
    }

    private fun readDataFromCache(): List<BusStation> {
        val json = FileHelper.readTextFile(app) ?: return emptyList()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val listType = Types.newParameterizedType(List::class.java, BusStation::class.java)

        val adapter: JsonAdapter<List<BusStation>> = moshi.adapter(listType)
        return adapter.fromJson(json) ?: emptyList()
    }
}