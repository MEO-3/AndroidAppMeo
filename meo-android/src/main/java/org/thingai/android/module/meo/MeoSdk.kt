package org.thingai.android.module.meo

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.thingai.android.module.meo.ble.impl.MBleClientImpl
import org.thingai.android.module.meo.handler.auth.AuthHandler
import org.thingai.android.module.meo.cloud.AuthApi
import org.thingai.android.module.meo.handler.auth.internal.AuthInterceptor
import org.thingai.android.module.meo.handler.auth.internal.AuthPrefs
import org.thingai.android.module.meo.handler.auth.internal.TokenAuthenticator
import org.thingai.android.module.meo.handler.discovery.MDeviceDiscoveryBleHandlerImpl
import org.thingai.base.log.ILog
import org.thingai.meo.common.callback.RequestCallback
import org.thingai.meo.common.handler.MDeviceDiscoveryHandlerBle
import org.thingai.meo.common.handler.MDeviceDiscoveryHandlerLan
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MeoSdk private constructor(
    private val appContext: Context
) {
    private val TAG = "MeoSdk"
    private val BASE_CLOUD_URL = "https://iot.yirlodt.io.vn/"

    private lateinit var retrofit: Retrofit
    private lateinit var authHandler: AuthHandler
    private lateinit var bleDiscoveryHandler: MDeviceDiscoveryHandlerBle
    private lateinit var lanDiscoveryHandler: MDeviceDiscoveryHandlerLan


    private fun init() {
        ILog.d(TAG, "init")

        // 1. Init Internal Prefs
        val authPrefs = AuthPrefs(appContext)

        // Init separated auth http client to avoid deadlock on refresh
        val authHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authPrefs))
            .build()

        // 2. Build OkHttpClient with Interceptor and Authenticator
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authPrefs))
            .authenticator(TokenAuthenticator({ authHandler }, authPrefs))
            .build()

        // 3. Init Retrofit
        val authRetrofit = Retrofit.Builder()
            .baseUrl(BASE_CLOUD_URL)
            .client(authHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_CLOUD_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // 4. Init handlers
        authHandler = AuthHandler(authRetrofit.create(AuthApi::class.java), authPrefs)
        bleDiscoveryHandler = MDeviceDiscoveryBleHandlerImpl(MBleClientImpl(appContext))

        instance = this
    }

    /**
     * Try to connect / validate current authentication state.
     * The callback receives a boolean indicating whether the SDK is authenticated.
     * onSuccess(true, message) -> authenticated
     * onSuccess(false, message) -> not authenticated or backend rejected token
     * onFailure(code, message) -> unrecoverable error (e.g., exception)
     */
    fun connect(callback: RequestCallback<Boolean>) {
        CoroutineScope(Dispatchers.IO).launch {
            ILog.d(TAG, "connect")
            try {
                // 1) Check if there is an access token stored
                val accessToken = authHandler.getAccessToken()
                if (accessToken.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        callback.onSuccess(false, "No access token")
                    }
                    return@launch
                }

                // 2) Try to refresh tokens to validate authentication; refresh will use stored refresh token
                val refreshResult = authHandler.refresh()

                if (refreshResult.isSuccess) {
                    withContext(Dispatchers.Main) {
                        callback.onSuccess(true, "Authenticated")
                    }
                } else {
                    val msg = refreshResult.exceptionOrNull()?.message ?: "Not authenticated"
                    withContext(Dispatchers.Main) {
                        callback.onSuccess(false, msg)
                    }
                }
            } catch (t: Throwable) {
                ILog.e(TAG, "connect failed", t.message)
                withContext(Dispatchers.Main) {
                    callback.onFailure(-1, t.message ?: "Unknown error")
                }
            }
        }
    }

    companion object {
        private lateinit var instance: MeoSdk

        fun init(appContext: Context) {
            instance = MeoSdk(appContext.applicationContext)
            instance.init()
        }

        fun authHandler(): AuthHandler {
            return instance.authHandler
        }

        // Convenience static connect method
        fun connect(callback: RequestCallback<Boolean>) {
            if (!::instance.isInitialized) {
                callback.onFailure(1, "MeoSdk not initialized")
                return
            }
            instance.connect(callback)
        }

        fun bleDiscoveryHandler(): MDeviceDiscoveryHandlerBle {
            return instance.bleDiscoveryHandler
        }
    }
}
