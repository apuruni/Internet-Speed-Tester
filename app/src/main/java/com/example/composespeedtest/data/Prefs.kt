package com.example.composespeedtest.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

object PrefsKeys {
    val OnboardingDone = booleanPreferencesKey("onboarding_done")
    val DarkTheme = booleanPreferencesKey("dark_theme")
}

class Prefs(private val context: Context) {
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs: Preferences ->
        prefs[PrefsKeys.OnboardingDone] ?: false
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data.map { prefs: Preferences ->
        prefs[PrefsKeys.DarkTheme] ?: true
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.OnboardingDone] = done
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.DarkTheme] = enabled
        }
    }
}
