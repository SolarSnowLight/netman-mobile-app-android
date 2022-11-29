package com.game.mobileappar.models.command.players

import com.google.gson.annotations.SerializedName

data class CommandPlayersModel(
    @SerializedName("users") var users : List<CommandPlayersElementModel>
)
