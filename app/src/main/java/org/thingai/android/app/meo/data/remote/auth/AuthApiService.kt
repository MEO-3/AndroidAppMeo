package org.thingai.android.app.meo.data.remote.auth

import org.thingai.android.app.meo.model.dto.AuthResponse
import org.thingai.android.app.meo.model.dto.LoginRequest
import org.thingai.android.app.meo.model.dto.SignupRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: Map<String, String>): AuthResponse

    @POST("api/v1/auth/logout")
    suspend fun logout(): Unit

    // synchronous refresh for Authenticator
    @POST("api/v1/auth/refresh")
    fun refreshSync(@Body body: Map<String, String>): Call<AuthResponse>
}

