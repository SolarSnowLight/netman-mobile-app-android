/*
* Активность для работы с видеофайлами (загрузка / воспроизведение)
* */

package com.game.mobileappar

import android.Manifest
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.widget.*
import com.game.mobileappar.components.toast.CustomToast
import android.widget.VideoView
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.network.service.APIService
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import retrofit2.Retrofit
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import com.game.mobileappar.config.game.ConfigStatusPlayer
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.game.GameCurrentQuestModel
import com.game.mobileappar.models.game.GameRefMediaModel
import com.game.mobileappar.models.media.MediaLocalPathModel
import com.game.mobileappar.models.media.MediaSendLocalPathModel
import com.game.mobileappar.models.media.MediaSendVideoModel
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import kotlin.collections.HashMap


class VideoActivity: AppCompatActivity() {
    private var _recordVideoUpdate: Button? = null
    private var _uploadVideo: Button? = null
    private var _recordVideo: Button? = null
    private var _pDialog: ProgressDialog? = null
    private var _videoView: VideoView? = null
    private var _bufferingTextView: TextView? = null
    private var _video: Uri? = null
    private var _videoPath: String? = null
    private var _currentPosition: Int? = 0

    // Данные, с которыми будет происходить работа
    private var _gameInfo: GameCurrentQuestModel? = null
    private var _authDataInfo: UserDataModel? = null
    private var _refMediaInstructions: GameRefMediaModel? = null

    // Константы для работы системы видео активности
    companion object{
        const val REQUEST_PICK_VIDEO = 3
        const val PLAYBACK_TIME = "play_time"
        const val CAMERA_PERMISSION_CODE = 100
        const val VIDEO_RECORD_CODE = 101
        const val REQUEST_EXTERNAL_STORAGE_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_layout)

        _recordVideoUpdate              = findViewById(R.id.btnRecordVideoUpdate)
        _uploadVideo                    = findViewById(R.id.uploadVideo)
        _recordVideo                    = findViewById(R.id.btnRecordVideo)
        _videoView                      = findViewById(R.id.videoview)

        _recordVideoUpdate?.visibility  = View.GONE
        _uploadVideo?.visibility        = View.GONE
        _recordVideo?.visibility        = View.VISIBLE

        // Обработка нажатия на кнопку для съёмки видео
        _recordVideo?.setOnClickListener {
            recordVideo()
            _recordVideo?.visibility = View.GONE
            _recordVideoUpdate?.visibility = View.VISIBLE
            _uploadVideo?.visibility = View.VISIBLE
        }

        // Обработка нажатия на кнопку пересъёмки видео
        _recordVideoUpdate?.setOnClickListener{
            recordVideo()
        }

        if(!isCameraPresentInPhone()){
            _recordVideo?.visibility = View.GONE
        }else{
            getCameraPermission()
        }

        //getCameraPermission()

        // Запрос разрешений для возможности работы с хранилищем
        verifyStoragePermissions()

        /*
        // Обработка выбора видео (возможно пригодиться)
        _button?.setOnClickListener(View.OnClickListener {
            val pickVideoIntent = Intent(Intent.ACTION_GET_CONTENT)
            pickVideoIntent.type = "video/mp4"
            startActivityForResult(pickVideoIntent, REQUEST_PICK_VIDEO)
        })*/

        // Загрузка видеофайл на сервер хранения медиафайлов
        _uploadVideo?.setOnClickListener {
            if (_video != null) {
                uploadFile()
            } else {
                CustomToast.makeText(this@VideoActivity, "Снимите видео для загрузки").show()
            }
        }

        if(savedInstanceState != null){
            _currentPosition = savedInstanceState.getInt(PLAYBACK_TIME)
        }

        var controller: MediaController? = MediaController(this)
        controller?.setMediaPlayer(_videoView)
        _videoView?.setMediaController(controller)

        initDialog()

