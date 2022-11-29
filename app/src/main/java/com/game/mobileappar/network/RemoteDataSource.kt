package com.game.mobileappar.network

import com.game.mobileappar.BuildConfig
import com.game.mobileappar.constants.addresses.main.MainApiConstants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class RemoteDataSource {
    fun <Api> buildApi(
        api: Class<Api>,
        accessToken: String? = null,
        serverAddress: String = MainApiConstants.MAIN_SERVER
    ): Api {
        return Retrofit.Builder()
            .baseUrl(serverAddress)
            .client(OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder().also {
                        it.addHeader("Authorization", "Bearer $accessToken")
                    }.build())
                }
                .also { client ->
                    if(BuildConfig.DEBUG){
                        val logging = HttpLoggingInterceptor()
                        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
                        client.addInterceptor(logging)
                    }
                }.build())
            .build()
            .create(api)
    }
}