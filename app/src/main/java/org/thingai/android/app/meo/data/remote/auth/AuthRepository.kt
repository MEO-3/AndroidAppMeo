package org.thingai.android.app.meo.data.remote.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.thingai.android.meo.model.dto.LoginRequest
import org.thingai.android.meo.model.dto.SignupRequest
import org.json.JSONObject
import retrofit2.HttpException

class AuthRepository(
    private val api: AuthApiService,
    private val store: PreferenceDataStoreManager
) {
    suspend fun login(authUsername: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.login(LoginRequest(auth_username = authUsername, password = password))
            store.saveTokens(res.access_token, res.refresh_token)
            Result.success(Unit)
        } catch (t: Throwable) {
            // try to extract backend error message under key "detail" when available
            if (t is HttpException) {
                val errorBody = try { t.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                val detail = try {
                    if (!errorBody.isNullOrBlank()) {
                        val parsed = JSONObject(errorBody).optString("detail", "")
                        parsed.ifBlank { null }
                    } else null
                } catch (_: Throwable) { null }
                val message = detail ?: (t.localizedMessage ?: "Login failed")
                Result.failure(Exception(message))
            } else {
                Result.failure(t)
            }
        }
    }

    suspend fun signup(username: String, email: String, phoneNumber: String, authUsername: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.signup(
                SignupRequest(
                    username = username,
                    email = email,
                    phone_number = phoneNumber,
                    auth_username = authUsername,
                    password = password
                )
            )
            store.saveTokens(res.access_token, res.refresh_token)
            Result.success(Unit)
        } catch (t: Throwable) {
            // try to extract backend error message under key "detail" when available
            if (t is HttpException) {
                val errorBody = try { t.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                val detail = try {
                    if (!errorBody.isNullOrBlank()) {
                        val parsed = JSONObject(errorBody).optString("detail", "")
                        if (parsed.isNotBlank()) parsed else null
                    } else null
                } catch (_: Throwable) { null }
                val message = detail ?: (t.localizedMessage ?: "Signup failed")
                Result.failure(Exception(message))
            } else {
                Result.failure(t)
            }
        }
    }

    suspend fun refreshToken(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val refresh = store.getRefreshToken() ?: return@withContext Result.failure(Exception("No refresh token"))
            val res = api.refresh(mapOf("refresh_token" to refresh))
            store.saveTokens(res.access_token, res.refresh_token)
            Result.success(Unit)
        } catch (t: Throwable) {
            store.clearTokens()
            Result.failure(t)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.logout()
            store.clearTokens()
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
