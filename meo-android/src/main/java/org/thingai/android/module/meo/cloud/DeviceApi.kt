package org.thingai.android.module.meo.cloud

import org.thingai.meo.common.dto.ResponseOk
import org.thingai.meo.common.dto.device.RequestDeviceUpsert
import org.thingai.meo.common.dto.device.ResponseDevice
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DeviceApi {
    @POST("/api/v1/devices")
    suspend fun createDevice(@Body requestBody: RequestDeviceUpsert): Response<ResponseDevice>

    @GET("/api/v1/devices/{deviceId}")
    suspend fun getDevice(@Path("deviceId") deviceId: String): Response<ResponseDevice>

    @GET("/api/v1/devices")
    suspend fun getDevices(): Response<List<ResponseDevice>>

    @PUT("/api/v1/devices/{deviceId}")
    suspend fun updateDevice(@Path("deviceId") deviceId: String, @Body requestBody: RequestDeviceUpsert): Response<ResponseDevice>

    @DELETE("/api/v1/devices/{deviceId}")
    suspend fun deleteDevice(@Path("deviceId") deviceId: String): Response<ResponseOk>
}