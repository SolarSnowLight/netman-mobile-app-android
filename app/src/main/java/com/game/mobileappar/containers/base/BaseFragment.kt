package com.game.mobileappar.containers.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.game.mobileappar.containers.auth.AuthActivity
import com.game.mobileappar.data.UserPreferences
import com.game.mobileappar.models.user.UserDataModel
import com.game.mobileappar.network.RemoteDataSource
import com.game.mobileappar.network.apis.AuthApi
import com.game.mobileappar.repositories.BaseRepository
import com.game.mobileappar.utils.startNewActivity
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/*
* Абстрактный класс для фрагментов (для поддержки архитектуры MVVM)
* */

abstract class BaseFragment<VM: BaseViewModel, B: ViewBinding, R: BaseRepository> : Fragment() {

    // Локальное хранилище пользовательских данных
    protected lateinit var userPreferences: UserPreferences

    // Класс ViewBinding, с помощью которого можно
    // осуществлять быстрый доступ к элементам вёрстки
    protected lateinit var binding : B

    // Ссылка на ViewModel, которая будет использована
    // для связывания данных и вёрстки
    protected lateinit var viewModel : VM

    // Инструмент для взаимодействия с сетью (формирует объект Retrofit)
    protected val remoteDataSource = RemoteDataSource()

    // Обработка одного из методов жизненного цикла фрагмента (создание вида)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Связывание пользовательских данных с экземпляром объекта UserPreferences
        userPreferences = UserPreferences(requireContext())

        // Связывание класса ViewBinding с конкретным экземпляром
        binding = getFragmentBinding(inflater, container)

        // Получение ViewModelFactory по определённому репозиторию
        val factory = ViewModelFactory(getFragmentRepository())

        // Получение ViewModel с помощью ViewModelFactory
        viewModel = ViewModelProvider(this, factory)[getViewModel()]

        // Запуск в рамках жизненного цикла получения
        // первого объекта из потока объектов Flow
        lifecycleScope.launch {
            userPreferences.authData.first()
        }

        // Возврат вида фрагмента
        return binding.root
    }

    abstract fun getViewModel() : Class<VM>
    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) : B
    abstract fun getFragmentRepository() : R

    // Реализация функции logout, для использования в других фрагментах
    // Запуск осуществляется в рамках жизненного цикла фрагмента
    // (иначе - в контексте жизненного цикла фрагмента)
    fun logout() = lifecycleScope.launch {
        // Получение первых данных из потока данных
        val data = userPreferences.authData.first()

        // Преобразование данных из JSON-формата в объект
        val dataObj = Gson().fromJson(data, UserDataModel::class.java)

        // Формирование API для взаимодействия с сервером
        val api = remoteDataSource.buildApi(AuthApi::class.java)

        // Вызов модели фрагмента (заранее установленного)
        // и передача ему созданного API
        viewModel.logout(dataObj.usersId,
            dataObj.accessToken,
            dataObj.refreshToken,
            dataObj.typeAuth, api)

        // Очистка пользовательских данных
        userPreferences.clear()

        // Открытие новой активности авторизации
        requireActivity().startNewActivity(AuthActivity::class.java)
    }
}

