package com.game.mobileappar.models.player

import com.google.gson.annotations.SerializedName

data class PlayerInfoElementModel(
    @SerializedName("id") var id : Int,
    @SerializedName("rating") var rating : Int,
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("commands_id") var commandsId : Int
)
