package com.game.mobileappar.models.messenger.message.send

import com.google.gson.annotations.SerializedName

data class MessengerMessageSendModel(
        @SerializedName("receiver_users_id") var receiverUsersId: Int?,
        @SerializedName("groups_id") var groupsId: Int?,
        @SerializedName("message") var message: String
)
