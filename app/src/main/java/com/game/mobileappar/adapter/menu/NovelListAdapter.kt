package com.game.mobileappar.adapter.menu

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

class NovelListAdapter(private val context: Context,
                      private val novels: ArrayList<MenuInfoGameModel>) : RecyclerView.Adapter<NovelListAdapter.InfoNovelViewHolder>() {

    class InfoNovelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var age: TextView = itemView.findViewById<TextView>(R.id.tvAgeItemMainMenu)
        var name: TextView = itemView.findViewById<TextView>(R.id.tvNameItemMainMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoNovelViewHolder {
        val gameItem = LayoutInflater.from(context).inflate(R.layout.adapter_item_main_menu, parent, false)

        val windowBackground = parent.background

        val blurView = gameItem.findViewById<BlurView>(R.id.bvNameItemMainMenu)
        blurView.setupWith(parent)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(context))
            .setBlurRadius(5f)
            .setBlurAutoUpdate(true)
            .setHasFixedTransformationMatrix(false)


        return InfoNovelViewHolder(gameItem)
    }

    override fun onBindViewHolder(holder: InfoNovelViewHolder, position: Int) {
        holder.name.text = novels[position].name
    }

    override fun getItemCount(): Int {
        return novels.size
    }
}