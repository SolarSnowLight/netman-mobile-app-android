package com.thesimplycoder.simpledatepicker.com.game.mobileappar.network.models.team.players

import com.google.gson.annotations.SerializedName

data class CommandPlayerModel(
    @SerializedName("users_id") var usersId: Int,
    @SerializedName("team_users_id") var teamUsersId: Int,
    @SerializedName("name_previous_file") var namePreviousFile: String?,
    @SerializedName("access_token") var accessToken: String
)
