package com.example.composevoicerecorder

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class SharedPreference : Application() {

    companion object {
        lateinit var prefs : MySharedPreferences
    }

    override fun onCreate() {
        prefs = MySharedPreferences(applicationContext)
        super.onCreate()
    }
}

class MySharedPreferences(context: Context) {

    private val prefsFilename = "prefs"
    private val setAudioSamplingRateEdt = "setAudioSamplingRateEdt"
    private val setAudioEncodingBitRateEdt = "setAudioEncodingBitRateEdt"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefsFilename, 0)

    var setAudioSamplingRateValue: String?
        get() = prefs.getString(setAudioSamplingRateEdt, "")
        set(value) = prefs.edit().putString(setAudioSamplingRateEdt, value).apply()

    var setAudioEncodingBitRateValue: String?
        get() = prefs.getString(setAudioEncodingBitRateEdt, "")
        set(value) = prefs.edit().putString(setAudioEncodingBitRateEdt, value).apply()
}