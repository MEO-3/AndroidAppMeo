package org.thingai.android.module.meo.handler.auth

import com.google.gson.Gson
import org.thingai.android.module.meo.cloud.AuthApi
import org.thingai.android.module.meo.handler.auth.internal.AuthPrefs
import org.thingai.base.log.ILog
import org.thingai.meo.common.define.MOtpPurpose
import org.thingai.meo.common.dto.ResponseError
import org.thingai.meo.common.dto.auth.RequestLogin
import org.thingai.meo.common.dto.auth.RequestRefresh
import org.thingai.meo.common.dto.auth.RequestSignup
import org.thingai.meo.common.dto.auth.ResponseAuth
import org.thingai.meo.common.dto.otp.RequestOtpCreate
import org.thingai.meo.common.dto.otp.RequestResetPasswordConfirm
import org.thingai.meo.common.dto.ResponseOk
import org.thingai.meo.common.dto.otp.RequestOtpVerify
import retrofit2.Response

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

    suspend fun verifyOtp(email: String, otp: String, purpose: Int): Result<ResponseOk> {
        return try {
            val requestBody = RequestOtpVerify()
            requestBody.email = email
            requestBody.otp = otp
            requestBody.purpose = purpose

            val response = api.verifyOtp(requestBody)
            handleOtpCreateResponse(response)
        } catch (t: Throwable) {
            t.printStackTrace()
            ILog.e(TAG, "failed", t.message)
            Result.failure(t)
        }
    }

    // Request a password-reset OTP. Purpose is forced to 1 by the caller
    suspend fun requestPasswordReset(email: String, ttlMinutes: Int? = null): Result<ResponseOk> {
        return try {
            val requestBody = RequestOtpCreate()
            requestBody.email = email
            requestBody.purpose = MOtpPurpose.PASSWORD_RESET
            if (ttlMinutes != null) requestBody.ttlMinutes = ttlMinutes.toString()

            val response = api.requestPasswordReset(requestBody)
            handleOtpCreateResponse(response)
        } catch (t: Throwable) {
            t.printStackTrace()
            ILog.e(TAG, "failed", t.message)
            Result.failure(t)
        }
    }

    // Verify password-reset OTP and update password
    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Unit> {
        return try {
            val requestBody = RequestResetPasswordConfirm()
            requestBody.email = email
            requestBody.otp = otp
            requestBody.newPassword = newPassword

            val response = api.resetPassword(requestBody)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val statusCode = response.code()
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
        } catch (t: Throwable) {
            t.printStackTrace()
            ILog.e(TAG, "failed", t.message)
            Result.failure(t)
        }
    }

    suspend fun getAccessToken(): String? = prefs.getAccessToken()

    suspend fun logout() {
        prefs.clear()
    }

    private suspend fun handleResponse(response: Response<ResponseAuth>): Result<ResponseAuth> {
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

    private fun handleOtpCreateResponse(response: Response<ResponseOk>): Result<ResponseOk> {
        val statusCode = response.code()
        return if (response.isSuccessful) {
            val body = response.body()!!
            Result.success(body)
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
}
