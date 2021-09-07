package ru.callagent.helpers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class PrefsHelper private constructor(){
    lateinit var preference: SharedPreferences

    val prefEditor: SharedPreferences.Editor
        get() = preference.edit()

    constructor(context: Context) : this() {
        preference = getDefaultPreference(context)
    }

    companion object {
        private const val DEFAULT_BOOLEAN_VALUE = false

        fun getDefaultPreference(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }

        fun writePrefBool(context: Context, key: String, value: Boolean) {
            PrefsHelper(context).prefEditor.putBoolean(key, value).commit()
        }

        fun readPrefBool(context: Context, key: String): Boolean {
            return PrefsHelper(context).preference.getBoolean(key, DEFAULT_BOOLEAN_VALUE)
        }
    }
}
