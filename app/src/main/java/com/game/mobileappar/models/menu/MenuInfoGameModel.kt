package com.game.mobileappar.models.menu

import com.google.gson.annotations.SerializedName

data class MenuInfoGameModel(
    @SerializedName("age") var age: Int,
    @SerializedName("name") var name: String
)
