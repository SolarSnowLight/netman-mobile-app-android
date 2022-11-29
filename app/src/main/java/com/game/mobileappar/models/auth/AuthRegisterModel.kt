package com.game.mobileappar.models.auth

import com.google.gson.annotations.SerializedName

data class AuthRegisterModel (
    @SerializedName("name") var name : String,
    @SerializedName("surname") var surname : String,
    @SerializedName("nickname") var nickname : String,
    @SerializedName("phone_num") var phoneNum : String,
    @SerializedName("date_birthday") var dateBirthday : String,
    @SerializedName("location") var location : String,
    @SerializedName("email") var email : String,
    @SerializedName("password") var password: String
)