package com.nexora.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.nexora.app.util.await
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class FirebaseAuthInterceptor(
    private val auth: FirebaseAuth,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking {
            auth.currentUser?.getIdToken(false)?.await()?.token
        }
        val request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
