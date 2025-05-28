package com.mitch.syncwork.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class UserPrefsDataSource(private val userPrefsDatastore: DataStore<Preferences>) {

    val data = userPrefsDatastore.data.map { prefs ->
        val rateSeconds = prefs[syncRateKey] ?: 0
        UserPreferences(
            isLoggedIn = prefs[isLoggedInKey] == true,
            syncRate = SyncRate.fromDurationOrDefault(rateSeconds.seconds)
        )
    }

    suspend fun saveIsUserLoggedIn(isLoggedIn: Boolean) {
        userPrefsDatastore.edit { prefs ->
            prefs[isLoggedInKey] = isLoggedIn
        }
    }

    suspend fun saveSyncRate(rate: SyncRate) {
        userPrefsDatastore.edit { prefs ->
            prefs[syncRateKey] = rate.duration.inWholeSeconds
        }
    }
}

enum class SyncRate(val duration: Duration) {
    Minutes1(duration = 1.minutes),
    Minutes2(duration = 2.minutes),
    Minutes5(duration = 5.minutes);

    companion object {
        private val Default = Minutes1

        fun fromDurationOrDefault(duration: Duration): SyncRate {
            return entries.find { it.duration == duration } ?: Default
        }
    }
}

private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
private val syncRateKey = longPreferencesKey("sync_rate")

data class UserPreferences(val isLoggedIn: Boolean, val syncRate: SyncRate)
