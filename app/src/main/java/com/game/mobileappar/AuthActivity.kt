/*
* Активность для авторизации нового пользователя
* */

package com.game.mobileappar

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.*
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.network.service.APIService
import com.game.mobileappar.components.toast.CustomToast
import com.game.mobileappar.models.AccessTokenModel
import com.game.mobileappar.models.UsersIdModel
import com.game.mobileappar.models.auth.AuthLoginModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.user.UserDataModel
import com.google.android.gms.common.SignInButton
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener

class AuthActivity: AppCompatActivity() {
    // NetMan Services Authentication
    private var _txtPassword: EditText? = null
    private var _txtEmail: EditText? = null
    private var _regActivity: Intent? = null       // Интент, для вызова окна авторизации
    private var _mainActivity: Intent? = null      // Интент, для вызова главной активности

    // Google OAuth2 Service Authentication
    private var _signInButton: SignInButton? = null
    private var _googleSignOptions: GoogleSignInOptions? = null
    private var _googleSignClient: GoogleSignInClient? = null

    companion object{
        const val RC_SIGN_IN = 0
    }

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.M)  // Установка ограничения на выполнение в определённой среде
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authorization_activity)
        getLocationPermission()

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        findViewById<ConstraintLayout>(R.id.cl_authorisation).setOnClickListener {
            // исчезновение клавиатуры при щелчке на свободное пространство
            this.currentFocus?.let { view ->
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager;
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener{
            onBackPressed()
        }

        // Инициализация данных
        _txtPassword = findViewById<EditText>(R.id.txtPasswordAuth)
        _txtEmail = findViewById<EditText>(R.id.txtEmailAuth)
        // _signInButton = findViewById(R.id.sign_in_button)


        _txtPassword?.setText("");
        _txtEmail?.setText("");



        val enterBtn = findViewById<Button>(R.id.EnterBtn)

        _regActivity = Intent(this@AuthActivity, RegActivity::class.java);
        _mainActivity = Intent(this@AuthActivity, MainActivity::class.java);

        /*_googleSignOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.oauth_client_id))
            .requestEmail()
            .build()

        _googleSignClient = GoogleSignIn.getClient(this@AuthActivity, _googleSignOptions!!)
        _googleSignClient!!.silentSignIn()
            .addOnCompleteListener(
                this,
                OnCompleteListener<GoogleSignInAccount?> { task -> handleSignInResult(task) })*/

        var shared = getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE);

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        if(shared.contains(ConfigStorage.USERS_DATA)){
            // Обработка случая, когда в лоакльном хранилище уже имеются данные для пользовательского входа
            val localData = shared.getString(ConfigStorage.USERS_DATA, null);
            val retrofit = Retrofit.Builder()
                .baseUrl(ConfigAddresses.SERVER_CENTRAL_ADDRESS)
                .build();

            var gson = Gson();
            var dataLocal: UserDataModel? = gson.fromJson(localData, UserDataModel::class.java);

            val service
                    = retrofit.create(APIService::class.java);

            var dataEmail = gson.toJson(
                UsersIdModel(
                usersId = dataLocal!!.usersId
            )
            )

            var dataToken = gson.toJson(
                AccessTokenModel(
                accessToken = dataLocal!!.accessToken
            )
            )

            // Преобразование строки в формат для передачи данных (application/json)
            val requestBodyEmail = dataEmail.toRequestBody("application/json".toMediaTypeOrNull())
            val requestBodyToken = dataToken.toRequestBody("application/json".toMediaTypeOrNull())

            CoroutineScope(Dispatchers.IO).launch {
                val responseEmail = service.userExists(requestBodyEmail)
                val responseToken = service.tokenValid(requestBodyToken)

                withContext(Dispatchers.Main) {
                    if ((responseEmail.isSuccessful) && (responseToken.isSuccessful)) {
                        val usersIdExistsData = gson.toJson(
                            JsonParser.parseString(responseEmail.body()?.string())
                        )

                        val accessTokenValidData = gson.toJson(
                            JsonParser.parseString(responseToken.body()?.string())
                        )

                        val errorUsersId = gson.fromJson(usersIdExistsData, ErrorDataModel::class.java)
                        val errorAccessToken = gson.fromJson(accessTokenValidData, ErrorDataModel::class.java)

                        // Проверка валидности данных
                        if(((errorUsersId.errors == null) && (errorUsersId.message == null))
                            && ((errorAccessToken.errors == null) && (errorAccessToken.message == null))
                        ){
                            CoroutineScope(Dispatchers.Main).launch {
                                this@AuthActivity.startActivity(_mainActivity);
                            }
                        }
                    }
                }
            }
        }

        (findViewById<TextView>(R.id.tvRefRegisterLogin)).setOnClickListener(){
            val intent = Intent(this, RegActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.to_left_in,R.anim.to_left_out)
        }

        enterBtn.stateListAnimator = null;
        enterBtn.setOnClickListener() {
            // SharedPreferences используется в качестве локального хранилища данных авторизованного пользователя
            val shared = getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE);
            AuthLoginPOST<AuthLoginModel>(
                this@AuthActivity,
                applicationContext,
                ConfigAddresses.SERVER_CENTRAL_ADDRESS,
                shared
            )
                .execute(
                    AuthLoginModel(
                        _txtEmail?.text.toString(),
                        _txtPassword?.text.toString()
                    )
                );
        }

        _signInButton?.setOnClickListener {
            val signInIntent: Intent = _googleSignClient?.signInIntent!!
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken

            // Проверка токена на стороне сервера ...

            println(idToken)
        } catch (e: ApiException) { println(e) }
    }

    private fun getLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1)
        }
    }

    private fun verifyStoragePermissions(){
        val permission = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                VideoActivity.REQUEST_EXTERNAL_STORAGE_CODE
            )
        }
    }

    // Класс, обеспечивающий передачу идентификационных данных пользователя,
    // такие как пароль и email, для авторизации
    class AuthLoginPOST<T>(auth: AuthActivity, con: Context, urlStr: String, localStorage: SharedPreferences) : AsyncTask<T, String, String>(){
        private var _auth = auth                   // Активность авторизации
        private var _context = con                 // Контекст текущей активности
        private var _urlAddress = urlStr           // Базовый адрес для отправки запроса
        private var _localStorage = localStorage   // Ссылка на локальное хранилище

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun doInBackground(vararg params: T?): String? {
            val retrofit = Retrofit.Builder()
                .baseUrl(_urlAddress)
                .build() //создание экземпляра объекта retrofit, для передачи данных по сети

            val service
            = retrofit.create(APIService::class.java)  //создание сервиса под определённый запрос

            var data: T? = params[0]                   //инициализация данных, для передачи на сервер

            var gson = Gson()                          //создание объекта Gson, для сериализации и десереализации данных
            var strData = gson.toJson(data)            //сериализация данных

            //преобразование строки в формат для передачи данных (application/json)
            val requestBody = strData.toRequestBody("application/json".toMediaTypeOrNull())

            CoroutineScope(Dispatchers.IO).launch {
                val response = service.authLogin(requestBody) //отправка данных на сервер по определённому запросу

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {              //обработка ответа из сервера
                        var prettyJson = gson.toJson(
                            JsonParser.parseString(response.body()?.string())
                        );  //получение данных с сервера в формате JSON-строки

                        val error = gson.fromJson(prettyJson, ErrorDataModel::class.java) //десериализация данных

                        //проверка на корректные значения данных
                        if((error.errors == null) && (error.message == null)){
                            //открытие возможности изменения локального хранилища
                            var editor = _localStorage.edit()

                            //добавление ответа с сервера в локальное хранилище (email и token)
                            editor.putString(ConfigStorage.USERS_DATA, prettyJson)

                            //сохранение изменений в локальное хранилище
                            editor.apply()

                            CoroutineScope(Dispatchers.Main).launch {
                                CustomToast.makeText(_context.applicationContext, "Успешная авторизация!").show()
                                _auth.startActivity(_auth._mainActivity)
                            }
                        }else{
                            CoroutineScope(Dispatchers.Main).launch {
                                if((error.errors != null) && (error.errors!!.isNotEmpty())){
                                    CustomToast.makeText(_context.applicationContext, error.errors!![0].msg).show()
                                }
                                delay(1000)
                                CustomToast.makeText(_context.applicationContext, error.message).show()
                            }
                        }
                    }
                }
            }

            return ""
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.to_right_in, R.anim.to_right_out)
    }
}



