package com.game.mobileappar.models.messenger.message

import com.google.gson.annotations.SerializedName

data class MessengerRoomElementModel(
        @SerializedName("id") var id : Int?,
        @SerializedName("sender_users_id") var senderUsersId : Int?,
        @SerializedName("receiver_users_id") var receiverUsersId : Int?,
        @SerializedName("message") var message : String?,
        @SerializedName("link_media") var linkMedia : String?,
        @SerializedName("type") var type : String?,
        @SerializedName("view") var view : Boolean?,
        @SerializedName("date_send") var dateSend : String?,
        @SerializedName("rooms_id") var roomsId : String?,
        @SerializedName("is_sender") var isSender : Boolean?,
        @SerializedName("groups_id") var groupsId: Int?,
        @SerializedName("nickname_sender") var nicknameSender: String?
)
