package com.game.mobileappar.models.messenger.status

import com.google.gson.annotations.SerializedName

data class MessengerStatusModel(
        @SerializedName("status") var status: Boolean
)
