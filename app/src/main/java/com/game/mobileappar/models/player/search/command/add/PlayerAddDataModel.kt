package com.game.mobileappar.models.player.search.command.add

import com.google.gson.annotations.SerializedName

data class PlayerAddDataModel(
    @SerializedName("users_id") var usersId: Int,
    @SerializedName("player_users_id") var playerUsersId: Int,
    @SerializedName("commands_id") var commandsId: Int,
    @SerializedName("access_token") var accessToken: String
)
