package com.game.mobileappar.models.player

import com.google.gson.annotations.SerializedName

data class PlayerInfoModel(
    @SerializedName("id") var id : Int,
    @SerializedName("name") var name : String,
    @SerializedName("surname") var surname : String,
    @SerializedName("nickname") var nickname : String,
    @SerializedName("ref_image") var refImage : String,
    @SerializedName("phone_num") var phoneNum : String,
    @SerializedName("date_birthday") var dateBirthday : String,
    @SerializedName("location") var location : String,
    @SerializedName("date_register") var dateRegister : String,
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("data_players") var dataPlayers : PlayerInfoElementModel
)
