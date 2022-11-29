/*
* Сервис для игровых уведомлений
* */

package com.game.mobileappar.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.network.handler.retrofit.AppMainHandler
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.models.GameStatusModel
import com.game.mobileappar.network.service.APIService
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class NotificationService : Service() {
    // Основной фоновый поток, в котором будут выполняться операции
    // по обращению к серверу с целью проверки запуска игр, на которые
    // зарегистрирован данный пользователь
    private var _coroutineScope: Job? = null
    private val channelId = "NOTIFICATION_ID"
    private val notificationId = 101

    override fun onBind(intent: Intent): IBinder {
        return TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        _coroutineScope = CoroutineScope(Dispatchers.IO).launch {
            try{
                var running = true
                var shared = getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
                var gson = Gson()
                val retrofit = AppMainHandler.getRetrofit()

                val service = retrofit.create(APIService::class.java)

                while(running){
                    var data = gson.fromJson(shared.getString(ConfigStorage.USERS_DATA, null),
                    UserDataModel::class.java)
                    var strData = gson.toJson(data)

                    val requestBody = strData.toRequestBody("application/json".toMediaTypeOrNull())
                    val response = service.isGameActiveExists(requestBody)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            var prettyJson = gson.toJson(
                                JsonParser.parseString(response.body()?.string())
                            );

                            val error = gson.fromJson(prettyJson, ErrorDataModel::class.java)

                            if((error.errors == null) && (error.message == null)){
                                // Реализация вывода уведомления ...
                                var gameStatus = gson.fromJson(prettyJson, GameStatusModel::class.java)

                                running = false
                                createNotificationChannel()

                                if(gameStatus.judge){
                                    sendNotification("Вы были выбраны судъей! Зайдите в игру чтобы начать судейство!")
                                }else{
                                    sendNotification("Игра, в которой участвует Ваша команда, уже началась!")
                                }
                            }else{
                            }
                        }
                    }
                    delay(5000L)
                }
            }catch(e: CancellationException){ }
            this@NotificationService.stopSelf()
            // Остановка службы в случае, когда статус в игре обнаружен
        }

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        _coroutineScope?.cancel()
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Title"
            val descriptionText = "Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(text: String?){
        val intent = Intent(this, MainActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val remoteViews = RemoteViews(packageName, R.layout.notification_layout)
        remoteViews.setTextViewText(R.id.notification_content, text)
        remoteViews.setOnClickPendingIntent(R.id.notification, pendingIntent)

        val builder = NotificationCompat.Builder(this, channelId)
            .setCustomContentView(remoteViews)
            .setSmallIcon(R.drawable.ic_game)
            /*.setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Игра, в которой участвует Ваша команда, уже началась! Зайдите в приложение" +
                    " чтобы узнать больше информации"))*/
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        with(NotificationManagerCompat.from(this)){
            notify(notificationId, builder.build())
        }
    }
}