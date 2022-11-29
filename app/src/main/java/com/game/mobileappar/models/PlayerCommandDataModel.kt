package com.game.mobileappar.models

import com.google.gson.annotations.SerializedName

data class PlayerCommandDataModel(
    @SerializedName("access_token") var accessToken : String,
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("commands_id") var commandsId : Int
)
