package com.example.lockedindungeon.data.local.repositories

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes


private val Context.dataStore by preferencesDataStore(name = "app_settings")


@Serializable
data class AppSettings(val disableAttemptWord : String? = null)
class AppDatastoreRepository @Inject constructor(
    @ApplicationContext val appContext : Context
) {

    companion object {
        val tag = "AppStateRepository"
    }
    val ticker = flow {
        while (true){
            emit(Unit)
            delay(5.minutes)
        }
    }
    private val IS_ACTIVE_KEY = booleanPreferencesKey("is_active")
    private val START_TIME_KEY = longPreferencesKey("start_time")
    private val APP_SETTINGS_KEY = stringPreferencesKey("app_settings")

    val isActive: Flow<Boolean> = appContext.dataStore.data
        .map { prefs -> prefs[IS_ACTIVE_KEY] ?: false }

    val startTime: Flow<LocalDateTime?> = appContext.dataStore.data
        .map { prefs ->
            prefs[START_TIME_KEY]?.let {
                second -> LocalDateTime.ofEpochSecond(second, 0, ZoneOffset.UTC)
            }
        }

    val currentDayStreak: Flow<Int> = combine(startTime, ticker) { start, tick  ->
        if (start == null) {
            0
        } else {
            ChronoUnit.DAYS.between(start, LocalDateTime.now(ZoneOffset.UTC)).toInt()
        }
    }

    suspend fun setActive(active: Boolean) {
        appContext.dataStore.edit { prefs ->
            if(active){
                prefs[IS_ACTIVE_KEY] = true
                prefs[START_TIME_KEY] = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
            }else{
                prefs.remove(IS_ACTIVE_KEY)
                prefs.remove(START_TIME_KEY)
            }
        }
    }

    suspend fun setSettings(settings : AppSettings){
        appContext.dataStore.edit { prefs -> prefs[APP_SETTINGS_KEY] = Json.encodeToString(settings) }
    }

    val settings : Flow<AppSettings?> = appContext.dataStore.data.map { prefs ->
        prefs[APP_SETTINGS_KEY]?.let {
            jsonString -> Json.decodeFromString<AppSettings>(jsonString)
        }
    }


}