package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.error.AppError
import ru.netology.nework.repository.AuthRepository
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    private val _data = MutableLiveData<AuthState>()
    val data: LiveData<AuthState> = _data

    val authorized: LiveData<Boolean> = appAuth.authState
        .asLiveData()
        .map { it.isAuthorized }

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    fun login(login: String, password: String) {
        viewModelScope.launch {
            try {
                _data.value = AuthState(loading = true)
                val token = authRepository.login(login, password)
                appAuth.setAuth(token)
                _data.value = AuthState(success = true)
            } catch (e: Exception) {
                _error.value = when (e) {
                    is AppError.ApiError -> {
                        if (e.status == 400) {
                            "Неправильный логин или пароль"
                        } else {
                            e.message ?: "Ошибка сети"
                        }
                    }
                    else -> e.message ?: "Неизвестная ошибка"
                }
                _data.value = AuthState(error = true)
            }
        }
    }

    fun register(login: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                _data.value = AuthState(loading = true)
                val token = authRepository.register(login, password, name)
                appAuth.setAuth(token)
                _data.value = AuthState(success = true)
            } catch (e: Exception) {
                _error.value = when (e) {
                    is AppError.ApiError -> {
                        if (e.status == 400) {
                            "Пользователь с таким логином уже зарегистрирован"
                        } else {
                            e.message ?: "Ошибка сети"
                        }
                    }
                    else -> e.message ?: "Неизвестная ошибка"
                }
                _data.value = AuthState(error = true)
            }
        }
    }

    fun logout() {
        appAuth.removeAuth()
    }

    data class AuthState(
        val loading: Boolean = false,
        val error: Boolean = false,
        val success: Boolean = false
    )
}