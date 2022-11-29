package com.game.mobileappar.fragment.messenger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.messenger.AddChatPlayersAdapter
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.player.search.command.PlayerSearchAccessModel
import com.game.mobileappar.models.player.search.command.PlayerSearchCommandModel
import com.game.mobileappar.network.service.APIService
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddChatFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _adapter: AddChatPlayersAdapter? = null

    private lateinit var _updateAdapter: Job
    private var _isChange: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
        val view  = inflater.inflate(R.layout.messenger_add_chat_fragment, container, false)

        val toolbar: Toolbar? = view?.findViewById<Toolbar>(R.id.tbAddChat)?.apply {
            setNavigationOnClickListener{
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                findNavController().navigate(R.id.action_addChatFragment2_to_messengerFragment2,null)
            }
        }

        val recyclerView: RecyclerView? = view.findViewById(R.id.rvAddChatList)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        _adapter = view.context?.let { AddChatPlayersAdapter(it) }
        recyclerView?.adapter = _adapter
        recyclerView?.itemAnimator = DefaultItemAnimator()

        recyclerView?.setOnTouchListener(View.OnTouchListener { v, event ->
            val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            false
        })


        val searchView = view?.findViewById(R.id.svAddChat) as androidx.appcompat.widget.SearchView

        searchView.isIconified = false
        searchView.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            /**
            Срабатывает, когда пользователь нажимает кнопку отправки
             */
            override fun onQueryTextSubmit(query: String): Boolean {
                // searchView.setIconified(false);
                return false
            }

            /**
            Срабатывает при изменении текста
             */
            override fun onQueryTextChange(newText: String): Boolean {
                searchView.isIconified = false;

                // this will trigger each time user changes (adds or removes) text
                // so when newText is empty, restore your views
                return false
            }
        })

        searchView.setOnCloseListener {
            searchView.isIconified = false;
            true
        }

        searchView.setOnSearchClickListener{
            _isChange = true
        }

        _updateAdapter = CoroutineScope(Dispatchers.Main).launch {
            try{
                while(true){
                    delay(1000)
                    if((_isChange) && (searchView.query.toString().isNotEmpty())){
                        _isChange = false
                        // Отправка запроса на сервер и обновление данных в адаптере
                        if(shared!!.contains(ConfigStorage.USERS_DATA)) {
                            val localData = shared.getString(ConfigStorage.USERS_DATA, null)
                            val retrofit = Retrofit.Builder()
                                .baseUrl(ConfigAddresses.SERVER_CENTRAL_ADDRESS)
                                .build();

                            var gson = Gson()
                            var dataLocal: UserDataModel? =
                                gson.fromJson(localData, UserDataModel::class.java);
                            val service = retrofit.create(APIService::class.java);

                            var dataAccessPlayer = gson.toJson(
                                PlayerSearchAccessModel(
                                    usersId = dataLocal!!.usersId,
                                    accessToken = dataLocal!!.accessToken,
                                    tag = searchView.query.toString()
                                )
                            );

                            val requestBody =
                                dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull());

                            CoroutineScope(Dispatchers.IO).launch {
                                var responseBody = service.funPlayerFindCertain(requestBody);
                                withContext(Dispatchers.Main) {
                                    if (responseBody.isSuccessful) {
                                        val result = gson.toJson(
                                            JsonParser.parseString(responseBody.body()?.string())
                                        );

                                        val error = gson.fromJson(result, ErrorDataModel::class.java);

                                        // Проверка валидности данных
                                        if ((error.errors == null) && (error.message == null)) {
                                            // Добавление данных
                                            var data = gson.fromJson(result, PlayerSearchCommandModel::class.java)
                                            _adapter?.setData(data)
                                            _adapter?.setDataUser(
                                                PlayerAccessModel(
                                                    usersId = dataLocal.usersId,
                                                    accessToken = dataLocal.accessToken
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }catch (e: Exception){}
        }


        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddChatFragment().apply {
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