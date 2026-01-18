package org.thingai.android.module.meo.handler.auth

import com.google.gson.Gson
import org.thingai.android.module.meo.handler.auth.internal.AuthApi
import org.thingai.base.log.ILog
import org.thingai.meo.common.dto.ResponseError
import org.thingai.meo.common.dto.auth.RequestLogin
import org.thingai.meo.common.dto.auth.ResponseAuth

class AuthHandler(private val api: AuthApi) {
    private val TAG = "AuthHandler"
    private val gson = Gson()

    suspend fun login(authUsername: String, password: String): Result<ResponseAuth> {
        return try {
            // build request body
            val requestBody = RequestLogin()
            requestBody.authUsername = authUsername
            requestBody.password = password

            val response = api.login(requestBody)
            val statusCode = response.code() // Access HTTP status code

            if (response.isSuccessful) {
                // Return parsed successful body
                Result.success(response.body()!!)
            } else {
                // Read and parse the error body once
                val errorBodyString = response.errorBody()?.string()
                val errorDetail = try {
                    val errorObj = gson.fromJson(errorBodyString, ResponseError::class.java)
                    errorObj?.detail ?: "Unknown error"
                } catch (e: Exception) {
                    "Error code $statusCode: ${response.message()}"
                }

                ILog.e(TAG, "login failed ($statusCode): $errorDetail")
                Result.failure(Exception(errorDetail))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            ILog.e(TAG, "failed", t.message)
            Result.failure(t)
        }
    }


}