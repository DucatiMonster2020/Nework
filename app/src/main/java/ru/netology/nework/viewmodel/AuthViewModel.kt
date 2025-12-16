package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.repository.AuthRepository
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.SingleLiveEvent
import ru.netology.nework.util.ValidationUtils
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    private val _data = MutableLiveData<AuthState>()
    val data: LiveData<AuthState> = _data

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    private val _validationError = MutableLiveData<Pair<String, Int>?>()
    val validationError: LiveData<Pair<String, Int>?> = _validationError

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun login(login: String, password: String) {

        val loginValidation = ValidationUtils.validateLogin(login)
        if (loginValidation is ValidationUtils.ValidationResult.Error) {
            _validationError.value = "login" to loginValidation.messageResId
            return
        }

        val passwordValidation = ValidationUtils.validatePassword(password)
        if (passwordValidation is ValidationUtils.ValidationResult.Error) {
            _validationError.value = "password" to passwordValidation.messageResId
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                val token = repository.login(login, password)
                appAuth.setAuth(token.id, token.token)
                _data.value = AuthState.Authorized
            } catch (e: Exception) {
                _loading.value = false
                when {
                    e.message?.contains("400") == true -> {
                        _error.value = AndroidUtils.getString(R.string.error_wrong_login_or_password)
                    }
                    e.message?.contains("Network") == true -> {
                        _error.value = AndroidUtils.getString(R.string.no_internet)
                    }
                    else -> {
                        _error.value = e.message ?: AndroidUtils.getString(R.string.error_occurred)
                    }
                }
            }
        }
    }

    fun register(
        login: String,
        password: String,
        name: String,
        avatarUri: Uri?
    ) {
        val loginValidation = ValidationUtils.validateLogin(login)
        if (loginValidation is ValidationUtils.ValidationResult.Error) {
            _validationError.value = "login" to loginValidation.messageResId
            return
        }

        val nameValidation = ValidationUtils.validateName(name)
        if (nameValidation is ValidationUtils.ValidationResult.Error) {
            _validationError.value = "name" to nameValidation.messageResId
            return
        }

        val passwordValidation = ValidationUtils.validatePassword(password)
        if (passwordValidation is ValidationUtils.ValidationResult.Error) {
            _validationError.value = "password" to passwordValidation.messageResId
            return
        }

        avatarUri?.let { uri ->
            val filePath = AndroidUtils.getFilePathFromUri(uri)
            val avatarValidation = ValidationUtils.validateAvatar(filePath)
            if (avatarValidation is ValidationUtils.ValidationResult.Error) {
                _validationError.value = "avatar" to avatarValidation.messageResId
                return
            }
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                val avatarPart = avatarUri?.let { uri ->
                    val file = File(AndroidUtils.getFilePathFromUri(uri) ?: return@let null)
                    MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        file.asRequestBody("image/*".toMediaType())
                    )
                }

                val loginBody = login.toRequestBody("text/plain".toMediaType())
                val passwordBody = password.toRequestBody("text/plain".toMediaType())
                val nameBody = name.toRequestBody("text/plain".toMediaType())

                val token = repository.register(
                    login = loginBody,
                    password = passwordBody,
                    name = nameBody,
                    avatar = avatarPart
                )
                appAuth.setAuth(token.id, token.token)
                _data.value = AuthState.Authorized
            } catch (e: Exception) {
                _loading.value = false
                when {
                    e.message?.contains("400") == true -> {
                        _error.value = AndroidUtils.getString(R.string.error_user_already_exists)
                    }
                    e.message?.contains("Network") == true -> {
                        _error.value = AndroidUtils.getString(R.string.no_internet)
                    }
                    else -> {
                        _error.value = e.message ?: AndroidUtils.getString(R.string.error_occurred)
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                appAuth.removeAuth()
                _data.value = AuthState.Unauthorized
            } catch (e: Exception) {
                _error.value = e.message ?: AndroidUtils.getString(R.string.error_occurred)
            }
        }
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    fun clearError() {
        _error.value = null
    }
}