package com.game.mobileappar.fragment.messenger

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.game.mobileappar.R
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.messenger.ChatInfoVPA
import com.game.mobileappar.network.handler.SMSocketHandler
import com.game.mobileappar.models.messenger.MessengerChatsElementModel
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import io.socket.client.Socket

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatInfoFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _chatInfo: MessengerChatsElementModel? = null
    private var _socket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(requireArguments().containsKey("chat")){
            var gson = Gson()
            _chatInfo = gson.fromJson(requireArguments().getString("chat"), MessengerChatsElementModel::class.java)
        }

        _socket = if((SMSocketHandler.getSocket() == null)
                || (!(SMSocketHandler.getSocket()?.connected()!!))){
            SMSocketHandler.setSocket()              // Добавление подключения
            SMSocketHandler.establishConnection()    // Подключение к серверу
            SMSocketHandler.getSocket()    // Получение информации об сокете для устройства
        }else{
            SMSocketHandler.getSocket()
        }

        /*if(_chatInfo!!.groupsId != null){
            _socket?.emit("get_groups_info")

            _socket?.on("set_groups_info"){ args ->
                if((args != null) && (args.isNotEmpty()) && (args[0] != null)){

                }
            }
        }*/

        val view:View = inflater.inflate(R.layout.messenger_chat_info_fragment, container, false);

        val viewPager: ViewPager = view.findViewById(R.id.viewPager_chatInfo)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout_chatInfo)


        var vpaGamesAndPlayers: ChatInfoVPA? = null

        var isCreator = true;

        vpaGamesAndPlayers = if (isCreator) {
            ChatInfoVPA(childFragmentManager,"ChatIsCreator")
        }
        else{
            ChatInfoVPA(childFragmentManager,"ChatNoCreator")
        }

        ChatInfoVPA(childFragmentManager,"ChatNoCreator")
        viewPager.adapter = vpaGamesAndPlayers
        tabLayout.setupWithViewPager(viewPager)

        view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_chatInfo).setNavigationOnClickListener{
            findNavController().navigate(R.id.action_chatInfoFragment2_to_chatListFragment, requireArguments().deepCopy());
        }


        val viewPlus = view.findViewById<View>(R.id.vPlusChatInfo)



        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab?.text == "Участники"){
                    viewPlus.visibility = View.VISIBLE
                }else{
                    viewPlus.visibility = View.GONE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        return view;
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}