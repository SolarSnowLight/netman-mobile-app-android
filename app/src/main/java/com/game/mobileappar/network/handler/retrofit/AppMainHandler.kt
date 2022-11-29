package com.game.mobileappar.network.handler.retrofit

import android.content.Context
import com.game.mobileappar.config.ConfigAddresses
import com.game.mobileappar.config.ConfigStorage
import com.game.mobileappar.helpers.CoreHelper
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.models.auth.AuthRefreshTokenModel
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import retrofit2.Retrofit
import java.io.IOException

class AppMainHandler {
    companion object{
        public fun getRetrofit(): Retrofit {
            val httpClient: OkHttpClient =
                OkHttpClient.Builder().addInterceptor(AppMainHandler.AuthorizationInterceptor())
                    .build()

            return Retrofit.Builder()
                .baseUrl(ConfigAddresses.SERVER_CENTRAL_ADDRESS)
                .client(httpClient)
                .build()
        }
    }

    // Интерцептор для перехвата статус кода 401 (пользователь не авторизован)
    internal class AuthorizationInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original: Request = chain.request()

            var gson: Gson = Gson()
            var sharedData = CoreHelper.contextGetter?.invoke()?.getSharedPreferences(ConfigStorage.LOCAL_STORAGE, Context.MODE_PRIVATE)
            var localData = gson.fromJson(sharedData?.getString(ConfigStorage.USERS_DATA, null), UserDataModel::class.java)

            var originalRequest: Request = original.newBuilder()
                .header("Authorization", "Bearer ${localData.typeAuth} ${localData.accessToken}")
                .method(original.method, original.body)
                .build()

            var responseBody = chain.proceed(originalRequest)

            // Code 401 on HTTP is UnAuthorization
            if(responseBody.code == 401){
                responseBody.close()

                val updateToken = gson?.toJson(
                    AuthRefreshTokenModel(
                        refreshToken = localData.refreshToken,
                        typeAuth = localData.typeAuth
                    )
                )

                val requestBody = updateToken?.toRequestBody("application/json".toMediaTypeOrNull())

                var request: Request = Request.Builder()
                    .url(ConfigAddresses.SERVER_CENTRAL_ADDRESS + "/" + ConfigAddresses.AUTH_REFRESH_TOKEN)
                    .method("POST", requestBody)
                    .build()

                responseBody = chain.proceed(request)

                println(responseBody)
                if(responseBody.code == 201){
                    val updateLocalData = gson.toJson(
                        JsonParser.parseString(responseBody.body?.string())
                    )

                    localData = gson.fromJson(updateLocalData, UserDataModel::class.java)

                    var editor = sharedData?.edit()
                    editor?.putString(ConfigStorage.USERS_DATA, updateLocalData)
                    editor?.apply()

                    println("NEW")
                    println(updateLocalData)
                    originalRequest = original.newBuilder()
                        .header("Authorization", "Bearer ${localData.typeAuth} ${localData.accessToken}")
                        .method(original.method, original.body)
                        .build()

                    responseBody.close()
                    responseBody = chain.proceed(originalRequest)
                }
            }

            return responseBody
        }
    }
}