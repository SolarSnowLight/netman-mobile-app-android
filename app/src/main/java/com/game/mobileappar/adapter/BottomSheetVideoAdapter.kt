package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter


import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

class BottomSheetVideoAdapter(private val context: Context, private val activity: Activity): RecyclerView.Adapter<BottomSheetVideoAdapter.ViewHolder>() {


//    private var _dataCommands: CommandsListJSON? = null;
//
//    public fun setDataCommands(data: CommandsListJSON?){
//        _dataCommands = data;
//    }
//
//    public fun getDataCommands(): CommandsListJSON{
//        return _dataCommands!!
//    }

    //private var lastDy = 0
    class ViewHolder(itemView: View, context: Context, activity: Activity, parent: ViewGroup) : RecyclerView.ViewHolder(itemView) {

        val bvBottomSheet = itemView?.findViewById(R.id.blImageView) as BlurView

        init {

            val decorView: View? = activity.window?.decorView


            val windowBackground = decorView?.background
            val rootView = decorView?.findViewById(android.R.id.content) as ViewGroup


            bvBottomSheet.setupWith(parent)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(RenderScriptBlur(context))
                .setBlurRadius(25.0f)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(false)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetVideoAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_item, parent, false);



        return BottomSheetVideoAdapter.ViewHolder(itemView, context, activity,parent) // возвращаем ViewHolder для заполнения
    }

    override fun getItemCount(): Int {
        //if(_dataCommands == null)
            return 5
        //return _dataCommands!!.commands.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //if((_dataCommands != null) && (_dataCommands?.commands?.isNotEmpty() == true)){
            //val data = _dataCommands!!.commands[position]
//            holder.itemView.findViewById<TextView>(R.id.textView9).text = data.name
//            holder.itemView.findViewById<TextView>(R.id.tv_city_team2).text = data.countPlayers.toString() + "/6"
//            holder.itemView.findViewById<TextView>(R.id.tv_city_team).text = data.location
       // }


    //-----------    holder.itemView.findViewById<ImageView>(R.id.ivBottomSheetItem)
    }

}