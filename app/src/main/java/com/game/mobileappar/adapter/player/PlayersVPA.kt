package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.player

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.game.mobileappar.fragment.team.TeamListPlayersFragment

class PlayersVPA(fm: FragmentManager,
                 private var _playersTeamList: TeamListPlayersFragment?
                ) : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int {
        return 1
    }

    override fun getItem(position: Int): Fragment {
        return _playersTeamList!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "Игроки"
    }
}