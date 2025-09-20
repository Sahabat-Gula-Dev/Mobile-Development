package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?
)