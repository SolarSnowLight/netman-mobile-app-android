package com.game.mobileappar.models.auth

import com.google.gson.annotations.SerializedName

data class AuthRefreshTokenModel(
    @SerializedName("refresh_token") var refreshToken : String,
    @SerializedName("type_auth") var typeAuth : Int,
)

