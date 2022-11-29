package com.game.mobileappar.models.user

import com.google.gson.annotations.SerializedName;

data class UserDataModel(
    @SerializedName("access_token") var accessToken : String,
    @SerializedName("refresh_token") var refreshToken: String,
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("attributes") var attributes : UserAttributesModel,
    @SerializedName("modules") var modules : UserModulesModel,
    @SerializedName("type_auth") var typeAuth : Int,
)
