package com.game.mobileappar.helpers

import android.content.Context

object CoreHelper {
    var contextGetter: (() -> Context)? = null
}