package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.judge

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.game.mobileappar.VideoActivity
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.config.game.ConfigStatusPlayer
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.game.GamePathMediaModel
import com.game.mobileappar.models.judge.JudgeInfoResultModel
import com.game.mobileappar.models.judge.JudgeSendResultModel
import com.game.mobileappar.network.service.APIService
import com.game.mobileappar.components.toast.CustomToast
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception

class RulerRateAdapter(private val context: Context): RecyclerView.Adapter<RulerRateAdapter.ViewHolder>() {

    private var _resultsData: JudgeInfoResultModel? = null
    private var _authData: UserDataModel? = null
    private var _externalDir: String? = null
    private var _idJudge: Int? = null

    fun setResultData(value: JudgeInfoResultModel?){
        _resultsData = value
        notifyDataSetChanged()
    }

    fun setAuthData(value: UserDataModel?){
        _authData = value
    }

    fun setExternalDir(value: String?){
        _externalDir = value
    }

    fun setJudgeId(id: Int?){
        _idJudge = id
    }

    private var lastDy = 0

    class ViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) { // класс для формирования структуры элемента списка
        var view8_ruler_answer_quality: ToggleButton? = null
        var view7_ruler_answer_quality: ToggleButton? = null
        var view6_ruler_answer_quality: ToggleButton? = null
        var view5_ruler_answer_quality: ToggleButton? = null
        var view4_ruler_answer_quality: ToggleButton? = null

        var view8_ruler_speed: ToggleButton? = null
        var view7_ruler_speed: ToggleButton? = null
        var view6_ruler_speed: ToggleButton? = null
        var view5_ruler_speed: ToggleButton? = null
        var view4_ruler_speed: ToggleButton? = null

        var view8_ruler: ToggleButton? = null
        var view7_ruler: ToggleButton? = null
        var view6_ruler: ToggleButton? = null
        var view5_ruler: ToggleButton? = null
        var view4_ruler: ToggleButton? = null

        private var vRulerItemArrow: View? = null
        var tvTaskCondition: TextView? = null

        var numRulerItem: TextView? = null
        var mediaInstruction: View? = null
        var mediaResult: View? = null
        var linkVideo: TextView? = null
        var setScoreVideo = false
        var speedScore = 0
        var teamers    = 0
        var quality    = 0

        init {
            view8_ruler_answer_quality = itemView.findViewById(R.id.view8_ruler_answer_quality)
            view7_ruler_answer_quality = itemView.findViewById(R.id.view7_ruler_answer_quality)
            view6_ruler_answer_quality = itemView.findViewById(R.id.view6_ruler_answer_quality)
            view5_ruler_answer_quality = itemView.findViewById(R.id.view5_ruler_answer_quality)
            view4_ruler_answer_quality = itemView.findViewById(R.id.view4_ruler_answer_quality)

            view8_ruler_speed = itemView.findViewById(R.id.view8_ruler_speed)
            view7_ruler_speed = itemView.findViewById(R.id.view7_ruler_speed)
            view6_ruler_speed = itemView.findViewById(R.id.view6_ruler_speed)
            view5_ruler_speed = itemView.findViewById(R.id.view5_ruler_speed)
            view4_ruler_speed = itemView.findViewById(R.id.view4_ruler_speed)

            view8_ruler = itemView.findViewById(R.id.view8_ruler)
            view7_ruler = itemView.findViewById(R.id.view7_ruler)
            view6_ruler = itemView.findViewById(R.id.view6_ruler)
            view5_ruler = itemView.findViewById(R.id.view5_ruler)
            view4_ruler = itemView.findViewById(R.id.view4_ruler)

            vRulerItemArrow = itemView.findViewById(R.id.view11_ruler_item_arrow)
            tvTaskCondition = itemView.findViewById(R.id.tv_task_condition)
            numRulerItem = itemView.findViewById(R.id.tv_task_num_ruler_item)
            mediaInstruction = itemView.findViewById(R.id.v_camera_ruler_item)
            mediaResult = itemView.findViewById(R.id.view2)
            linkVideo = itemView.findViewById(R.id.tv_ruler_rate_link)

            vRulerItemArrow?.setOnClickListener {
                if(tvTaskCondition?.visibility==View.VISIBLE){
                    vRulerItemArrow?.rotation = vRulerItemArrow?.rotation?.plus(180f)!!
                    tvTaskCondition?.visibility = View.GONE;
                }else{
                    vRulerItemArrow?.rotation = vRulerItemArrow?.rotation?.plus(180f)!!
                    tvTaskCondition?.visibility = View.VISIBLE
                }

            }
        }
    }

    private fun on1StarClick(t1:ToggleButton, t2:ToggleButton, t3:ToggleButton, t4:ToggleButton, t5:ToggleButton){

        if(t5.isChecked || t2.isChecked || t3.isChecked || t4.isChecked)
        {
            t1.isChecked = true
        }
        t5.isChecked = false
        t4.isChecked = false
        t3.isChecked = false
        t2.isChecked = false
    }

    private fun on2StarClick(t1:ToggleButton, t2:ToggleButton, t3:ToggleButton, t4:ToggleButton, t5:ToggleButton){
        t1.isChecked = true

        if(t5.isChecked || t3.isChecked || t4.isChecked)
        {
            t2.isChecked = true
            t1.isChecked = true
        }

        if (!t2.isChecked) {
            t1.isChecked = false
            t3.isChecked = false
            t4.isChecked = false
            t5.isChecked = false
        }
        t5.isChecked = false
        t4.isChecked = false
        t3.isChecked = false
    }

    private fun on3StarClick(t1:ToggleButton, t2:ToggleButton, t3:ToggleButton, t4:ToggleButton, t5:ToggleButton){
        t1.isChecked = true
        t2.isChecked = true

        if(t5.isChecked || t4.isChecked)
        {
            t3.isChecked = true
            t2.isChecked = true
            t1.isChecked = true
        }

        if (!t3.isChecked) {
            t1.isChecked = false
            t2.isChecked = false
            t4.isChecked = false
            t5.isChecked = false
        }
        t5.isChecked = false
        t4.isChecked = false
    }

    private fun on4StarClick(t1:ToggleButton, t2:ToggleButton, t3:ToggleButton, t4:ToggleButton, t5:ToggleButton){
        t1.isChecked = true;
        t2.isChecked = true;
        t3.isChecked = true;

        if(t5.isChecked)
        {
            t4.isChecked = true;
            t3.isChecked = true;
            t2.isChecked = true;
            t1.isChecked = true;
        }

        if (!t4.isChecked) {
            t1.isChecked = false;
            t2.isChecked = false;
            t3.isChecked = false;
            t5.isChecked = false;
        }
        t5.isChecked = false;
    }

    private fun on5StarClick(t1:ToggleButton, t2:ToggleButton, t3:ToggleButton, t4:ToggleButton, t5:ToggleButton){
        t1.isChecked = true;
        t2.isChecked = true;
        t3.isChecked = true;
        t4.isChecked = true;


        if (!t5.isChecked) {
            t1.isChecked = false;
            t2.isChecked = false;
            t3.isChecked = false;
            t4.isChecked = false;
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.ruler_rate_item, parent, false);

        return ViewHolder(itemView, context) // возвращаем ViewHolder для заполнения
    }

    override fun getItemCount(): Int {
        return if(_resultsData == null) 0 else _resultsData!!.resultsInfo.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentData = _resultsData!!.resultsInfo[position]
        holder.numRulerItem?.text = (position + 1).toString()
        holder.tvTaskCondition?.text = currentData.questInfo.task

        downloadMediaInstructions(currentData.questInfo.refMedia, _authData!!.accessToken, holder)
        downloadMediaResults(currentData.resultInfo.refImage, _authData!!.accessToken, holder)

        //------------------------------------STARS----------------------------------//
        holder.view4_ruler_speed?.setOnClickListener{
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.speedScore = 5

            if((holder.teamers > 0) && (holder.quality > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }

            on5StarClick(holder.view8_ruler_speed!!, holder.view7_ruler_speed!!,
                holder.view6_ruler_speed!!, holder.view5_ruler_speed!!, holder.view4_ruler_speed!!)
        }

        holder.view5_ruler_speed?.setOnClickListener{
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.speedScore = 4

            if((holder.teamers > 0) && (holder.quality > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }

            on4StarClick(holder.view8_ruler_speed!!, holder.view7_ruler_speed!!,
                holder.view6_ruler_speed!!, holder.view5_ruler_speed!!, holder.view4_ruler_speed!!)
        }

        holder.view6_ruler_speed?.setOnClickListener{
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.speedScore = 3

            if((holder.teamers > 0) && (holder.quality > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on3StarClick(holder.view8_ruler_speed!!, holder.view7_ruler_speed!!,
                holder.view6_ruler_speed!!, holder.view5_ruler_speed!!, holder.view4_ruler_speed!!)
        }

        holder.view7_ruler_speed?.setOnClickListener{
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.speedScore = 2

            if((holder.teamers > 0) && (holder.quality > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on2StarClick(holder.view8_ruler_speed!!, holder.view7_ruler_speed!!,
                holder.view6_ruler_speed!!, holder.view5_ruler_speed!!, holder.view4_ruler_speed!!)
        }

        holder.view8_ruler_speed?.setOnClickListener{
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.speedScore = 1

            if((holder.teamers > 0) && (holder.quality > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on1StarClick(holder.view8_ruler_speed!!, holder.view7_ruler_speed!!,
                holder.view6_ruler_speed!!, holder.view5_ruler_speed!!, holder.view4_ruler_speed!!)
        }

        holder.view4_ruler?.setOnClickListener {
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.teamers = 5

            if((holder.speedScore > 0) && (holder.quality > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on5StarClick(holder.view8_ruler!!, holder.view7_ruler!!,
                holder.view6_ruler!!, holder.view5_ruler!!, holder.view4_ruler!!)
        }

        holder.view5_ruler?.setOnClickListener {
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.teamers = 4

            if((holder.speedScore > 0) && (holder.quality > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on4StarClick(holder.view8_ruler!!, holder.view7_ruler!!,
                holder.view6_ruler!!, holder.view5_ruler!!, holder.view4_ruler!!)
        }

        holder.view6_ruler?.setOnClickListener {
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.teamers = 3

            if((holder.speedScore > 0) && (holder.quality > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on3StarClick(holder.view8_ruler!!, holder.view7_ruler!!,
                holder.view6_ruler!!, holder.view5_ruler!!, holder.view4_ruler!!)
        }

        holder.view7_ruler?.setOnClickListener {
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.teamers = 2

            if((holder.speedScore > 0) && (holder.quality > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on2StarClick(holder.view8_ruler!!, holder.view7_ruler!!,
                holder.view6_ruler!!, holder.view5_ruler!!, holder.view4_ruler!!)
        }

        holder.view8_ruler?.setOnClickListener {
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.teamers = 1

            if((holder.speedScore > 0) && (holder.quality > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on1StarClick(holder.view8_ruler!!, holder.view7_ruler!!, holder.view6_ruler!!,
                holder.view5_ruler!!, holder.view4_ruler!!)
        }

        holder.view4_ruler_answer_quality?.setOnClickListener {
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.quality = 5

            if((holder.speedScore > 0) && (holder.teamers > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on5StarClick(holder.view8_ruler_answer_quality!!, holder.view7_ruler_answer_quality!!,
                holder.view6_ruler_answer_quality!!, holder.view5_ruler_answer_quality!!,
                holder.view4_ruler_answer_quality!!)
        }

        holder.view8_ruler_answer_quality?.setOnClickListener {
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.quality = 4

            if((holder.speedScore > 0) && (holder.teamers > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on1StarClick(holder.view8_ruler_answer_quality!!, holder.view7_ruler_answer_quality!!,
                holder.view6_ruler_answer_quality!!, holder.view5_ruler_answer_quality!!,
                holder.view4_ruler_answer_quality!!)
        }
        holder.view7_ruler_answer_quality?.setOnClickListener {
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.quality = 3

            if((holder.speedScore > 0) && (holder.teamers > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on2StarClick(holder.view8_ruler_answer_quality!!, holder.view7_ruler_answer_quality!!,
                holder.view6_ruler_answer_quality!!,holder.view5_ruler_answer_quality!!, holder.view4_ruler_answer_quality!!)
        }

        holder.view6_ruler_answer_quality?.setOnClickListener {
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.quality = 2

            if((holder.speedScore > 0) && (holder.teamers > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on3StarClick(holder.view8_ruler_answer_quality!!, holder.view7_ruler_answer_quality!!,
                holder.view6_ruler_answer_quality!!, holder.view5_ruler_answer_quality!!, holder.view4_ruler_answer_quality!!)
        }

        holder.view5_ruler_answer_quality?.setOnClickListener {
            if(holder.setScoreVideo){
                return@setOnClickListener
            }
            holder.quality = 1

            if((holder.speedScore > 0) && (holder.teamers > 0)){
                var sum = (holder.speedScore + holder.teamers + holder.quality)
                holder.setScoreVideo = true
                setGameScore(sum, currentData.resultInfo.id, _idJudge!!)
            }
            on4StarClick(holder.view8_ruler_answer_quality!!, holder.view7_ruler_answer_quality!!,
                holder.view6_ruler_answer_quality!!, holder.view5_ruler_answer_quality!!,
                holder.view4_ruler_answer_quality!!)
        }

        //------------------------------------STARS----------------------------------//
    }

    private fun downloadMediaInstructions(refMedia: String, token: String, holder: ViewHolder) {
        val retrofit = Retrofit.Builder()
            .baseUrl(ConfigAddresses.SERVER_MEDIA_ADDRESS)
            .build()
        val service = retrofit.create(APIService::class.java)

        val gson = Gson()
        var videoData = gson.toJson(
            GamePathMediaModel(
                localPath = refMedia,
                accessToken = token
            )
        )

        val videoRequestBody = videoData.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            var responseBody = service.mediaInstructionsDownload(videoRequestBody)
            if (responseBody.isSuccessful) {
                writeResponseBodyToStorage(responseBody.body()!!,
                    responseBody.headers()["filename"].toString(),
                    holder,
                    true
                )
            }
        }
    }

    private fun downloadMediaResults(refMedia: String, accessToken: String, holder: ViewHolder) {
        val retrofit = Retrofit.Builder()
            .baseUrl(ConfigAddresses.SERVER_MEDIA_ADDRESS)
            .build()
        val service = retrofit.create(APIService::class.java)

        val gson = Gson()
        var videoData = gson.toJson(
            GamePathMediaModel(
                localPath = refMedia,
                accessToken = accessToken
            )
        )

        val videoRequestBody = videoData.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            var responseBody = service.mediaDownload(videoRequestBody)
            if (responseBody.isSuccessful) {
                writeResponseBodyToStorage(responseBody.body()!!,
                    responseBody.headers()["filename"].toString(),
                    holder,
                    false
                )
            }
        }
    }

    // Загрузка файла на мобильное устройство
    private fun writeResponseBodyToStorage(body: ResponseBody, filename: String, holder: ViewHolder, b: Boolean): Boolean {
        return try {
            val filePath =
                File(_externalDir + File.separator
                        + filename + ".mp4")
            var fullPath = _externalDir + File.separator + filename + ".mp4"
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(filePath)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                }
                outputStream.flush()

                CoroutineScope(Dispatchers.Main).launch {
                    if(b){
                        holder.mediaInstruction?.setBackgroundResource(R.drawable.ic_media)
                        holder.mediaInstruction?.setOnClickListener {
                            var intent = Intent(context, VideoActivity::class.java)
                            intent.putExtra("player_status", ConfigStatusPlayer.PLAYER_ACTIVE)
                            intent.putExtra("file_path", fullPath)

                            context.startActivity(intent)
                        }
                    }else{
                        holder.linkVideo?.text = fullPath.subSequence(0, 30)
                        holder.mediaResult?.setBackgroundResource(R.drawable.ic_camera)
                        holder.mediaResult?.setOnClickListener {
                            var intent = Intent(context, VideoActivity::class.java)
                            intent.putExtra("player_status", ConfigStatusPlayer.PLAYER_ACTIVE)
                            intent.putExtra("file_path", fullPath)

                            context.startActivity(intent)
                        }
                    }
                }
                true
            } catch (e: Exception) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun setGameScore(score: Int, gameFinished: Int, judgeId: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl(ConfigAddresses.SERVER_CENTRAL_ADDRESS)
            .build()
        val service = retrofit.create(APIService::class.java)

        val gson = Gson()
        var videoData = gson.toJson(
            JudgeSendResultModel(
                accessToken = _authData!!.accessToken,
                score = score,
                gameFinishedId = gameFinished,
                fixJudgesId = judgeId
            )
        )

        val videoRequestBody = videoData.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            var responseBody = service.funPlayerJudgeSetScore(videoRequestBody)
            if (responseBody.isSuccessful) {
                val result = gson.toJson(
                    JsonParser.parseString(responseBody.body()?.string())
                )

                val error = gson.fromJson(result, ErrorDataModel::class.java)

                if((error.errors == null) && (error.message == null)){
                    CoroutineScope(Dispatchers.Main).launch {
                        CustomToast.makeText(context, "Выполнение задания было оценено").show();
                    }
                }
            }
        }
    }
}