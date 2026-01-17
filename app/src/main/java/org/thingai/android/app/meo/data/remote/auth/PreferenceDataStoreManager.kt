package org.thingai.android.app.meo.data.remote.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.thingai.android.meo.model.dto.TokenModel

class PreferenceDataStoreManager(private val dataStore: DataStore<Preferences>) {
    private val KEY_ACCESS = stringPreferencesKey("pref_access_token")
    private val KEY_REFRESH = stringPreferencesKey("pref_refresh_token")

    suspend fun saveTokens(access: String, refresh: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = access
            prefs[KEY_REFRESH] = refresh
        }
    }

    val tokenFlow: Flow<TokenModel?> = dataStore.data.map { prefs ->
        val a = prefs[KEY_ACCESS] ?: return@map null
        val r = prefs[KEY_REFRESH] ?: return@map null
        TokenModel(a, r)
    }

    suspend fun getAccessToken(): String? =
        dataStore.data.map { it[KEY_ACCESS] }.firstOrNull()

    suspend fun getRefreshToken(): String? =
        dataStore.data.map { it[KEY_REFRESH] }.firstOrNull()

    suspend fun clearTokens() {
        dataStore.edit { it.clear() }
    }
}
