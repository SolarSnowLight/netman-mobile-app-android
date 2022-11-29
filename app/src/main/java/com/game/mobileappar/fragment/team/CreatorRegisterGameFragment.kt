package com.game.mobileappar.fragment.team

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.game.GameRegisterModel
import com.game.mobileappar.models.game.list.GameListElementModel
import com.game.mobileappar.network.service.APIService
import com.game.mobileappar.components.toast.CustomToast
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreatorRegisterGameFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var _gameInfo: GameListElementModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(requireArguments().containsKey("selected_game")){
            var gson = Gson()
            _gameInfo = gson.fromJson(requireArguments().getString("selected_game"), GameListElementModel::class.java)
        }
        var shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
        val view = inflater.inflate(R.layout.team_reg_on_game_fragment, container, false);

        view.findViewById<TextView>(R.id.tv_teamName_RegOnGame).text = _gameInfo.name
        view.findViewById<TextView>(R.id.tv_ageTeam_RegOnGame).text = _gameInfo.ageLimit.toString() + "+"
        view.findViewById<TextView>(R.id.tv_city_team_RegOnGame).text = _gameInfo.location
        view.findViewById<TextView>(R.id.tv_pointerCount_RegOnGame).text = "Количество меток: " + _gameInfo.countQuests.toString()

        val dateBeginString = _gameInfo.dateBegin
            ?.split("T")[0]
            ?.split('-')
        val strBeginDate = dateBeginString[2] + "." + dateBeginString[1]

        val dateEndString = _gameInfo.dateEnd
            ?.split("T")[0]
            ?.split('-')
        val strEndDate = dateEndString[2] + "." + dateEndString[1]

        view.findViewById<TextView>(R.id.tv_time_choiceTem).text = "$strBeginDate - $strEndDate"

        view.findViewById<Button>(R.id.button).setOnClickListener{
            if(shared!!.contains(ConfigStorage.USERS_DATA)) {
                val localData = shared.getString(ConfigStorage.USERS_DATA, null);
                val retrofit = AppMainHandler.getRetrofit()

                var gson = Gson();
                var dataLocal: UserDataModel? =
                    gson.fromJson(localData, UserDataModel::class.java);
                val service = retrofit.create(APIService::class.java);

                var dataAccessPlayer = gson.toJson(
                    GameRegisterModel(
                        usersId = dataLocal!!.usersId,
                        accessToken = dataLocal!!.accessToken,
                        infoGamesId = _gameInfo.id
                    )
                );

                val requestBody =
                    dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull());

                CoroutineScope(Dispatchers.IO).launch {
                    var responseBody = service.funCommandRegisterGame(requestBody);
                    withContext(Dispatchers.Main) {
                        if (responseBody.isSuccessful) {
                            val result = gson.toJson(
                                JsonParser.parseString(responseBody.body()?.string())
                            );

                            val error = gson.fromJson(result, ErrorDataModel::class.java)

                            // Проверка валидности данных
                            if ((error.errors == null) && (error.message == null)) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    CustomToast.makeText(requireContext(), "Регистрация на игру прошла успешно!").show()
                                    findNavController().navigate(R.id.action_regOnGameFragment_to_teamBossFragment)
                                }
                            }else{
                                CoroutineScope(Dispatchers.Main).launch {
                                    if((error.errors != null) && (error.errors!!.isNotEmpty())){
                                        CustomToast.makeText(requireContext(), error.errors!![0].msg).show()
                                    }
                                    CustomToast.makeText(requireContext(), error.message).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        view.findViewById<Toolbar>(R.id.toolBar_OnGameReg).setNavigationOnClickListener {
            (activity as MainActivity).onBackPressed();
        }

        return view;
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreatorRegisterGameFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}