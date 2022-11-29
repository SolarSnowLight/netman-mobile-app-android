/*
* Активность регистрации нового пользователя
* */

package com.game.mobileappar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.models.auth.AuthRegisterModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.network.service.APIService
import com.game.mobileappar.components.toast.CustomToast
import com.game.mobileappar.utils.date.DatePickerHelper
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.util.*

class RegActivity: AppCompatActivity() {
    private var _btnReg: Button? = null
    private var _btnExit: Button? = null

    private var _txtNickname: EditText? = null
    private var _txtName: EditText? = null
    private var _txtSurname: EditText? = null
    private var _txtEmail: EditText? = null
    private var _txtPhone: EditText? = null
    private var _txtBirthday: EditText? = null
    private var _txtLocation: EditText? = null
    private var _txtPassword: EditText? = null

    private var _authActivity: Intent? = null

    lateinit var datePicker: DatePickerHelper       // Объект для работы с DatePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_activity)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        _btnReg = findViewById<Button>(R.id.btnRegister)
        _txtNickname = findViewById<EditText>(R.id.etNicknameRegister)
        _txtName = findViewById<EditText>(R.id.etNameRegister)
        _txtSurname = findViewById<EditText>(R.id.etSurnameRegister)
        _txtEmail = findViewById<EditText>(R.id.etEmailRegister)
        _txtBirthday = findViewById<EditText>(R.id.etBirthdayRegister)
        _txtPhone = findViewById<EditText>(R.id.etPhoneRegister)
        _txtLocation = findViewById<EditText>(R.id.etLocationRegister)
        _txtPassword = findViewById<EditText>(R.id.etPasswordRegister)

        _authActivity = Intent(this@RegActivity, AuthActivity::class.java)

        val checkBox: CheckBox = findViewById(R.id.cbAgreeRegister)


        _txtBirthday?.addTextChangedListener {object : TextWatcher {

            var sb : StringBuilder = StringBuilder("")
            var ignore = false
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(ignore){
                    ignore = false
                    return
                }

                sb.clear()
                sb.append(if(s!!.length > 10){ s.subSequence(0,10) }else{ s })

                if(sb.lastIndex == 2){
                    if(sb[2] != '/'){
                        sb.insert(2,"/")
                    }
                } else if(sb.lastIndex == 5){
                    if(sb[5] != '/'){
                        sb.insert(5,"/")
                    }
                }

                ignore = true
                _txtBirthday?.setText(sb.toString())
                _txtBirthday?.setSelection(sb.length)

            }
            }
        }

        findViewById<Toolbar>(R.id.tbReg).setNavigationOnClickListener {
            onBackPressed()
        }

        datePicker = DatePickerHelper(this)
        _txtBirthday?.setOnClickListener {
            showDatePickerDialog()
        }

        findViewById<ConstraintLayout>(R.id.cl_registration).setOnClickListener {
            // Исчезновение клавиатуры при щелчке на свободное пространство
            this.currentFocus?.let { view ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        _btnReg?.setOnClickListener {
            // Валидация входных данных с клиентской части приложения
            if(!checkBox.isChecked){
                CoroutineScope(Dispatchers.Main).launch {
                    CustomToast.makeText(applicationContext, "Необходимо принять согласие на обработку" +
                            " персональных данных!").show()
                }
                return@setOnClickListener
            }

            //"+7 (###) ###-##-##"
            var phoneNum = _txtPhone?.text.toString()
                .replace(" ", "")
                .replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .drop(2)

            val retrofit = Retrofit.Builder()
                .baseUrl(ConfigAddresses.SERVER_CENTRAL_ADDRESS)
                .build()

            val service = retrofit.create(APIService::class.java)
            var data: AuthRegisterModel? = AuthRegisterModel(
                nickname = _txtNickname?.text.toString().trim(),
                name = _txtName?.text.toString().trim(),
                surname = _txtSurname?.text.toString().trim(),
                email = _txtEmail?.text.toString().trim(),
                phoneNum = phoneNum,
                dateBirthday = _txtBirthday?.text.toString(),
                location = _txtLocation?.text.toString().trim(),
                password = _txtPassword?.text.toString()
            )

            var gson = Gson()
            var strData = gson.toJson(data)
            val requestBody = strData.toRequestBody("application/json".toMediaTypeOrNull())

            CoroutineScope(Dispatchers.IO).launch {
                val response = service.authRegister(requestBody)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        var prettyJson = gson.toJson(
                            JsonParser.parseString(response.body()?.string())
                        );

                        val error = gson.fromJson(prettyJson, ErrorDataModel::class.java);

                        if((error.errors == null) && (error.message == null)){
                            // В данном случае, можно обработать автовход, т.к. сервер
                            // возвращает пару токенов access и refresh, для беспрерывной работы

                            CoroutineScope(Dispatchers.Main).launch {
                                CustomToast.makeText(this@RegActivity.applicationContext, "Регистрация прошла успешно!").show()
                                this@RegActivity.startActivity(Intent(this@RegActivity, AuthActivity::class.java))
                            }
                        }else{
                            CoroutineScope(Dispatchers.Main).launch {
                                if((error.errors != null) && (error.errors!!.isNotEmpty())){
                                    CustomToast.makeText(this@RegActivity.applicationContext, error.errors!![0].msg).show()
                                }
                                delay(1000)
                                CustomToast.makeText(this@RegActivity.applicationContext, error.message).show()
                            }
                        }
                    }
                }
            }
       }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.to_right_in, R.anim.to_right_out)
    }

    // Открытие панели выбора даты дня рождения
    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = cal.get(Calendar.MONTH)
        val y = cal.get(Calendar.YEAR)
        datePicker.showDialog(d, m, y, object : DatePickerHelper.Callback {
            @SuppressLint("SetTextI18n")
            override fun onDateSelected(dayofMonth: Int, month: Int, year: Int) {
                val dayStr = if (dayofMonth < 10) "0${dayofMonth}" else "$dayofMonth"
                val mon = month + 1
                val monthStr = if (mon < 10) "0${mon}" else "$mon"
                _txtBirthday?.setText("${year}${monthStr}${dayStr}")
            }
        })
    }
}

