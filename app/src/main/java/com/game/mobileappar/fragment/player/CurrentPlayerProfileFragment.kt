package com.game.mobileappar.fragment.player

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.player.PlayerInfoModel
import com.game.mobileappar.network.service.APIService
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.canhub.cropper.CropImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.canhub.cropper.CropImageView
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.net.toUri
import com.game.mobileappar.models.storage.StorageProfileImageElementModel
import com.game.mobileappar.models.storage.StorageProfileImageModel
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.game.mobileappar.utils.storage.ProfileImageUtil
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.*
import java.io.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@Suppress("UsePropertyAccessSyntax")
class CurrentPlayerProfileFragment : Fragment() {
    private var _cropImageUri:      Uri? = null
    private var _imageViewProfile:  CircleImageView? = null
    private var _gson:              Gson? = Gson()

    // Локальные хранилища с пользовательскими данными
    private var _sharedUserProfile: SharedPreferences?      = null
    private var _sharedUserData:    SharedPreferences?      = null
    private var _sharedTeamImagesData: SharedPreferences?   = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Получение доступа к локальному хранилищу
        _sharedUserProfile =
            context?.getSharedPreferences(ConfigStorage.PROFILE_IMAGE, Context.MODE_PRIVATE)
        _sharedUserData =
            context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
        _sharedTeamImagesData =
            context?.getSharedPreferences(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS, Context.MODE_PRIVATE)

        val view: View = inflater.inflate(R.layout.player_player_profile_fragment, container,false)
        val toolBar: Toolbar? = view.findViewById(R.id.toolBar_profile)
        _imageViewProfile = view.findViewById(R.id.imageView9)

