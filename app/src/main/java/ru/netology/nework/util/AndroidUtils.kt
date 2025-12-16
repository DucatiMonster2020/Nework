package ru.netology.nework.util

import android.content.Context
import android.view.View
import android.widget.Toast

object AndroidUtils {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun fixRecyclerViewItem(view: View) {
        view.isFocusable = false
        view.isClickable = false
    }
}