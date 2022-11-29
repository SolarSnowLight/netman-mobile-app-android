package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.command

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.game.mobileappar.R
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.player.search.command.PlayerSearchCommandModel
import com.game.mobileappar.models.player.search.command.add.PlayerAddDataModel
import com.game.mobileappar.network.service.APIService
import com.game.mobileappar.components.toast.CustomToast
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit


class SearchingAdapter(private val context: Context): RecyclerView.Adapter<SearchingAdapter.ViewHolder>() {

    private var _data: PlayerSearchCommandModel? = null
    private var _clicked: MutableMap<Int, Boolean> = mutableMapOf()
    private var _userData: PlayerAccessModel? = null
    private var _commandsId: Int? = null

    public fun setData(values: PlayerSearchCommandModel?){
        _data = values
        notifyDataSetChanged()
    }

    public fun setUserData(value: PlayerAccessModel?){
        _userData = value
    }

    public fun setCommandsId(value: Int){
        _commandsId = value
    }

    class ViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) { // класс для формирования структуры элемента списка
        var iwAvatarPlayer: ImageView = itemView.findViewById(R.id.avatarPlayerItem)
        var vAddPlayer: View = itemView.findViewById(R.id.v_findPlayerBtn)
        var tvNickname: TextView = itemView.findViewById(R.id.textView22)
        var tvFullName: TextView = itemView.findViewById(R.id.txtNickPlayerCommand)
        var tvRatingPlayer: TextView = itemView.findViewById(R.id.txtRatingPlayerCommand)

        init {
            itemView.findViewById<TextView>(R.id.textView23).visibility = View.GONE
            vAddPlayer.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.player_item, parent, false)

        return ViewHolder(itemView, context) // возвращаем ViewHolder для заполнения
    }

    override fun getItemCount(): Int {
        return if(_data != null) _data!!.freePlayers.size else 0;
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentData = _data!!.freePlayers[position]

        holder.vAddPlayer.setOnClickListener {
            if(_clicked.containsKey(currentData.usersId)){
                return@setOnClickListener
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(ConfigAddresses.SERVER_CENTRAL_ADDRESS)
                .build()

            val service = retrofit.create(APIService::class.java)
            val gson = Gson()
            var dataAccessPlayer = gson.toJson(
                PlayerAddDataModel(
                    usersId = _userData!!.usersId,
                    accessToken = _userData!!.accessToken,
                    playerUsersId = currentData.usersId,
                    commandsId = _commandsId!!
                )
            );

            val requestBody =
                dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull())

            CoroutineScope(Dispatchers.IO).launch {
                var responseBody = service.funPlayerCommandJoinCertain(requestBody);
                withContext(Dispatchers.Main) {
                    if (responseBody.isSuccessful) {
                        val result = gson.toJson(
                            JsonParser.parseString(responseBody.body()?.string())
                        )

                        val error = gson.fromJson(result, ErrorDataModel::class.java)

                        // Проверка валидности данных
                        if ((error.errors == null) && (error.message == null)) {
                            // Добавление игрока в определённую команду
                            CustomToast.makeText(this@SearchingAdapter.context, "Игрок " + currentData.nickname + " добавлен в команду!").show()
                            holder.vAddPlayer.setBackgroundResource(R.drawable.ic_galka)
                        }else{
                            if(error.message != null){
                                CustomToast.makeText(this@SearchingAdapter.context, error.message).show()
                            }
                        }
                    }
                }
            }

            _clicked[currentData.usersId] = true
        }

        holder.tvFullName.text = currentData.name + " " + currentData.surname
        holder.tvNickname.text = currentData.nickname
        holder.tvRatingPlayer.text = currentData.rating.toString()
    }
}
