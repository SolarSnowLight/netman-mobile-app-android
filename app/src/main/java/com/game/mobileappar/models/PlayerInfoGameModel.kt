package com.game.mobileappar.models

import com.google.gson.annotations.SerializedName

data class PlayerInfoGameModel(
    @SerializedName("data") var data : List<PlayerInfoGameDataModel>
)
