package org.thingai.android.module.meo.handler.auth.internal

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "meo_auth_prefs")

internal class AuthPrefs(private val context: Context) {
    private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun getAccessToken(): String? = context.dataStore.data
        .map { it[KEY_ACCESS_TOKEN] }
        .firstOrNull()

    suspend fun getRefreshToken(): String? = context.dataStore.data
        .map { it[KEY_REFRESH_TOKEN] }
        .firstOrNull()

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
