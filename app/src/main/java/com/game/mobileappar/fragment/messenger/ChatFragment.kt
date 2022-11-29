package com.game.mobileappar.fragment.messenger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.messenger.MessageAdapter
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.network.handler.SMSocketHandler
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.messenger.MessengerChatsElementModel
import com.game.mobileappar.models.messenger.message.MessengerRoomElementModel
import com.game.mobileappar.models.messenger.message.send.MessengerMessageSendModel
import com.game.mobileappar.models.messenger.status.MessengerStatusUserModel
import com.game.mobileappar.models.messenger.status.MessengerStatusModel
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.google.android.material.bottomsheet.BottomSheetBehavior

import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback

import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import android.widget.Button
import androidx.navigation.fragment.NavHostFragment
import com.game.mobileappar.fragment.EmptyFragment
import com.game.mobileappar.models.messenger.message.MessengerRoomModel
import com.game.mobileappar.models.messenger.room.MessengerRoomInfoModel
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import eightbitlab.com.blurview.RenderScriptBlur

import com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.BottomSheetVideoAdapter
import eightbitlab.com.blurview.BlurView


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _chatInfo: MessengerChatsElementModel? = null
    private var _socket: Socket? = null
    private var _adapter: MessageAdapter? = null
    private var _statusView: TextView? = null

    //private val blurLayout: BlurLayout? =view?.findViewById(R.id.bottom_sheet)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var gson = Gson()
        var imageUri = ""
        if(requireArguments().containsKey("chat")){
            _chatInfo = gson.fromJson(requireArguments().getString("chat"), MessengerChatsElementModel::class.java)
        }

        if(requireArguments().containsKey("image_uri")){
            imageUri = requireArguments().getString("image_uri")!!
        }

        activity?.window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)

        val shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
        val userData = gson.fromJson(shared?.getString(ConfigStorage.USERS_DATA, null), UserDataModel::class.java)

        val view: View? = inflater.inflate(R.layout.messenger_fragment_chats, container, false)
        if(imageUri.isNotEmpty()){
            view?.findViewById<ImageView>(R.id.imageView)
                ?.setImageURI(Uri.parse(imageUri))
        }

        val textMess: EditText? = view?.findViewById<EditText>(R.id.pt_inputTextMess)
        val chatName: TextView? = view?.findViewById(R.id.tv_1)
        var viewType = if(_chatInfo?.groupsId == null) 2 else 1;

        val privateInfo:View? = view?.findViewById(R.id.chatInfo);
        val privateInfoFunctions: ConstraintLayout? = view?.findViewById(R.id.clViewPrivateFunctionsHolder)
        val groupInfo: View? = view?.findViewById(R.id.view_info_chats);
        val infoHolderGroup: ConstraintLayout? = view?.findViewById(R.id.cl_viewInfoHolder);

        var sendTextMessage: View? = view?.findViewById(R.id.vSendTextMess);
        val recyclerView: RecyclerView? = view?.findViewById(R.id.rv_listOfChat);
        recyclerView?.layoutManager = LinearLayoutManager(activity);

        _adapter = view?.context?.let { MessageAdapter(it, viewType) };
        _statusView = view?.findViewById(R.id.tv_2)

        chatName?.text = _chatInfo?.nameChat
        _statusView?.text = ""


        //--------------------

        /* получение вью нижнего экрана
        Ссылка : https://habr.com/ru/post/567828/
        Режимы для нижнего меню:
        BottomSheetBehavior.STATE_COLLAPSED -> "STATE_COLLAPSED"
        BottomSheetBehavior.STATE_DRAGGING -> "STATE_DRAGGING"
        BottomSheetBehavior.STATE_EXPANDED -> "STATE_EXPANDED"
        BottomSheetBehavior.STATE_HALF_EXPANDED -> "STATE_HALF_EXPANDED"
        BottomSheetBehavior.STATE_HIDDEN -> "STATE_HIDDEN"
        BottomSheetBehavior.STATE_SETTLING -> "STATE_SETTLING"

         */

        val llBottomSheet = view?.findViewById(R.id.bottom_sheet) as BlurView


        val decorView: View? = activity?.window?.decorView

        val rootView = decorView?.findViewById(android.R.id.content) as ViewGroup

        val windowBackground = decorView.background

        llBottomSheet.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(context))
            .setBlurRadius(0.1f)
            .setBlurAutoUpdate(true)
            .setHasFixedTransformationMatrix(false)

        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(llBottomSheet)


        /*Код полуения NavController в фрагменте
        val navHostFragment = childFragmentManager.findFragmentById(R.id.containerChatsVideo)
                as NavHostFragment
        val navController = navHostFragment.navController
         */

        val navHostFragment = childFragmentManager.findFragmentById(R.id.containerChatsVideo)
                as NavHostFragment
        val navController = navHostFragment.navController

        // событие нажатие на кнопку камеры в нижнем меню чата, некотороые элементы становятся
        // видимымм,а другие - невидимыми
        view.findViewById<Button>(R.id.btnCameraChatBottom).setOnClickListener{
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
            bottomSheetBehavior.isDraggable = false
            //view.findViewById<ConstraintLayout>(R.id.clMessage).visibility = View.INVISIBLE
           // view.findViewById<ConstraintLayout>(R.id.clBottomSheetContent).visibility = View.GONE
           // view.findViewById<TextView>(R.id.tvCancellation).visibility = View.GONE
            sendTextMessage?.visibility = View.INVISIBLE
            textMess?.visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.tvCancellationVideoMess).visibility = View.VISIBLE
            navController.navigate(R.id.action_emptyFragment3_to_videoRecordFragment)
        }





        bottomSheetBehavior.peekHeight = 210
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
                else{
                    activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })

        val rvBottomSheetVideo: RecyclerView = view.findViewById(R.id.rvBottomSheet)


        val layoutManager: LinearLayoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.HORIZONTAL, false)
        rvBottomSheetVideo.layoutManager = layoutManager
        rvBottomSheetVideo.setHasFixedSize(true)

        val adapter =
            view.context?.let { activity?.let{ it1 -> BottomSheetVideoAdapter(it, it1) } }
        rvBottomSheetVideo.adapter = adapter
        rvBottomSheetVideo.itemAnimator = DefaultItemAnimator()
        rvBottomSheetVideo.isNestedScrollingEnabled = false


        textMess?.setOnClickListener {
            if(bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED){
                val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        //-----------------------




        _socket = if((SMSocketHandler.getSocket() == null)
                || (!(SMSocketHandler.getSocket()?.connected()!!))){
            SMSocketHandler.setSocket()              // Добавление подключения
            SMSocketHandler.establishConnection()    // Подключение к серверу
            SMSocketHandler.getSocket()    // Получение информации об сокете для устройства
        }else{
            SMSocketHandler.getSocket()
        }

        if(_chatInfo!!.usersId != null){
            // Отправка данных на сервер для получения текущего статуса игрока (в сети или не в сети)
            _socket?.on("set_status_user"){ args ->
                if((args != null) && (args.isNotEmpty())){
                    val data = args[0] as String
                    val value = gson.fromJson(data, MessengerStatusModel::class.java)
                    CoroutineScope(Dispatchers.Main).launch {
                        _statusView?.text = if(value.status) "Online" else "Offline"
                    }
                }
            }

            _socket?.on("room_connection_success"){
                CoroutineScope(Dispatchers.Main).launch {
                    _statusView?.text = "Online"
                }
            }

            _socket?.on("room_disconnection_success"){
                CoroutineScope(Dispatchers.Main).launch {
                    _statusView?.text = "Offline"
                }
            }

            _socket?.emit("get_status_user", gson.toJson(
                MessengerStatusUserModel(
                    usersId = _chatInfo?.usersId!!
            )
            ))
        }

        _socket?.on("set_room_messages"){ args ->
            if((args != null) && (args.isNotEmpty())){
                val data = args[0] as String

                val value = gson.fromJson(data, MessengerRoomModel::class.java)
                CoroutineScope(Dispatchers.Main).launch {
                    _adapter?.setUsersId(userData.usersId)
                    _adapter?.setMessages(value)
                    _adapter?.notifyDataSetChanged()
                    recyclerView?.scrollToPosition((recyclerView?.adapter?.itemCount!! - 1))
                }
            }
        }

        _socket?.on("new_private_message"){args ->
            if((args != null) && (args.isNotEmpty())){
                val data = args[0] as String

                val value = gson.fromJson(data, MessengerRoomElementModel::class.java)
                CoroutineScope(Dispatchers.Main).launch {
                    _adapter?.setUsersId(userData.usersId)
                    _adapter?.setMessage(value)
                    _adapter?.notifyDataSetChanged()

                    // Слежка за текущим сообщением при определённом условии
                    if(((recyclerView!!.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                            >= (recyclerView?.adapter?.itemCount!! - 3))
                            || (value.senderUsersId == userData.usersId)){
                        recyclerView?.scrollToPosition((recyclerView?.adapter?.itemCount!! - 1))
                    }
                }
            }
        }

        _socket?.on("new_group_message"){args ->
            if((args != null) && (args.isNotEmpty())){
                val data = args[0] as String

                val value = gson.fromJson(data, MessengerRoomElementModel::class.java)
                CoroutineScope(Dispatchers.Main).launch {
                    _adapter?.setUsersId(userData.usersId)
                    _adapter?.setMessage(value)
                    _adapter?.notifyDataSetChanged()

                    // Слежка за текущим сообщением при определённом условии
                    if(((recyclerView!!.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                                    >= (recyclerView?.adapter?.itemCount!! - 3))
                            || (value.senderUsersId == userData.usersId)){
                        recyclerView?.scrollToPosition((recyclerView?.adapter?.itemCount!! - 1))
                    }
                }
            }
        }

        _socket?.emit("room_connection", gson.toJson(
            MessengerRoomInfoModel(
                groupsId = _chatInfo!!.groupsId,
                roomsId = _chatInfo!!.roomsId
            )
        ))

        _socket?.on("create_new_chat"){
            CoroutineScope(Dispatchers.Main).launch {
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                findNavController().navigate(R.id.action_chatListFragment_to_messengerFragment2, requireArguments().deepCopy())
            }
        }


        val tb =view?.findViewById<Toolbar>(R.id.chatToolbar)
        tb?.apply {
            setNavigationOnClickListener{
                _socket?.emit("room_disconnection")
                //activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
                when(currentFragment){
                    is EmptyFragment ->{
                        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                        findNavController().navigate(R.id.action_chatListFragment_to_messengerFragment2, requireArguments().deepCopy())
                    }
                    is VideoRecordFragment ->{
                        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
                        }
                        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                        bottomSheetBehavior.isDraggable = true
                        sendTextMessage?.visibility = View.VISIBLE
                        textMess?.visibility = View.VISIBLE
                        //view.findViewById<ConstraintLayout>(R.id.clMessage).visibility = View.VISIBLE
                        //view.findViewById<ConstraintLayout>(R.id.clBottomSheetContent).visibility = View.VISIBLE
                        view.findViewById<TextView>(R.id.tvCancellationVideoMess).visibility = View.GONE
                        navController.navigate(R.id.action_videoRecordFragment_to_emptyFragment3, null)
                    }
                }

            }
        }

        recyclerView?.setOnTouchListener(View.OnTouchListener { v, event ->
            val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager;
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            false
        })

        recyclerView?.addOnLayoutChangeListener(View.OnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                recyclerView.post(Runnable {
                    recyclerView.scrollToPosition((recyclerView.adapter?.itemCount!! - 1))
                })
            }
        })

        when (viewType){
            1->{
                privateInfo?.visibility = View.GONE;
                privateInfoFunctions?.visibility = View.GONE
                infoHolderGroup?.visibility = View.VISIBLE;
                groupInfo?.setOnClickListener {
                    findNavController().navigate(R.id.action_chatListFragment_to_chatInfoFragment2, requireArguments().deepCopy());
                }
            }
            2->{
                privateInfo?.visibility = View.VISIBLE;
                privateInfoFunctions?.visibility = View.VISIBLE
                infoHolderGroup?.visibility = View.GONE;
                privateInfo?.findViewById<ImageView>(R.id.imageView)?.setOnClickListener(View.OnClickListener {
                    var bundle = Bundle()
                    bundle.putString("type", "ChatInfo")
                    bundle.putInt("users_id", _chatInfo?.usersId!!)
                    bundle.putString("access_token", userData.accessToken)
                    bundle.putString("chat", requireArguments().getString("chat"))
                    bundle.putString("image_uri", imageUri)
                    //findNavController().navigate(R.id.action_chatsFragment_to_playerProfileInTeamFragment, bundle)
                })
            }
        }

        recyclerView?.adapter = _adapter;
        recyclerView?.itemAnimator = DefaultItemAnimator();

        sendTextMessage?.setOnClickListener {
            val text = textMess?.text.toString()
            if(text.isEmpty()){
                return@setOnClickListener
            }

            if(viewType == 2){
                _socket?.emit("send_private_message",
                        gson.toJson(
                            MessengerMessageSendModel(
                                receiverUsersId = _chatInfo?.usersId,
                                groupsId = null,
                                message = text
                        )
                        )
                );
            }else{
                _socket?.emit("send_group_message",
                        gson.toJson(
                            MessengerMessageSendModel(
                                receiverUsersId = null,
                                groupsId = _chatInfo?.groupsId,
                                message = text
                        )
                        )
                );
            }

            view?.findViewById<EditText>(R.id.pt_inputTextMess)?.setText("")
        }

        return view;
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }



    override fun onResume() {
        activity?.window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)
        super.onResume()
    }


    override fun onPause() {
       
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        super.onPause()
    }
}