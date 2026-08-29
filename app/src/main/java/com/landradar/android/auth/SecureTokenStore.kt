package com.landradar.android.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores tokens encrypted with a key protected by Android Keystore.
 * Never store passwords, OTP values, API secrets, or tokens in logs/build config.
 */
class SecureTokenStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "landradar_auth",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(tokens: AuthTokens) {
        preferences.edit()
            .putString("access_token", tokens.accessToken)
            .putString("refresh_token", tokens.refreshToken)
            .putLong("expires_at", tokens.expiresAtEpochSeconds)
            .apply()
    }

    fun load(): AuthTokens? {
        val access = preferences.getString("access_token", null) ?: return null
        val refresh = preferences.getString("refresh_token", null) ?: return null
        return AuthTokens(access, refresh, preferences.getLong("expires_at", 0))
    }

    fun clear() = preferences.edit().clear().apply()
}
