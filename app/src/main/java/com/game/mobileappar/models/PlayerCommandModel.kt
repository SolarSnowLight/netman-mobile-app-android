package com.game.mobileappar.models

import com.google.gson.annotations.SerializedName

data class PlayerCommandModel(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name : String,
    @SerializedName("date_register") var dateRegister : String,
    @SerializedName("rating") var rating : Int,
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("name_creator") var nameCreator : String,
    @SerializedName("users") var users : List<PlayerInfoTeamModel>
)
