package com.game.mobileappar.adapter.menu

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.game.mobileappar.models.menu.MenuInfoGameModel
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

class GameListAdapter(private val context: Context,
                      private val games: ArrayList<MenuInfoGameModel>
) : RecyclerView.Adapter<GameListAdapter.InfoGameViewHolder>() {

    class InfoGameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var age: TextView = itemView.findViewById<TextView>(R.id.tvAgeItemMainMenu)
        var name: TextView = itemView.findViewById<TextView>(R.id.tvNameItemMainMenu)

        init {
            age.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoGameViewHolder {
        val gameItem = LayoutInflater.from(context).inflate(R.layout.adapter_item_main_menu, parent, false)

        val windowBackground = parent.background

        val blurView = gameItem.findViewById<BlurView>(R.id.bvNameItemMainMenu)
        blurView.setupWith(parent)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(context))
            .setBlurRadius(5f)
            .setBlurAutoUpdate(true)
            .setHasFixedTransformationMatrix(false)


        return InfoGameViewHolder(gameItem)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: InfoGameViewHolder, position: Int) {
        holder.age.text = games[position].age.toString() + "+"
        holder.name.text = games[position].name
    }

    override fun getItemCount(): Int {
        return games.size
    }
}