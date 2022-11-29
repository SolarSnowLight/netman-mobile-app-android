package com.game.mobileappar.models.messenger.room

import com.google.gson.annotations.SerializedName

data class MessengerRoomInfoModel(
        @SerializedName("groups_id") var groupsId: Int?,
        @SerializedName("rooms_id") var roomsId: String?
)
