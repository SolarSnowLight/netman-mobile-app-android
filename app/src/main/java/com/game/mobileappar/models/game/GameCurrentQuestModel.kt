package com.game.mobileappar.models.game

import com.google.gson.annotations.SerializedName

data class GameCurrentQuestModel(
    @SerializedName("task") var task : String,
    @SerializedName("hint") var hint : String,
    @SerializedName("number") var number : Int,
    @SerializedName("id") var id : Int,
    @SerializedName("commands_id") var commandsId : Int,
    @SerializedName("register_commands_id") var registerCommandsId: Int,
    @SerializedName("quests_id") var questsId: Int,
    @SerializedName("current_games_id") var currentGamesId: Int
)
