package com.game.mobileappar.containers.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.game.mobileappar.containers.auth.models.AuthViewModel
import com.game.mobileappar.repositories.AuthRepository
import com.game.mobileappar.repositories.BaseRepository
import java.lang.IllegalArgumentException

/*
* Элемент ViewModelFactory используется для возможности передавать ViewModel,
* которые закреплены за различными фрагментами и активностями определённые
* данные, которые могут быть переданны в виде аргументов
* */

class ViewModelFactory(
    private val repository: BaseRepository
) : ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository as AuthRepository) as T
            // modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository as UserRepository) as T
            else -> throw IllegalArgumentException("ViewModelClass Not Found")
        }
    }
}