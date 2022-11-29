package com.game.mobileappar.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar

// Установка видимости элемента
fun View.visible(isVisible: Boolean){
    visibility = if(isVisible) View.VISIBLE else View.GONE
}

// Установка активности элемента
fun View.enable(enabled: Boolean){
    isEnabled = enabled
    alpha = if(enabled) 1f else 0.5f
}

// Вызов снэкбара для каждого элемента являющегося дочерним классу View
fun View.snackbar(message: String, action: (() -> Unit)? = null){
    val snack = Snackbar.make(this, message, Snackbar.LENGTH_LONG)

    action?.let {
        snack.setAction("Retry"){
            it()
        }
    }

    snack.show()
}

// Скрытие клавиатуры
fun View.hideKeyboard(){
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}