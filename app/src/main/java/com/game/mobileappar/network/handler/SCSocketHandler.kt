package com.game.mobileappar.network.handler
import com.game.mobileappar.config.ConfigAddresses
import io.socket.client.IO
import io.socket.client.Socket

object SCSocketHandler {
    private var mSocket: Socket? = null

    @Synchronized
    fun setSocket(){
        try{
            mSocket = IO.socket(ConfigAddresses.SERVER_CENTRAL_ADDRESS)
        }catch(e: Exception){}
    }

    @Synchronized
    fun getSocket(): Socket?{
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