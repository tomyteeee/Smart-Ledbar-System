package me.frostingly.app

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("App_Preferences", Context.MODE_PRIVATE)

    /*
    * first_time: Boolean
    * access_code: String
    * */

    fun writeString(key: String, value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun readString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun writeStringSet(key: String, value: Set<String>) {
        with(sharedPreferences.edit()) {
            putStringSet(key, value)
            apply()
        }
    }

    fun readStringSet(key: String, defaultValue: Set<String>? = null): Set<String>? {
        return sharedPreferences.getStringSet(key, defaultValue)
    }
}