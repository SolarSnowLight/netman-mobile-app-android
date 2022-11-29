package com.game.mobileappar.models.error

import com.google.gson.annotations.SerializedName

data class ErrorElementModel(
    @SerializedName("value") var value : String,
    @SerializedName("msg") var msg : String,
    @SerializedName("param") var param : String,
    @SerializedName("location") var location : String
)