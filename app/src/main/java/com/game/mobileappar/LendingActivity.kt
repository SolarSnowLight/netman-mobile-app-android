package com.game.mobileappar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import com.game.mobileappar.helpers.CoreHelper
import com.game.mobileappar.utils.startNewActivity

class LendingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lending)

        // FOR TEST
        // startNewActivity(com.game.mobileappar.containers.auth.AuthActivity::class.java)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        findViewById<Button>(R.id.regBtnLending).setOnClickListener{
            val intent = Intent(this, RegActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.to_left_in, R.anim.to_left_out)
        }

        findViewById<Button>(R.id.enterBtnLending).setOnClickListener{
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.to_left_in, R.anim.to_left_out)

        }
    }
}