package com.game.mobileappar.fragment.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R

class HolderGroupChatTextMessage(view: View): RecyclerView.ViewHolder(view) {
    val blockSender: ConstraintLayout = view.findViewById(R.id.block_sender_layout_groupchat)
    val senderMessage: TextView = view.findViewById(R.id.tv_senderMessageGroupChat)
    val senderTimeStamp:TextView = view.findViewById(R.id.tv_senderTimeStamp)

    val blockReceiver: ConstraintLayout = view.findViewById(R.id.block_receiver_chatGroupMess)
    val avatar:ImageView = view.findViewById(R.id.message_avatar)
    val receiverNickname:TextView = view.findViewById(R.id.tv_nickname_chat)
    val receiverText:TextView = view.findViewById(R.id.tv_groupChatMessageOtherUsers)
    val timestampReceiver: TextView = view.findViewById(R.id.tv_receiverTimeStamp)
}