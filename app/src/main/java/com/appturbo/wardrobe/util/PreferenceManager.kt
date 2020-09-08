package com.appturbo.wardrobe.util

import android.content.SharedPreferences
import com.appturbo.tinder.application.MyApplication

class PreferenceManager {


    fun getSharedPreference(): SharedPreferences {

        var PRIVATE_MODE = 0
        val PREF_NAME = "cp_store"
        val sharedPref: SharedPreferences =
            MyApplication.getContext().getSharedPreferences(PREF_NAME, PRIVATE_MODE)

        return sharedPref
    }

    fun setPrefString(key: PreferenceKeys, value: String) {
        val editor = getSharedPreference().edit()
        editor.putString(key.name, value)
        editor.apply()
    }

    fun setPrefBoolean(key: PreferenceKeys, value: Boolean) {
        val editor = getSharedPreference().edit()
        editor.putBoolean(key.name, value)
        editor.apply()
    }

    fun getPrefBoolean(key: PreferenceKeys): Boolean {
        return getSharedPreference().getBoolean(key.name, false)
    }

    fun getPrefString(key: PreferenceKeys): String? {
        return getSharedPreference().getString(key.name, null)
    }

    fun removePref(key: PreferenceKeys) {
        val preferences: SharedPreferences = getSharedPreference()
        preferences.edit().remove(key.name).apply()
    }
}