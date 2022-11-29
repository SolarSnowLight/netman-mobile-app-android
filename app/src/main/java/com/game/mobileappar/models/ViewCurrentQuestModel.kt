package com.game.mobileappar.models

import com.google.gson.annotations.SerializedName

data class ViewCurrentQuestModel(
    @SerializedName("radius") var radius : Int,
    @SerializedName("lat") var lat : Double,
    @SerializedName("lng") var lng : Double
)
