package com.relationshipradar.app.data.repo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("settings")

data class AppSettings(
    val roundupHour: Int = 9,
    val roundupEnabled: Boolean = true,
    val individualAlertsEnabled: Boolean = true,
    val lastContactsSyncAt: Long = 0L,
    val onboardingDone: Boolean = false,
)

class Settings(private val context: Context) {
    private object K {
        val roundupHour = intPreferencesKey("roundup_hour")
        val roundupEnabled = booleanPreferencesKey("roundup_enabled")
        val individual = booleanPreferencesKey("individual_alerts")
        val lastSync = longPreferencesKey("last_contacts_sync")
        val onboarding = booleanPreferencesKey("onboarding_done")
    }

    val flow: Flow<AppSettings> = context.store.data.map { p ->
        AppSettings(
            roundupHour = p[K.roundupHour] ?: 9,
            roundupEnabled = p[K.roundupEnabled] ?: true,
            individualAlertsEnabled = p[K.individual] ?: true,
            lastContactsSyncAt = p[K.lastSync] ?: 0L,
            onboardingDone = p[K.onboarding] ?: false,
        )
    }

    suspend fun setRoundupHour(h: Int) = context.store.edit { it[K.roundupHour] = h }
    suspend fun setRoundupEnabled(v: Boolean) = context.store.edit { it[K.roundupEnabled] = v }
    suspend fun setIndividualAlerts(v: Boolean) = context.store.edit { it[K.individual] = v }
    suspend fun setLastSync(t: Long) = context.store.edit { it[K.lastSync] = t }
    suspend fun setOnboardingDone() = context.store.edit { it[K.onboarding] = true }
}
