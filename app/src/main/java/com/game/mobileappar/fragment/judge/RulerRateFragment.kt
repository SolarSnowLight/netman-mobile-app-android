package com.game.mobileappar.fragment.judge

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.judge.RulerRateAdapter
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.judge.JudgeInfoResultModel
import com.game.mobileappar.models.judge.JudgeInfoModel
import com.game.mobileappar.models.judge.JudgeInfoSendModel
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

class RulerRateFragment : Fragment() {

    private var _rvRulerRateList: RecyclerView? = null
    private var _adapter: RulerRateAdapter? = null
    private var _judgeInfo: JudgeInfoModel? = null
    private var _externalDir: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var gson = Gson()
        _judgeInfo = gson.fromJson(requireArguments().getString("judge_info"), JudgeInfoModel::class.java)
        _externalDir = requireArguments().getString("external_dir")
        var shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
        val view: View =  inflater.inflate(R.layout.ruler_rate_fragment, container, false)

        view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBar_ruler_rate).setNavigationOnClickListener{
            (activity as MainActivity).onBackPressed();
        }

        _rvRulerRateList = view.findViewById(R.id.rv_rulerRateList)
        _rvRulerRateList?.layoutManager = LinearLayoutManager(activity)
        _adapter = view.context?.let { RulerRateAdapter(it) }
        _rvRulerRateList?.adapter = _adapter
        _rvRulerRateList?.itemAnimator = DefaultItemAnimator()

        if(shared!!.contains(ConfigStorage.USERS_DATA)) {
            val localData = shared.getString(ConfigStorage.USERS_DATA, null);
            val retrofit = AppMainHandler.getRetrofit()

            var gson = Gson();
            var dataLocal: UserDataModel? =
                gson.fromJson(localData, UserDataModel::class.java);
            val service = retrofit.create(APIService::class.java);

            var dataAccessPlayer = gson.toJson(
                JudgeInfoSendModel(
                    usersId = _judgeInfo!!.usersId,
                    commandsId = _judgeInfo!!.commandsId,
                    infoGamesId = _judgeInfo!!.infoGamesId,
                    accessToken = dataLocal!!.accessToken
                )
            )

            val requestBody =
                dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull());

            CoroutineScope(Dispatchers.IO).launch {
                var responseBody = service.funPlayerJudgeGetInfo(requestBody);
                withContext(Dispatchers.Main) {
                    if (responseBody.isSuccessful) {
                        val result = gson.toJson(
                            JsonParser.parseString(responseBody.body()?.string())
                        )

                        val error = gson.fromJson(result, ErrorDataModel::class.java)

                        // Проверка валидности данных
                        if ((error.errors == null) && (error.message == null)) {
                            var data = gson.fromJson(result, JudgeInfoResultModel::class.java)
                            view.findViewById<TextView>(R.id.tv_teamName_ruler3).text = data.infoCommand.name
                            view.findViewById<TextView>(R.id.tv_gameName_ruler_rating).text = data.infoCommand.rating.toString()
                            view.findViewById<TextView>(R.id.tv_gameName_ruler_members)
                                .text = "Количество участников: " + data.infoCommand.countPlayers

                            view.findViewById<TextView>(R.id.tv_gameName_ruler_members).text = data.infoGame.name
                            view.findViewById<TextView>(R.id.tv_pointerCount_ruler_rate)
                                .text = "Количество точек: " + data.infoGame.countPoints.toString()
                            view.findViewById<TextView>(R.id.tv_ageTeam_ruler)
                                .text = data.infoGame.ageLimit.toString()
                            view.findViewById<TextView>(R.id.tv_city)
                                .text = data.infoGame.location

                            val dateBeginString = data.infoGame.dateBegin
                                ?.split("T")[0]
                                ?.split('-')
                            val strBeginDate = dateBeginString[2] + "." + dateBeginString[1]

                            val dateEndString = data.infoGame.dateEnd
                                ?.split("T")[0]
                                ?.split('-')
                            val strEndDate = dateEndString[2] + "." + dateEndString[1]

                            view.findViewById<TextView>(R.id.textView39)
                                .text = "$strBeginDate - $strEndDate"

                            _adapter?.setResultData(data)
                            _adapter?.setAuthData(dataLocal)
                            _adapter?.setExternalDir(_externalDir)
                            _adapter?.setJudgeId(_judgeInfo?.id)
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
            RulerRateFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}