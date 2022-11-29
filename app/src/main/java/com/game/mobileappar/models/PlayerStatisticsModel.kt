package com.game.mobileappar.models

import com.google.gson.annotations.SerializedName

data class PlayerStatisticsModel(
    @SerializedName("rating_player") var ratingPlayer : Int,
    @SerializedName("rating_command") var ratingCommand : Int
)
