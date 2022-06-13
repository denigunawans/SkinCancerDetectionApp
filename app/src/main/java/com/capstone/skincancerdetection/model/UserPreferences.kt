package com.capstone.skincancerdetection.model

import android.content.Context

internal class UserPreference(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setUser(value: User) {
        val editor = preferences.edit()
        editor.putString(EMAIL, value.email)
        editor.putString(NAME, value.name)
        editor.putBoolean(IS_LOGIN, value.isLogin)
        editor.apply()
    }

    fun getUser(): User {
        val model = User()
        model.email = preferences.getString(EMAIL, "")
        model.name = preferences.getString(NAME, "")
        model.isLogin = preferences.getBoolean(IS_LOGIN, false)
        return model
    }

    companion object {
        private const val PREFS_NAME = "user_pref"
        private const val NAME = "name"
        private const val EMAIL = "email"
        private const val HISTORY = "history"
        private const val IS_LOGIN = "isLogin"
    }
}
