package com.example.composespeedtest.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

object PrefsKeys {
    val OnboardingDone = booleanPreferencesKey("onboarding_done")
    val DarkTheme = booleanPreferencesKey("dark_theme")
    val PersonalizedAds = booleanPreferencesKey("personalized_ads")
    val AdFrequency = stringPreferencesKey("ad_frequency")
    val PrivacyPolicyAccepted = booleanPreferencesKey("privacy_policy_accepted")
    val DataUsageAccepted = booleanPreferencesKey("data_usage_accepted")
}

class Prefs(private val context: Context) {
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs: Preferences ->
        prefs[PrefsKeys.OnboardingDone] ?: false
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data.map { prefs: Preferences ->
        prefs[PrefsKeys.DarkTheme] ?: true
    }

    val personalizedAds: Flow<Boolean> = context.dataStore.data.map { prefs: Preferences ->
        prefs[PrefsKeys.PersonalizedAds] ?: true
    }

    val adFrequency: Flow<String> = context.dataStore.data.map { prefs: Preferences ->
        prefs[PrefsKeys.AdFrequency] ?: "Normal"
    }

    val privacyPolicyAccepted: Flow<Boolean> = context.dataStore.data.map { prefs: Preferences ->
        prefs[PrefsKeys.PrivacyPolicyAccepted] ?: false
    }

    val dataUsageAccepted: Flow<Boolean> = context.dataStore.data.map { prefs: Preferences ->
        prefs[PrefsKeys.DataUsageAccepted] ?: false
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

    suspend fun setPersonalizedAds(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.PersonalizedAds] = enabled
        }
    }

    suspend fun setAdFrequency(frequency: String) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.AdFrequency] = frequency
        }
    }

    suspend fun setPrivacyPolicyAccepted(accepted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.PrivacyPolicyAccepted] = accepted
        }
    }

    suspend fun setDataUsageAccepted(accepted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.DataUsageAccepted] = accepted
        }
    }
}
