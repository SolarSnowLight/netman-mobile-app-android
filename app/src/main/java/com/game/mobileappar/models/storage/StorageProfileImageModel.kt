package com.game.mobileappar.models.storage

import com.google.gson.annotations.SerializedName

data class StorageProfileImageModel(
    @SerializedName("list_profile_image") var listProfileImage: MutableList<StorageProfileImageElementModel>
)
