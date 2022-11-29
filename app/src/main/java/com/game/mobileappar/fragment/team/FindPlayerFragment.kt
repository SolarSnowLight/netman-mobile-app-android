package com.game.mobileappar.fragment.team

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.game.mobileappar.R
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.MainActivity
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.command.SearchingAdapter
import android.app.Activity
import android.content.Context
import android.view.WindowManager
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.network.service.APIService
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


import android.view.View.OnTouchListener
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.player.search.command.PlayerSearchAccessModel
import com.game.mobileappar.models.player.search.command.PlayerSearchCommandModel
import kotlinx.coroutines.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FindPlayerFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var _updateAdapter: Job
    private var _isChange: Boolean = false

    private var _adapter: SearchingAdapter? = null

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
    ): View? {
        var commandsId = if((arguments != null) && (requireArguments().containsKey("commands_id"))){
            requireArguments().getInt("commands_id")
        }else{
            null
        }

        var shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        var gson = Gson()
        val view:View = inflater.inflate(R.layout.teamt_find_player_fragmen, container, false)
        val recyclerView: RecyclerView? = view.findViewById(R.id.rv_findPLayer)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        _adapter = view.context?.let { SearchingAdapter(it) }

        if(shared!!.contains(ConfigStorage.USERS_DATA)){
            var dataLocal: UserDataModel? =
                gson.fromJson(shared.getString(ConfigStorage.USERS_DATA, null), UserDataModel::class.java);

            _adapter!!.setUserData(
                PlayerAccessModel(
                    usersId = dataLocal!!.usersId,
                    accessToken = dataLocal!!.accessToken
                )
            )

            if (commandsId != null) {
                _adapter!!.setCommandsId(commandsId)
            }
        }

        recyclerView?.adapter = _adapter
        recyclerView?.itemAnimator = DefaultItemAnimator()

        var searchView = view.findViewById(R.id.search) as androidx.appcompat.widget.SearchView

        searchView.isIconified = false
        searchView.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
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
                            val retrofit = AppMainHandler.getRetrofit()

                            var dataLocal: UserDataModel? =
                                gson.fromJson(localData, UserDataModel::class.java)
                            val service = retrofit.create(APIService::class.java)

                            var dataAccessPlayer = gson.toJson(
                                PlayerSearchAccessModel(
                                    usersId = dataLocal!!.usersId,
                                    accessToken = dataLocal!!.accessToken,
                                    tag = searchView.query.toString()
                                )
                            )

                            val requestBody =
                                dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull())

                            CoroutineScope(Dispatchers.IO).launch {
                                var responseBody = service.funSearchFreePlayerTag(requestBody)
                                withContext(Dispatchers.Main) {
                                    if (responseBody.isSuccessful) {
                                        val result = gson.toJson(
                                            JsonParser.parseString(responseBody.body()?.string())
                                        )

                                        val error = gson.fromJson(result, ErrorDataModel::class.java)

                                        // Проверка валидности данных
                                        if ((error.errors == null) && (error.message == null)) {
                                            // Добавление данных
                                            var data = gson.fromJson(result, PlayerSearchCommandModel::class.java)
                                            _adapter?.setData(data)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }catch (e: Exception){}
        }

        recyclerView?.isClickable = true
        view.findViewById<ConstraintLayout>(R.id.cl_findPlayer).isClickable = true

        recyclerView?.setOnTouchListener(OnTouchListener { v, event ->
            val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager;
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            false
        })


        view.findViewById<Toolbar>(R.id.toolbar7).setNavigationOnClickListener {
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager;
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            (activity as MainActivity).onBackPressed();
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            /**
            Срабатывает, когда пользователь нажимает кнопку отправки
             */
            override fun onQueryTextSubmit(query: String): Boolean {
                //searchView.setIconified(false);
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


//        val searchEditText =
//            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
//        searchEditText.setTextColor(resources.getColor(R.color.colorPrimary))
//        searchEditText.setHintTextColor(resources.getColor(R.color.colorPrimary))
        return view;
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FindPlayerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FindPlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onPause() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager;
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _updateAdapter.cancel()
    }
}


