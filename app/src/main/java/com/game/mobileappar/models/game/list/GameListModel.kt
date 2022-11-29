package com.game.mobileappar.models.game.list

import com.google.gson.annotations.SerializedName

data class GameListModel(
    @SerializedName("info_games") var infoGames : MutableList<GameListElementModel>
)
