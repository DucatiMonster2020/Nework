package ru.netology.nework.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nework.BuildConfig
import ru.netology.nework.api.ApiService
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var apiService: ApiService

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (BuildConfig.DEBUG) {
            Log.d("FCM", "Refreshed token: $token")
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
            } catch (e: Exception) {
                Log.e("FCM", "Failed to send token", e)
            }
        }
    }
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (BuildConfig.DEBUG) {
            Log.d("FCM", "Message received: ${message.data}")
        }
        message.notification?.let { notification ->
        }
    }
}
