package com.game.mobileappar.models.judge

import com.google.gson.annotations.SerializedName

data class JudgeInfoModel(
    @SerializedName("id") var id: Int,
    @SerializedName("users_id") var usersId: Int,
    @SerializedName("commands_id") var commandsId: Int,
    @SerializedName("info_games_id") var infoGamesId: Int,
)
