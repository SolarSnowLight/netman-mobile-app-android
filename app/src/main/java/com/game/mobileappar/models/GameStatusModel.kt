package com.game.mobileappar.models

import com.google.gson.annotations.SerializedName

data class GameStatusModel(
    @SerializedName("player") var player : Boolean,
    @SerializedName("judge") var judge : Boolean,
    @SerializedName("player_status") var playerStatus: Int
)
