package com.game.mobileappar.models.media

import com.google.gson.annotations.SerializedName

data class MediaSendLocalPathModel(
    @SerializedName("access_token") var accessToken: String?,
    @SerializedName("game_id") var gameId: Int?,
    @SerializedName("ref_media") var refMedia: String?,
)
