package com.game.mobileappar.containers.auth.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.game.mobileappar.MainActivity
import com.game.mobileappar.containers.auth.models.AuthViewModel
import com.game.mobileappar.containers.base.BaseFragment
import com.game.mobileappar.databinding.FragmentRegisterBinding
import com.game.mobileappar.models.auth.AuthRegisterModel
import com.game.mobileappar.models.error.ErrorDataModel
import com.game.mobileappar.network.Resource
import com.game.mobileappar.network.apis.AuthApi
import com.game.mobileappar.repositories.AuthRepository
import com.game.mobileappar.utils.*
import com.game.mobileappar.utils.date.DatePickerHelper
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import java.util.*

class RegisterFragment : BaseFragment<AuthViewModel, FragmentRegisterBinding, AuthRepository>() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.pgRegister.visible(false)
        //binding.btnRegister.enable(false)

        viewModel.registerResponse.observe(viewLifecycleOwner, {
            binding.pgRegister.visible(it is Resource.Loading)

            when(it){
                is Resource.Success -> {
                    if(it.value.isSuccessful){
                        lifecycleScope.launch {
                            viewModel.saveAuthData(Gson().toJson(JsonParser.parseString(it.value.body()?.string())))
                            requireActivity().startNewActivity(MainActivity::class.java)
                        }
                    }else{
                        hideKeyboard()
                        handleError(Gson().fromJson(it.value.errorBody()?.string().toString(), ErrorDataModel::class.java).message!!)
                    }
                }

                is Resource.Failure -> {
                    hideKeyboard()
                    handleApiError(it){
                        register()
                    }
                }

                else -> {}
            }
        })

        /*binding.etPasswordRegister.addTextChangedListener{
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString()
            val name = binding.etNameRegister.text.toString().trim()
            val surname = binding.etSurnameRegister.text.toString().trim()
            val nickname = binding.etNicknameRegister.text.toString().trim()
            var phoneNum = binding.etPhoneRegister.text.toString().trim()
                .replace(" ", "")
                .replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .drop(2)

            binding.btnRegister.enable(
                email.isNotEmpty() && password.isNotEmpty()
                        && name.isNotEmpty() && surname.isNotEmpty()
            )
        }*/

        binding.btnRegister.setOnClickListener {
            register()
        }

        /*binding.linearLayoutLinkToLogin.setOnClickListener {
            navigation(R.id.action_registerFragment_to_loginFragment)
        }*/

        binding.etBirthdayRegister.addTextChangedListener {object : TextWatcher {

            var sb : StringBuilder = StringBuilder("")
            var ignore = false
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(ignore){
                    ignore = false
                    return
                }

                sb.clear()
                sb.append(if(s!!.length > 10){ s.subSequence(0,10) }else{ s })

                if(sb.lastIndex == 2){
                    if(sb[2] != '/'){
                        sb.insert(2,"/")
                    }
                } else if(sb.lastIndex == 5){
                    if(sb[5] != '/'){
                        sb.insert(5,"/")
                    }
                }

                ignore = true
                binding.etBirthdayRegister.setText(sb.toString())
                binding.etBirthdayRegister.setSelection(sb.length)

            }
        }
        }

        binding.etBirthdayRegister.setOnClickListener {
            showDatePickerDialog(DatePickerHelper(requireContext()))
        }
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRegisterBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = AuthRepository(remoteDataSource.buildApi(AuthApi::class.java), userPreferences)

    private fun register(){
        val email = binding.etEmailRegister.text.toString().trim()
        val password = binding.etPasswordRegister.text.toString()
        val name = binding.etNameRegister.text.toString().trim()
        val surname = binding.etSurnameRegister.text.toString().trim()
        val nickname = binding.etNicknameRegister.text.toString().trim()
        val phoneNum = binding.etPhoneRegister.text.toString().trim()
            .replace(" ", "")
            .replace("(", "")
            .replace(")", "")
            .replace("-", "")
            .drop(2)
        val dateBirthday = binding.etBirthdayRegister.text.toString().trim()
        val location = binding.etLocationRegister.text.toString().trim()

        viewModel.register(AuthRegisterModel(
            name = name,
            email = email,
            password = password,
            surname = surname,
            nickname = nickname,
            phoneNum = phoneNum,
            dateBirthday = dateBirthday,
            location = location
        ))
    }

    // Открытие панели выбора даты дня рождения
    private fun showDatePickerDialog(datePicker: DatePickerHelper) {
        val cal = Calendar.getInstance()
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = cal.get(Calendar.MONTH)
        val y = cal.get(Calendar.YEAR)
        datePicker.showDialog(d, m, y, object : DatePickerHelper.Callback {
            @SuppressLint("SetTextI18n")
            override fun onDateSelected(dayofMonth: Int, month: Int, year: Int) {
                val dayStr = if (dayofMonth < 10) "0${dayofMonth}" else "$dayofMonth"
                val mon = month + 1
                val monthStr = if (mon < 10) "0${mon}" else "$mon"
                binding.etBirthdayRegister.setText("${year}${monthStr}${dayStr}")
            }
        })
    }
}