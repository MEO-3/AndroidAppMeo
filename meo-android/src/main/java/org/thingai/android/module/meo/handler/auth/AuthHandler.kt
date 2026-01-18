package org.thingai.android.module.meo.handler.auth

import com.google.gson.Gson
import org.thingai.android.module.meo.handler.auth.internal.AuthApi
import org.thingai.android.module.meo.handler.auth.internal.AuthPrefs
import org.thingai.base.log.ILog
import org.thingai.meo.common.dto.ResponseError
import org.thingai.meo.common.dto.auth.RequestLogin
import org.thingai.meo.common.dto.auth.RequestRefresh
import org.thingai.meo.common.dto.auth.RequestSignup
import org.thingai.meo.common.dto.auth.ResponseAuth

class AuthHandler internal constructor(
    private val api: AuthApi,
    private val prefs: AuthPrefs
) {
    private val TAG = "AuthHandler"
    private val gson = Gson()

    suspend fun login(authUsername: String, password: String): Result<ResponseAuth> {
        return try {
            val requestBody = RequestLogin()
            requestBody.authUsername = authUsername
            requestBody.password = password

            val response = api.login(requestBody)
            handleResponse(response)
        } catch (t: Throwable) {
            handleError(t)
        }
    }

    suspend fun signup(
        username: String,
        email: String,
        phoneNumber: String,
        authUsername: String,
        password: String
    ): Result<ResponseAuth> {
        return try {
            val requestBody = RequestSignup()
            requestBody.username = username
            requestBody.email = email
            requestBody.phoneNumber = phoneNumber
            requestBody.authUsername = authUsername
            requestBody.password = password

            val response = api.signup(requestBody)
            handleResponse(response)
        } catch (t: Throwable) {
            handleError(t)
        }
    }

    suspend fun refresh(refreshToken: String? = null): Result<ResponseAuth> {
        return try {
            val tokenToUse = refreshToken ?: prefs.getRefreshToken()
                ?: return Result.failure(Exception("No refresh token available"))

            val requestBody = RequestRefresh()
            requestBody.refreshToken = tokenToUse

            val response = api.refresh(requestBody)
            handleResponse(response)
        } catch (t: Throwable) {
            handleError(t)
        }
    }

    suspend fun getAccessToken(): String? = prefs.getAccessToken()

    suspend fun logout() {
        prefs.clear()
    }

    private suspend fun handleResponse(response: retrofit2.Response<ResponseAuth>): Result<ResponseAuth> {
        val statusCode = response.code()
        return if (response.isSuccessful) {
            val authResponse = response.body()!!
            prefs.saveTokens(authResponse.accessToken, authResponse.refreshToken)
            Result.success(authResponse)
        } else {
            val errorBodyString = response.errorBody()?.string()
            val errorDetail = try {
                val errorObj = gson.fromJson(errorBodyString, ResponseError::class.java)
                errorObj?.detail ?: "Unknown error"
            } catch (e: Exception) {
                "Error code $statusCode: ${response.message()}"
            }

            ILog.e(TAG, "API call failed ($statusCode): $errorDetail")
            Result.failure(Exception(errorDetail))
        }
    }

    private fun handleError(t: Throwable): Result<ResponseAuth> {
        t.printStackTrace()
        ILog.e(TAG, "failed", t.message)
        return Result.failure(t)
    }
}
