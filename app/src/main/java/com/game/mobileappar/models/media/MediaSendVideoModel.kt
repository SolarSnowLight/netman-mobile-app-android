package com.game.mobileappar.models.media

import com.google.gson.annotations.SerializedName

data class MediaSendVideoModel(
    @SerializedName("current_games") var currentGames: Int?,
    @SerializedName("games_id") var gamesId: Int?,
    @SerializedName("users_id") var usersId: Int?,
    @SerializedName("ref_media_instructions") var refMediaInstructions: String?,
    @SerializedName("access_token") var accessToken: String?
)
