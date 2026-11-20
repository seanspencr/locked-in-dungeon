package com.example.lockedindungeon.data.local.repositories

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes


private val Context.dataStore by preferencesDataStore(name = "app_settings")


@Serializable
data class AppSettings(
    val disableAttemptWord : String = "failure",
    val showStats : Boolean = true,
    val showBlockList : Boolean = true
)

@Singleton
class AppDatastoreRepository @Inject constructor(
    @ApplicationContext val appContext : Context,
    private val applicationScope : CoroutineScope
) {

    companion object {
        const val tag = "AppStateRepository"
    }

    /** Emits every 5 minutes, only used to re-evaluate the day based values. */
    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(5.minutes)
        }
    }

    private val IS_ACTIVE_KEY = booleanPreferencesKey("is_active")
    private val START_TIME_KEY = longPreferencesKey("start_time")
    private val APP_SETTINGS_KEY = stringPreferencesKey("app_settings")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refreshToken")

    // All of these are Eagerly on the application scope, not WhileSubscribed: workers, the OkHttp
    // Authenticator and services read .value / .first() outside of a UI subscriber, and a lazy
    // StateFlow would hand them the initial value instead of what is actually stored.
    val isActive : StateFlow<Boolean> = appContext.dataStore.data
        .map { prefs -> prefs[IS_ACTIVE_KEY] ?: false }
        .stateIn(scope = applicationScope, started = SharingStarted.Eagerly, initialValue = false)

    val startTime : StateFlow<LocalDateTime?> = appContext.dataStore.data
        .map { prefs ->
            prefs[START_TIME_KEY]?.let { epochSecond ->
                LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.UTC)
            }
        }
        .stateIn(scope = applicationScope, started = SharingStarted.Eagerly, initialValue = null)

    val currentDayStreak : StateFlow<Int> = combine(startTime, ticker) { start, _ ->
        if (start == null) {
            0
        } else {
            ChronoUnit.DAYS.between(start, LocalDateTime.now(ZoneOffset.UTC)).toInt()
        }
    }.stateIn(scope = applicationScope, started = SharingStarted.Eagerly, initialValue = 0)

    val settings : StateFlow<AppSettings> = appContext.dataStore.data
        .map { prefs ->
            prefs[APP_SETTINGS_KEY]?.let { encoded ->
                Json.decodeFromString<AppSettings>(encoded)
            } ?: AppSettings()
        }
        .stateIn(scope = applicationScope, started = SharingStarted.Eagerly, initialValue = AppSettings())

    val refreshToken : StateFlow<String?> = appContext.dataStore.data
        .map { prefs -> prefs[REFRESH_TOKEN_KEY] }
        .stateIn(scope = applicationScope, started = SharingStarted.Eagerly, initialValue = null)

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

    suspend fun setRefreshToken(token: String?){
        // token!! would throw whenever the server answers without a refresh token
        appContext.dataStore.edit { prefs ->
            if (token.isNullOrEmpty()) prefs.remove(REFRESH_TOKEN_KEY)
            else prefs[REFRESH_TOKEN_KEY] = token
        }
    }
}
