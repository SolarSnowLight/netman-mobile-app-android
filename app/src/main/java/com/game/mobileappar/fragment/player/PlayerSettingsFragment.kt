package com.game.mobileappar.fragment.player

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import com.game.mobileappar.R
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import com.game.mobileappar.AuthActivity


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PlayerSettingsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view:View = inflater.inflate(R.layout.player_player_settings_fragment, container, false)

        val toolBar: androidx.appcompat.widget.Toolbar? = view.findViewById(R.id.toolbar5)
        toolBar?.setNavigationIcon(R.drawable.ic_arrow_back)
        toolBar?.setNavigationOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_playerSettingsFragment_to_playerInformationFragment, null))


        val acc:TextView = view.findViewById(R.id.tv_account)

        acc.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_playerSettingsFragment_to_profileSettingsAccountFragment, null))

        view.findViewById<TextView>(R.id.tv_exit).setOnClickListener{
            val intent = Intent(activity, AuthActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onPause() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        super.onPause()
    }
}