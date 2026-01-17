package org.thingai.android.meo.data.remote.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val api: AuthApiService,
    private val store: PreferenceDataStoreManager
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // if we've already tried to authenticate, give up
        if (responseCount(response) >= 2) return null

        val refreshToken = runCatching { runBlocking { store.getRefreshToken() } }.getOrNull()
            ?: return null

        // Use correct field name for refresh
        val call = api.refreshSync(mapOf("refresh_token" to refreshToken)).execute()
        if (!call.isSuccessful) {
            runBlocking { store.clearTokens() }
            return null
        }
        val body = call.body() ?: return null
        // Save only access_token and refresh_token
        runBlocking { store.saveTokens(body.access_token, body.refresh_token) }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${body.access_token}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var res: Response? = response
        var result = 0
        while (res != null) {
            result++
            res = res.priorResponse
        }
        return result
    }
}
