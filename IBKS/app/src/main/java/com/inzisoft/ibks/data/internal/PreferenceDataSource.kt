package com.inzisoft.ibks.data.internal

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PreferenceDataSource @Inject constructor(private val context: Context) {
    private val Context.preferenceDataStore by preferencesDataStore(name = "preferenceDataStore")
    fun getPreference(key: String): Flow<String> {
        return context.preferenceDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val key = stringPreferencesKey(key)
                preferences[key] ?: ""
            }
    }

    suspend fun setPreference(key: String, text: String) {
        val key = stringPreferencesKey(key)
        context.preferenceDataStore.edit { preferences ->
            preferences[key] = text
        }
    }
}