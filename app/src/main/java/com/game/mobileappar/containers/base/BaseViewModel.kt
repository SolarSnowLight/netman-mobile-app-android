package com.game.mobileappar.containers.base

import androidx.lifecycle.ViewModel
import com.game.mobileappar.network.apis.AuthApi
import com.game.mobileappar.repositories.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
* Базовая ViewModel, которая используется BaseFragment для связи видов с данными.
* ViewModel - класс, позволяющий Activity и фрагментам сохранять
* необходимые им объекты живыми при повороте экрана
* */
abstract class BaseViewModel(
    private val repository: BaseRepository
): ViewModel() {
    // Асинхронная функция выхода из системы.
    // Выполняется в контексте потока IO
    suspend fun logout(usersId: Int,
                       accessToken: String,
                       refreshToken: String,
                       typeAuth: Int,
                       api: AuthApi
    ) = withContext(Dispatchers.IO) {
        repository.logout(usersId, accessToken, refreshToken, typeAuth, api)
    }
    //suspend fun logout(api: UserApi) = repository.logout(api)
}