package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.team

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.game.mobileappar.models.command.list.CommandListModel

class TeamAdapter(private val context: Context): RecyclerView.Adapter<TeamAdapter.ViewHolder>() {
    private var _dataCommands: CommandListModel? = null;

    public fun setDataCommands(data: CommandListModel?){
        _dataCommands = data;
    }

    public fun getDataCommands(): CommandListModel {
        return _dataCommands!!
    }

    private var lastDy = 0
    class ViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.team_item, parent, false);

        return ViewHolder(itemView, context) // возвращаем ViewHolder для заполнения
    }

    override fun getItemCount(): Int {
        if(_dataCommands == null)
            return 5
        return _dataCommands!!.commands.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if((_dataCommands != null) && (_dataCommands?.commands?.isNotEmpty() == true)){
            val data = _dataCommands!!.commands[position]
            holder.itemView.findViewById<TextView>(R.id.textView9).text = data.name
            holder.itemView.findViewById<TextView>(R.id.tv_city_team2).text = data.countPlayers.toString() + "/6"
            holder.itemView.findViewById<TextView>(R.id.tv_city_team).text = data.location
        }
    }

}