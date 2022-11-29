package com.game.mobileappar.models.messenger

import com.google.gson.annotations.SerializedName

data class MessengerChatsElementModel(
        @SerializedName("groups_id") var groupsId : Int?,
        @SerializedName("users_id") var usersId : Int?,
        @SerializedName("count_messages") var countMessages : Int?,
        @SerializedName("last_message") var lastMessage : String?,
        @SerializedName("name_chat") var nameChat : String?,
        @SerializedName("nickname_sender") var nicknameSender : String?,
        @SerializedName("ref_image") var refImage : String?,
        @SerializedName("date_send") var dateSend : String?,
        @SerializedName("rooms_id") var roomsId : String?
)
