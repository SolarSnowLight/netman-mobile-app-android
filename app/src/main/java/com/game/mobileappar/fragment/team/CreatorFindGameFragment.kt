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
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.game.mobileappar.adapter.game.FindGameAdapter
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.game.list.GameListModel
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

class CreatorFindGameFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

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
        var shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
        val view = inflater.inflate(R.layout.team_find_game_creator_fragment, container, false);
        val rvFindGameList: RecyclerView = view.findViewById(R.id.rv_findGameListCreator)
        rvFindGameList.layoutManager = LinearLayoutManager(activity)

        view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBar_findGame).setNavigationOnClickListener{
            (activity as MainActivity).onBackPressed()
        }

        if(shared!!.contains(ConfigStorage.USERS_DATA)) {
            val localData = shared.getString(ConfigStorage.USERS_DATA, null);
            val retrofit = AppMainHandler.getRetrofit()

            var gson = Gson();
            var dataLocal: UserDataModel? =
                gson.fromJson(localData, UserDataModel::class.java);
            val service = retrofit.create(APIService::class.java);

            var dataAccessPlayer = gson.toJson(
                PlayerAccessModel(
                    usersId = dataLocal!!.usersId,
                    accessToken = dataLocal!!.accessToken,
                )
            );

            val requestBody =
                dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull());

            CoroutineScope(Dispatchers.IO).launch {
                var responseBody = service.funCommandAvailableGames(requestBody);
                withContext(Dispatchers.Main) {
                    if (responseBody.isSuccessful) {
                        val result = gson.toJson(
                            JsonParser.parseString(responseBody.body()?.string())
                        );

                        val error = gson.fromJson(result, ErrorDataModel::class.java);

                        // Проверка валидности данных
                        if ((error.errors == null) && (error.message == null)) {
                            val data = gson.fromJson(result, GameListModel::class.java)

                            CoroutineScope(Dispatchers.Main).launch {
                                val mAdapter: FindGameAdapter? = context?.let { FindGameAdapter(it) }
                                mAdapter?.setData(data)
                                mAdapter?.notifyDataSetChanged()

                                rvFindGameList.adapter = mAdapter
                                rvFindGameList.itemAnimator = DefaultItemAnimator()
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
            CreatorFindGameFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}