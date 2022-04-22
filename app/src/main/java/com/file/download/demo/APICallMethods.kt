package com.file.download.demo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface APICallMethods {
    @GET("check-url.php")
    fun checkUrl(@Query("url") URL: String): Call<CheckUrlResponse>
}
