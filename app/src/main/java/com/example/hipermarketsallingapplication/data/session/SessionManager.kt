package com.example.hipermarketsallingapplication.data.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hipermarket_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_SERVER_LINK = "server_link"
    }

    fun saveUserSession(userName: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, userName)
            apply()
        }
    }

    fun getSavedUserName(): String? {
        return if (isLoggedIn()) prefs.getString(KEY_USER_NAME, null) else null
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun saveServerLink(url: String) {
        prefs.edit().putString(KEY_SERVER_LINK, url).apply()
    }

    fun getServerLink(): String? {
        return prefs.getString(KEY_SERVER_LINK, null)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
