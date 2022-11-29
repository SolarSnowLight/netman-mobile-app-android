package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.player

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.game.mobileappar.models.PlayerInfoTeamModel

class PlayerAdapter(private val context: Context) : RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {

    private var _data: List<PlayerInfoTeamModel> = listOf();    //список данных для инициализации элементов адаптера

    //функция для установки списка данных
//    public fun setData(data: List<PlayerInfoTeamJSON>){
//        _data = data;
//    }

    class ViewHolder(itemView: View,context: Context) : RecyclerView.ViewHolder(itemView) { // класс для формирования структуры элемента списка
        var avatar: ImageView? = null;
        var tvNickname: TextView? = null;
        var tvScore: TextView? = null;
        var itemData: String? = null;

//        init {
//            avatar = itemView.findViewById(R.id.avatarPlayerItem);          //аватар игрока
//            tvNickname = itemView.findViewById(R.id.txtNickPlayerCommand);  //ник игрока
//            tvScore = itemView.findViewById(R.id.txtRatingPlayerCommand);   //рейтинг игрока
//            itemData = "";                                                  //персональные данные игрока
//
//            //событие нажатия на игрока (из списка членов команды)
//            itemView.setOnClickListener{
//                val intent = Intent(context, PlayerTeamInfoActivity::class.java);
//                //intent.putExtra(context.getString(R.string.users_data), itemData);  //передача данных активности
//
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
//                context.startActivity(intent);
//            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { // указание идентификатора макета для каждого элемента списка
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.player_item, parent, false);
        return ViewHolder(itemView, context) // возвращаем ViewHolder для заполнения
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {// метод для заполнения объекта ViewHolder нужными данными
        holder.tvNickname?.text = "Васюнкин"//_data[position].nickname;
        holder.tvScore?.text = "Васюнович"//_data[position].rating.toString();
        holder.itemData = "Васян" //Gson().toJson(_data[position]); //преобразование данных в строку JSON
    }

    override fun getItemCount(): Int { // метод, определяющий количество генерируемых элементов
        return _data.size;
    }
}