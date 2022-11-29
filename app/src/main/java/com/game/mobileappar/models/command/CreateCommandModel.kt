package com.game.mobileappar.models.command

import com.google.gson.annotations.SerializedName

data class CreateCommandModel(
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("access_token") var accessToken : String,
    @SerializedName("name") var name : String,
)
