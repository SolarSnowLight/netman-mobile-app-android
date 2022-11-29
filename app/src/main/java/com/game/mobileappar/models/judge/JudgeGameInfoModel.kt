package com.game.mobileappar.models.judge

import com.google.gson.annotations.SerializedName

data class JudgeGameInfoModel(
    @SerializedName("id") var id : Int,
    @SerializedName("name") var name : String,
    @SerializedName("max_count_commands") var maxCountCommands : Int,
    @SerializedName("date_begin") var dateBegin : String,
    @SerializedName("date_end") var dateEnd : String,
    @SerializedName("age_limit") var ageLimit : Int,
    @SerializedName("type") var type : Boolean,
    @SerializedName("rating") var rating : Int,
    @SerializedName("min_score") var minScore : Int,
    @SerializedName("location") var location : String,
    @SerializedName("users_id") var usersId : Int,
    @SerializedName("count_points") var countPoints : Int
)
