package com.game.mobileappar

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.fragment.EmptyFragment
import com.game.mobileappar.fragment.judge.RulerRateFragment
import com.game.mobileappar.fragment.messenger.ChatFragment
import com.game.mobileappar.fragment.messenger.MessengerFragment
import com.game.mobileappar.fragment.player.CurrentPlayerProfileFragment
import com.game.mobileappar.network.handler.SCSocketHandler
import com.game.mobileappar.models.GameStatusModel
import com.game.mobileappar.service.NotificationService
import com.game.mobileappar.components.toast.CustomToast
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.coroutines.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import com.game.mobileappar.containers.messenger.MessengerActivity
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.config.command.ConfigStatusCommand
import com.game.mobileappar.config.game.ConfigStatusPlayer
import com.game.mobileappar.fragment.game.HintFragment
import com.game.mobileappar.fragment.messenger.ChatInfoFragment
import com.game.mobileappar.fragment.player.PlayerSettingsFragment
import com.game.mobileappar.fragment.player.ProfileSettingsAccountFragment
import com.game.mobileappar.fragment.team.*
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.game.GameCurrentQuestModel
import com.game.mobileappar.models.game.GamePathMediaModel
import com.game.mobileappar.models.game.GameQuestIdModel
import com.game.mobileappar.models.game.GameRefMediaModel
import com.game.mobileappar.models.judge.JudgeInfoModel
import com.game.mobileappar.models.command.status.CommandStatusModel
import com.game.mobileappar.network.service.APIService
import com.google.gson.JsonParser
import com.game.mobileappar.models.command.CommandIdModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var _drawer: DrawerLayout? = null
    private var _toolBar: Toolbar? = null
    private var _appBarConfiguration: AppBarConfiguration? = null
    private var _messageCountBadge: BadgeDrawable? = null           // счётчик сообщений
    private var _currentFragment: Fragment? = null
    private var _socket: Socket? = null
    private var _commonContainer: NavHostFragment? = null
    private var _commonController: NavController? = null
    private var _tbMain: ConstraintLayout? = null
    private var _tbHintContainer: FragmentContainerView? = null
    private var _bottomNavigationView: BottomNavigationView? = null
    private var _cameraView: View? = null
    private var _hintTextView: TextView? = null
    private var _tbTextView: TextView? = null
    private var _tbTextViewCentral: TextView? = null

    // Управления фоновыми корутинами
    private var _updateStatusUser: Job? = null

    // Конфигурация судьи
    // Указывает на то, была ли первая установка судейского статуса
    // (для безопасности связанной с большим числом переопределений на
    // прослушивание одних и тех же событий)
    private var _beginJudgeState = false

    // Состояния каждого игрока
    private var _gameStatusPlayer: Byte? = ConfigStatusPlayer.PLAYER_DEFAULT
    private var _commandStatusPlayer: Byte? = ConfigStatusCommand.TEAM_CREATOR
    private var _commandInfo: CommandIdModel? = null

    // Состояния визуального представления
    private var _viewMainFragment: Boolean = true
    private var _loadMediaInstructions: Boolean = false
    private var _currentQuestData: GameCurrentQuestModel? = null
    private var _currentMediaLocalPath: String = ""
    private var _shared: SharedPreferences? = null
    private var _refMediaInstructions: GameRefMediaModel? = null
    private var _judgeData: JudgeInfoModel? = null

    companion object{
        const val REQUEST_EXTERNAL_STORAGE_CODE = 1
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        // Запрос пользовательских разрешений
        verifyStoragePermissions()
        storagePermissions()

        // Идентификация нижней навигационной панели
        _bottomNavigationView = findViewById(R.id.bottomNavigationView)
        
        // Информация о текущей игре
        _tbMain = findViewById(R.id.tb_main)
        _tbTextViewCentral = findViewById<TextView>(R.id.textView7)
        _tbHintContainer = findViewById(R.id.fragmentContainerView3)
        val view: View = findViewById(R.id.view11)
        _tbTextView = findViewById(R.id.textView13)
        val icMedia: View = findViewById(R.id.view10)
        _cameraView = findViewById(R.id.view1010)
        val hintFragment =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainerView3) as NavHostFragment)
                .childFragmentManager.primaryNavigationFragment as HintFragment
        _hintTextView = hintFragment.view?.findViewById<TextView>(R.id.tv_gameHint)

        _shared = getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)

        if (_hintTextView == null) {
            // Ожидание отрисовки фрагмента
            CoroutineScope(Dispatchers.Main).launch {
                while (_hintTextView == null) {
                    _hintTextView = hintFragment.view?.findViewById(R.id.tv_gameHint)
                }
            }
        }

        val host: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment? ?: return
        val navController = host.navController

        _drawer = findViewById(R.id.drawer_layout)

        _appBarConfiguration = AppBarConfiguration(navController.graph)
        _appBarConfiguration =
            AppBarConfiguration.Builder(navController.graph).setOpenableLayout(_drawer)
                .build()

        _toolBar = findViewById(R.id.toolbar2)
        _toolBar?.setupWithNavController(navController, _appBarConfiguration!!)

        _toolBar?.setNavigationIcon(R.drawable.ic_______)

        // Идентификация главного контейнера фрагментов для главной активности
        _commonContainer =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment?
                ?: return

        // Инициализация контроллера (общего)
        _commonController = _commonContainer!!.navController

        //*******************************************************
        // Панель вывода текущего задания
        icMedia.visibility = View.GONE

        // При нажатии на стрелку происходит закрытие TextView
        _tbTextView?.visibility = View.GONE

        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        view.setOnClickListener {
            // Поворот view элемента на 180 градусов
            view.rotation = view.rotation + 180f
            if (_tbTextView?.visibility == View.VISIBLE) {
                _tbTextView?.visibility = View.GONE
            } else {
                _tbTextView?.visibility = View.VISIBLE
            }
        }
        //*******************************************************

        // Добавление количества новых сообщений в мессенджере
        _messageCountBadge = _bottomNavigationView?.getOrCreateBadge(R.id.ChatFragmentItem)
        _messageCountBadge?.verticalOffset = 20
        _messageCountBadge?.horizontalOffset = -20
        _messageCountBadge?.backgroundColor = resources.getColor(R.color.invisible)
        _messageCountBadge?.badgeTextColor = Color.parseColor("#FFFFFF")
        _messageCountBadge?.number = 10
        _messageCountBadge?.isVisible = true


        // Обработка нажатия на кнопки нижнего навигационного меню
        _bottomNavigationView?.setOnNavigationItemSelectedListener() { menuItem ->
            _tbMain?.visibility                 = View.GONE
            _tbHintContainer?.visibility        = View.GONE
            _viewMainFragment                   = false
            _bottomNavigationView?.visibility   = BottomNavigationView.GONE

            var bundle = Bundle()
            bundle.putByte("status_player", _gameStatusPlayer!!)
            var gson = Gson()

            when (menuItem.itemId) {
                R.id.ChatFragmentItem -> {
                    _currentFragment =
                        _commonContainer?.childFragmentManager?.primaryNavigationFragment
                    if (_currentFragment is ChatFragment) {
                       // _commonController!!.navigate(R.id.action_chatsFragment_to_emptyFragment2)
                    }
                    if (_currentFragment is EmptyFragment) {
                        //_commonController!!.navigate(R.id.action_emptyFragment_to_messenger_router3)
                        startActivity(Intent(this, MessengerActivity::class.java))
                    }
                    if (_currentFragment is MessengerFragment) {
                       // _commonController!!.navigate(R.id.action_chatFragment_to_emptyFragment)
                      //  _viewMainFragment = true
                      //  _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
                    }
                    true
                }
                R.id.Profile -> {
                    _currentFragment =
                        _commonContainer?.childFragmentManager?.primaryNavigationFragment
                    if (_currentFragment is EmptyFragment) {
                        _commonController!!.navigate(R.id.action_emptyFragment_to_playerInformationFragment)
                    }
                    if (_currentFragment is CurrentPlayerProfileFragment) {
                        _commonController!!.navigate(R.id.action_playerInformationFragment_to_emptyFragment)
                        _viewMainFragment = true
                        _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
                    }

                    true
                }
                R.id.Team -> {
                    _currentFragment =
                        _commonContainer?.childFragmentManager?.primaryNavigationFragment
                    if (_currentFragment is EmptyFragment) {
                        if (_gameStatusPlayer == ConfigStatusPlayer.JUDGE) {
                            var bundle = Bundle()
                            bundle.putString("judge_info", gson.toJson(_judgeData))
                            bundle.putString("external_dir", getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString())

                            _commonController!!.navigate(R.id.action_emptyFragment_to_rulerRateFragment, bundle)
                        }else{
                            if (_commandInfo != null) {
                                bundle.putInt("commands_id", _commandInfo!!.commandsId)
                                if (_commandStatusPlayer == ConfigStatusCommand.TEAM_MEMBER) {
                                    _commonController!!.navigate(
                                        R.id.action_emptyFragment_to_playerInExistingTeamFragment,
                                        bundle
                                    )
                                }

                                if (_commandStatusPlayer == ConfigStatusCommand.WITHOUT_TEAM) {
                                    _commonController!!.navigate(R.id.action_emptyFragment_to_teamFragment)
                                }

                                if (_commandStatusPlayer == ConfigStatusCommand.TEAM_CREATOR) {
                                    _commonController!!.navigate(
                                        R.id.action_emptyFragment_to_teamBossFragment,
                                        bundle
                                    )
                                }
                            }
                        }
                    }

                    if (_currentFragment is RulerRateFragment) {
                        _commonController!!.navigate(R.id.action_rulerRateFragment_to_emptyFragment)
                        _viewMainFragment = true
                        _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
                    }

                    if (_currentFragment is PlayerExistsTeamFragment) {
                        _commonController!!.navigate(R.id.action_playerInExistingTeamFragment_to_emptyFragment)
                        _viewMainFragment = true
                        _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
                    }

                    if (_currentFragment is CurrentPlayerProfileFragment) {
                        _commonController!!.navigate(R.id.action_playerInformationFragment_to_emptyFragment)
                        _viewMainFragment = true
                        _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
                    }


                    if (_currentFragment is PlayerExistsTeamFragment) {
                        _commonController!!.navigate(R.id.action_playerInExistingTeamFragment_to_emptyFragment)
                        _viewMainFragment = true
                        _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
                    }

                    true
                }

                else -> false
            }
        }

        // Запуск службы уведомлений о текущих играх
        startService(Intent(this, NotificationService::class.java))

        _socket = if((SCSocketHandler.getSocket() == null)
            || (!(SCSocketHandler.getSocket()?.connected()!!))){
            SCSocketHandler.setSocket()              // Добавление подключения
            SCSocketHandler.establishConnection()    // Подключение к серверу
            SCSocketHandler.getSocket()              // Получение информации об сокете для устройства
        }else{
            SCSocketHandler.getSocket()
        }

        // Отправка данных на сервер для аутентификации
        _socket?.emit("authentication", _shared?.getString(ConfigStorage.USERS_DATA, null))

        // Обработка обратного сообщения от сервера о успешной аутентификации
        _socket?.on("authentication_success"){
            _socket?.emit("status")
        }

        // Приём сведений о статусе пользователя
        _socket?.on("status_on"){ args ->
            if(args[0] != null){
                val data = args[0] as String
                val gson = Gson()
                val gstat = gson.fromJson(data, GameStatusModel::class.java)
                if(gstat.judge){
                    if(!_beginJudgeState){
                        _gameStatusPlayer = ConfigStatusPlayer.JUDGE

                        CoroutineScope(Dispatchers.Main).launch {
                            _bottomNavigationView?.menu?.findItem(R.id.Team)?.setIcon(R.drawable.ic_star_ruler)

                            _tbMain?.visibility = View.GONE
                            _tbHintContainer?.visibility = View.GONE

                            _hintTextView?.text = ""
                            _tbTextView?.text = ""
                            _tbTextViewCentral?.text = ""
                        }
                    }
                    _beginJudgeState = true

                    if(args[1] != null){
                        val dataStatus = args[1] as String
                        _judgeData = gson.fromJson(dataStatus, JudgeInfoModel::class.java)
                    }
                }else if(gstat.player){
                    _gameStatusPlayer = ConfigStatusPlayer.PLAYER_ACTIVE

                    if(_beginJudgeState){
                        _bottomNavigationView?.menu?.findItem(R.id.Team)?.setIcon(R.drawable.ic_team)
                    }
                    _beginJudgeState = false
                    if(args[1] != null){
                        val data = gson.fromJson((args[1] as String), GameCurrentQuestModel::class.java)
                        _currentQuestData = data
                        CoroutineScope(Dispatchers.Main).launch {
                            if(gstat.playerStatus.toByte() == ConfigStatusPlayer.PLAYER_ACTIVE_VIDEO){
                                // Данный игрок может вести съёмку видео (снимает пока не закончит съёмку)
                                if(_refMediaInstructions != null){
                                    var intent = Intent(this@MainActivity, VideoActivity::class.java)
                                    intent.putExtra("player_status", ConfigStatusPlayer.PLAYER_ACTIVE_VIDEO)
                                    intent.putExtra("game_info", gson.toJson(_currentQuestData))
                                    intent.putExtra("user_data", _shared?.getString(ConfigStorage.USERS_DATA, null))
                                    intent.putExtra("ref_media", gson.toJson(_refMediaInstructions))
                                    _socket?.off("status_on")
                                    startActivity(intent)
                                }
                            }else{
                                _tbMain?.visibility = View.VISIBLE
                                _tbHintContainer?.visibility = View.VISIBLE

                                _hintTextView?.text = data.hint
                                _tbTextView?.text = data.task
                                _tbTextViewCentral?.text = "№" + data.number
                            }
                        }
                    }
                }else{
                    _gameStatusPlayer = ConfigStatusPlayer.PLAYER_DEFAULT

                    if(_beginJudgeState){
                        CoroutineScope(Dispatchers.Main).launch {
                            _bottomNavigationView?.menu?.findItem(R.id.Team)?.setIcon(R.drawable.ic_team)
                        }
                    }

                    _beginJudgeState = false
                    CoroutineScope(Dispatchers.Main).launch {
                        _tbMain?.visibility = View.GONE
                        _tbHintContainer?.visibility = View.GONE

                        _hintTextView?.text = ""
                        _tbTextView?.text = ""
                        _tbTextViewCentral?.text = ""
                    }
                }
            }else{
                _tbMain?.visibility = View.GONE
                _tbHintContainer?.visibility = View.GONE
            }
        }

        _socket?.on("command_status_on"){ args ->
            if((args != null) && (args.isNotEmpty()) && (args[0] != null)){
                val data = args[0] as String
                val gson = Gson()
                val result = gson.fromJson(data, CommandStatusModel::class.java)
                val gstat = result.status.toByte()

                CoroutineScope(Dispatchers.Main).launch {
                    _commandInfo = CommandIdModel(
                        commandsId = result.commandsId
                    )
                }

                if(gstat != _commandStatusPlayer){
                    _commandStatusPlayer = if(gstat > 2){
                        ConfigStatusCommand.WITHOUT_TEAM
                    }else{
                        gstat
                    }
                }
            }
        }

        // Обработка обратного сообщения от сервера о не успешной аутентификации
        _socket?.on("authentication_failed"){
            CoroutineScope(Dispatchers.Main).launch {
                // CustomToast.makeText(this@MainActivity, "Возникла непредвиденная ошибка!").show()
            }
        }

        _socket?.on("game_over"){
            CoroutineScope(Dispatchers.Main).launch {
                CustomToast.makeText(this@MainActivity, "Игра пройдена!").show()
            }
        }

        _socket?.on("load_media_instructions"){
            if((!_loadMediaInstructions)
                && (_currentQuestData != null)){
                _loadMediaInstructions = true

                if(_shared!!.contains(ConfigStorage.USERS_DATA)){
                    val localData = _shared?.getString(ConfigStorage.USERS_DATA, null);
                    val retrofit = Retrofit.Builder()
                        .baseUrl(ConfigAddresses.SERVER_CENTRAL_ADDRESS)
                        .build()

                    var gson = Gson()
                    var dataLocal: UserDataModel? = gson.fromJson(localData, UserDataModel::class.java)
                    val service = retrofit.create(APIService::class.java)

                    var dataAccessPlayer = gson.toJson(
                        GameQuestIdModel(
                            usersId = dataLocal!!.usersId,
                            accessToken = dataLocal.accessToken,
                            questsId = _currentQuestData!!.questsId
                        )
                    )

                    val requestBody = dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull())

                    CoroutineScope(Dispatchers.IO).launch {
                        var responseBody = service.funPlayerCommandCurrentMedia(requestBody)
                        withContext(Dispatchers.Main) {
                            if (responseBody.isSuccessful) {
                                val result = gson.toJson(
                                    JsonParser.parseString(responseBody.body()?.string())
                                )

                                val error = gson.fromJson(result, ErrorDataModel::class.java)

                                // Проверка валидности данных
                                if((error.errors == null) && (error.message == null)
                                ){
                                    _refMediaInstructions = gson.fromJson(result, GameRefMediaModel::class.java)
                                    downloadFile(_refMediaInstructions!!.refMedia, dataLocal.accessToken)
                                }
                            }
                        }
                    }
                }
            }
        }

        _socket?.on("not_load_media_instructions"){
            CoroutineScope(Dispatchers.Main).launch{
                _loadMediaInstructions = false
                _cameraView?.setBackgroundResource(R.drawable.ic_camera_none)
                _cameraView?.setOnClickListener {}
            }
        }

        // В данной корутине происходит следующее:
        // сокет отправляет запрос о текущем статусе данного игрока
        // обработка данных идёт чуть выше, обрабатываемое событие - status_on
        _updateStatusUser = CoroutineScope(Dispatchers.IO).launch {
            try{
                while(true){
                    if(_viewMainFragment){
                        _socket?.emit("status")
                        _socket?.emit("command_status")
                    }
                    delay(1000)
                }
            }catch(e: CancellationException){}
        }
    }

    // Разрешение на использование хранение данных на устройстве
    private fun storagePermissions(){
        val readStoragePermissionState =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writeStoragePermissionState =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val externalStoragePermissionGranted =
            readStoragePermissionState == PackageManager.PERMISSION_DENIED ||
                    writeStoragePermissionState == PackageManager.PERMISSION_DENIED

        if (!externalStoragePermissionGranted) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                VideoActivity.REQUEST_EXTERNAL_STORAGE_CODE
            )
        }
    }

    override fun onBackPressed() {
        _tbMain?.visibility = View.GONE
        _tbHintContainer?.visibility = View.GONE

        _currentFragment = _commonContainer?.childFragmentManager?.primaryNavigationFragment

        _tbMain?.visibility = View.GONE
        _tbHintContainer?.visibility = View.GONE
        _viewMainFragment = false

        when(_currentFragment){
            is EmptyFragment->{
                super.onBackPressed()
            }

            is ChatFragment->{
                //_commonController?.navigate(R.id.action_chatsFragment_to_messengerFragment)
            }

            is FindTeamFragment->{
                _commonController?.navigate(R.id.action_teamFragment_to_emptyFragment)
                _viewMainFragment = true
                _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
            }

            is PlayerExistsTeamFragment->{
                _commonController?.navigate(R.id.action_playerInExistingTeamFragment_to_emptyFragment)
                _viewMainFragment = true
                _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
            }

            is CreateTeamFragment->{
                _commonController?.navigate(R.id.action_inTeamFragment_to_teamFragment)
            }

            is FindPlayerFragment->{
                if(_currentFragment?.requireArguments()?.getString("arg1") == "FromTeamBoss") {
                    _commonController?.navigate(R.id.action_findPlayerFragment_to_teamBossFragment)
                }else{
                    _commonController?.navigate(R.id.action_findPlayerFragment_to_inTeamFragment)
                }
            }

            is MessengerFragment->{
              //  _commonController!!.navigate(R.id.action_chatFragment_to_emptyFragment)
                _viewMainFragment = true
                _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
            }

            is CurrentPlayerProfileFragment->{
                _commonController?.navigate(R.id.action_playerInformationFragment_to_emptyFragment)
                _viewMainFragment = true
                _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
            }

            is com.game.mobileappar.fragment.team.PlayerProfileFragment ->{
                if(_currentFragment?.requireArguments()?.getString("arg1")!=null){
                   when(_currentFragment?.requireArguments()?.getString("arg1")){
                       "ExistingTeamFragment"->{ // аргумент из фрагмента команды, обычный игрок, не создатель
                           _commonController?.navigate(R.id.action_playerProfileInTeamFragment_to_playerInExistingTeamFragment)
                           _bottomNavigationView?.visibility = View.VISIBLE
                       }
                       "TeamCreatorFragment"->{ // аргумент из фрагмента создателя команды
                           _commonController?.navigate(R.id.action_playerProfileInTeamFragment_to_teamBossFragment)
                           _bottomNavigationView?.visibility = View.VISIBLE
                       }
                       "ChatNoCreator"->{
                        //   _commonController?.navigate(R.id.action_playerProfileInTeamFragment_to_chatInfoFragment)
                       }
                       "ChatIsCreator"->{
                       //    _commonController?.navigate(R.id.action_playerProfileInTeamFragment_to_chatInfoFragment)
                       }
                   }
                }

            }

            is CreatorTeamFragment->{
                _commonController?.navigate(R.id.action_teamBossFragment_to_emptyFragment)
                _viewMainFragment = true
                _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
            }

            is RulerRateFragment->{
                _commonController?.navigate(R.id.action_rulerRateFragment_to_emptyFragment)
                _viewMainFragment = true
                _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
            }

            is PlayerSettingsFragment ->{
                _commonController?.navigate(R.id.action_playerSettingsFragment_to_playerInformationFragment)
            }

            is ProfileSettingsAccountFragment ->{
                _commonController?.navigate(R.id.action_profileSettingsAccountFragment_to_playerSettingsFragment)
            }

            is CreatorTeamFragment ->{
                _commonController?.navigate(R.id.action_teamBossFragment_to_emptyFragment)
                _viewMainFragment = true
                _bottomNavigationView?.visibility = BottomNavigationView.VISIBLE
            }

            is CreatorFindGameFragment ->{
                _commonController?.navigate(R.id.action_findGameCreatorFragment_to_teamBossFragment)
            }
            is CreatorRegisterGameFragment ->{
                _commonController?.navigate(R.id.action_regOnGameFragment_to_findGameCreatorFragment)
            }

            is ChatInfoFragment ->{
             //   _commonController?.navigate(R.id.action_chatInfoFragment_to_chatsFragment)
            }
        }


        return Unit
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

    override fun onRestart() {
        super.onRestart()

        _socket?.on("status_on"){ args ->
            if(args[0] != null){
                val data = args[0] as String
                val gson = Gson()
                val gstat = gson.fromJson(data, GameStatusModel::class.java)

                if(gstat.judge){
                    if(!_beginJudgeState){
                        _gameStatusPlayer = ConfigStatusPlayer.JUDGE
                        _bottomNavigationView?.menu?.findItem(R.id.Team)?.setIcon(R.drawable.ic_star_ruler)
                    }
                    _beginJudgeState = true
                }else if(gstat.player){
                    _gameStatusPlayer = ConfigStatusPlayer.PLAYER_ACTIVE

                    if(_beginJudgeState){
                        _bottomNavigationView?.menu?.findItem(R.id.Team)?.setIcon(R.drawable.ic_team)
                    }
                    _beginJudgeState = false
                    if(args[1] != null){
                        val data = gson.fromJson((args[1] as String), GameCurrentQuestModel::class.java)
                        _currentQuestData = data
                        CoroutineScope(Dispatchers.Main).launch {
                            if(gstat.playerStatus.toByte() == ConfigStatusPlayer.PLAYER_ACTIVE_VIDEO){
                                // Данный игрок может вести съёмку видео (снимает пока не закончит съёмку)
                                if(_refMediaInstructions != null){
                                    var intent = Intent(this@MainActivity, VideoActivity::class.java)
                                    intent.putExtra("player_status", ConfigStatusPlayer.PLAYER_ACTIVE_VIDEO)
                                    intent.putExtra("game_info", gson.toJson(_currentQuestData))
                                    intent.putExtra("user_data", _shared?.getString(ConfigStorage.USERS_DATA, null))
                                    intent.putExtra("ref_media", gson.toJson(_refMediaInstructions))
                                    _socket?.off("status_on")
                                    startActivity(intent)
                                }
                            }else{
                                _tbMain?.visibility = View.VISIBLE
                                _tbHintContainer?.visibility = View.VISIBLE

                                _hintTextView?.text = data.hint
                                _tbTextView?.text = data.task
                                _tbTextViewCentral?.text = "№" + data.number
                            }
                        }
                    }
                }else{
                    _gameStatusPlayer = ConfigStatusPlayer.PLAYER_DEFAULT

                    if(_beginJudgeState){
                        _bottomNavigationView?.menu?.findItem(R.id.Team)?.setIcon(R.drawable.ic_team)
                    }

                    _beginJudgeState = false
                    CoroutineScope(Dispatchers.Main).launch {
                        _tbMain?.visibility = View.GONE
                        _tbHintContainer?.visibility = View.GONE

                        _hintTextView?.text = ""
                        _tbTextView?.text = ""
                        _tbTextViewCentral?.text = ""
                    }
                }
            }else{
                _tbMain?.visibility = View.GONE
                _tbHintContainer?.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Остановка сервиса уведомлений
        stopService(Intent(this, NotificationService::class.java))

        // Отключение сокетов от сервера
        _socket?.off()
        _socket?.disconnect()
        SCSocketHandler.closeConnection()
        _updateStatusUser?.cancel()
    }

    private fun downloadFile(refMedia: String, accessToken: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(ConfigAddresses.SERVER_MEDIA_ADDRESS)
            .build()
        val service = retrofit.create(APIService::class.java)

        val gson = Gson()
        var videoData = gson.toJson(
            GamePathMediaModel(
                localPath = refMedia,
                accessToken = accessToken
            )
        )

        val videoRequestBody = videoData.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            var responseBody = service.mediaInstructionsDownload(videoRequestBody)
            if (responseBody.isSuccessful) {
                writeResponseBodyToStorage(responseBody.body()!!,
                    responseBody.headers()["filename"].toString()
                )
            }
        }
    }

    // Загрузка файла на мобильное устройство
    private fun writeResponseBodyToStorage(body: ResponseBody, filename: String): Boolean {
        return try {
            val filePath =
                File(getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString() + File.separator
                        + filename + ".mp4")

            _currentMediaLocalPath =
                getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString() + File.separator + filename + ".mp4"
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)
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
                    // Файл загружен, можно поменять определённые элементы интерфейса
                    _cameraView?.setBackgroundResource(R.drawable.ic_media)
                    _cameraView?.setOnClickListener {
                        var intent = Intent(this@MainActivity, VideoActivity::class.java)
                        intent.putExtra("player_status", ConfigStatusPlayer.PLAYER_ACTIVE)
                        intent.putExtra("file_path", _currentMediaLocalPath)

                        startActivity(intent)
                    }
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