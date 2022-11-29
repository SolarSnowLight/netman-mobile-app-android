package com.game.mobileappar.models.player.search.command

import com.google.gson.annotations.SerializedName

data class PlayerSearchCommandElementModel(
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("rating") var rating : Int,
    @SerializedName("name") var name : String,
    @SerializedName("surname") var surname : String,
    @SerializedName("nickname") var nickname : String,
    @SerializedName("date_birthday") var dateBirthday : String,
    @SerializedName("ref_image") var refImage : String,
    @SerializedName("location") var location : String
)
