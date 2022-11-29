package com.game.mobileappar.models

import com.google.gson.annotations.SerializedName

data class PlayerAccessModel(
    @SerializedName("access_token") var accessToken : String,
    @SerializedName("users_id") var usersId : Int,
)
