package com.game.mobileappar.models.judge

import com.google.gson.annotations.SerializedName

data class JudgeInfoResultModel(
    @SerializedName("results_info") var resultsInfo : MutableList<JudgeResultsModel>,
    @SerializedName("info_game") var infoGame : JudgeGameInfoModel,
    @SerializedName("info_command") var infoCommand : JudgeCommandInfoModel
)
