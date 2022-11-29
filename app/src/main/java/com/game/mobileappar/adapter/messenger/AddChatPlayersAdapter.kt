package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.messenger

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.player.search.command.PlayerSearchCommandModel
import com.game.mobileappar.network.service.APIService
import com.google.gson.Gson
import com.game.mobileappar.models.storage.StorageProfileImageModel
import com.game.mobileappar.models.storage.StorageProfileImageElementModel
import com.game.mobileappar.utils.storage.ProfileImageUtil
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.network.models.team.players.CommandPlayerModel
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

class AddChatPlayersAdapter(private val context: Context): RecyclerView.Adapter<AddChatPlayersAdapter.ViewHolder>() {

    private var _data: PlayerSearchCommandModel? = null
    private var _dataUser : PlayerAccessModel? = null
    private var _gson = Gson()
    private var _cashProfileImage: StorageProfileImageModel = StorageProfileImageModel(
        listProfileImage = mutableListOf()
    )

    // Локальное хранилище данных с ссылками на изображения пользователей, с которыми
    // текущий пользователь имеет общие чаты
    private var _sharedChatImagesData =
        context.getSharedPreferences(ConfigStorage.PROFILE_IMAGES_CHAT, Context.MODE_PRIVATE)

    // Локальное хранилище данных, в котором содержатся
    // ссылки на изображения членов команды текущего игрока
    private var _sharedTeamImagesData =
        context.getSharedPreferences(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS, Context.MODE_PRIVATE)

    // Локальное хранилище данных, в котором содержатся ссылки на изображения пользователей
    // которые были найдены с помощью строки поиска текущим пользователем
    private var _sharedCashImagesData =
        context.getSharedPreferences(ConfigStorage.PROFILE_IMAGES_CASH, Context.MODE_PRIVATE)

    public fun setData(values: PlayerSearchCommandModel?){
        _data = values
        notifyDataSetChanged()
    }

    public fun setDataUser(element: PlayerAccessModel?){
        _dataUser = element
    }

    class ViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
        var ivUserImage: ImageView = itemView.findViewById(R.id.avatarPlayerItem)
        var tvNickname: TextView = itemView.findViewById(R.id.textView22)
        var tvFullName: TextView = itemView.findViewById(R.id.txtNickPlayerCommand)

        init {
            itemView.findViewById<TextView>(R.id.textView23).visibility = View.GONE
            itemView.findViewById<View>(R.id.v_findPlayerBtn).visibility = View.GONE
            itemView.findViewById<TextView>(R.id.txtRatingPlayerCommand).visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.player_item, parent, false);

        var data = StorageProfileImageModel(
            listProfileImage = mutableListOf()
        )

        /*var editor = _sharedCashImagesData?.edit()
        editor?.clear()
        editor?.apply()*/

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
            var localData = _gson.fromJson(
                _sharedChatImagesData.getString(ConfigStorage.PROFILE_IMAGES_CHAT_DATA, null),
                StorageProfileImageModel::class.java
            )

            for(i in localData.listProfileImage){
                if(data.listProfileImage.indexOf(i) < 0){
                    data.listProfileImage.add(i)
                }
            }
        }

        if(_sharedCashImagesData.contains(ConfigStorage.PROFILE_IMAGES_CASH_DATA)){
            _cashProfileImage = _gson.fromJson(
                _sharedCashImagesData.getString(ConfigStorage.PROFILE_IMAGES_CASH_DATA, null),
                StorageProfileImageModel::class.java
            )

            for(i in data.listProfileImage){
                if(_cashProfileImage.listProfileImage.indexOf(i) < 0){
                    _cashProfileImage.listProfileImage.add(i)
                }
            }
        }else{
            var editor = _sharedCashImagesData.edit()
            editor.putString(ConfigStorage.PROFILE_IMAGES_CASH_DATA, _gson.toJson(data))
            editor.apply()

            _cashProfileImage = _gson.fromJson(
                _sharedCashImagesData.getString(ConfigStorage.PROFILE_IMAGES_CASH_DATA, null),
                StorageProfileImageModel::class.java
            )
        }

        return ViewHolder(itemView, context)
    }


    override fun getItemCount(): Int {
        return if(_data != null) _data!!.freePlayers.size else 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentData = _data!!.freePlayers[position]

        holder.tvFullName.text = currentData.name + " " + currentData.surname
        holder.tvNickname.text = currentData.nickname

        // Загрузка изображений для приватного чата
        val index = ProfileImageUtil.indexOfProfileImagesByUsersId(_cashProfileImage, currentData.usersId)
        var imageUri = ""

        if(index >= 0){
            // Установка изображения чата (старое изображение)
            holder.ivUserImage
                ?.setImageURI(Uri.parse(_cashProfileImage.listProfileImage[index].uri))

            imageUri = _cashProfileImage.listProfileImage[index].uri

            // Загрузка нового изображения чата при необходимости
            downloadFile(_dataUser!!.usersId,
                _dataUser!!.accessToken,
                currentData.usersId,
                File(_cashProfileImage.listProfileImage[index].filePath).name.split(".")[0],
                holder)
        }else{
            // В любом другом случае требуется загрузка нового файла
            downloadFile(_dataUser!!.usersId,
                _dataUser!!.accessToken,
                currentData.usersId,
                null,
                holder)
        }

        holder.itemView.setOnClickListener {
            val bundle = Bundle();
            bundle.putString("type", "ChatPrivate");
            bundle.putInt("users_id", currentData.usersId)
            bundle.putString("access_token", _dataUser?.accessToken)
            bundle.putString("image_uri", imageUri)
            //holder.itemView.findNavController().navigate(R.id.action_addChatFragment_to_playerProfileFragment, bundle)
        }
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
                    var editor = _sharedCashImagesData?.edit()

                    val profileImageData = StorageProfileImageElementModel(
                        uri = filePath.toUri().toString(),
                        filePath = filePath.absolutePath,
                        usersId = teamUsersId
                    )

                    var index = ProfileImageUtil.indexOfProfileImagesByUsersId(
                        _cashProfileImage,
                        profileImageData.usersId
                    )

                    if(index >= 0){
                        _cashProfileImage.listProfileImage.removeAt(index)
                    }

                    _cashProfileImage.listProfileImage.add(profileImageData)

                    editor?.putString(ConfigStorage.PROFILE_IMAGES_CHAT_DATA, _gson.toJson(_cashProfileImage))
                    editor?.apply()

                    holder.ivUserImage
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