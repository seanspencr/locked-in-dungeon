package com.example.lockedindungeon.data.local.repositories

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private val Context.dataStore by preferencesDataStore(name = "app_settings")

class AppStateRepository @Inject constructor(@ApplicationContext val appContext : Context) {
    private val IS_ACTIVE_KEY = booleanPreferencesKey("is_active")
    val isActive: Flow<Boolean> = appContext.dataStore.data
        .map { prefs -> prefs[IS_ACTIVE_KEY] ?: false }

    suspend fun setActive(active: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[IS_ACTIVE_KEY] = active
        }
    }
}