package com.game.mobileappar.models.command.games

import com.google.gson.annotations.SerializedName

data class CommandGamesModel(
    @SerializedName("games") var games : List<CommandGamesElementModel>
)
