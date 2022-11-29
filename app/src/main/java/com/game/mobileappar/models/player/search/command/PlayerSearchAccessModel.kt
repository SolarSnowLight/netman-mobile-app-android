package com.game.mobileappar.models.player.search.command

import com.google.gson.annotations.SerializedName

data class PlayerSearchAccessModel(
    @SerializedName("access_token") var accessToken : String,
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("tag") var tag: String,
)
