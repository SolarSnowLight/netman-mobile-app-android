package com.game.mobileappar.network.apis

import com.game.mobileappar.constants.addresses.auth.AuthApiConstants
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    // Авторизация пользователя (обычное)
    @POST(AuthApiConstants.LOGIN)
    suspend fun login(@Body requestBody: RequestBody): Response<ResponseBody>

    // Авторизация пользователя (через Google OAuth)
    @POST(AuthApiConstants.OAUTH)
    suspend fun loginOAuth(@Body requestBody: RequestBody): Response<ResponseBody>

    // Регистрация пользователя в системе
    @POST(AuthApiConstants.REGISTER)
    suspend fun register(@Body requestBody: RequestBody): Response<ResponseBody>

    // Обновление токена доступа
    @POST(AuthApiConstants.REFRESH_TOKEN)
    suspend fun refreshToken(@Body requestBody: RequestBody): Response<ResponseBody>

    // Выход из системы
    @POST(AuthApiConstants.LOGOUT)
    suspend fun logout(@Body requestBody: RequestBody): Response<ResponseBody>
}