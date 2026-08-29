package com.landradar.android.auth

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long
)

data class AuthSession(
    val userId: String,
    val tokens: AuthTokens
)

sealed interface AuthResult {
    data class OtpRequired(val challengeId: String) : AuthResult
    data class Authenticated(val session: AuthSession) : AuthResult
    data class Failure(val safeMessage: String) : AuthResult
}

interface AuthApi {
    suspend fun requestOtp(identifier: String): AuthResult
    suspend fun verifyOtp(challengeId: String, otp: String): AuthResult
    suspend fun refresh(refreshToken: String): AuthResult
    suspend fun revoke(refreshToken: String)
}
