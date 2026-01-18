package org.thingai.android.module.meo

import android.content.Context
import org.thingai.android.module.meo.handler.auth.AuthHandler
import org.thingai.android.module.meo.handler.auth.internal.AuthApi
import org.thingai.base.log.ILog
import org.thingai.meo.common.handler.MDeviceDiscoveryBleHandler
import org.thingai.meo.common.handler.MDeviceDiscoveryLanHandler
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MeoSdk private constructor(
    private val appContext: Context
) {
    private val TAG = "MeoSdk"
    private val BASE_CLOUD_URL = "https://iot.yirlodt.io.vn/"

    private lateinit var retrofit: Retrofit
    private lateinit var authHandler: AuthHandler
    private lateinit var bleDiscoveryHandler: MDeviceDiscoveryBleHandler
    private lateinit var lanDiscoveryHandler: MDeviceDiscoveryLanHandler

    private fun init() {
        // Init retrofit
        ILog.d(TAG, "init")

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_CLOUD_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Init handlers
        authHandler = AuthHandler(retrofit.create(AuthApi::class.java))

        instance = this
    }

    companion object {
        private lateinit var instance: MeoSdk

        fun init(appContext: Context) {
            instance = MeoSdk(appContext)
            instance.init()

            ILog.d("MeoSdk", "init")
        }

        fun authHandler(): AuthHandler {
            return instance.authHandler
        }
    }
}