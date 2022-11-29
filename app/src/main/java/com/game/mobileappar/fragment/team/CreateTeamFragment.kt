package com.game.mobileappar.fragment.team

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.Navigation
import androidx.viewpager.widget.ViewPager
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.player.PlayersVPA
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.player.PlayerInfoModel
import com.game.mobileappar.models.command.players.CommandPlayersElementModel
import com.game.mobileappar.models.command.players.CommandPlayersModel
import com.game.mobileappar.network.service.APIService
import com.google.android.material.tabs.TabLayout
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

class CreateTeamFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var _currentPlayers: CommandPlayersModel? = null

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
        val shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
        val view:View = inflater.inflate(R.layout.team_create_team_fragment, container, false);
        val toolBar: Toolbar? = view.findViewById(R.id.tb_in_team)
        toolBar?.setNavigationOnClickListener{
            (activity as MainActivity).onBackPressed();
        }

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)//
        // изменение параметров клавиатуры

        val viewPager:ViewPager = view.findViewById(R.id.viewPager2)
        val tabLayout:TabLayout = view.findViewById(R.id.tab_layout2)


        view.findViewById<ConstraintLayout>(R.id.cl_createTeam).setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        view.findViewById<View>(R.id.v_addPlayerToNewTeam).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_inTeamFragment_to_findPlayerFragment, null))

        if(shared!!.contains(ConfigStorage.USERS_DATA)){
            val localData = shared.getString(ConfigStorage.USERS_DATA, null);
            val retrofit = AppMainHandler.getRetrofit()

            var gson = Gson()
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
                var responseBody = service.funPlayerInfo(requestBody)
                withContext(Dispatchers.Main) {
                    if (responseBody.isSuccessful) {
                        val result = gson.toJson(
                            JsonParser.parseString(responseBody.body()?.string())
                        )

                        val error = gson.fromJson(result, ErrorDataModel::class.java)

                        // Проверка валидности данных
                        if((error.errors == null) && (error.message == null)
                        ){
                            var data = gson.fromJson(result, PlayerInfoModel::class.java)
                            CoroutineScope(Dispatchers.Main).launch {
                                val players = TeamListPlayersFragment("", null)
                                var firstElement = listOf(
                                    CommandPlayersElementModel(
                                    id = null,
                                    name = data.name,
                                    surname = data.surname,
                                    nickname = data.nickname,
                                    rating = data.dataPlayers.rating,
                                    creator = true,
                                    refImage = null,
                                    phoneNum = null,
                                    dateBirthday = null,
                                    location = null,
                                    usersId = data.usersId,
                                    dateRegister = null
                                )
                                )
                                players.getAdapter()?.setPlayers(
                                    CommandPlayersModel(
                                        users = firstElement
                                    )
                                )

                                val viewPagerAdapter: PlayersVPA = PlayersVPA(childFragmentManager, players)
                                viewPager.adapter=viewPagerAdapter
                                tabLayout.setupWithViewPager(viewPager)
                            }
                        }
                    }
                }
            }
        }

        return view;
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateTeamFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}