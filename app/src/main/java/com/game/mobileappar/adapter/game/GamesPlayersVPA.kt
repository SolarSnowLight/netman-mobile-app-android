package com.game.mobileappar.adapter.game

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.game.mobileappar.config.game.ConfigStatusPlayer
import com.game.mobileappar.fragment.team.TeamGamesFragment
import com.game.mobileappar.fragment.team.TeamListPlayersFragment

class GamesPlayersVPA(fm: FragmentManager,
                      private var listType: String,
                      private var gameType: String,
                      private var commandsId: Int? = null,
                      private var playerStatus: Byte = ConfigStatusPlayer.PLAYER_DEFAULT
                      ) : FragmentStatePagerAdapter(fm) {

    // Возвращает определённый элемент в зависимости от позиции, на которой он находится
    override fun getItem(position: Int): Fragment {
        if(position == 1){
            return TeamGamesFragment(gameType, commandsId)
        }

        return TeamListPlayersFragment(listType, commandsId, playerStatus) // Возвращение фрагмента определённого типа
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        if (position == 0) {
            return "Игроки"
        }

        return "Игры"
    }
}