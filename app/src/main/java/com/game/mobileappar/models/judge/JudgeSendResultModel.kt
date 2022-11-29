package com.game.mobileappar.models.judge

import com.google.gson.annotations.SerializedName

data class JudgeSendResultModel(
    @SerializedName("score") var score: Int,
    @SerializedName("game_finished_id") var gameFinishedId: Int,
    @SerializedName("fix_judges_id") var fixJudgesId: Int,
    @SerializedName("access_token") var accessToken: String
)
