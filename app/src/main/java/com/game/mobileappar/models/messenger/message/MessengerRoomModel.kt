package com.game.mobileappar.models.messenger.message

import com.google.gson.annotations.SerializedName

data class MessengerRoomModel(
        @SerializedName("messages") var messages : MutableList<MessengerRoomElementModel>
)
