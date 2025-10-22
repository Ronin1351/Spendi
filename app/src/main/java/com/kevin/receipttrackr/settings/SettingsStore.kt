package com.kevin.receipttrackr.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keywordRulesKey = stringPreferencesKey("keyword_rules")
    private val currencyKey = stringPreferencesKey("currency")

    val keywordRules: Flow<Map<String, String>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[keywordRulesKey] ?: "{}"
            try {
                Json.decodeFromString<Map<String, String>>(jsonString)
            } catch (e: Exception) {
                emptyMap()
            }
        }

    val currency: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[currencyKey] ?: "USD"
        }

    suspend fun addKeywordRule(keyword: String, category: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[keywordRulesKey]?.let {
                try {
                    Json.decodeFromString<Map<String, String>>(it)
                } catch (e: Exception) {
                    emptyMap()
                }
            } ?: emptyMap()

            val updated = current + (keyword.lowercase() to category)
            preferences[keywordRulesKey] = Json.encodeToString(updated)
        }
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[currencyKey] = currency
        }
    }

    suspend fun getKeywordRulesSnapshot(): Map<String, String> {
        return context.dataStore.data.map { preferences ->
            val jsonString = preferences[keywordRulesKey] ?: "{}"
            try {
                Json.decodeFromString<Map<String, String>>(jsonString)
            } catch (e: Exception) {
                emptyMap()
            }
        }.first()
    }
}
