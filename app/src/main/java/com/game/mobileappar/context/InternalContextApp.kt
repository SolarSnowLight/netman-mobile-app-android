package com.game.mobileappar.context
import android.app.Application
import com.game.mobileappar.helpers.CoreHelper

class InternalContextApp: Application() {
    override fun onCreate() {
        super.onCreate()

        // Установка глобального контекста
        CoreHelper.contextGetter = {
            this
        }
    }
}