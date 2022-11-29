package com.game.mobileappar.adapter.game

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.R
import com.game.mobileappar.models.command.CommandCurrentGameModel
import com.game.mobileappar.models.command.games.CommandGamesModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class GameAdapter(private val context: Context, private val inputArgGameFrom: String): RecyclerView.Adapter<GameAdapter.ViewHolder>() {
    private var _currentGame: CommandCurrentGameModel? = null
    private var _allGames: CommandGamesModel? = null

    public fun setCurrentGame(game: CommandCurrentGameModel?){
        _currentGame = game
    }

    public fun setAllGames(games: CommandGamesModel?){
        _allGames = games
    }

    private var lastDy = 0
    class ViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
        // класс для формирования структуры элемента списка

        var clCompleted: ConstraintLayout? = null // надпись "пройденные" // сделать невидимым при
        // остуствии пройденных игры

        var tvAgeTeam:TextView? = null    // возрастное ограничение // сделать невидимым
        // при остуствии игры

        var tvTeamName:TextView? = null   // название игры // сделать невидимым при остуствии игры
        var textView37:TextView? = null    //сделать невидимым при начавшейся игре // сделать
        // невидимым при остуствии игры

        var textView38:TextView? = null    // сделать невидимым при начавшейся игре
        // сделать невидимым при остуствии игры
        var view16:View? = null            // сделать невидимым при начавшейся игре //иконка часов
        var view14:View? = null            // сделать невидимым при остуствии игры //иконка метки
        var textView40:TextView? = null    // количество точек // сделать невидимым при остуствии игры

        var linearLayout5:ConstraintLayout? = null  // сделать невидимым при остуствии игры
        var clGameIsBegin:ConstraintLayout? = null // сделать ВИДИМЫМ при начавшейся игре
        var clMain:ConstraintLayout? = null
        var clAddGameCreator:ConstraintLayout? = null// разметка для создателя команды,
        // когда нет текущей игры

        var addGameBoss:View? = null

        init {
            clCompleted = itemView.findViewById(R.id.cl_completed)
            tvAgeTeam = itemView.findViewById(R.id.tv_ageTeam)
            tvTeamName = itemView.findViewById(R.id.tv_teamName)
            textView37 = itemView.findViewById(R.id.textView37)
            textView38 = itemView.findViewById(R.id.textView38)
            view16 = itemView.findViewById(R.id.view16)
            view14 = itemView.findViewById(R.id.view14)
            textView40 = itemView.findViewById(R.id.tv_pointerCountGame)
            linearLayout5 = itemView.findViewById(R.id.linearLayout5)
            clGameIsBegin = itemView.findViewById(R.id.cl_gameIsBegin)
            clAddGameCreator = itemView.findViewById(R.id.cl_addGameCreator)
            clMain = itemView.findViewById(R.id.cl_main)


            addGameBoss = itemView.findViewById(R.id.v_addGameTeamCreator)

            addGameBoss?.setOnClickListener{
                itemView.findNavController().navigate(R.id.action_teamBossFragment_to_findGameCreatorFragment, null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.game_item, parent, false)

        return ViewHolder(itemView, context) // возвращаем ViewHolder для заполнения
    }


    override fun getItemCount(): Int {
        return (1 + (if ((_allGames != null) && (_allGames?.games != null)) _allGames!!.games.size else 1))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        print("HELLO")
        if((inputArgGameFrom == "TeamBoss") && (_currentGame?.dateBegin == null) && (position == 0)){
            holder.clMain?.visibility = View.GONE
            holder.clAddGameCreator?.visibility = View.VISIBLE
        }else if((inputArgGameFrom != "TeamBoss") && (_currentGame?.dateBegin == null) && (position == 0)){
            holder.clMain?.visibility = View.GONE
            holder.clAddGameCreator?.visibility = View.GONE
        }

        if(position == 0){
            // Делаем видимой надпись "пройденные игры"
            if(_allGames!!.games.isNotEmpty()){
                holder.clCompleted?.visibility = View.VISIBLE
            }
        }

        if((_currentGame != null) && (_currentGame?.dateBegin != null)){
            // Вывод информации о статистики прохождения игр командой в том случае,
            // когда у команды есть текущая игра
            if(position == 0){
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val dateBegin = LocalDateTime.parse(_currentGame?.dateBegin, dateFormatter)
                val dateNow = LocalDateTime.now()

                val dateBeginString = _currentGame?.dateBegin
                    ?.split("T")?.get(0)
                    ?.split('-')
                val strBeginDate = dateBeginString?.get(2) + "." + dateBeginString?.get(1)

                val dateEndString = _currentGame?.dateEnd
                    ?.split("T")?.get(0)
                    ?.split('-')
                val strEndDate = dateEndString?.get(2) + "." + dateEndString?.get(1)

                holder.itemView.findViewById<TextView>(R.id.textView39).text = "$strBeginDate - $strEndDate"
                holder.itemView.findViewById<TextView>(R.id.tv_pointerCountGame).text =
                    _currentGame?.countQuests.toString() + " точек"
                holder.tvTeamName?.text = _currentGame?.name
                holder.tvAgeTeam?.text = _currentGame?.ageLimit.toString() + "+"

                if(dateBegin > dateNow){
                    holder.textView37?.visibility = View.VISIBLE
                    holder.view16?.visibility     = View.VISIBLE
                    holder.textView38?.visibility = View.VISIBLE

                    holder.textView38?.text = (abs(ChronoUnit.HOURS.between(dateBegin, dateNow))).toString() + " ч"
                }else{
                    holder.textView37?.visibility = View.INVISIBLE
                    holder.view16?.visibility     = View.INVISIBLE
                    holder.textView38?.visibility = View.INVISIBLE
                    holder.clGameIsBegin?.visibility = View.VISIBLE

                    val dateEnd = LocalDateTime.parse(_currentGame?.dateEnd, dateFormatter)

                    holder.itemView.findViewById<TextView>(R.id.textView33).text =
                        (abs(ChronoUnit.HOURS.between(dateEnd, dateNow))).toString() + " ч"
                }
            }else if((_allGames != null) && (_allGames!!.games.isNotEmpty()) && (position > 0)){
                var game = _allGames!!.games[position - 1];

                val dateBeginString = game.dateBegin
                    ?.split("T")?.get(0)
                    ?.split('-')
                val strBeginDate = dateBeginString?.get(2) + "." + dateBeginString?.get(1)

                val dateEndString = game.dateEnd
                    ?.split("T")?.get(0)
                    ?.split('-')
                val strEndDate = dateEndString?.get(2) + "." + dateEndString?.get(1)

                holder.itemView.findViewById<TextView>(R.id.textView39).text = "$strBeginDate - $strEndDate"
                holder.itemView.findViewById<TextView>(R.id.tv_pointerCountGame).text =
                    game.countQuests.toString() + " точек"
                holder.tvTeamName?.text = game.name
                holder.tvAgeTeam?.text = game.ageLimit.toString() + "+"

                holder.textView37?.visibility = View.INVISIBLE
                holder.view16?.visibility     = View.INVISIBLE
                holder.textView38?.visibility = View.INVISIBLE
                holder.clGameIsBegin?.visibility = View.INVISIBLE
            }
        }else if(position != 0){
            if((_allGames == null)
                || (_allGames!!.games.isEmpty())
                || ((position-1) >= _allGames!!.games.size)){
                return
            }

            // Обработка ситуации, когда нет текущей игры на которую зарегистрирована команда
            var game = _allGames!!.games[position-1]

            val dateBeginString = game.dateBegin
                ?.split("T")?.get(0)
                ?.split('-')
            val strBeginDate = dateBeginString?.get(2) + "." + dateBeginString?.get(1)

            val dateEndString = game.dateEnd
                ?.split("T")?.get(0)
                ?.split('-')
            val strEndDate = dateEndString?.get(2) + "." + dateEndString?.get(1)

            holder.itemView.findViewById<TextView>(R.id.textView39).text = "$strBeginDate - $strEndDate"
            holder.itemView.findViewById<TextView>(R.id.tv_pointerCountGame).text =
                game.countQuests.toString() + " точек"
            holder.tvTeamName?.text = game.name
            holder.tvAgeTeam?.text = game.ageLimit.toString() + "+"
            holder.textView37?.visibility = View.INVISIBLE
            holder.view16?.visibility     = View.INVISIBLE
            holder.textView38?.visibility = View.INVISIBLE
            holder.clGameIsBegin?.visibility = View.INVISIBLE
        }
    }
}