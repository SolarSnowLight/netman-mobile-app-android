package com.game.mobileappar.fragment.team

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.thesimplycoder.simpledatepicker.com.game.mobileappar.adapter.team.TeamAdapter
import com.game.mobileappar.adapter.game.GamesPlayersVPA
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.PlayerAccessModel
import com.game.mobileappar.models.command.list.CommandListModel
import com.game.mobileappar.network.service.APIService
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

import kotlin.time.*
import android.view.MotionEvent

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.game.mobileappar.models.PlayerCommandDataModel
import com.game.mobileappar.models.command.CreateCommandModel
import com.game.mobileappar.components.toast.CustomToast
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TeamFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var adapter: TeamAdapter? = null
        private var viewPagerAdapter: GamesPlayersVPA? = null
        private var viewPager: ViewPager? = null
        private var tabLayout: TabLayout? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        var shared = context?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
        val view: View = inflater.inflate(R.layout.team_find_team_fragment, container, false)
        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tab_layout)

        val rvTeamListHorizontal: RecyclerView = view.findViewById(R.id.testrw);

        val layoutManager: LinearLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvTeamListHorizontal.layoutManager = layoutManager
        rvTeamListHorizontal.setHasFixedSize(true)
        rvTeamListHorizontal.isNestedScrollingEnabled = false

        rvTeamListHorizontal.addOnItemTouchListener(
            RecyclerItemClickListener(context, rvTeamListHorizontal, object :
                RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    if(shared!!.contains(ConfigStorage.USERS_DATA)) {
                        val localData = shared.getString(ConfigStorage.USERS_DATA, null)
                        val retrofit = AppMainHandler.getRetrofit()

                        var gson = Gson()
                        var dataLocal: UserDataModel? =
                            gson.fromJson(localData, UserDataModel::class.java)
                        val service = retrofit.create(APIService::class.java)

                        var dataAccessPlayer = gson.toJson(
                            PlayerCommandDataModel(
                                usersId = dataLocal!!.usersId,
                                accessToken = dataLocal!!.accessToken,
                                commandsId = adapter!!.getDataCommands().commands[position].id
                            )
                        );

                        val requestBody =
                            dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull())

                        CoroutineScope(Dispatchers.IO).launch {
                            var responseBody = service.funPlayerCommandJoin(requestBody)
                            withContext(Dispatchers.Main) {
                                if (responseBody.isSuccessful) {
                                    val result = gson.toJson(
                                        JsonParser.parseString(responseBody.body()?.string())
                                    );

                                    val error = gson.fromJson(result, ErrorDataModel::class.java)

                                    // Проверка валидности данных
                                    if ((error.errors == null) && (error.message == null)) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            CustomToast.makeText(this@TeamFragment.requireContext(), "Успешный вход в команду!").show()
                                            (activity as MainActivity).onBackPressed()
                                        }
                                    }else{
                                        if(error.message != null){
                                            CoroutineScope(Dispatchers.Main).launch {
                                                CustomToast.makeText(this@TeamFragment.requireContext(), error.message).show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onLongItemClick(view: View?, position: Int) {}

                override fun onItemClick(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {}
            })
        )

        //view.findViewById<ImageView>(R.id.imageView12).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_teamFragment_to_inTeamFragment, null))
        view.findViewById<ImageView>(R.id.imageView12).setOnClickListener{
            var dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            var inflater = (activity as MainActivity).layoutInflater
            var viewDialog = inflater.inflate(R.layout.dialog_create_command_layout, null)
            dialogBuilder.setView(viewDialog)
            val dialog: androidx.appcompat.app.AlertDialog? = dialogBuilder.show()

            viewDialog.findViewById<Button>(R.id.cancel_create_command).setOnClickListener(View.OnClickListener {
                dialog?.dismiss()
            })
            var commandName = viewDialog.findViewById<EditText>(R.id.create_command_name)
            viewDialog.findViewById<Button>(R.id.accept_create_command).setOnClickListener(View.OnClickListener {
                dialog?.dismiss()
                if(commandName.text.toString().length < 3){
                    CoroutineScope(Dispatchers.Main).launch {
                        CustomToast.makeText(requireContext(), "Название команды должно содержать" +
                                " как минимум 3 символа!").show();
                    }
                    return@OnClickListener
                }

                if(shared!!.contains(ConfigStorage.USERS_DATA)){
                    val localData = shared.getString(ConfigStorage.USERS_DATA, null)
                    val retrofit = AppMainHandler.getRetrofit()

                    var gson = Gson()
                    var dataLocal: UserDataModel? = gson.fromJson(localData, UserDataModel::class.java)
                    val service = retrofit.create(APIService::class.java)

                    var dataAccessPlayer = gson.toJson(
                        CreateCommandModel(
                            usersId = dataLocal!!.usersId,
                            accessToken = dataLocal!!.accessToken,
                            name = commandName.text.toString()
                        )
                    );

                    val requestBody = dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull())

                    CoroutineScope(Dispatchers.IO).launch {
                        var responseBody = service.funCommandCreate(requestBody)
                        withContext(Dispatchers.Main) {
                            if (responseBody.isSuccessful) {
                                val result = gson.toJson(
                                    JsonParser.parseString(responseBody.body()?.string())
                                )

                                val error = gson.fromJson(result, ErrorDataModel::class.java)

                                // Проверка валидности данных
                                if((error.errors == null) && (error.message == null)){
                                    CoroutineScope(Dispatchers.Main).launch {
                                        CustomToast.makeText(requireContext(), "Команда создана!").show()
                                        (activity as MainActivity).onBackPressed()
                                    }
                                }else{
                                    if((error.errors == null) && (error.errors!!.isNotEmpty())){
                                        CustomToast.makeText(requireContext(), error.errors!![0].msg).show()
                                    }

                                    delay(500)
                                    CustomToast.makeText(requireContext(), error.message).show()
                                }
                            }
                        }
                    }
                }
                dialog?.dismiss()
            })
        }

        view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar2).setNavigationOnClickListener{
            (activity as MainActivity).onBackPressed()
        }

        if(shared!!.contains(ConfigStorage.USERS_DATA)){
            val localData = shared.getString(ConfigStorage.USERS_DATA, null)
            val retrofit = AppMainHandler.getRetrofit()

            var gson = Gson();
            var dataLocal: UserDataModel? = gson.fromJson(localData, UserDataModel::class.java)
            val service = retrofit.create(APIService::class.java)

            var dataAccessPlayer = gson.toJson(
                PlayerAccessModel(
                    usersId = dataLocal!!.usersId,
                    accessToken = dataLocal!!.accessToken
                )
            )

            val requestBody = dataAccessPlayer.toRequestBody("application/json".toMediaTypeOrNull())

            CoroutineScope(Dispatchers.IO).launch {
                var responseBody = service.funCommandList(requestBody);
                withContext(Dispatchers.Main) {
                    if (responseBody.isSuccessful) {
                        val result = gson.toJson(
                            JsonParser.parseString(responseBody.body()?.string())
                        );

                        val error = gson.fromJson(result, ErrorDataModel::class.java);

                        // Проверка валидности данных
                        if((error.errors == null) && (error.message == null)){
                            var data = gson.fromJson(result, CommandListModel::class.java);
                            CoroutineScope(Dispatchers.Main).launch {
                                adapter = view.context?.let { TeamAdapter(it) }
                                rvTeamListHorizontal.adapter = adapter
                                rvTeamListHorizontal.itemAnimator = DefaultItemAnimator()
                                rvTeamListHorizontal.addOnScrollListener(ScrollListener(childFragmentManager))
                                adapter?.setDataCommands(data)

                                if(data.commands.isNotEmpty()){
                                    childFragmentManager.fragments.clear();
                                    viewPagerAdapter = GamesPlayersVPA(childFragmentManager,
                                        "","",
                                        adapter!!.getDataCommands().commands[0].id)
                                    viewPager?.adapter = viewPagerAdapter
                                    tabLayout?.setupWithViewPager(viewPager)
                                }
                            }
                        }
                    }
                }
            }
        }

        viewPager?.adapter = viewPagerAdapter
        tabLayout?.setupWithViewPager(viewPager)
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                Navigation.createNavigateOnClickListener(R.id.action_teamFragment_to_inTeamFragment, null)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

     class ScrollListener (private var childFragment: FragmentManager?) : RecyclerView.OnScrollListener() {
        private var lastDx = 0;
        private  var _currentPos = 0;

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val lm = recyclerView.layoutManager as LinearLayoutManager?

                val pos =
                    if (lastDx > 0)
                        lm!!.findLastVisibleItemPosition()
                    else
                        lm!!.findFirstVisibleItemPosition()
                recyclerView.smoothScrollToPosition(pos)

                if((_currentPos != pos) && (adapter != null)){
                    viewPagerAdapter = GamesPlayersVPA(childFragment!!,"","",
                        adapter!!.getDataCommands().commands[pos].id)
                    viewPager?.adapter = viewPagerAdapter
                    tabLayout?.setupWithViewPager(viewPager)
                }

                _currentPos = pos;
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            lastDx = dx
        }
    }

    class RecyclerItemClickListener(
        context: Context?,
        recyclerView: RecyclerView,
        private val mListener: OnItemClickListener?
    ) :
        OnItemTouchListener {
        interface OnItemClickListener {
            fun onItemClick(view: View?, position: Int)
            fun onLongItemClick(view: View?, position: Int)
            fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
        }

        var mGestureDetector: GestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null && mListener != null) {
                    mListener.onLongItemClick(
                        child,
                        recyclerView.getChildAdapterPosition(child)
                    )
                }
            }
        })

        override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
            val childView = view.findChildViewUnder(e.x, e.y)
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
                return true
            }
            return false
        }

        override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    }
}


