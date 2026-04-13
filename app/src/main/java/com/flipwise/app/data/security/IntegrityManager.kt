package com.flipwise.app.data.security

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import kotlinx.coroutines.tasks.await

class IntegrityManager(private val context: Context) {
    private val integrityManager = IntegrityManagerFactory.create(context)

    /**
     * Requests an integrity token from Google Play Integrity API.
     * @param nonce A unique, server-generated base-64 encoded string to prevent replay attacks.
     */
    suspend fun fetchIntegrityToken(nonce: String): String? {
        return try {
            val request = IntegrityTokenRequest.builder()
                .setCloudProjectNumber(1028374659123L) // Replace with your actual Google Cloud Project Number
                .setNonce(nonce)
                .build()

            val response: IntegrityTokenResponse = integrityManager.requestIntegrityToken(request).await()
            response.token()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
