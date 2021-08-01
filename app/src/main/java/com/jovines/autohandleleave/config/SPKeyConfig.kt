package com.jovines.autohandleleave.config

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import kotlin.reflect.KProperty

object SPKeyConfig {
    const val USER = "user"
    const val USERNAME = "username"
    const val PASSWORD = "password"
    const val REAL_NAME = "real_name"
}

fun Context.userSharedPreferences(action: SharedPreferences.Editor.() -> Unit) {
    getSharedPreferences(SPKeyConfig.USER, AppCompatActivity.MODE_PRIVATE).edit(action = action)

}

fun Context.userSharedPreferences(): SharedPreferences =
    getSharedPreferences(SPKeyConfig.USER, AppCompatActivity.MODE_PRIVATE)

fun Context.userSharedPreferencesDelegate(key: String = ""): UserSharedPreferencesDelegate {
    return UserSharedPreferencesDelegate(this, key)
}


// 委托的类
class UserSharedPreferencesDelegate(private val context: Context, private val key: String = "") {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        val realKey = if (key.isNotEmpty()) key else property.name
        return context.userSharedPreferences().getString(realKey, "") ?: ""
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        val realKey = if (key.isNotEmpty()) key else property.name
        context.userSharedPreferences {
            putString(realKey, value)
        }
    }
}

