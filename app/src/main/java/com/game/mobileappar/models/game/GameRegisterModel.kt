package com.game.mobileappar.models.game

import com.google.gson.annotations.SerializedName

data class GameRegisterModel(
    @SerializedName("access_token") var accessToken : String,
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("info_games_id") var infoGamesId : Int
)
