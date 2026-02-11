package org.thingai.android.module.meo.cloud

import okhttp3.OkHttpClient
import org.thingai.android.module.meo.handler.auth.internal.AuthInterceptor
import org.thingai.android.module.meo.handler.auth.internal.TokenAuthenticator
import org.thingai.base.log.ILog
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CloudApiClient internal constructor(
    private val baseCloudUrl: String,
    private val interceptor: AuthInterceptor,
    private val authenticator: TokenAuthenticator,
) {
    private val TAG = "CloudApiClient"

    private lateinit var httpClient: OkHttpClient
    private lateinit var retrofit: Retrofit

    private lateinit var apiDevice: ApiDevice
    private lateinit var apiProduct: ApiProduct

    fun init() {
        ILog.d(TAG, "init cloud api client")

        httpClient = OkHttpClient.Builder()
            .authenticator(authenticator)
            .addInterceptor(interceptor)
            .build()
        retrofit = Retrofit.Builder()
            .baseUrl(baseCloudUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiProduct = retrofit.create(ApiProduct::class.java)
        apiDevice = retrofit.create(ApiDevice::class.java)
    }

    fun deviceApi(): ApiDevice {
        return apiDevice
    }

    fun productApi(): ApiProduct {
        return apiProduct
    }
}