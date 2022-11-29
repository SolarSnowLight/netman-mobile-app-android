package com.game.mobileappar.models.player.search.command

import com.google.gson.annotations.SerializedName

data class PlayerSearchCommandModel(
    @SerializedName("free_players") var freePlayers : MutableList<PlayerSearchCommandElementModel>
)
