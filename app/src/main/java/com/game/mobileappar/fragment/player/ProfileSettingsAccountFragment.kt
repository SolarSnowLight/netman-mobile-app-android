package com.game.mobileappar.fragment.player

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.network.service.APIService
import com.game.mobileappar.components.toast.CustomToast
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.PlayerInfoDataModel
import com.game.mobileappar.models.PlayerInfoUpdateModel
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.user.UserDataModel
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileSettingsAccountFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _isChange: Boolean = false
    private var _toolbar: androidx.appcompat.widget.Toolbar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
        val view:View = inflater.inflate(R.layout.player_profile_settings_account_fragment, container, false)
        _toolbar = view.findViewById(R.id.toolBar_profile)
        _toolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        _toolbar?.setNavigationOnClickListener{
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            (activity as MainActivity).onBackPressed();
        }

        view.findViewById<ConstraintLayout>(R.id.frameLayout2).setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        var edit            = view.findViewById<View>(R.id.v_red_acc)
        var surnameView     = view.findViewById<EditText>(R.id.et_surnameAcc)
        var nameView        = view.findViewById<EditText>(R.id.et_nameAcc)
        var nicknameView    = view.findViewById<EditText>(R.id.et_patronymicAcc)
        var phoneView       = view.findViewById<EditText>(R.id.et_phoneNumAcc)
        var emailView       = view.findViewById<EditText>(R.id.et_emailAcc)
        var oldEmail: String = ""

        edit.setBackgroundResource(R.drawable.ic_none_pen)

        edit.setOnClickListener(View.OnClickListener {
            if((_isChange) && (shared!!.contains(ConfigStorage.USERS_DATA))){
                val localData = shared.getString(ConfigStorage.USERS_DATA, null);
                val retrofit = AppMainHandler.getRetrofit()

                var gson = Gson()
                var dataLocal: UserDataModel? = gson.fromJson(localData, UserDataModel::class.java)
                val service = retrofit.create(APIService::class.java)

                var infoPlayerUpdate = gson.toJson(
                    PlayerInfoUpdateModel(
                        accessToken = dataLocal!!.accessToken,
                        name = nameView.text.toString(),
                        surname = surnameView.text.toString(),
                        nickname = nicknameView.text.toString(),
                        phone_num = phoneView.text.toString(),
                        oldEmail = emailView.text.toString(),
                        date_birthday = null,
                        newEmail = oldEmail,
                        location = null,
                        usersId = dataLocal.usersId
                    )
                )

                val requestBody = infoPlayerUpdate.toRequestBody("application/json".toMediaTypeOrNull())

                CoroutineScope(Dispatchers.IO).launch {
                    var responseBody = service.funPlayerInfoUpdate(requestBody)
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
                                    _isChange = false
                                    edit.setBackgroundResource(R.drawable.ic_none_pen)
                                    CustomToast.makeText(requireContext(), "Данные обновлены").show()
                                }
                            }else{
                                CoroutineScope(Dispatchers.Main).launch {
                                    if((error.errors != null) && (error.errors!!.isNotEmpty())){
                                        CustomToast.makeText(requireContext(), error.errors!![0].msg).show()
                                    }

                                    delay(500)
                                    if(error.message != null){
                                        CustomToast.makeText(requireContext(), error.message).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })

        if(shared!!.contains(ConfigStorage.USERS_DATA)){
            val localData = shared.getString(ConfigStorage.USERS_DATA, null);
            val retrofit = AppMainHandler.getRetrofit()

            var gson = Gson()
            var dataLocal: UserDataModel? = gson.fromJson(localData, UserDataModel::class.java)
            val service = retrofit.create(APIService::class.java)

            var dataAccessPlayer = gson.toJson(
                PlayerAccessModel(
                    usersId = dataLocal!!.usersId,
                    accessToken = dataLocal!!.accessToken
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
                            var data = gson.fromJson(result, PlayerInfoDataModel::class.java)
                            CoroutineScope(Dispatchers.Main).launch {
                                surnameView.setText(data.surname)
                                nameView.setText(data.name)
                                nicknameView.setText(data.nickname)

                                var phoneNum = if(data.phoneNum.contains("+"))
                                    data.phoneNum else ("+7" + data.phoneNum)
                                phoneView.setText(phoneNum)
                                emailView.setText(data.email)
                                oldEmail = data.email

                                var arrayEditText = arrayOf(surnameView, nameView, nicknameView, phoneView)
                                for(value in arrayEditText){
                                    value.addTextChangedListener(object : TextWatcher {
                                        override fun afterTextChanged(s: Editable?) {}
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                            _isChange = true;
                                            view.findViewById<View>(R.id.v_red_acc).setBackgroundResource(R.drawable.ic_red_pen)
                                        }
                                    })
                                }
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
            ProfileSettingsAccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onPause() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        super.onPause()
    }
}