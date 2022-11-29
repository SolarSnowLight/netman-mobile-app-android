package com.game.mobileappar.models.command.list

import com.google.gson.annotations.SerializedName

data class CommandListModel(
    @SerializedName("commands") var commands : List<CommandListElementModel>
)
