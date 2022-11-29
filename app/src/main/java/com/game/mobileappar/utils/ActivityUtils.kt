package com.game.mobileappar.utils

import android.app.Activity
import android.content.Intent

// Старт новой активности с определёнными флагами
fun<A : Activity> Activity.startNewActivity(activity: Class<A>){
    Intent(this, activity).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}