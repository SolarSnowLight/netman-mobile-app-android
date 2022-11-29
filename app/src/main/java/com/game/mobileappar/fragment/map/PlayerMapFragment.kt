package com.game.mobileappar.fragment.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.game.mobileappar.R
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.models.ViewCurrentQuestModel
import com.game.mobileappar.network.handler.SCSocketHandler
import com.game.mobileappar.models.game.GamePlayerCoordinatesModel
import com.game.mobileappar.models.user.UserDataModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.coroutines.*

class PlayerMapFragment : Fragment() {
    private var _mapFragment: SupportMapFragment? = null   // Ссылка на фрагмент карты
    private var _shared: SharedPreferences? = null         // Ссылка на локальное хранилище
    private var _socket: Socket? = null                    // Сокет для взаимодействия с сервером
    private var _coroutine: Job? = null
    private var _coroutineGetCoordinates: Job? = null
    private var _firstUpdate = true
    private var _googleMap : GoogleMap? = null

    // Информация о текущем игроке
    private var _currentPlayerMarker : Marker? = null
    private var _currentPlayerCircle : Circle? = null

    // Информация о текущей игре
    private var _currentGameMarker : Marker? = null
    private var _currentGameCircle : Circle? = null

    // Информация о всех игроках, которые находятся
    // в одной команде с текущим игроком
    private var _teamPlayers: MutableMap<Int?, PlayerMapFragment.DataMapInfo>? = mutableMapOf()

    private var _cameraPosition: CameraPosition? = null
    private lateinit var _fusedLocationProviderClient: FusedLocationProviderClient
    private var _locationPermissionGranted = true
    private var _lastKnownLocation: Location? = null

    private val DEFAULT_ZOOM = 15
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"

    companion object CurrentLocation{
        var lat: Double = 52.28200359470799 //52.28345729824867 //52.28645729824869 //52.28345729824867
        var lng: Double = 104.28500876197288 //104.2850717672249 //104.28507176722498
    }

    // Классы для взаимодействия с данными
    private data class DataMapInfo(
        var marker: Marker? = null,
        var circle: Circle? = null
    )

