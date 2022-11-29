package com.game.mobileappar.fragment.holders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R


class AppHolderFactory {
    companion object{
        fun getHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder{
            return when(viewType){
                2 ->{
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.messenger_private_chat_messages_item,parent,false)
                    HolderPrivateChatTextMessage(view)}
                1->{

                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.messenger_group_chat_layout_items,parent,false)
                    HolderGroupChatTextMessage(view)

                }else ->{
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.messenger_group_chat_layout_items,parent,false)
                    HolderGroupChatTextMessage(view)
                }
            }
        }
    }
}