package com.aizoid.unityadsdemo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aizoid.unityadsdemo.ESTIMATED_ECPM_USD
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ad_stats_prefs")

data class AdStats(
    val totalClicks: Int = 0,
    val rewardedWatched: Int = 0,
    val interstitialsShown: Int = 0,
    val adsSkipped: Int = 0,
    val totalCoins: Int = 0,
    val loadSuccessCount: Int = 0,
    val loadFailedCount: Int = 0
) {
    val totalImpressions: Int
        get() = rewardedWatched + interstitialsShown

    val ctrPercent: Double
        get() = if (totalImpressions > 0) {
            (totalClicks.toDouble() / totalImpressions.toDouble()) * 100.0
        } else {
            0.0
        }

    val estimatedEarningsUsd: Double
        get() = (totalImpressions.toDouble() / 1000.0) * ESTIMATED_ECPM_USD
}

class AdStatsRepository(private val context: Context) {

    private object PreferencesKeys {
        val KEY_TOTAL_CLICKS = intPreferencesKey("total_clicks")
        val KEY_REWARDED_WATCHED = intPreferencesKey("rewarded_watched")
        val KEY_INTERSTITIALS_SHOWN = intPreferencesKey("interstitials_shown")
        val KEY_ADS_SKIPPED = intPreferencesKey("ads_skipped")
        val KEY_TOTAL_COINS = intPreferencesKey("total_coins")
        val KEY_LOAD_SUCCESS = intPreferencesKey("load_success_count")
        val KEY_LOAD_FAILED = intPreferencesKey("load_failed_count")
    }

    val adStatsFlow: Flow<AdStats> = context.dataStore.data.map { preferences ->
        AdStats(
            totalClicks = preferences[PreferencesKeys.KEY_TOTAL_CLICKS] ?: 0,
            rewardedWatched = preferences[PreferencesKeys.KEY_REWARDED_WATCHED] ?: 0,
            interstitialsShown = preferences[PreferencesKeys.KEY_INTERSTITIALS_SHOWN] ?: 0,
            adsSkipped = preferences[PreferencesKeys.KEY_ADS_SKIPPED] ?: 0,
            totalCoins = preferences[PreferencesKeys.KEY_TOTAL_COINS] ?: 0,
            loadSuccessCount = preferences[PreferencesKeys.KEY_LOAD_SUCCESS] ?: 0,
            loadFailedCount = preferences[PreferencesKeys.KEY_LOAD_FAILED] ?: 0
        )
    }

    suspend fun recordClick() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.KEY_TOTAL_CLICKS] ?: 0
            preferences[PreferencesKeys.KEY_TOTAL_CLICKS] = current + 1
        }
    }

    suspend fun recordRewardedCompleted(coinsToAdd: Int) {
        context.dataStore.edit { preferences ->
            val currentWatched = preferences[PreferencesKeys.KEY_REWARDED_WATCHED] ?: 0
            preferences[PreferencesKeys.KEY_REWARDED_WATCHED] = currentWatched + 1

            val currentCoins = preferences[PreferencesKeys.KEY_TOTAL_COINS] ?: 0
            preferences[PreferencesKeys.KEY_TOTAL_COINS] = currentCoins + coinsToAdd
        }
    }

    suspend fun recordInterstitialShown() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.KEY_INTERSTITIALS_SHOWN] ?: 0
            preferences[PreferencesKeys.KEY_INTERSTITIALS_SHOWN] = current + 1
        }
    }

    suspend fun recordSkipped() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.KEY_ADS_SKIPPED] ?: 0
            preferences[PreferencesKeys.KEY_ADS_SKIPPED] = current + 1
        }
    }

    suspend fun recordLoadSuccess() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.KEY_LOAD_SUCCESS] ?: 0
            preferences[PreferencesKeys.KEY_LOAD_SUCCESS] = current + 1
        }
    }

    suspend fun recordLoadFailed() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.KEY_LOAD_FAILED] ?: 0
            preferences[PreferencesKeys.KEY_LOAD_FAILED] = current + 1
        }
    }

    suspend fun resetAllStats() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_TOTAL_CLICKS] = 0
            preferences[PreferencesKeys.KEY_REWARDED_WATCHED] = 0
            preferences[PreferencesKeys.KEY_INTERSTITIALS_SHOWN] = 0
            preferences[PreferencesKeys.KEY_ADS_SKIPPED] = 0
            preferences[PreferencesKeys.KEY_TOTAL_COINS] = 0
            preferences[PreferencesKeys.KEY_LOAD_SUCCESS] = 0
            preferences[PreferencesKeys.KEY_LOAD_FAILED] = 0
        }
    }
}