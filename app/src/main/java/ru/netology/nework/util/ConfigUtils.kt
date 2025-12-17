package ru.netology.nework.util

import android.content.Context
import android.content.pm.PackageManager

object ConfigUtils {
    const val BASE_URL = "http://94.228.125.136:8080/"

    fun getApiKey(context: Context): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData.getString("API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}