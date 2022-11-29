package com.game.mobileappar.models.judge

import com.google.gson.annotations.SerializedName

data class JudgeQuestInfoModel(
    @SerializedName("id") var id : Int,
    @SerializedName("task") var task : String,
    @SerializedName("hint") var hint : String,
    @SerializedName("ref_media") var refMedia : String,
    @SerializedName("radius") var radius : Int,
    @SerializedName("marks_lat") var marksLat : Double,
    @SerializedName("marks_lng") var marksLng : Double
)
