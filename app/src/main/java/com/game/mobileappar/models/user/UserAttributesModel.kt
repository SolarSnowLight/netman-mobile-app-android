package com.game.mobileappar.models.user

import com.google.gson.annotations.SerializedName

data class UserAttributesModel(
    @SerializedName("read") var read : Boolean,
    @SerializedName("write") var write : Boolean,
    @SerializedName("update") var update : Boolean,
    @SerializedName("delete") var delete : Boolean
)
