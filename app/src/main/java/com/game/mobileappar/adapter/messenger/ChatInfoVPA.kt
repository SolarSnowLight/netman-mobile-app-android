package com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.messenger

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.game.mobileappar.fragment.team.TeamListPlayersFragment

class ChatInfoVPA(fm: FragmentManager, private var string: String) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // Нельзя тут использовать PlayersTeamListFragment
        var fragment: Fragment = TeamListPlayersFragment(string, null);
        if (position == 0) {
            fragment = TeamListPlayersFragment(string, null)
        }
        if (position == 1) {
            //fragment = Galler(string, null)
        }


        return fragment
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title: String? = null
        if (position == 0) {
            title = "Участники"
        }
        if (position == 1) {
            title = "Медиа"
        }
        if(position == 2){
            title = "Ссылки"
        }

        return title
    }
}