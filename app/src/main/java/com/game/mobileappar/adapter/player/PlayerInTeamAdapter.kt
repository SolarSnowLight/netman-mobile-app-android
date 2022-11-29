package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.player

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.config.game.ConfigStatusPlayer
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.command.players.CommandPlayersModel
import com.game.mobileappar.models.storage.StorageProfileImageElementModel
import com.game.mobileappar.models.storage.StorageProfileImageModel
import com.game.mobileappar.network.service.APIService
import com.google.gson.Gson
import com.game.mobileappar.utils.storage.ProfileImageUtil
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.network.models.team.players.CommandPlayerModel
import de.hdodenhof.circleimageview.CircleImageView
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

class PlayerInTeamAdapter(private val context: Context, private val inputArg: String): RecyclerView.Adapter<PlayerInTeamAdapter.ViewHolder>() {

    private var _data: CommandPlayersModel? = null
    private var _dataUser: PlayerAccessModel? = null
    private var _playerStatus: Byte = ConfigStatusPlayer.PLAYER_DEFAULT
    private var _teamPlayersImages: StorageProfileImageModel = StorageProfileImageModel(
        listProfileImage = mutableListOf()
    )

    // Локальные хранилище данных, в котором содержатся ссылки на изображения
    // определённых пользователей, с помощью которых можно добавить изображение члену команды
    private var _sharedTeamImagesData: SharedPreferences? =
        context.getSharedPreferences(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS, Context.MODE_PRIVATE)

    // Локальное хранилище данных, в котором содержится ссылка на изображения текущего пользователя
    private var _sharedUserProfile  =
        context.getSharedPreferences(ConfigStorage.PROFILE_IMAGE, Context.MODE_PRIVATE)

    private var _gson: Gson = Gson()

    public fun setPlayers(element: CommandPlayersModel?){
        _data = element
    }

    public fun setDataUser(element: PlayerAccessModel?){
        _dataUser = element
    }

    fun setPlayerStatus(playerStatus: Byte) {
        _playerStatus = playerStatus
    }

    class ViewHolder(itemView: View, context: Context, private val inputArg: String) : RecyclerView.ViewHolder(itemView) {
        // класс для формирования структуры элемента списка
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.player_item, parent, false);

        // Пример очистки данных локального хранилища
        /*var editor = _sharedTeamImagesData?.edit()
        editor?.clear()
        editor?.apply()*/

        if(_sharedTeamImagesData!!.contains(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS_DATA)){
            // Обработка ситуации, когда изображения для каждых пользователей уже известны
            _teamPlayersImages = _gson.fromJson(
                _sharedTeamImagesData?.getString(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS_DATA, null),
                StorageProfileImageModel::class.java)
        }

        return ViewHolder(itemView, context,inputArg)
    }


    override fun getItemCount(): Int {
        if(_data == null)
            return 0
        return _data!!.users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(_data != null){
            val element = _data!!.users[position];
            holder.itemView.findViewById<TextView>(R.id.textView22).text = element.nickname;
            holder.itemView.findViewById<TextView>(R.id.textView23).text =
                if(element.creator == true) "создатель" else ""
            holder.itemView.findViewById<TextView>(R.id.txtRatingPlayerCommand).text =
                element.rating.toString()
            holder.itemView.findViewById<TextView>(R.id.txtNickPlayerCommand).text =
                element.name + " " + element.surname

            var index = ProfileImageUtil.indexOfProfileImagesByUsersId(_teamPlayersImages, element.usersId!!)
            if(index >= 0){
                // Установка изображения члена команды (старое изображение)
                holder.itemView.findViewById<CircleImageView>(R.id.avatarPlayerItem)
                    .setImageURI(Uri.parse(_teamPlayersImages.listProfileImage[index].uri))

                // Загрузка нового изображения члена команды при необходимости
                downloadFile(_dataUser!!.usersId,
                    _dataUser!!.accessToken,
                    element.usersId!!,
                    File(_teamPlayersImages.listProfileImage[index].filePath).name.split(".")[0],
                    holder)
            }else{
                if(element.usersId == _dataUser?.usersId){
                    val localData = _sharedUserProfile?.getString(ConfigStorage.PROFILE_IMAGE_DATA, null)
                    val data = _gson.fromJson(localData, StorageProfileImageElementModel::class.java)

                    if((data != null) && (data!!.usersId == _dataUser!!.usersId)){
                        holder.itemView.findViewById<CircleImageView>(R.id.avatarPlayerItem)
                            .setImageURI(Uri.parse(data.uri))
                        _teamPlayersImages.listProfileImage.add(data)
                    }else{
                        downloadFile(_dataUser!!.usersId, _dataUser!!.accessToken,
                            element.usersId!!, null,  holder)
                    }
                }else{
                    // Необходима загрузка изображения члена команды
                    downloadFile(_dataUser!!.usersId, _dataUser!!.accessToken,
                        element.usersId!!, null, holder)
                }
            }

            when (inputArg){
                "ExistingTeamFragment"->{ // аргумент из фрагмента команды, обычный игрок, не создатель
                    val bundle = Bundle()
                    bundle.putString("type", inputArg)
                    bundle.putInt("users_id", element.usersId!!)
                    bundle.putString("access_token", _dataUser?.accessToken)
                    bundle.putByte("status_player", _playerStatus)

                    holder.itemView.setOnClickListener(Navigation.createNavigateOnClickListener
                        (R.id.action_playerInExistingTeamFragment_to_playerProfileInTeamFragment, bundle))
                }
                "TeamCreatorFragment"->{ // аргумент из фрагмента создателя чата
                    val bundle = Bundle()
                    bundle.putString("type", inputArg)
                    bundle.putInt("users_id", element.usersId!!)
                    bundle.putString("access_token", _dataUser?.accessToken)
                    bundle.putByte("status_player", _playerStatus)

                    holder.itemView.setOnClickListener(Navigation.createNavigateOnClickListener
                        (R.id.action_teamBossFragment_to_playerProfileInTeamFragment, bundle))
                }
            }
        }
    }

    private fun downloadFile(usersId: Int, accessToken: String,
                             teamUsersId: Int, namePreviousFile: String?, holder: ViewHolder){
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
                                           teamUsersId: Int, holder: ViewHolder): Boolean {
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
                    var editor = _sharedTeamImagesData?.edit()

                    val profileImageData = StorageProfileImageElementModel(
                        uri = filePath.toUri().toString(),
                        filePath = filePath.absolutePath,
                        usersId = teamUsersId
                    )

                    var index = ProfileImageUtil.indexOfProfileImagesByUsersId(
                        _teamPlayersImages,
                        profileImageData.usersId
                    )

                    if(index >= 0){
                        _teamPlayersImages.listProfileImage.removeAt(index)
                    }

                    _teamPlayersImages.listProfileImage.add(profileImageData)

                    editor?.putString(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS_DATA, _gson.toJson(_teamPlayersImages))
                    editor?.apply()

                    holder.itemView.findViewById<CircleImageView>(R.id.avatarPlayerItem)
                        .setImageURI(filePath.toUri())
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