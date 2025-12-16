package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.LoginRequest
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.RegisterRequest
import ru.netology.nework.error.AppError
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth
) : ViewModel() {
    private val _authState = MutableLiveData(false)
    val authState: LiveData<Boolean> = _authState
    private val _errorState = SingleLiveEvent<String>()
    val errorState: LiveData<String> = _errorState
    private val _loadingState = MutableLiveData(false)
    val loadingState: LiveData<Boolean> = _loadingState

    init {
        _authState.value = appAuth.authStateFlow.value.isAuthorized
    }

    fun login(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            _errorState.postValue("Заполните все поля")
            return
        }
        viewModelScope.launch {
            try {
                _loadingState.value = true
                val response = apiService.login(LoginRequest(login, password))
                appAuth.setAuth(response.token, response.id)
                _authState.value = true
            } catch (e: Exception) {
                if (e is AppError.ApiError && e.code == 400) {
                    _errorState.postValue("Неправильный логин или пароль")
                } else {
                    _errorState.postValue("Ошибка сети: ${e.message}")
                }
                _authState.value = false
            } finally {
                _loadingState.value = false
            }
        }
    }
    fun register(login: String, password: String, name: String, avatar: String? = null) {
        if (login.isBlank() || password.isBlank()) {
            _errorState.postValue("Заполните все обязательные поля")
            return
        }
        viewModelScope.launch {
            try {
                _loadingState.value = true
                val request = RegisterRequest(login, password, name, avatar?.let { MediaUpload(it) })
                val response = apiService.register(request)
                appAuth.setAuth(response.token, response.id)
                _authState.value = true
            } catch (e: Exception) {
                if (e is AppError.ApiError && e.code == 400) {
                    _errorState.postValue("Пользователь с таким логином уже зарегистрирован")
                } else {
                    _errorState.postValue("Ошибка регистрации: ${e.message}")
                }
                _authState.value = false
            } finally {
                _loadingState.value = false
            }
        }
    }
    fun logout() {
        appAuth.removeAuth()
        _authState.value = false
    }
    fun checkAuth() {
        _authState.value = appAuth.authStateFlow.value.isAuthorized
    }
}