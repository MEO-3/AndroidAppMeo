package org.thingai.android.module.meo

import android.content.Context
import org.thingai.android.module.meo.handler.auth.AuthHandler
import org.thingai.android.module.meo.handler.auth.internal.AuthApi
import org.thingai.base.log.ILog
import org.thingai.meo.common.handler.MDeviceDiscoveryBleHandler
import org.thingai.meo.common.handler.MDeviceDiscoveryLanHandler
import retrofit2.Retrofit

class MeoSdk private constructor(context: Context) {
    private val TAG = "MeoSdk"
    private val BASE_CLOUD_URL = "https://iot.yirlodt.io.vn/"

    private lateinit var authHandler: AuthHandler
    private lateinit var bleDiscoveryHandler: MDeviceDiscoveryBleHandler
    private lateinit var lanDiscoveryHandler: MDeviceDiscoveryLanHandler
    private lateinit var retrofit: Retrofit

    private fun init() {
        // Init retrofit
        ILog.d(TAG, "init")
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_CLOUD_URL)
            .build()

        // Init handlers
        authHandler = AuthHandler(retrofit.create(AuthApi::class.java))
    }

    companion object {
        private lateinit var instance: MeoSdk

        fun init(context: Context) {
            instance = MeoSdk(context)
            instance.init()
        }

        fun authHandler(): AuthHandler {
            return instance.authHandler
        }
    }
}