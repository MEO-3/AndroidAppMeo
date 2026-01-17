package org.thingai.android.meo.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import org.thingai.android.meo.data.remote.auth.AuthApiService
import org.thingai.android.meo.data.remote.auth.AuthInterceptor
import org.thingai.android.meo.data.remote.auth.AuthRepository
import org.thingai.android.meo.data.remote.auth.PreferenceDataStoreManager
import org.thingai.android.meo.data.remote.auth.TokenAuthenticator

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providePreferenceDataStore(@ApplicationContext context: Context): PreferenceDataStoreManager =
        PreferenceDataStoreManager(
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO),
                produceFile = { context.preferencesDataStoreFile("auth_prefs") }
            )
        )

    @Provides
    @Singleton
    fun provideAuthApi(store: PreferenceDataStoreManager): AuthApiService {
        val baseUrl = "https://iot.yirlodt.io.vn/"
        val retrofitForAuth = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(store))
            .authenticator(
                TokenAuthenticator(
                    retrofitForAuth.create(AuthApiService::class.java),
                    store
                )
            )
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApiService, store: PreferenceDataStoreManager): AuthRepository =
        AuthRepository(api, store)

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

