package com.game.mobileappar.models.game

import com.google.gson.annotations.SerializedName

data class GamePlayerCoordinatesModel(
    @SerializedName("lat") var lat : Double,
    @SerializedName("lng") var lng : Double,
    @SerializedName("users_id") var usersId: Int
)
