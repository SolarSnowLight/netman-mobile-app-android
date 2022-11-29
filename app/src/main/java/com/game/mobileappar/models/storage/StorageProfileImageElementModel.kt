package com.game.mobileappar.models.storage

import com.google.gson.annotations.SerializedName

data class StorageProfileImageElementModel(
    @SerializedName("uri") var uri : String,
    @SerializedName("file_path") var filePath: String,
    @SerializedName("users_id") var usersId: Int
)
