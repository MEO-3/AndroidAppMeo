package org.thingai.android.module.meo.handler.auth.internal

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.thingai.android.module.meo.handler.auth.AuthHandler
import org.thingai.base.log.ILog

internal class TokenAuthenticator(
    private val authHandlerProvider: () -> AuthHandler,
    private val prefs: AuthPrefs
) : Authenticator {
    private val TAG = "TokenAuthenticator"

    override fun authenticate(route: Route?, response: Response): Request? {
        // 1. We only want to attempt refresh if the response is 401
        if (response.code != 401) return null

        // 2. Avoid infinite loops: if we already tried to refresh and failed, stop.
        if (response.priorResponse != null) {
            ILog.w(TAG, "Authentication failed again after refresh. Giving up.")
            return null
        }

        synchronized(this) {
            val currentToken = runBlocking { prefs.getAccessToken() }
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // 3. If the token has already been changed by another thread, retry with the new token
            if (currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // 4. Perform the refresh
            ILog.d(TAG, "Token expired, attempting refresh...")
            val refreshResult = runBlocking { authHandlerProvider().refresh() }

            return if (refreshResult.isSuccess) {
                val newToken = refreshResult.getOrNull()?.accessToken
                ILog.d(TAG, "Refresh successful.")
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } else {
                ILog.e(TAG, "Refresh failed: ${refreshResult.exceptionOrNull()?.message}")
                null
            }
        }
    }
}
