package com.franscar.instabus.data

import retrofit2.Response
import retrofit2.http.GET

interface BusStationsService {
    @GET("/bus/nearstation/latlon/%2041.3985182/2.1917991/1.json")
    //@GET("/raw/C198Qzwp")
    suspend fun getBusStationsData(): Response<ResponseData>
}