       val intent = intent
        if(intent.getByteExtra("player_status", ConfigStatusPlayer.PLAYER_ACTIVE) == ConfigStatusPlayer.PLAYER_ACTIVE){
            // Обработка ситуации, когда игрок решил посмотреть видео с игровыми инструкциями
            _recordVideoUpdate?.visibility  = View.GONE
            _uploadVideo?.visibility        = View.GONE
            _recordVideo?.visibility        = View.GONE

            var file = File(intent.getStringExtra("file_path")!!)
            initializePlayer(Uri.fromFile(file))
        }else{
            var gson = Gson()

            // Получение игровой информации
            _gameInfo = gson.fromJson(intent.getStringExtra("game_info"),
                GameCurrentQuestModel::class.java)

            // Получение информации о пользователе
            _authDataInfo = gson.fromJson(intent.getStringExtra("user_data"),
                UserDataModel::class.java)

            // Получение информации о игровой инструкции
            _refMediaInstructions = gson.fromJson(intent.getStringExtra("ref_media"),
                GameRefMediaModel::class.java)
        }
    }

    // Обработка постановки активности на паузу
    override fun onPause() {
        super.onPause()

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            _videoView?.pause()
        }
    }

    // Обработка остановки активности
    override fun onStop() {
        super.onStop()

        releasePlayer()
    }

    // Сохранение состояния
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(PLAYBACK_TIME, _videoView!!.currentPosition)
    }

    // Проверка на присутствие камеры в данном телефоне
    private fun isCameraPresentInPhone(): Boolean{
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            return true
        }
        return false
    }

    // Проверка ограничений мобильного устройства
    private fun getCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE)
        }
    }

    // Проверка ограничений мобильного устройства для записи в хранилище
    private fun verifyStoragePermissions(){
        val permission = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                REQUEST_EXTERNAL_STORAGE_CODE
            )
        }
    }

    // Проигрывание видео
    private fun recordVideo(){
        var intent: Intent? = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, VIDEO_RECORD_CODE)
    }

    private fun initializePlayer(uri: Uri){
        _bufferingTextView?.visibility = VideoView.VISIBLE
        if(uri != null){
            _videoView?.setVideoURI(uri)
        }

        _videoView?.setOnPreparedListener {
            _bufferingTextView?.visibility = VideoView.INVISIBLE

            if (_currentPosition!! > 0) {
                _videoView?.seekTo(_currentPosition!!)
            } else {
                _videoView?.seekTo(1)
            }

            _videoView?.start()
        }

        _videoView?.setOnCompletionListener {
            CustomToast.makeText(this@VideoActivity, "Воспроизведение завершено").show()
            _videoView?.seekTo(0)
        }
    }

    private fun releasePlayer(){
        _videoView?.stopPlayback()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((resultCode == RESULT_OK) && (data != null)) {
            if((requestCode == REQUEST_PICK_VIDEO)
                || (requestCode == VIDEO_RECORD_CODE)){
                _video = data.data
                _videoPath = getPath(_video)
                initializePlayer(_video!!)
                //uploadFile(video.getPath());
            }
        } else if (resultCode != RESULT_CANCELED) {
            CustomToast.makeText(this@VideoActivity, "Отмена воспроизведения").show()
        }
    }

    private fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else null
    }

    private fun initDialog() {
        _pDialog = ProgressDialog(this)
        _pDialog?.setMessage("Загрузка...")
        _pDialog?.setCancelable(true)
    }


    private fun showDialog() {
        if (!(_pDialog?.isShowing!!)) _pDialog?.show()
    }

    private fun hideDialog() {
        if (_pDialog?.isShowing == true) _pDialog?.dismiss()
    }

    private fun uploadFile(){
        if((_video == null) || (_video!!.equals(""))){
            CustomToast.makeText(this@VideoActivity, "Необходимо выбрать видео для отправки!").show()
            return
        }else{
            val retrofit = Retrofit.Builder()
                .baseUrl(ConfigAddresses.SERVER_MEDIA_ADDRESS)
                .build()
            val service = retrofit.create(APIService::class.java)

            val map: HashMap<String?, RequestBody?> = HashMap()
            var file = File(_videoPath.toString())

            val requestBody: RequestBody = RequestBody.create("video/mp4".toMediaTypeOrNull(), file)
            map["file\"; filename=\"" + file.name + "\""] = requestBody
            val gson = Gson()

            var videoData = gson.toJson(
                MediaSendVideoModel(
                currentGames = _gameInfo?.currentGamesId,
                usersId = _authDataInfo?.usersId,
                gamesId = _gameInfo?.id,
                refMediaInstructions = _refMediaInstructions?.refMedia,
                accessToken = _authDataInfo?.accessToken
            )
            )

            val videoRequestBody = videoData.toRequestBody("application/json".toMediaTypeOrNull())
            map["video_data"] = videoRequestBody

            CoroutineScope(Dispatchers.IO).launch {
                // Загрузка видео на медиа сервер
                var responseBody = service.mediaUpload(map)
                withContext(Dispatchers.Main) {
                    if (responseBody.isSuccessful) {
                        val result = gson.toJson(
                            JsonParser.parseString(responseBody.body()?.string())
                        )

                        val error = gson.fromJson(result, ErrorDataModel::class.java)

                        if ((error.message != null) || (error.errors != null)) {
                            CustomToast.makeText(this@VideoActivity, error.message).show()
                        }else{
                            // Добавление результата видео в базу данных
                            addResultVideoCommand(gson.fromJson(result, MediaLocalPathModel::class.java))
                        }
                    }
                }
            }
        }
    }

    private fun addResultVideoCommand(value: MediaLocalPathModel?) {
        val retrofit = Retrofit.Builder()
            .baseUrl(ConfigAddresses.SERVER_CENTRAL_ADDRESS)
            .build()

        var gson = Gson()
        val service = retrofit.create(APIService::class.java)

        var dataAccessPlayer = gson.toJson(
            MediaSendLocalPathModel(
                refMedia = value?.localPath,
                accessToken = _authDataInfo?.accessToken,
                gameId = _gameInfo?.id,
            )
        )

        val requestBody = dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            var responseBody = service.funCommandAddResultMedia(requestBody)
            withContext(Dispatchers.Main) {
                if (responseBody.isSuccessful) {
                    val result = gson.toJson(
                        JsonParser.parseString(responseBody.body()?.string())
                    )

                    val error = gson.fromJson(result, ErrorDataModel::class.java)

                    // Проверка валидности данных
                    if((error.errors == null) && (error.message == null)
                    ){
                        CoroutineScope(Dispatchers.Main).launch {
                            CustomToast.makeText(this@VideoActivity, "Видео результат загружен!").show()
                            startActivity(Intent(this@VideoActivity, MainActivity::class.java))
                        }
                    }
                }
            }
        }
    }
}