        val settings: View = view.findViewById(R.id.v_profileSettings)
        settings.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_playerInformationFragment_to_playerSettingsFragment, null))
        toolBar?.title = ""

        toolBar?.setNavigationOnClickListener { (activity as MainActivity).onBackPressed() }

        if(_sharedUserProfile!!.contains(ConfigStorage.PROFILE_IMAGE_DATA)
            && (_sharedUserData!!.contains(ConfigStorage.USERS_DATA))){
            val localAuthData = _sharedUserData?.getString(ConfigStorage.USERS_DATA, null)
            var dataAuth = _gson?.fromJson(localAuthData, UserDataModel::class.java)
            val localData = _sharedUserProfile?.getString(ConfigStorage.PROFILE_IMAGE_DATA, null)
            val data = _gson?.fromJson(localData, StorageProfileImageElementModel::class.java)

            if(data!!.usersId == dataAuth?.usersId){
                _imageViewProfile?.setImageURI(Uri.parse(data?.uri))
            }else{
                downloadFile(dataAuth!!.usersId, dataAuth.accessToken)
            }
        }

        // Выбор изображения для CropImage
        view.findViewById<ImageView>(R.id.imageView9).setOnClickListener {
            if (CropImage.isExplicitCameraPermissionRequired(requireContext())) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE
                )
            } else {
                CropImage.startPickImageActivity(requireContext(), this)
            }
        }

        if(_sharedUserData!!.contains(ConfigStorage.USERS_DATA)){
            val localData = _sharedUserData?.getString(ConfigStorage.USERS_DATA, null)
            val retrofit = AppMainHandler.getRetrofit()

            val dataLocal: UserDataModel? = _gson?.fromJson(localData, UserDataModel::class.java)
            val service = retrofit.create(APIService::class.java)

            val dataAccessPlayer = _gson?.toJson(
                PlayerAccessModel(
                    usersId = dataLocal!!.usersId,
                    accessToken = dataLocal.accessToken
                )
            )

            val requestBody = dataAccessPlayer?.toRequestBody("application/json".toMediaTypeOrNull())

            CoroutineScope(Dispatchers.IO).launch {
                val responseBody = service.funPlayerInfo(requestBody!!)
                withContext(Dispatchers.Main) {
                    if (responseBody.isSuccessful) {
                        val result = _gson?.toJson(
                            JsonParser.parseString(responseBody.body()?.string())
                        )

                        val error = _gson?.fromJson(result, ErrorDataModel::class.java)

                        // Проверка валидности данных
                        if((error?.errors == null) && (error?.message == null)
                        ){
                            val data = _gson?.fromJson(result, PlayerInfoModel::class.java)
                            CoroutineScope(Dispatchers.Main).launch {
                                val dateNow = LocalDateTime.now()
                                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000'Z'")
                                val dateBirthday = LocalDateTime.parse(data?.dateBirthday, dateFormatter)

                                view.findViewById<TextView>(R.id.tv_NameNick).text = data?.nickname
                                view.findViewById<TextView>(R.id.textView4).text = data?.dataPlayers?.rating.toString()
                                view.findViewById<TextView>(R.id.tv_playerFIO_profile)
                                    .setText("Возраст: " + Period.between(dateBirthday.toLocalDate(),
                                        dateNow.toLocalDate()).years.toString())
                                view.findViewById<TextView>(R.id.tv_city_profile)
                                    .setText(data?.location)
                            }
                        }
                    }
                }
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CurrentPlayerProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun startCropImageActivity(imageUri: Uri) {
        val intent = CropImage.activity(imageUri)
            .setCropShape(CropImageView.CropShape.OVAL)
            .setGuidelines(CropImageView.Guidelines.OFF)
            .setRequestedSize(640, 640)
            .setAspectRatio(5, 5)
            .setNoOutputImage(false)
            .getIntent(requireContext())
        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Выбор изображения из имеющихся
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUriContent(requireContext(), data)

            if (CropImage.isReadExternalStoragePermissionsRequired(requireContext(), imageUri)) {
                _cropImageUri = imageUri
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE
                )
            } else {
                startCropImageActivity(imageUri)
            }
        }

        // Установка обрезанного изображения
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val result = CropImage.getActivityResult(data)

            if(_sharedUserProfile!!.contains(ConfigStorage.PROFILE_IMAGE_DATA)){
                var localData   = _sharedUserProfile?.getString(ConfigStorage.PROFILE_IMAGE_DATA, null)
                val data        = _gson?.fromJson(localData, StorageProfileImageElementModel::class.java)

                // Удаление старого изображения
                var file = File(data!!.filePath)
                file.delete()
            }

            // Перезапись данных в Shared Preferences
            var editor = _sharedUserProfile?.edit()
            val localData = _sharedUserData?.getString(ConfigStorage.USERS_DATA, null)
            var data = _gson?.fromJson(localData, UserDataModel::class.java)

            val profileImageData = StorageProfileImageElementModel(
                uri = result?.uriContent.toString(),
                filePath = result?.getUriFilePath(requireContext())!!,
                usersId = data!!.usersId
            )

            editor?.putString(ConfigStorage.PROFILE_IMAGE_DATA, _gson?.toJson(profileImageData))
            editor?.apply()

            val teamImages = _sharedTeamImagesData?.
                getString(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS_DATA, null)
            val teamImagesData = _gson?.fromJson(teamImages, StorageProfileImageModel::class.java)
            val index = ProfileImageUtil.indexOfProfileImagesByUsersId(teamImagesData, data.usersId)

            if(index >= 0){
                teamImagesData?.listProfileImage?.removeAt(index)
                teamImagesData?.listProfileImage?.add(profileImageData)

                var teamEditor = _sharedTeamImagesData?.edit()
                teamEditor?.putString(ConfigStorage.PROFILE_IMAGES_TEAM_PLAYERS_DATA,
                    _gson?.toJson(teamImagesData))
                teamEditor?.apply()
            }

            _imageViewProfile?.setImageURI(result.uriContent)

            // Загрузка пользовательского изображения на сервер хранения медиафайлов
            uploadFile(profileImageData.filePath, data.usersId, data.accessToken)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )  {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(requireContext(), this)
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (_cropImageUri != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCropImageActivity(_cropImageUri!!)
            }
        }
    }

    private fun uploadFile(filePath: String, usersId: Int, accessToken: String){
        val retrofit = Retrofit.Builder()
            .baseUrl(ConfigAddresses.SERVER_MEDIA_ADDRESS)
            .build()
        val service = retrofit.create(APIService::class.java)

        val map: HashMap<String?, RequestBody?> = HashMap()
        var file = File(filePath)

        val requestBody: RequestBody = RequestBody.create("*/*".toMediaTypeOrNull(), file)
        map["file\"; filename=\"" + file.name + "\""] = requestBody
        val gson = Gson()

        var imageData = gson.toJson(
            PlayerAccessModel(
                usersId = usersId,
                accessToken = accessToken
            )
        )

        val imageRequestBody = imageData.toRequestBody("application/json".toMediaTypeOrNull())
        map["image_data"] = imageRequestBody

        CoroutineScope(Dispatchers.IO).launch {
            var responseBody = service.userImageUpload(map)
            /*withContext(Dispatchers.Main) {
                if (responseBody.isSuccessful) {
                    val result = gson.toJson(
                        JsonParser.parseString(responseBody.body()?.string())
                    )

                    val error = gson.fromJson(result, ErrorJSON::class.java)

                    if ((error.message != null) || (error.errors != null)) {
                        //
                    }
                }
            }*/
        }
    }

    private fun downloadFile(usersId: Int, accessToken: String){
        val retrofit = Retrofit.Builder()
            .baseUrl(ConfigAddresses.SERVER_MEDIA_ADDRESS)
            .build()
        val service = retrofit.create(APIService::class.java)

        val gson = Gson()
        var imageData = gson.toJson(
            PlayerAccessModel(
                usersId = usersId,
                accessToken = accessToken
            )
        )

        val imageRequestBody = imageData.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            var responseBody = service.userImageDownload(imageRequestBody)
            if (responseBody.isSuccessful) {
                writeResponseBodyToStorage(responseBody.body()!!,
                    responseBody.headers()["filename"].toString()
                )
            }
        }
    }

    private fun writeResponseBodyToStorage(body: ResponseBody, filename: String): Boolean {
        return try {
            val filePath =
                File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + File.separator
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
                    var editor = _sharedUserProfile?.edit()
                    val localData = _sharedUserData?.getString(ConfigStorage.USERS_DATA, null)
                    var data = _gson?.fromJson(localData, UserDataModel::class.java)

                    val profileImageData = StorageProfileImageElementModel(
                        uri = filePath.toUri().toString(),
                        filePath = filePath.absolutePath,
                        usersId = data!!.usersId
                    )

                    editor?.putString(ConfigStorage.PROFILE_IMAGE_DATA, _gson?.toJson(profileImageData))
                    editor?.apply()

                    _imageViewProfile?.setImageURI(filePath.toUri())
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

