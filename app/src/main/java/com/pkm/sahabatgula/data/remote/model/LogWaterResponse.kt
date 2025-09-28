package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class LogWaterResponse (
    val success: Boolean,
    val message: String,
    val data: LogWaterData? = null
)

data class LogWaterData (
    @SerializedName("id")
    val id: String,

    @SerializedName("amount")
    val amount: Int,

    @SerializedName("logged_at")
    val date: String
)

data class LogWaterRequest (
    val amount: Int
)