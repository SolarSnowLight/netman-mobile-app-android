package com.game.mobileappar.models.user

import com.google.gson.annotations.SerializedName;

data class UserModulesModel(
    @SerializedName("player") var player : Boolean,
    @SerializedName("judge") var judge : Boolean,
    @SerializedName("creator") var creator : Boolean,
    @SerializedName("moderator") var moderator : Boolean,
    @SerializedName("manager") var manager : Boolean,
    @SerializedName("admin") var admin : Boolean,
    @SerializedName("super_admin") var superAdmin : Boolean
)
