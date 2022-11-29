package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.messenger

import android.content.Context
import android.os.Build

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

import com.game.mobileappar.fragment.holders.AppHolderFactory
import com.game.mobileappar.fragment.holders.HolderGroupChatTextMessage
import com.game.mobileappar.fragment.holders.HolderPrivateChatTextMessage

import com.game.mobileappar.R
import com.game.mobileappar.models.messenger.message.MessengerRoomElementModel
import com.game.mobileappar.models.messenger.message.MessengerRoomModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class MessageAdapter(private val context: Context, private var viewType: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var _dataMessages: MessengerRoomModel? = null
    private var _usersId: Int? = null

    public fun setMessages(messages: MessengerRoomModel?){
        _dataMessages = messages
    }

    public fun setUsersId(usersId: Int){
        _usersId = usersId
    }

    public fun setMessage(message: MessengerRoomElementModel?){
        _dataMessages?.messages?.add(message!!)
    }

    override fun getItemViewType(position: Int): Int {
        // определение типа сообщения (для каждого элемента списка по position)
        return viewType;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        return AppHolderFactory.getHolder(parent, viewType)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if((_dataMessages != null) && (_dataMessages!!.messages.isNotEmpty())){
            val messageData = _dataMessages!!.messages[position];

            // Отрисовка сообщения определённого вида
            when (holder){
                is HolderPrivateChatTextMessage -> drawPrivateTextMessage(holder, messageData)
                is HolderGroupChatTextMessage -> drawGroupTextMessage(holder, messageData)
                else->{}
            }
        }
    }

    /**
     * Функция для заполнения данными сообщений группового чата
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawGroupTextMessage(holder: HolderGroupChatTextMessage, message: MessengerRoomElementModel){
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val times = LocalTime.parse(message.dateSend, dateFormatter)
                .toString().split(".")[0].split(":")
        val currentTime = times[0] + ":" + times[1]

        if((message.senderUsersId!!) == _usersId){
            holder.blockReceiver.visibility = View.GONE
            holder.blockSender.visibility = View.VISIBLE
            holder.senderMessage.text = message.message
            holder.senderTimeStamp.text = currentTime
        }else{
            holder.blockReceiver.visibility = View.VISIBLE
            holder.blockSender.visibility = View.GONE
            holder.receiverText.text = message.message
            holder.senderTimeStamp.text = currentTime
            holder.itemView.findViewById<TextView>(R.id.tv_nickname_chat).text = message.nicknameSender
        }
    }

    /**
     * Функция для заполнения данными сообщений приватного чата
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawPrivateTextMessage(holder: HolderPrivateChatTextMessage, message: MessengerRoomElementModel){
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val times = LocalTime.parse(message.dateSend, dateFormatter)
                .toString().split(".")[0].split(":")
        val currentTime = times[0] + ":" + times[1]

        if((message.senderUsersId!!) != _usersId) {
            holder.blockReceiverPrivate.visibility = View.VISIBLE
            holder.blockSenderPrivate.visibility = View.GONE
            holder.receiverMessagePrivate.text = message.message
            holder.itemView.findViewById<TextView>(R.id.tv_privateChatTimestampReceiver).text = currentTime
        }else{
            holder.blockReceiverPrivate.visibility = View.GONE
            holder.blockSenderPrivate.visibility = View.VISIBLE
            holder.senderMessagePrivate.text = message.message
            holder.senderTimestamp.text = currentTime
        }
    }

    override fun getItemCount(): Int {
        return if(_dataMessages == null) 0 else _dataMessages!!.messages.size
    }
}

