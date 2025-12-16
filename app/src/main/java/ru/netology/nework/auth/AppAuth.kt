package ru.netology.nework.auth

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableLiveData(getAuthState())
    val authState: LiveData<AuthState> = _authState

    private var authToken: String? = prefs.getString(TOKEN_KEY, null)
        set(value) {
            field = value
            prefs.edit {
                if (value != null) {
                    putString(TOKEN_KEY, value)
                } else {
                    remove(TOKEN_KEY)
                }
            }
        }

    private var authId: Long = prefs.getLong(ID_KEY, 0L)
        set(value) {
            field = value
            prefs.edit {
                putLong(ID_KEY, value)
            }
        }

    init {
        prefs.edit {
            apply()
        }
    }

    fun setAuth(id: Long, token: String) {
        authId = id
        authToken = token
        _authState.value = getAuthState()
    }

    fun removeAuth() {
        authId = 0L
        authToken = null
        _authState.value = getAuthState()
    }

    fun getAuthState(): AuthState {
        return if (authToken != null && authId != 0L) {
            AuthState.Authorized(id = authId, token = authToken!!)
        } else {
            AuthState.Unauthorized
        }
    }

    companion object {
        private const val ID_KEY = "id"
        private const val TOKEN_KEY = "token"
    }
}