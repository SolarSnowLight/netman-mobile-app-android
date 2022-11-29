package com.game.mobileappar.components.scrollview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import com.game.mobileappar.utils.hideKeyboard

/*
* Компонент ScrollView, который скрывает
* клавиатуру если все элементы, которые
* могут иметь фокус потеряли его (нажатие в пустое
* пространство)
* */

class HideKeyboardScrollView : ScrollView {
    private var _isScrolled: Boolean = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        _isScrolled = true
        super.onScrollChanged(l, t, oldl, oldt)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event != null){
            if((!_isScrolled) && (event.action == MotionEvent.ACTION_UP)){
                val currentFocus = super.findFocus()
                val currentView: View? = currentFocus

                if ((currentView != null) && (currentView.isFocusable)) {
                    currentView.clearFocus()
                    currentView.hideKeyboard()
                }
            }

            if(event.action == MotionEvent.ACTION_UP){
                _isScrolled = false
            }
        }

        performClick()
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}