package com.game.mobileappar.models

import com.google.gson.annotations.SerializedName

data class AccessTokenModel(
    @SerializedName("access_token") var accessToken : String,
)
