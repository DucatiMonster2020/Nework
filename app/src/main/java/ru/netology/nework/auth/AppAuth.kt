package ru.netology.nework.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authStateFlow = MutableStateFlow(AuthState())
    val authStateFlow = _authStateFlow.asStateFlow()

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)

        if (token != null && id != 0L) {
            _authStateFlow.update { it.copy(token = token, userId = id) }
        }
    }

    @Synchronized
    fun setAuth(token: String?, id: Long?) {
        _authStateFlow.update {
            AuthState(
                token = token,
                userId = id ?: 0L
            )
        }
        prefs.edit {
            if (token == null) {
                remove(TOKEN_KEY)
                remove(ID_KEY)
            } else {
                putString(TOKEN_KEY, token)
                putLong(ID_KEY, id ?: 0L)
            }
        }
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.update { AuthState() }
        prefs.edit {
            remove(TOKEN_KEY)
            remove(ID_KEY)
        }
    }

    companion object {
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"
    }
}
data class AuthState(
    val token: String? = null,
    val userId: Long = 0L
    ) {
    val isAuthorized: Boolean
        get() = token !=null && userId != 0L
}