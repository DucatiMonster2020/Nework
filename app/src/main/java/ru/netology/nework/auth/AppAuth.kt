package ru.netology.nework.auth

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.netology.nework.dto.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)

        if (token == null || id == 0L) {
            _authState.value = AuthState()
        } else {
            _authState.value = AuthState(
                id = id,
                token = token
            )
        }
    }

    @Synchronized
    fun setAuth(token: Token) {
        prefs.edit {
            putString(TOKEN_KEY, token.token)
            putLong(ID_KEY, token.id)
        }
        _authState.value = AuthState(
            id = token.id,
            token = token.token
        )
    }

    @Synchronized
    fun removeAuth() {
        prefs.edit {
            clear()
        }
        _authState.value = AuthState()
    }

    data class AuthState(
        val id: Long = 0,
        val token: String? = null
    ) {
        val isAuthorized: Boolean
            get() = token != null && id != 0L
    }

    companion object {
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"
    }
}