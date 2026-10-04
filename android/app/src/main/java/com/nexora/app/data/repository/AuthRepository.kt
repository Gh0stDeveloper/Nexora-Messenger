package com.nexora.app.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.nexora.app.data.preview.PreviewSession
import com.nexora.app.util.PhoneNumberNormalizer
import com.nexora.app.util.await
import java.util.concurrent.TimeUnit

class AuthRepository(
    private val auth: FirebaseAuth,
) {
    val currentUid: String?
        get() = PreviewSession.uid ?: auth.currentUser?.uid

    val currentPhone: String?
        get() = PreviewSession.phone ?: auth.currentUser?.phoneNumber

    val isPreview: Boolean
        get() = PreviewSession.active

    fun requestOtp(
        activity: Activity,
        phone: String,
        onCodeSent: (String) -> Unit,
        onAutoVerified: (String) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val normalized = PhoneNumberNormalizer.normalizeMexico(phone)
        if (normalized == PreviewSession.PreviewPhone) {
            PreviewSession.enable()
            onCodeSent("preview-verification")
            return
        }

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
            .setPhoneNumber(normalized)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyCode(verificationId: String, code: String): String {
        if (verificationId == "preview-verification" && code == PreviewSession.PreviewCode) {
            PreviewSession.enable()
            return PreviewSession.PreviewUid
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        val result = auth.signInWithCredential(credential).await()
        return requireNotNull(result.user?.uid) { "Firebase Auth did not return a UID" }
    }

    fun signOut() {
        PreviewSession.disable()
        auth.signOut()
    }
}
