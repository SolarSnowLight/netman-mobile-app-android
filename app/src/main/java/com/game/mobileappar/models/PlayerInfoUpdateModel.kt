package com.game.mobileappar.models

import com.google.gson.annotations.SerializedName

data class PlayerInfoUpdateModel(
    @SerializedName("access_token") var accessToken: String,
    @SerializedName("nickname") var nickname: String,
    @SerializedName("name") var name: String,
    @SerializedName("surname") var surname: String,
    @SerializedName("old_email") var oldEmail: String,
    @SerializedName("new_email") var newEmail: String?,
    @SerializedName("phone_num") var phone_num: String,
    @SerializedName("date_birthday") var date_birthday: String?,
    @SerializedName("location") var location: String?,
    @SerializedName("users_id") var usersId: Int
)
