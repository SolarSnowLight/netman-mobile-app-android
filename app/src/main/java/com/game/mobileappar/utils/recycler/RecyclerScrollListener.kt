package com.game.mobileappar.utils.recycler

import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerScrollListener<T> (private var childFragment: T?) : RecyclerView.OnScrollListener() {
    private var lastDx = 0
    private  var _currentPos = 0

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            val lm = recyclerView.layoutManager as LinearLayoutManager?

            val pos =
                if (lastDx > 0)
                    lm!!.findLastVisibleItemPosition()
                else
                    lm!!.findFirstVisibleItemPosition()
            recyclerView.smoothScrollToPosition(pos)

            _currentPos = pos
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        lastDx = dx
    }
}