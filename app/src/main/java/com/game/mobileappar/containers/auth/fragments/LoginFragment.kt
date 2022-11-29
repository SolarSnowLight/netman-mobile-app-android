package com.game.mobileappar.containers.auth.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.game.mobileappar.MainActivity
import com.game.mobileappar.R
import com.game.mobileappar.containers.auth.models.AuthViewModel
import com.game.mobileappar.containers.base.BaseFragment
import com.game.mobileappar.databinding.FragmentLoginBinding
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.network.Resource
import com.game.mobileappar.network.apis.AuthApi
import com.game.mobileappar.repositories.AuthRepository
import com.game.mobileappar.utils.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.launch

/*
* Фрагмент авторизации пользователя, который является наследником
* BaseFragment, с использованием ViewModel для авторизации, класс
* FragmentLoginBinding сгенерированный средой для быстрой связки с
* элементами вёрстки, а также AuthRepository, который предоставляет
* функционал для работы с данными
* */

class LoginFragment : BaseFragment<AuthViewModel, FragmentLoginBinding, AuthRepository>() {

    // Обработка одного из методов жизненного цикла фрагмента,
    // который обозначает что содержащая его активность вызвала
    // метод onCreate() и полностью создалась, таким образом
    // предоставив возможность использовать данные активности
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Первоначальная установка состояний компонентов
        binding.pbLogin.visible(false)
        binding.btnLogin.enable(false)

        // Устанавливаем прослушивание на изменение данных loginResponse из viewModel
        // абстрактного класса (который был сгенерирован на основе переданного AuthViewModel)
        viewModel.loginResponse.observe(viewLifecycleOwner, {
            // Состояние компонента зависит от того, находится ли ресурс
            // в состоянии загрузки или нет
            binding.pbLogin.visible(it is Resource.Loading)
            hideKeyboard()

            when(it){
                // Обработка успешного сетевого взаимодействия
                is Resource.Success -> {
                    if(it.value.isSuccessful){
                        // Запуск в контексте жизненного цикла
                        lifecycleScope.launch {
                            // Сохранение полученных данных в формате JSON-строки
                            viewModel.saveAuthData(Gson().toJson(JsonParser.parseString(it.value.body()?.string())))

                            // Запуск новой активности
                            requireActivity().startNewActivity(MainActivity::class.java)
                        }
                    }else{
                        handleError(Gson().fromJson(it.value.errorBody()?.string().toString(), ErrorDataModel::class.java).message!!)
                    }
                }

                // Обработка ошибок связанные с сетью
                is Resource.Failure -> {
                    handleApiError(it){ login() }
                }
                else -> {}
            }
        })

        // Установка поведения на изменения текста в элементе управления
        binding.etPasswordLogin.addTextChangedListener {
            val email = binding.etEmailLogin.text.toString().trim()
            binding.btnLogin.enable(email.isNotEmpty() && it.toString().isNotEmpty())
        }

        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.tvRefRegisterLogin.setOnClickListener {
            navigation(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = AuthRepository(remoteDataSource.buildApi(AuthApi::class.java), userPreferences)

    // Функционал элемента управления, который был выведен в отдельную функцию
    private fun login(){
        val email = binding.etEmailLogin.text.toString().trim()
        val password = binding.etPasswordLogin.text.toString()

        viewModel.login(email, password)
    }
}