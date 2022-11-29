package com.game.mobileappar

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.game.mobileappar.adapter.menu.GameListAdapter
import com.game.mobileappar.adapter.menu.NovelListAdapter
import com.game.mobileappar.models.menu.MenuInfoGameModel
import com.game.mobileappar.utils.recycler.RecyclerScrollListener

class MainMenuActivity: AppCompatActivity() {
    private var prevFocusView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        setContentView(R.layout.activity_main_menu)

        val searchView = findViewById<SearchView>(R.id.svTopMainMenu)
        val gameListRecycler = findViewById<RecyclerView>(R.id.rvGameListMainMenu)
        val novelListRecycler = findViewById<RecyclerView>(R.id.rvNovelListMainMenu)

        val gameList: ArrayList<MenuInfoGameModel> = arrayListOf()
        gameList.add(
            MenuInfoGameModel(
            age = 18,
            name = "Название игры 1"
        ))

        gameList.add(MenuInfoGameModel(
            age = 11,
            name = "Название игры 2"
        ))

        gameList.add(MenuInfoGameModel(
            age = 17,
            name = "Название игры 3"
        ))

        gameList.add(MenuInfoGameModel(
            age = 16,
            name = "Название игры 4"
        ))

        setGameListRecycler(gameListRecycler, gameList)
        setNovelListRecycler(novelListRecycler, gameList)

        initMainSearchView(searchView)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event!!.action == MotionEvent.ACTION_DOWN){
            val v: View? = currentFocus

            if (v is EditText) {
                if(currentFocus != prevFocusView){
                    v.clearFocus()
                    val imm: InputMethodManager =
                        this.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }

                prevFocusView = currentFocus
            }
        }
        return super.onTouchEvent(event)
    }

    private fun initMainSearchView(searchView: SearchView?){
        if(searchView == null)
            return

        searchView.setOnClickListener {
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchView.isIconified = false
                return false
            }
        })

        searchView.setOnCloseListener {
            searchView.clearFocus()
            val imm: InputMethodManager =
                this.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchView.windowToken, 0)

            false
        }
    }

    private fun setGameListRecycler(recycler: RecyclerView?,
                                        gameList: ArrayList<MenuInfoGameModel>) {
        val layoutManager: RecyclerView.LayoutManager
                = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false,
        )

        recycler?.layoutManager = layoutManager
        recycler?.adapter = GameListAdapter(this, gameList)
        recycler?.addOnScrollListener(RecyclerScrollListener(layoutManager))
    }

    private fun setNovelListRecycler(recycler: RecyclerView?,
                                    gameList: ArrayList<MenuInfoGameModel>) {
        val layoutManager: RecyclerView.LayoutManager
                = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false,
        )

        recycler?.layoutManager = layoutManager
        recycler?.adapter = NovelListAdapter(this, gameList)
        recycler?.addOnScrollListener(RecyclerScrollListener(layoutManager))
    }
}