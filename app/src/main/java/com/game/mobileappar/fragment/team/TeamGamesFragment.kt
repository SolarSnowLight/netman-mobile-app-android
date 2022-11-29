package com.game.mobileappar.fragment.team

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.game.mobileappar.adapter.game.GameAdapter
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.PlayerCommandDataModel
import com.game.mobileappar.models.command.CommandCurrentGameModel
import com.game.mobileappar.models.command.games.CommandGamesModel
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

class TeamGamesFragment(
    private var gamesFrom : String,
    private var commandsId: Int?
    ) : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _rvGamesList: RecyclerView? = null
    private var _adapter: GameAdapter? = null

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
    ): View {
        var shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE);
        val view: View = inflater.inflate(R.layout.team_games_fragment, container, false)

        _rvGamesList = view.findViewById(R.id.rv_gamesList);

        _rvGamesList?.layoutManager = LinearLayoutManager(activity);

        if(shared!!.contains(ConfigStorage.USERS_DATA)){
            val localData = shared.getString(ConfigStorage.USERS_DATA, null)
            val retrofit = AppMainHandler.getRetrofit()

            var gson = Gson();
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
                var responseBodyCurrentGame = service.funCommandCurrentGame(requestBody)
                var responseBodyAllGames    = service.funCommandGames(requestBody)

                withContext(Dispatchers.Main) {
                    if ((responseBodyCurrentGame.isSuccessful) && (responseBodyAllGames.isSuccessful)) {
                        val currentGame = gson.toJson(
                            JsonParser.parseString(responseBodyCurrentGame.body()?.string())
                        )

                        val allGames = gson.toJson(
                            JsonParser.parseString(responseBodyAllGames.body()?.string())
                        )

                        val currentGameError = gson.fromJson(currentGame, ErrorDataModel::class.java)
                        val allGamesError    = gson.fromJson(allGames, ErrorDataModel::class.java)

                        if(((currentGameError.errors == null) && (currentGameError.message == null))
                            && ((allGamesError.errors == null) && (allGamesError.message == null))){

                            var dataCurrentGame = gson.fromJson(currentGame,
                                CommandCurrentGameModel::class.java)
                            var dataGames = gson.fromJson(allGames,
                                CommandGamesModel::class.java)

                            CoroutineScope(Dispatchers.Main).launch {
                                _adapter = view.context?.let { GameAdapter(it, gamesFrom) }
                                _adapter?.setAllGames(dataGames)
                                _adapter?.setCurrentGame(dataCurrentGame)
                                _adapter?.notifyDataSetChanged()

                                _rvGamesList?.adapter = _adapter
                                _rvGamesList?.itemAnimator = DefaultItemAnimator()
                            }
                        }
                    }
                }
            }
        }
        return view
    }
}