package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.messenger

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.messenger.MessengerChatsModel
import com.game.mobileappar.models.messenger.message.MessengerRoomElementModel
import com.game.mobileappar.models.storage.StorageProfileImageElementModel
import com.game.mobileappar.models.storage.StorageProfileImageModel
import com.game.mobileappar.network.service.APIService
import com.google.gson.Gson
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.network.models.team.players.CommandPlayerModel
import com.game.mobileappar.utils.storage.ProfileImageUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ChatAdapter(private val context: Context) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private var count: Int = 0;
    private var _dataChats: MessengerChatsModel? = null
    private var _authData: UserDataModel? = null
    private var _chatProfileImages: StorageProfileImageModel = StorageProfileImageModel(
        listProfileImage = mutableListOf()
    )

    private var _gson = Gson()

    // Локальное хранилище данных с ссылками на изображения пользователей, с которыми
    // текущий пользователь имеет общие чаты
    private var _sharedChatImagesData =
        context.getSharedPreferences(ConfigStorage.PROFILE_IMAGES_CHAT, Context.MODE_PRIVATE)

    // Локальное хранилище данных, в котором содержатся данные о текущем пользователе
    private var _sharedUserData  =
        context.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)

    // Локальное хранилище данных, в котором содержатся
    // ссылки на изображения членов команды текущего игрока
    private var _sharedTeamImagesData =
        context.getSharedPreferences(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS, Context.MODE_PRIVATE)

    public fun setChats(value: MessengerChatsModel?){
        _dataChats = value
    }

    fun setNewMessage(value: MessengerRoomElementModel?) {
        var index = (-1)
        for(i in 0..(_dataChats?.chats?.size ?: 0)){
            if ((value != null) && (i < _dataChats?.chats?.size!!)) {
                if(_dataChats?.chats?.get(i)?.roomsId == value.roomsId){
                    index = i
                    break
                }
            }
        }

        if(index >= 0){
            _dataChats?.chats?.get(index)?.lastMessage = value?.message.toString()
            _dataChats?.chats?.get(index)?.dateSend = value?.dateSend.toString()
            _dataChats?.chats?.get(index)?.countMessages = (_dataChats?.chats?.get(index)?.countMessages?.plus(1)!!)
            this.notifyItemChanged(index)
        }
    }

    class ViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) { // класс для формирования структуры элемента списка
        var chatImage: ImageView? = null
        var chatName: TextView? = null
        var chatLastMess: TextView? = null

        init {
            chatImage = itemView.findViewById(R.id.chatImage)               //аватар игрока
            chatName = itemView.findViewById(R.id.tv_chatName)              //название чата
            chatLastMess = itemView.findViewById(R.id.tv_chatLastMess)      //текст последнего сообщения
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { // указание идентификатора макета для каждого элемента списка
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_item, parent, false);

        /*var editor = _sharedChatImagesData?.edit()
        editor?.clear()
        editor?.apply()*/

        // Считывание данных из внутреннего хранилища
        // для быстрого доступа к данным и быстрой визуализации изображений
        if(_sharedUserData.contains(ConfigStorage.USERS_DATA)){
            _authData = _gson.fromJson(
                _sharedUserData.getString(ConfigStorage.USERS_DATA, null),
                UserDataModel::class.java)
        }

        var data = StorageProfileImageModel(
            listProfileImage = mutableListOf()
        )

        if(_sharedTeamImagesData.contains(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS_DATA)){
            var localData = _gson.fromJson(
                _sharedTeamImagesData.getString(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS_DATA, null),
                StorageProfileImageModel::class.java
            )

            for(i in localData.listProfileImage){
                if(data.listProfileImage.indexOf(i) < 0){
                    data.listProfileImage.add(i)
                }
            }
        }

        if(_sharedChatImagesData.contains(ConfigStorage.PROFILE_IMAGES_CHAT_DATA)){
            _chatProfileImages = _gson.fromJson(
                _sharedChatImagesData.getString(ConfigStorage.PROFILE_IMAGES_CHAT_DATA, null),
                StorageProfileImageModel::class.java
            )

            // Добавление ссылок на изображение членов команды
            for(i in data.listProfileImage){
                if(_chatProfileImages.listProfileImage.indexOf(i) < 0){
                    _chatProfileImages.listProfileImage.add(i)
                }
            }
        }else{
            var editor = _sharedChatImagesData.edit()
            editor.putString(ConfigStorage.PROFILE_IMAGES_CHAT_DATA, _gson.toJson(data))
            editor.apply()

            _chatProfileImages = _gson.fromJson(
                _sharedChatImagesData.getString(ConfigStorage.PROFILE_IMAGES_CHAT_DATA, null),
                StorageProfileImageModel::class.java
            )
        }

        return ViewHolder(itemView, context) // возвращаем ViewHolder для заполнения
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {// метод для заполнения объекта ViewHolder нужными данными
        if((_dataChats == null) || (_dataChats!!.chats.isEmpty())){
            return
        }

        var currentChatData = _dataChats!!.chats[position]

        // Сетим слушателя нажатий на нужный компонент
        if((currentChatData.lastMessage as String).length > 22){
            holder.chatLastMess?.text = (currentChatData.lastMessage!!.subSequence(0, 22).toString() + "...")
        }else{
            holder.chatLastMess?.text = currentChatData.lastMessage
        }

        if((currentChatData.nameChat as String).length > 22){
            holder.chatName?.text = (currentChatData.nameChat!!.subSequence(0, 22).toString() + "...")
        }else{
            holder.chatName?.text = currentChatData.nameChat
        }

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        // Определяем время отправки последнего сообщения
        val time = LocalTime.parse(currentChatData.dateSend, dateFormatter)

        holder.itemView.findViewById<TextView>(R.id.textView31).text = time.toString().split(".")[0]

        var element = holder.itemView.findViewById<TextView>(R.id.textView5)
        if(currentChatData.countMessages!! > 0){
            element.visibility = TextView.VISIBLE
            element.text = currentChatData.countMessages.toString()
        }else{
            element.visibility = TextView.GONE
            element.text = ""
        }

        var imageUri = ""
        if(currentChatData.usersId != null){
            // Загрузка изображений для приватного чата
            var index = ProfileImageUtil.indexOfProfileImagesByUsersId(_chatProfileImages, currentChatData.usersId!!)

            // Если ссылка на изображение пользователя было найдено, то необходимо
            // отправить серверу сообщение с названием этого файла и при
            // необходимости его обновить
            if(index >= 0){
                // Установка изображения чата (старое изображение)
                holder.chatImage
                    ?.setImageURI(Uri.parse(_chatProfileImages.listProfileImage[index].uri))

                imageUri = _chatProfileImages.listProfileImage[index].uri

                // Загрузка нового изображения чата при необходимости
                downloadFile(_authData!!.usersId,
                    _authData!!.accessToken,
                    currentChatData.usersId!!,
                    File(_chatProfileImages.listProfileImage[index].filePath).name.split(".")[0],
                    holder)
            }else{
                // В любом другом случае требуется загрузка нового файла
                downloadFile(_authData!!.usersId,
                    _authData!!.accessToken,
                    currentChatData.usersId!!,
                    null,
                    holder)
            }
        }

        holder.itemView.setOnClickListener {
            var gson = Gson()
            var bundle = Bundle()
            bundle.putString("chat", gson.toJson(currentChatData))
            bundle.putString("image_uri", imageUri)
            holder.itemView.findNavController().navigate(R.id.action_messengerFragment2_to_chatListFragment, bundle)
        }
    }

    override fun getItemCount(): Int { // метод, определяющий количество генерируемых элементов
        return if(_dataChats != null) _dataChats!!.chats.size else 0
    }

    private fun downloadFile(usersId: Int, accessToken: String,
                             teamUsersId: Int, namePreviousFile: String?, holder: ViewHolder
    ){
        val retrofit = Retrofit.Builder()
            .baseUrl(ConfigAddresses.SERVER_MEDIA_ADDRESS)
            .build()
        val service = retrofit.create(APIService::class.java)

        val gson = Gson()
        var imageData = gson.toJson(
            CommandPlayerModel(
                usersId = usersId,
                accessToken = accessToken,
                teamUsersId = teamUsersId,
                namePreviousFile = namePreviousFile
            )
        )

        val imageRequestBody = imageData.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            var responseBody = service.teamPlayersImageDownload(imageRequestBody)
            if (responseBody.isSuccessful) {
                writeResponseBodyToStorage(responseBody.body()!!,
                    responseBody.headers()["filename"].toString(),
                    teamUsersId, holder
                )
            }
        }
    }

    private fun writeResponseBodyToStorage(body: ResponseBody, filename: String,
                                           teamUsersId: Int, holder: ViewHolder
    ): Boolean {
        return try {
            val filePath =
                File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + File.separator
                        + filename + ".jpg")

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)
                //val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(filePath)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                }
                outputStream.flush()

                CoroutineScope(Dispatchers.Main).launch {
                    // Перезапись данных в Shared Preferences
                    var editor = _sharedChatImagesData?.edit()

                    val profileImageData = StorageProfileImageElementModel(
                        uri = filePath.toUri().toString(),
                        filePath = filePath.absolutePath,
                        usersId = teamUsersId
                    )

                    var index = ProfileImageUtil.indexOfProfileImagesByUsersId(
                        _chatProfileImages,
                        profileImageData.usersId
                    )

                    if(index >= 0){
                        _chatProfileImages.listProfileImage.removeAt(index)
                    }

                    _chatProfileImages.listProfileImage.add(profileImageData)

                    editor?.putString(ConfigStorage.PROFILE_IMAGES_CHAT_DATA, _gson.toJson(_chatProfileImages))
                    editor?.apply()

                    holder.chatImage
                        ?.setImageURI(filePath.toUri())
                }
                true
            } catch (e: Exception) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: Exception) {
            false
        }
    }
}

