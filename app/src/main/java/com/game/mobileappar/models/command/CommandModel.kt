package com.game.mobileappar.models.command

import com.google.gson.annotations.SerializedName

data class CommandModel(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name : String,
    @SerializedName("date_register") var dateRegister : String,
    @SerializedName("rating") var rating : Int,
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("count_players") var countPlayers: Int,
    @SerializedName("location") var location: String,
)
