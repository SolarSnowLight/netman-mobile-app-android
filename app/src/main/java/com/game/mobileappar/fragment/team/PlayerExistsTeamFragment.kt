package com.game.mobileappar.fragment.team

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.game.mobileappar.adapter.game.GamesPlayersVPA
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.config.game.ConfigStatusPlayer
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.PlayerCommandDataModel
import com.game.mobileappar.models.command.CommandModel
import com.game.mobileappar.network.service.APIService
import com.game.mobileappar.components.toast.CustomToast
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.game.mobileappar.models.command.CommandIdModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PlayerExistsTeamFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _gameStatusPlayer: Byte? = null
    private var _teamInfo: CommandIdModel? = null

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
        _gameStatusPlayer = if((arguments != null) && (requireArguments().containsKey("status_player"))){
            requireArguments().getByte("status_player")
        }else{
            ConfigStatusPlayer.PLAYER_DEFAULT
        }

        _teamInfo = if((arguments != null) && (requireArguments().containsKey("commands_id"))){
            CommandIdModel(
                commandsId = requireArguments().getInt("commands_id")
            )
        }else{
            null
        }

        var shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE);
        val view: View = inflater.inflate(R.layout.team_player_in_existing_team_fragment, container, false)
        val viewPager: ViewPager = view.findViewById(R.id.viewPager3)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout3)

        // Построение вёрстки команды по передаваемым специальным параметрам
        val vpaGamesPlayers: GamesPlayersVPA = GamesPlayersVPA(childFragmentManager,
            "ExistingTeamFragment",
            "Team",
            null,
            _gameStatusPlayer!!
        )

        viewPager.adapter = vpaGamesPlayers
        tabLayout.setupWithViewPager(viewPager)

        val tbExistsTeam: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.toolbar4)

        tbExistsTeam.setNavigationOnClickListener{
            (activity as MainActivity).onBackPressed()
        }

        view.findViewById<View>(R.id.vPlusTeamBoss2).visibility = View.GONE


        view.findViewById<TextView>(R.id.tv_leaveTeam).setOnClickListener(View.OnClickListener {
            if((_gameStatusPlayer == ConfigStatusPlayer.PLAYER_ACTIVE)
                || (_gameStatusPlayer == ConfigStatusPlayer.PLAYER_ACTIVE_VIDEO)){
                CoroutineScope(Dispatchers.Main).launch {
                    CustomToast.makeText(this@PlayerExistsTeamFragment.requireContext(), "Нельзя выходить из команды" +
                            " во время игры!").show();
                }

                return@OnClickListener
            }

            if(shared!!.contains(ConfigStorage.USERS_DATA)) {
                val localData = shared.getString(ConfigStorage.USERS_DATA, null)
                val retrofit = AppMainHandler.getRetrofit()

                var gson = Gson();
                var dataLocal: UserDataModel? =
                    gson.fromJson(localData, UserDataModel::class.java)
                val service = retrofit.create(APIService::class.java)

                var dataAccessPlayer = gson.toJson(
                    PlayerCommandDataModel(
                        usersId = dataLocal!!.usersId,
                        accessToken = dataLocal.accessToken,
                        commandsId = _teamInfo!!.commandsId
                    )
                )

                val requestBody =
                    dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull())

                CoroutineScope(Dispatchers.IO).launch {
                    var responseBody = service.funPlayerCommandDetach(requestBody)
                    withContext(Dispatchers.Main) {
                        if (responseBody.isSuccessful) {
                            val result = gson.toJson(
                                JsonParser.parseString(responseBody.body()?.string())
                            )

                            val error = gson.fromJson(result, ErrorDataModel::class.java)

                            // Проверка валидности данных
                            if ((error.errors == null) && (error.message == null)) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    CustomToast.makeText(this@PlayerExistsTeamFragment.requireContext(), "Выход из команды!").show();
                                }
                            }
                        }
                    }
                }
            }
            (activity as MainActivity).onBackPressed()
        });

        if(shared!!.contains(ConfigStorage.USERS_DATA)){
            val localData = shared.getString(ConfigStorage.USERS_DATA, null);
            val retrofit = AppMainHandler.getRetrofit()

            var gson = Gson();
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
                var responseBody = service.funCommand(requestBody);
                withContext(Dispatchers.Main) {
                    if (responseBody.isSuccessful) {
                        val result = gson.toJson(
                            JsonParser.parseString(responseBody.body()?.string())
                        )

                        val error = gson.fromJson(result, ErrorDataModel::class.java)

                        // Проверка валидности данных
                        if((error.errors == null) && (error.message == null)
                        ){
                            var data = gson.fromJson(result, CommandModel::class.java)
                            CoroutineScope(Dispatchers.Main).launch {
                                view.findViewById<TextView>(R.id.tv_existingTeamName).text = data.name
                                view.findViewById<TextView>(R.id.tv_inTeamMembers9).text = "Количество участников: " + data.countPlayers
                                view.findViewById<TextView>(R.id.tv_inTeamMembers2).text = data.rating.toString()
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
            PlayerExistsTeamFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}