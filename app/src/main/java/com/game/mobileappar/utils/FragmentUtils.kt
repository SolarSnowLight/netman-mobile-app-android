package com.game.mobileappar.utils

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.game.mobileappar.network.Resource

// Вызов обработчика ошибок для фрагментов
fun Fragment.handleApiError(
    failure: Resource.Failure,
    retry: (() -> Unit)? = null
){
    when{
        failure.isNetworkError -> {
            requireView().snackbar("Пожалуйста, проверьте интернет соединение", retry)
        }
        failure.errorCode == 401 -> {
            /*if(this is LoginFragment){
                requireView().snackbar("Пользователь не авторизован")
            }else{
                (this as BaseFragment<*, *, *>).logout()
            }*/
        }
        else -> {
            val error = failure.errorBody?.string().toString()
            requireView().snackbar(error)
        }
    }
}

fun Fragment.handleError(
    message: String
){
    requireView().snackbar(message)
}

fun Fragment.hideKeyboard(){
    // Скрытие клавиатуры, перед выводом сообщения об ошибке
    val imm: InputMethodManager =
        activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}

fun Fragment.navigation(
    resId: Int,
    args: Bundle? = null
){
    view?.findNavController()?.navigate(resId, args)
}