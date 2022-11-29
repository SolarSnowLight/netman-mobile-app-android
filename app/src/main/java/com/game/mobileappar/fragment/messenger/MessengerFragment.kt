package com.game.mobileappar.fragment.messenger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.messenger.ChatAdapter
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.network.handler.SMSocketHandler
import com.game.mobileappar.models.messenger.MessengerChatsModel
import com.game.mobileappar.models.messenger.message.MessengerRoomElementModel
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.view.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MessengerFragment : Fragment() {
    private var _socket: Socket?        = null
    private var _dataChats: MessengerChatsModel?  = null
    private var _toolBar: Toolbar?      = null
    private var _recyclerView: RecyclerView?    = null
    private var _adapter: ChatAdapter?          = null


    override fun onDestroy() {
        super.onDestroy()
        _socket?.disconnect()
        SMSocketHandler.closeConnection()
        _socket = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Клавиатура не подстравивается под элементы
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val view = inflater.inflate(R.layout.messenger_fragment, container, false)
        _toolBar = view?.findViewById(R.id.toolbar_messenger)
        _toolBar?.title = ""


        _recyclerView = view?.findViewById(R.id.rv_chatList)
        _recyclerView?.layoutManager = LinearLayoutManager(activity)

        _adapter = view?.context?.let { ChatAdapter(it) }
        _recyclerView?.adapter = _adapter
        _recyclerView?.itemAnimator = DefaultItemAnimator()
        _toolBar?.setNavigationIcon(R.drawable.ic_arrow_back)


        _recyclerView?.setOnTouchListener(View.OnTouchListener { v, event ->
            val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager;
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            false
        })


        view.findViewById<View>(R.id.vPlusAddChat).setOnClickListener {
            findNavController().navigate(R.id.action_messengerFragment2_to_addChatFragment2)
        }




        _toolBar?.setNavigationOnClickListener{
            val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
            (activity as MainActivity).onBackPressed()
        }

        val searchView = view?.findViewById(R.id.svMessengerFragment) as androidx.appcompat.widget.SearchView

        searchView.isIconified = false
        searchView.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

        val gson = Gson()

        if((SMSocketHandler.getSocket() == null)
                || (!(SMSocketHandler.getSocket()?.connected()!!))){
            SMSocketHandler.setSocket()              // Добавление подключения
            SMSocketHandler.establishConnection()    // Подключение к серверу
            _socket = SMSocketHandler.getSocket()    // Получение информации об сокете для устройства

            val shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)

            // Отправка данных на сервер для аутентификации
            _socket?.emit("authentication", shared?.getString(ConfigStorage.USERS_DATA, null))

            _socket?.on("authentication_success"){
                // Получить новые чаты со всеми сообщениями
                _socket?.emit("get_new_messages")

                _socket?.on("set_new_messages"){ args ->
                    if((args != null)
                            && (args.isNotEmpty())
                            && (args[0] != null)){
                        val data = args[0] as String

                        CoroutineScope(Dispatchers.Main).launch {
                            _dataChats = gson.fromJson(data, MessengerChatsModel::class.java)
                            _adapter?.setChats(_dataChats)
                            _adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }else{
            _socket = SMSocketHandler.getSocket()
            _socket?.off("set_new_messages")
            _socket?.emit("get_new_messages")
            _socket?.on("set_new_messages"){ args ->
                if((args != null)
                        && (args.isNotEmpty())
                        && (args[0] != null)){
                    val data = args[0] as String

                    CoroutineScope(Dispatchers.Main).launch {
                        _dataChats = gson.fromJson(data, MessengerChatsModel::class.java)
                        _adapter?.setChats(_dataChats)
                        _adapter?.notifyDataSetChanged()
                    }
                }
            }
        }

        _socket?.on("new_private_message_chat"){args ->
            if((args != null) && (args.isNotEmpty())){
                val data = args[0] as String

                val value = gson.fromJson(data, MessengerRoomElementModel::class.java)
                CoroutineScope(Dispatchers.Main).launch {
                    _adapter?.setNewMessage(value)
                }
            }
        }

        _socket?.on("new_group_message_chat"){args ->
            if((args != null) && (args.isNotEmpty())){
                val data = args[0] as String

                val value = gson.fromJson(data, MessengerRoomElementModel::class.java)
                CoroutineScope(Dispatchers.Main).launch {
                    _adapter?.setNewMessage(value)
                }
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            /**
            Срабатывает, когда пользователь нажимает кнопку отправки
             */
            override fun onQueryTextSubmit(query: String): Boolean {
               // searchView.setIconified(false);
                return false
            }

            /**
            Срабатывает при изменении текста
             */
            override fun onQueryTextChange(newText: String): Boolean {
                searchView.isIconified = false

                // this will trigger each time user changes (adds or removes) text
                // so when newText is empty, restore your views
                return false
            }
        })

        searchView.setOnCloseListener {
            searchView.isIconified = false;
            true
        }

        return view;
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MessengerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }




    override fun onPause() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager;
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        super.onPause()
    }
}