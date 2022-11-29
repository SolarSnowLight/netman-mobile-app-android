package com.game.mobileappar.fragment.holders

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R

class HolderPrivateChatTextMessage(view: View):RecyclerView.ViewHolder(view) {
    val blockReceiverPrivate:ConstraintLayout = view.findViewById(R.id.block_privateChatReceiver)
    val receiverMessagePrivate: TextView =view.findViewById(R.id.tv_receiverMessPrivateChat)
    val receiverTimestamp: TextView = view.findViewById(R.id.tv_privateChatTimestampReceiver)

    val blockSenderPrivate:ConstraintLayout = view.findViewById(R.id.block_privateChatSender)
    val senderMessagePrivate: TextView = view.findViewById(R.id.tv_privateChatSender)
    val senderTimestamp: TextView = view.findViewById(R.id.tv_privateChatTimestampSender)
}