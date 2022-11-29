package com.game.mobileappar.adapter.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.game.mobileappar.models.game.list.GameListModel
import com.google.gson.Gson

class FindGameAdapter(private val context: Context): RecyclerView.Adapter<FindGameAdapter.ViewHolder>() {
    private var _data: GameListModel? = null

    public fun setData(value: GameListModel?){
        _data = value
    }

    class ViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
        // класс для формирования структуры элемента списка
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.team_game_not_chosen_item, parent, false);

        return ViewHolder(itemView, context) // возвращаем ViewHolder для заполнения
    }

    override fun getItemCount(): Int {
        return if(_data == null) 0 else _data!!.infoGames.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position < _data!!.infoGames.size){
            var currentData = _data!!.infoGames[position]
            holder.itemView.setOnClickListener{
                var gson = Gson()
                var bundle = Bundle()
                bundle.putString("selected_game", gson.toJson(currentData))
                holder.itemView.findNavController().navigate(R.id.action_findGameCreatorFragment_to_regOnGameFragment, bundle)
            }

            holder.itemView.findViewById<TextView>(R.id.tv_teamName_choiceTeam).text = currentData.name
            holder.itemView.findViewById<TextView>(R.id.tv_ageTeam_Choice_team_item).text = currentData.ageLimit.toString() + "+"
            holder.itemView.findViewById<TextView>(R.id.tv_city_team4).text = currentData.location
            holder.itemView.findViewById<TextView>(R.id.textView).text = "Количество меток: " + currentData.countQuests.toString()

            val dateBeginString = currentData.dateBegin
                ?.split("T")[0]
                ?.split('-')
            val strBeginDate = dateBeginString[2] + "." + dateBeginString[1]

            val dateEndString = currentData.dateEnd
                ?.split("T")[0]
                ?.split('-')
            val strEndDate = dateEndString[2] + "." + dateEndString[1]

            holder.itemView.findViewById<TextView>(R.id.tv_time_choiceTem_item).text = "$strBeginDate - $strEndDate"
        }
    }
}