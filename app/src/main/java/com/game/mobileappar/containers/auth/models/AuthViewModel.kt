package com.game.mobileappar.containers.auth.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.game.mobileappar.containers.base.BaseViewModel
import com.game.mobileappar.models.auth.AuthRegisterModel
import com.game.mobileappar.network.Resource
import com.game.mobileappar.repositories.AuthRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

/*
* ViewModel для фрагментов авторизации и регистрации.
* Содержит в себе определённые данные и методы,
* позволяющие эти данные модифицировать
* */

class AuthViewModel(
    private val repository: AuthRepository
) : BaseViewModel(repository) {

    // Результат авторизации пользователя (LiveData)
    private val _loginResponse : MutableLiveData<Resource<Response<ResponseBody>>> = MutableLiveData()
    val loginResponse : LiveData<Resource<Response<ResponseBody>>>
        get() = _loginResponse

    private val _registerResponse : MutableLiveData<Resource<Response<ResponseBody>>> = MutableLiveData()
    val registerResponse : LiveData<Resource<Response<ResponseBody>>>
        get() = _registerResponse

    // Функция авторизации пользователя, запускаемая в контексте viewModel
    // (это позволяет осуществлять работу в зависимости от состояния ViewModel)
    fun login(
        email: String,
        password: String
    ) = viewModelScope.launch {
        // Установка данных по умолчанию (загрузка ресурсов)
        _loginResponse.value = Resource.Loading

        // Изменение значения на возвращаемый функциоей авторизации из репозитория
        _loginResponse.value = repository.login(email, password)
    }

    // Функция авторизации пользователя
    fun register(
        data: AuthRegisterModel
    ) = viewModelScope.launch {
        _registerResponse.value = Resource.Loading
        _registerResponse.value = repository.register(data)
    }

    // Сохранение данных пользователя
    suspend fun saveAuthData(authData: String){
        repository.saveAuthData(authData)
    }
}