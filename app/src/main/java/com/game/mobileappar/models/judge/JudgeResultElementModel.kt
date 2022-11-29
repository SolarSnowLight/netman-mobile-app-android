package com.game.mobileappar.models.judge

import com.google.gson.annotations.SerializedName

data class JudgeResultElementModel(
    @SerializedName("id") var id : Int,
    @SerializedName("ref_image") var refImage : String,
    @SerializedName("game_id") var gameId : Int
)
