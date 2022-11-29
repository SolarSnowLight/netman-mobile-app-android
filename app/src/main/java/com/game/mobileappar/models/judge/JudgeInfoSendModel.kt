package com.game.mobileappar.models.judge

import com.google.gson.annotations.SerializedName

data class JudgeInfoSendModel(
    @SerializedName("users_id") var usersId: Int,
    @SerializedName("commands_id") var commandsId: Int,
    @SerializedName("info_games_id") var infoGamesId: Int,
    @SerializedName("access_token") var accessToken: String,
)
