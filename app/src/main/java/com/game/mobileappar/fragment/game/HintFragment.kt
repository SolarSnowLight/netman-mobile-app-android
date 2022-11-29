package com.game.mobileappar.fragment.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.game.mobileappar.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HintFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Создание шаблона вида с определённым layout
        val view: View = inflater.inflate(R.layout.game_interface_fragment_hint, container, false);

        // Связывание переменных с id на layout
        val btn_lamp: View = view.findViewById(R.id.v_lamp)
        val tv_hint: View = view.findViewById(R.id.tv_gameHint)

        // При нажатии на лампу текст подсказки либо скрывается, либо показывается
        tv_hint.visibility = View.GONE; // Изначально равен View.GONE
        btn_lamp.setOnClickListener {
            if(tv_hint.visibility == View.VISIBLE){
                tv_hint.visibility = View.GONE;
            } else{tv_hint.visibility = View.VISIBLE}
        }

        return view;
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HintFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}