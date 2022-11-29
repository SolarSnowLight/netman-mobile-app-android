package com.game.mobileappar.utils.recycler

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.game.mobileappar.containers.base.BaseAdapter

fun <T, B: ViewBinding> RecyclerView.setAdapter(
    context: Context?,
    adapter: BaseAdapter<T, B>?,
    reverseLayout: Boolean = false,
    scrollView: Boolean = true
){
    val layoutManager: RecyclerView.LayoutManager
            = LinearLayoutManager(
        context,
        RecyclerView.HORIZONTAL,
        reverseLayout
    )

    this.layoutManager = layoutManager
    this.adapter = adapter

    if(scrollView){
        this.addOnScrollListener(RecyclerScrollListener(layoutManager))
    }
}