package com.franscar.instabus.data

import com.squareup.moshi.Json

data class ResponseData (
    val code: Int,
    val data: NearStations
)

data class NearStations (
    val nearstations: List<BusStation>
)

data class BusStation (
    @Json(name="id") val id_station: Int,
    val street_name: String,
    val city: String,
    val utm_x: String,
    val utm_y: String,
    val lat: Double,
    val lon: Double,
    val furniture: String,
    val buses: String,
    val distance: Double
)