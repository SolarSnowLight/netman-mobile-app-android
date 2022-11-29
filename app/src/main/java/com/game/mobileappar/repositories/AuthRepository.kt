package com.game.mobileappar.repositories

import com.game.mobileappar.data.UserPreferences
import com.game.mobileappar.models.auth.AuthLoginModel
import com.game.mobileappar.models.auth.AuthRegisterModel
import com.game.mobileappar.network.apis.AuthApi
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/*
* Репозиторий для взаимодействия с пользовательской авторизацией.
* В качестве параметров ему передаётся используемое API
* и ссылка на экземпляр хранилища данных
* */

class AuthRepository(
    private val api: AuthApi,
    private val preferences: UserPreferences
) : BaseRepository(){

    // Реализация функции авторизации
    suspend fun login(
        email: String,
        password: String
    ) = safeApiCall {
        // Формирование тела запроса в виде JSON-строки
        var requestBody = Gson().toJson(
            AuthLoginModel(
                email = email,
                password = password
            )
        ).toRequestBody("application/json".toMediaTypeOrNull())

        // Авторизация пользователя
        api.login(requestBody)
    }

    // Реализация функции регистрации
    suspend fun register(
        data: AuthRegisterModel
    ) = safeApiCall {
        var requestBody = Gson().toJson(
            data
        ).toRequestBody("application/json".toMediaTypeOrNull())

        // Регистрация пользователя
        api.register(requestBody)
    }

    // Сохранение авторизационных данных
    suspend fun saveAuthData(authData: String){
        preferences.saveAuthData(authData)
    }
}