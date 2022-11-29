package com.game.mobileappar.fragment.team

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.network.handler.SMSocketHandler
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.game.mobileappar.models.UsersIdModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.messenger.MessengerChatsElementModel
import com.game.mobileappar.models.player.PlayerInfoModel
import com.game.mobileappar.models.storage.StorageProfileImageModel
import com.game.mobileappar.network.service.APIService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.game.mobileappar.utils.storage.ProfileImageUtil
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PlayerProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private val _GALLERY_REQUEST_CODE = 1234
    private val TAG = "AppDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val type            = requireArguments().getString("type")
        val usersId         = requireArguments().getInt("users_id")
        val accessToken     = requireArguments().getString("access_token")
        val statusPlayer    = requireArguments().getByte("status_player")
        val imageUri        = requireArguments().getString("image_uri")

        val view: View = inflater.inflate(R.layout.team_player_profile_in_team_fragment,
            container, false)
        val btnExcludeFromChat = view.findViewById<Button>(R.id.btn_excludeFromChat)
        val btnCreateChat = view.findViewById<Button>(R.id.button3)
        val toolbar:Toolbar = view.findViewById(R.id.toolBar_profile_inTeam)
        var gson = Gson()

        when(type){
            "ExistingTeamFragment" -> {
                var sharedTeamImagesData: SharedPreferences? =
                    context?.getSharedPreferences(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS, Context.MODE_PRIVATE)

                if(sharedTeamImagesData!!.contains(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS_DATA)){
                    var data = gson.fromJson(
                        sharedTeamImagesData.getString(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS_DATA, null),
                        StorageProfileImageModel::class.java)
                    var index = ProfileImageUtil.indexOfProfileImagesByUsersId(data, usersId!!)

                    if(index >= 0){
                        view.findViewById<CircleImageView>(R.id.imageView9)
                            .setImageURI(Uri.parse(data.listProfileImage[index].uri))
                    }
                }
            }
        }

        toolbar.setNavigationOnClickListener {
            var bundle = Bundle()
            when(type){
                "ChatPrivate" -> {
                    bundle.putString("chat", requireArguments().getString("chat"))
                    bundle.putString("image_uri", imageUri)
                    //findNavController().navigate(R.id.action_playerProfileInTeamFragment_to_addChatFragment, bundle)
                }

                "ChatInfo" -> {
                    bundle.putString("chat", requireArguments().getString("chat"))
                    bundle.putString("image_uri", imageUri)
                    //findNavController().navigate(R.id.action_playerProfileInTeamFragment_to_chatsFragment, bundle)
                }

                "ExistingTeamFragment" -> {
                    bundle.putByte("status_player", statusPlayer)
                    findNavController().navigate(R.id.action_playerProfileInTeamFragment_to_playerInExistingTeamFragment, bundle)
                }

                else -> {
                    (activity as MainActivity).onBackPressed()
                }
            }
        }

        val retrofit = AppMainHandler.getRetrofit()

        val service = retrofit.create(APIService::class.java)

        var dataAccessPlayer = gson.toJson(
            PlayerAccessModel(
                usersId = usersId!!,
                accessToken = accessToken!!
            )
        )

        val requestBody = dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            var responseBody = service.funPlayerInfo(requestBody)
            withContext(Dispatchers.Main) {
                if (responseBody.isSuccessful) {
                    val result = gson.toJson(
                        JsonParser.parseString(responseBody.body()?.string())
                    )

                    val error = gson.fromJson(result, ErrorDataModel::class.java)

                    // Проверка валидности данных
                    if((error.errors == null) && (error.message == null)
                    ){
                        println(result)
                        var data = gson.fromJson(result, PlayerInfoModel::class.java)
                        CoroutineScope(Dispatchers.Main).launch {
                            var dateNow = LocalDateTime.now()
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000'Z'")
                            val dateBirthday = LocalDateTime.parse(data?.dateBirthday, dateFormatter)

                            view.findViewById<TextView>(R.id.tv_NameNick).text = data.nickname
                            view.findViewById<TextView>(R.id.textView4).text = data.dataPlayers.rating.toString()
                            view.findViewById<TextView>(R.id.tv_playerFIO_profile).text = "Возраст: " + Period.between(dateBirthday.toLocalDate(),
                                dateNow.toLocalDate()).years.toString()
                            view.findViewById<TextView>(R.id.tv_city_profile_inTeam).text = data.location
                        }
                    }
                }
            }
        }

        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.GONE

        when(type){
            "ExistingTeamFragment" -> { // аргумент из фрагмента команды, обычный игрок, не создатель
                view.findViewById<Button>(R.id.button3).visibility = View.GONE
            }

            "TeamCreatorFragment" ->{ // аргумент из фрагмента создателя чата
                val btnExcludeFromTeam = view.findViewById<Button>(R.id.btn_excludeFromTeam)
                btnExcludeFromTeam.visibility = View.VISIBLE
            }

            "ChatNoCreator" -> {
                btnExcludeFromChat.visibility = View.GONE
            }

            "ChatIsCreator" -> {
                btnExcludeFromChat.visibility = View.VISIBLE
            }

            "ChatInfo" -> {
                if((imageUri != null) && (imageUri.isNotEmpty())){
                    view.findViewById<CircleImageView>(R.id.imageView9)
                        .setImageURI(Uri.parse(imageUri))
                }

                btnExcludeFromChat.visibility   = View.GONE
                btnCreateChat.visibility        = View.GONE
            }

            "ChatPrivate" -> {
                if((imageUri != null) && (imageUri.isNotEmpty())){
                    view.findViewById<CircleImageView>(R.id.imageView9)
                        .setImageURI(Uri.parse(imageUri))
                }

                btnExcludeFromChat.visibility   = View.GONE
                btnCreateChat.visibility        = View.VISIBLE

                btnCreateChat.setOnClickListener {
                    var gson = Gson()
                    var bundle = Bundle()
                    /*bundle.putString("chat",
                        gson.toJson(ChatsElementJSON(
                            email = email,
                            groupsId = null,
                            countMessages = null,
                            lastMessage = null,
                            nameChat = "New chat",
                            nicknameSender = null,
                            refImage = null,
                            dateSend = null,
                            roomsId = null
                        ))
                    )*/

                    var socket = if((SMSocketHandler.getSocket() == null)
                        || (!(SMSocketHandler.getSocket()?.connected()!!))){
                        SMSocketHandler.setSocket()
                        SMSocketHandler.establishConnection()
                        SMSocketHandler.getSocket()
                    }else{
                        SMSocketHandler.getSocket()
                    }

                    socket?.emit("find_chat_room",
                        gson.toJson(
                            UsersIdModel(
                                usersId = usersId
                            )
                        )
                    )

                    socket?.on("find_chat_room_failed"){
                        CoroutineScope(Dispatchers.Main).launch {
                            bundle.putString("chat",
                                gson.toJson(
                                    MessengerChatsElementModel(
                                    usersId = usersId,
                                    groupsId = null,
                                    countMessages = null,
                                    lastMessage = null,
                                    nameChat = "Создание чата",
                                    nicknameSender = null,
                                    refImage = null,
                                    dateSend = null,
                                    roomsId = null
                                )
                                )
                            )
                           // findNavController().navigate(R.id.action_playerProfileInTeamFragment_to_chatsFragment, bundle)
                        }
                    }

                    socket?.on("find_chat_room_success"){ args ->
                        if((args != null) && (args.isNotEmpty()) && (args[0] != null)){
                            var data = args[0].toString()
                            CoroutineScope(Dispatchers.Main).launch {
                                println(data);
                                bundle.putString("chat", data)
                              //  findNavController().navigate(R.id.action_playerProfileInTeamFragment_to_chatsFragment, bundle)
                            }
                        }
                    }
                }
            }
        }

        return view;
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlayerProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}