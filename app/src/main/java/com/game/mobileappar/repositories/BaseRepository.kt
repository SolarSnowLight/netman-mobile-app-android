package com.game.mobileappar.repositories

// import com.game.mobileappar.models.auth.AuthLogoutModel
import com.game.mobileappar.network.Resource
import com.game.mobileappar.network.apis.AuthApi
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

/*
* Базовый репозиторий.
* Используется в BaseFragment, BaseViewModel и ViewModelFactory.
* */

abstract class BaseRepository {
    // Защищённый вызов API
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ) : Resource<T> {
        // Используется контекст IO
        return withContext(Dispatchers.IO){
            // Возвращаемое значение будет представлять собой
            // либо Resource.Success, либо Resource.Failure
            try{
                // Возврат осуществляется без явного указания return
                Resource.Success(apiCall.invoke())
            }catch (throwable: Throwable){
                when(throwable){
                    is HttpException -> {
                        Resource.Failure(false, throwable.code(),
                            throwable.response()?.errorBody()
                        )
                    }
                    else -> {
                        Resource.Failure(true, null, null)
                    }
                }
            }
        }
    }

    // Безопасный вызов функции logout
    suspend fun logout(usersId: Int,
                       accessToken: String,
                       refreshToken: String,
                       typeAuth: Int,
                       api: AuthApi
    ) = safeApiCall {
        val requestBody = null /*Gson().toJson(
            AuthLogoutModel(
                usersId = usersId,
                accessToken = accessToken,
                refreshToken = refreshToken,
                typeAuth = typeAuth
            )
        ).toRequestBody("application/json".toMediaTypeOrNull())*/

        //api.logout(requestBody)
    }
}