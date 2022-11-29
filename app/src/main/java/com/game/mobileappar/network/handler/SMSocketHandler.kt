package com.game.mobileappar.network.handler

import com.game.mobileappar.config.ConfigAddresses
import io.socket.client.IO
import io.socket.client.Socket

object SMSocketHandler {
    private var mSocket: Socket? = null

    @Synchronized
    fun setSocket(){
        try{
            mSocket = IO.socket(ConfigAddresses.SERVER_MESSENGER_ADDRESS)
        }catch(e: Exception){}
    }

    @Synchronized
    fun getSocket(): Socket? {
        return mSocket
    }

    @Synchronized
    fun establishConnection(){
        mSocket?.connect()
    }

    @Synchronized
    fun closeConnection(){
        mSocket?.disconnect()
    }
}