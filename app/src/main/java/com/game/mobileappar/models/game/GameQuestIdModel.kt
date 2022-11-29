package com.game.mobileappar.models.game

import com.google.gson.annotations.SerializedName

data class GameQuestIdModel(
    @SerializedName("users_id") var usersId: Int,
    @SerializedName("access_token") var accessToken: String,
    @SerializedName("quests_id") var questsId: Int
)