    // Функция для установки конфигурации игровой карты,
    // вызывается несколько раз (в зависимости от параметров текущей локации)
    private val callback = OnMapReadyCallback { googleMap ->
        _googleMap = googleMap
        _googleMap?.setMapStyle(
            this.context?.let {
                MapStyleOptions.loadRawResourceStyle(
                    it, R.raw.style_map)
            })

        _currentPlayerMarker = _googleMap?.addMarker(MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_netman_black_low))
            .position(LatLng(CurrentLocation.lat - 0.00085, CurrentLocation.lng + 0.0001)))
        _currentPlayerCircle = _googleMap?.addCircle(CircleOptions()
            .center(LatLng(CurrentLocation.lat, CurrentLocation.lng))
            .radius(110.0)
            .strokeWidth(4F)
            .strokeColor(Color.BLUE)
            .fillColor(0x400000FF))

        _googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(CurrentLocation.lat,
                CurrentLocation.lng), DEFAULT_ZOOM.toFloat()))

        //установка максимального и минимального зума
        googleMap.setMinZoomPreference(15.0f)
        googleMap.setMaxZoomPreference(20.0f)
        _googleMap?.uiSettings?.isMyLocationButtonEnabled = true

        //getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _shared = this.requireActivity()
            .getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE);

        // Запуск основной корутины
        _coroutine = CoroutineScope(Dispatchers.IO).launch {
            try{
                _socket = SCSocketHandler.getSocket();
                while((_socket == null) || (_socket?.isActive == false)){   // Ожидание подключения сокета
                    _socket = SCSocketHandler.getSocket();
                }

                var gson = Gson();
                var userData = gson.fromJson(_shared?.getString(ConfigStorage.USERS_DATA, null), UserDataModel::class.java);

                // Обработка события "передать координаты другим членам команды"
                _socket?.on("get_player_coordinates"){
                    _socket?.emit("set_player_coordinates", gson.toJson(
                        GamePlayerCoordinatesModel(
                            lat = lat,
                            lng = lng,
                            usersId = userData.usersId
                        )
                    ))
                }

                _socket?.on("clear_games_marks"){
                    CoroutineScope(Dispatchers.Main).launch {
                        if(_currentGameMarker != null){
                            _currentGameMarker?.remove()
                            _currentGameMarker = null
                        }

                        if(_currentGameCircle != null){
                            _currentGameCircle?.remove()
                            _currentGameCircle = null
                        }
                    }
                }

                // Обработка события "получить координаты от других членов команды"
                _socket?.on("add_player_coordinates"){ args ->
                    if(args[0] != null){
                        var data = gson.fromJson((args[0] as String), GamePlayerCoordinatesModel::class.java);
                        CoroutineScope(Dispatchers.Main).launch {
                            if(!(_teamPlayers!!.contains(data.usersId))){
                                _teamPlayers?.put(
                                    key = data.usersId,
                                    PlayerMapFragment.DataMapInfo(
                                        marker = _googleMap?.addMarker(MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_netman_black_low))
                                            .position(LatLng(data.lat - 0.00085, data.lng + 0.0001))),
                                        circle = _googleMap?.addCircle(CircleOptions()
                                            .center(LatLng(data.lat, data.lng))
                                            .radius(110.0)
                                            .strokeWidth(4F)
                                            .strokeColor(Color.BLUE)
                                            .fillColor(Color.YELLOW)
                                        )
                                    )
                                );
                            }else{
                                var value = _teamPlayers?.get(data.usersId);
                                value?.marker?.position = LatLng(data.lat - 0.00085, data.lng + 0.0001);
                                value?.circle?.center = LatLng(data.lat, data.lng);
                            }
                        }
                    }
                }

                // Установка новых координат
                _socket?.on("set_my_coordinates"){ args ->
                    if(args[0] != null){
                        val obj = gson.fromJson((args[0] as String), GamePlayerCoordinatesModel::class.java);

                        // Установка новых координат игрока, полученных из БД
                        CurrentLocation.lat = obj.lat;
                        CurrentLocation.lng = obj.lng;

                        CoroutineScope(Dispatchers.Main).launch {
                            if(_currentPlayerMarker == null){
                                _currentPlayerMarker = _googleMap?.addMarker(MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_netman_black_low))
                                    .position(LatLng(CurrentLocation.lat - 0.00085, CurrentLocation.lng + 0.0001)));
                            }

                            if(_currentPlayerCircle == null){
                                _currentPlayerCircle = _googleMap?.addCircle(CircleOptions()
                                    .center(LatLng(CurrentLocation.lat, CurrentLocation.lng))
                                    .radius(110.0)
                                    .strokeWidth(4F)
                                    .strokeColor(Color.BLUE)
                                    .fillColor(0x400000FF));
                            }

                            _currentPlayerMarker?.position = LatLng(CurrentLocation.lat - 0.00085,
                                CurrentLocation.lng + 0.0001);
                            _currentPlayerCircle?.center = LatLng(CurrentLocation.lat,
                                CurrentLocation.lng);
                        }
                    }
                }

                // Установка видимого текущего квеста
                _socket?.on("set_view_current_quest"){ args ->
                    if((args[0] != null) && ((_currentGameMarker == null) || (_currentGameCircle == null))){
                        val data = gson.fromJson((args[0] as String), ViewCurrentQuestModel::class.java);

                        CoroutineScope(Dispatchers.Main).launch {
                            if(_currentGameMarker == null){
                                _currentGameMarker = _googleMap?.addMarker(MarkerOptions()
                                    .position(LatLng(data.lat, data.lng)));
                            }else{
                                _currentGameMarker?.position = LatLng(data.lat, data.lng)
                            }

                            if(_currentGameCircle == null){
                                _currentGameCircle =_googleMap?.addCircle(CircleOptions()
                                    .center(LatLng(data.lat, data.lng))
                                    .radius(data.radius.toDouble())
                                    .strokeWidth(4F)
                                    .strokeColor(Color.BLACK)
                                    .fillColor(Color.GREEN))
                            }else{
                                _currentGameCircle?.center = LatLng(data.lat, data.lng)
                            }
                        }
                    }
                }

                _socket?.on("clear_games_marks"){ args -> {
                    CoroutineScope(Dispatchers.Main).launch{
                        _currentGameCircle = null
                        _currentGameCircle = null
                    }
                }}

                // Удаление координат игроков, которые отключились от игрового процесса
                _socket?.on("team_player_disconnect"){ args ->
                    if(args[0] != null){
                        val obj = gson.fromJson((args[0] as String), GamePlayerCoordinatesModel::class.java);

                        CoroutineScope(Dispatchers.Main).launch {
                            if(_teamPlayers!!.containsKey(obj.usersId)){
                                val value = _teamPlayers?.get(obj.usersId);
                                value?.marker?.remove();
                                value?.circle?.remove();
                                _teamPlayers?.remove(obj.usersId);
                            }
                        }
                    }
                }

                _coroutineGetCoordinates = CoroutineScope(Dispatchers.IO).launch {
                    try{
                        while(true){
                            /*CoroutineScope(Dispatchers.Main).launch {
                                //_googleMap?.clear();
                            }*/

                            // Запрос координат игроков (обработка данных событий осуществляется в основной корутине)
                            // Информацию обо всех игроках имеет смысл брать только тогда, когда началась игра
                            /*if(_socket?.hasListeners("is_this_player") == true){
                                _socket?.emit("coordinates_players");
                            }*/

                            _socket?.emit("coordinates_players");

                            // Обновление координат игрока
                            getDeviceLocation()

                            //_socket?.emit("get_my_coordinates");

                            delay(500)

                            // Отправить текущие координаты на сервер
                            _socket?.emit("set_current_coordinates",
                                gson.toJson(
                                    GamePlayerCoordinatesModel(
                                        lat = lat + 0.00005,
                                        lng = lng,
                                        usersId = userData.usersId
                                    )
                                )
                            )

                            delay(500)    // Каждые 2 секунды обновляем информацию о координатах игроков в команде

                            /* Тестовый временный код */
                            //_socket?.emit("get_my_coordinates");    // Получить координаты текущего пользователя из БД
                        }
                    }catch (e: CancellationException){}
                }
                _coroutineGetCoordinates?.join(); // Ожидание выполнения корутины
            }catch (e: CancellationException){}
        }

        return inflater.inflate(R.layout.game_interface_player_map_fragment, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        _googleMap?.let { _googleMap ->
            outState.putParcelable(KEY_CAMERA_POSITION, _googleMap.cameraPosition)
            outState.putParcelable(KEY_LOCATION, _lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation(){
        try{
            if(_locationPermissionGranted){
                val locationResult = _fusedLocationProviderClient.lastLocation
                locationResult?.addOnCompleteListener(requireActivity()){ task ->
                    if(task.isSuccessful){
                        _lastKnownLocation = task.result;
                        if(_lastKnownLocation != null){
                           if(_firstUpdate){
                               _googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                   LatLng(_lastKnownLocation!!.latitude,
                                       _lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()));
                               _firstUpdate = false;
                           }
                            CurrentLocation.lat = _lastKnownLocation!!.latitude;
                            CurrentLocation.lng = _lastKnownLocation!!.longitude;
                            _currentPlayerMarker?.position = LatLng(_lastKnownLocation!!.latitude,
                                _lastKnownLocation!!.longitude);
                            _currentPlayerCircle?.center = LatLng(_lastKnownLocation!!.latitude,
                                _lastKnownLocation!!.longitude);
                        }else{
                            /*_googleMap?.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(
                                    LatLng(
                                    CurrentLocation.lat,
                                    CurrentLocation.lng
                                ), DEFAULT_ZOOM.toFloat()))*/
                            //_googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                        }
                    }
                }
            }
        }catch (e: Exception){}
    }

    private fun getLocationPermission(){
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            _locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        _locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (_googleMap == null) {
            return
        }
        try {
            if (_locationPermissionGranted) {
                _googleMap?.isMyLocationEnabled = true
                _googleMap?.uiSettings?.isMyLocationButtonEnabled = true
                _googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(_lastKnownLocation!!.latitude,
                        _lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
            } else {
                _googleMap?.isMyLocationEnabled = false
                //_googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                _lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: Exception){}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null){
            _lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            _cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        _fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext);

        _mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?;
        _mapFragment?.getMapAsync(callback);

        /*CoroutineScope(Dispatchers.Main).launch {
            try{
                // Ожидание получения корректных данных
                while((CurrentLocation.lat == 0.0) && (CurrentLocation.lng == 0.0)){ }
                _mapFragment?.getMapAsync(callback);
            }catch(e: CancellationException){}
        }*/

        /*TransportDataGET(   //получение данных обо всех актуальных играх
            "http://10.0.2.2:5000",
            _shared!!,
            _mapFragment!!,
            callback,
            getString(R.string.task_marks)
        ).execute(_shared?.getString(getString(R.string.users_data), null));*/
    }

    override fun onDestroy() {
        super.onDestroy();
        _coroutineGetCoordinates?.cancel();
        _coroutine?.cancel();
    }

    override fun onResume() {
        super.onResume()

        /*TransportDataGET(   //обновление данных обо всех актуальных играх
            "http://10.0.2.2:5000",
            _shared!!,
            _mapFragment!!,
            callback,
            getString(R.string.task_marks)
        ).execute(_shared?.getString(getString(R.string.users_data), null));*/
    }

    /* Получение данных с сервера
    class TransportDataGET(
        urlStr: String, localStorage: SharedPreferences, mapFragment: SupportMapFragment,
        fCallback: OnMapReadyCallback, taskMarks: String
    ) : AsyncTask<String, String, String>() {
        private var _urlAddress = urlStr;           //базовый адрес для отправки запроса
        private var _localStorage = localStorage;   //ссылка на локальное хранилище
        private var _mapFragment = mapFragment;     //ссылка на фрагмент
        private var _callback = fCallback;          //ссылка на функцию для вызова
        private var _taskMarks = taskMarks;         //ссылка на область в хранилище, для хранения информации о заданиях

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result);
            _mapFragment?.getMapAsync(_callback);   //связка фрагмента карты с функцией обратного вызова
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun doInBackground(vararg params: String?): String? {
            val retrofit = Retrofit.Builder()
                .baseUrl(_urlAddress)
                .build(); //создание экземпляра объекта retrofit, для передачи данных по сети

            val service =
                retrofit.create(APIService::class.java);  //создание сервиса под определённый запрос
            var data: String? = params[0];                //инициализация данных, для передачи на сервер

            var gson = Gson();                            //создание объекта Gson, для сериализации и десереализации данных
            var dataUser =
                gson.fromJson<AuthDataJSON>(data, AuthDataJSON::class.java);   //десериализация данных

            CoroutineScope(Dispatchers.IO).launch {
                val response = service.funPlayerGames(
                    dataUser.token,
                    dataUser.usersEmail
                );

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {              //обработка ответа из сервера
                        var prettyJson = gson.toJson(
                            JsonParser.parseString(response.body()?.string())
                        );  //получение данных с сервера в формате JSON-строки

                        val error = gson.fromJson(
                            prettyJson,
                            ErrorJSON::class.java
                        ); //десериализация данных

                        //проверка на корректные значения данных
                        if ((error.errors == null) && (error.message == null)) {
                            var editor =
                                _localStorage.edit();  //открытие возможности изменения локального хранилища

                            editor.putString(
                                _taskMarks,
                                prettyJson
                            );

                            //принятие изменений
                            editor.apply();
                        }

                        //TODO: обработка ошибок с сервера
                        /*Handler(Looper.getMainLooper()).post {
                            if(error.errors != null){
                                Toast.makeText(_context, error.errors[0].msg, Toast.LENGTH_LONG).show();
                            }else if(error.message != null){
                                Toast.makeText(_context, error.message, Toast.LENGTH_LONG).show();
                            }
                        }*/
                    }
                }
            }

            return "";
        }
    }*/
}