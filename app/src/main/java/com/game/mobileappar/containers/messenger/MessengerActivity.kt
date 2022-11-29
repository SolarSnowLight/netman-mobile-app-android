package com.game.mobileappar.containers.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.game.mobileappar.R

class MessengerActivity : AppCompatActivity() {
    private var _commonContainer: NavHostFragment? = null
    private var _commonController: NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger)


        _commonContainer =
            supportFragmentManager.findFragmentById(R.id.hostMessenger) as NavHostFragment?
                ?: return

        // Инициализация контроллера (мессенджера)
        _commonController = _commonContainer!!.navController
    }
}