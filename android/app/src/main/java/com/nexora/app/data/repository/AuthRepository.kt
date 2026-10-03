package com.nexora.app.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.nexora.app.util.await
import java.util.concurrent.TimeUnit

class AuthRepository(
    private val auth: FirebaseAuth,
) {
    val currentUid: String?
        get() = auth.currentUser?.uid

    val currentPhone: String?
        get() = auth.currentUser?.phoneNumber

    fun requestOtp(
        activity: Activity,
        phone: String,
        onCodeSent: (String) -> Unit,
        onAutoVerified: (String) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { result -> onAutoVerified(result.user?.uid.orEmpty()) }
                    .addOnFailureListener(onError)
            }

            override fun onVerificationFailed(error: FirebaseException) {
                onError(error)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyCode(verificationId: String, code: String): String {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        val result = auth.signInWithCredential(credential).await()
        return requireNotNull(result.user?.uid) { "Firebase Auth did not return a UID" }
    }

    fun signOut() {
        auth.signOut()
    }
}
