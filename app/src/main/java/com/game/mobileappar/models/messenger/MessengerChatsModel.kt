package com.game.mobileappar.models.messenger

import com.google.gson.annotations.SerializedName

data class MessengerChatsModel(
        @SerializedName("chats") var chats : MutableList<MessengerChatsElementModel>
)
