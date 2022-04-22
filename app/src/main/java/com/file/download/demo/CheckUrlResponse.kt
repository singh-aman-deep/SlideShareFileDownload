package com.file.download.demo


import com.google.gson.annotations.SerializedName

data class CheckUrlResponse(
    @SerializedName("url_valid")
    val urlValid: Boolean
)