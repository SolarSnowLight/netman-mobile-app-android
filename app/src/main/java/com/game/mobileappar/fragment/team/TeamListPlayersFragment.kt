package com.game.mobileappar.fragment.team

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.player.PlayerInTeamAdapter
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.config.game.ConfigStatusPlayer
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.PlayerCommandDataModel
import com.game.mobileappar.models.command.players.CommandPlayersModel
import com.game.mobileappar.network.service.APIService
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


/**
 * Фрагмент списка игроков в GamesPLayersVPA
 */
class TeamListPlayersFragment(
    private var type: String,
    private var commandsId: Int?,
    private var playerStatus: Byte = ConfigStatusPlayer.PLAYER_DEFAULT
) : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var _adapter: PlayerInTeamAdapter? = null

    public fun getAdapter(): PlayerInTeamAdapter?{
        return _adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE);
        val view:View = inflater.inflate(R.layout.team_players_in_team_fragment, container, false)
        val recyclerView: RecyclerView? = view.findViewById(R.id.rv_playersInTeam)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        recyclerView?.layoutManager = LinearLayoutManager(activity)

        if(shared!!.contains(ConfigStorage.USERS_DATA)){
            val localData = shared.getString(ConfigStorage.USERS_DATA, null)
            val retrofit = AppMainHandler.getRetrofit()

            var gson = Gson()
            var dataLocal: UserDataModel? = gson.fromJson(localData, UserDataModel::class.java)
            val service = retrofit.create(APIService::class.java)

            var dataAccessPlayer =
                if(commandsId == null) gson.toJson(
                    PlayerAccessModel(
                        usersId = dataLocal!!.usersId,
                        accessToken = dataLocal!!.accessToken
                    )
                ) else gson.toJson(
                    PlayerCommandDataModel(
                        usersId = dataLocal!!.usersId,
                        accessToken = dataLocal!!.accessToken,
                        commandsId = commandsId!!
                    )
                )

            val requestBody = dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull())

            CoroutineScope(Dispatchers.IO).launch {
                var responseBody = service.funCommandPlayers(requestBody)
                withContext(Dispatchers.Main) {
                    if (responseBody.isSuccessful) {
                        val result = gson.toJson(
                            JsonParser.parseString(responseBody.body()?.string())
                        );

                        val error = gson.fromJson(result, ErrorDataModel::class.java)

                        // Проверка валидности данных
                        if((error.errors == null) && (error.message == null)
                        ){
                            var data = gson.fromJson(result, CommandPlayersModel::class.java);
                            CoroutineScope(Dispatchers.Main).launch {
                                _adapter = view.context?.let { PlayerInTeamAdapter(it, type) };
                                recyclerView?.adapter = _adapter;
                                recyclerView?.itemAnimator = DefaultItemAnimator();
                                _adapter?.setPlayers(data);
                                _adapter?.setDataUser(
                                    PlayerAccessModel(
                                        usersId = dataLocal.usersId,
                                        accessToken = dataLocal.accessToken
                                    )
                                )
                                _adapter?.setPlayerStatus(playerStatus)
                            }
                        }
                    }
                }
            }
        }

        return view
    }
}