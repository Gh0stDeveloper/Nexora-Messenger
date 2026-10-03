package com.nexora.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RelayClientFactory {
    fun create(baseUrl: String, auth: FirebaseAuth): RelayApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(FirebaseAuthInterceptor(auth))
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl.ensureTrailingSlash())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RelayApi::class.java)
    }

    private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"
}
