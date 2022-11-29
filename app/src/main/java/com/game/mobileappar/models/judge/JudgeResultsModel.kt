package com.game.mobileappar.models.judge

import com.google.gson.annotations.SerializedName

data class JudgeResultsModel(
    @SerializedName("id") var id : Int,
    @SerializedName("view") var view : Boolean,
    @SerializedName("commands_id") var commandsId : Int,
    @SerializedName("register_commands_id") var registerCommandsId : Int,
    @SerializedName("quests_id") var questsId : Int,
    @SerializedName("result_info") var resultInfo : JudgeResultElementModel,
    @SerializedName("quest_info") var questInfo : JudgeQuestInfoModel
)
