package org.thingai.android.module.meo.handler.auth.internal

import org.thingai.meo.common.dto.auth.RequestLogin
import org.thingai.meo.common.dto.auth.ResponseAuth
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body requestBody: RequestLogin): Response<ResponseAuth>
}