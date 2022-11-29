/*
* Стилизированное пользовательское уведомление
* */

package com.game.mobileappar.components.toast

import android.content.Context
import android.widget.Toast
import android.widget.TextView
import android.view.Gravity
import com.game.mobileappar.R
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View

class CustomToast(context: Context) : Toast(context) {
    companion object {
        private lateinit var toastText: TextView

        fun makeText(context: Context, text: CharSequence?): CustomToast {
            val result = CustomToast(context)
            toastText.text = text
            return result
        }

        fun makeText(context: Context, text: CharSequence?, duration: Int): CustomToast {
            val result = CustomToast(context)
            result.duration = duration
            toastText.text = text
            return result
        }

        @Throws(Resources.NotFoundException::class)
        fun makeText(context: Context, resId: Int): Toast {
            return makeText(context, context.resources.getText(resId))
        }

        @Throws(Resources.NotFoundException::class)
        fun makeText(context: Context, resId: Int, duration: Int): Toast {
            return makeText(context, context.resources.getText(resId), duration)
        }
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView: View = inflater.inflate(R.layout.toast_game_layout, null)
        toastText = rootView.findViewById(R.id.toast_game_text)

        view = rootView
        setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 300)
        this.duration = LENGTH_LONG
    }
}