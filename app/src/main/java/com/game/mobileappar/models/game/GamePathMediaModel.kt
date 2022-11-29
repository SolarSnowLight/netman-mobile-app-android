package com.game.mobileappar.models.game

import com.google.gson.annotations.SerializedName

data class GamePathMediaModel(
    @SerializedName("local_path") var localPath: String,
    @SerializedName("access_token") var accessToken: String
)
