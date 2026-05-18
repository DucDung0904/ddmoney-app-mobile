package com.dung.ddmoney.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

class TokenManager(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun hasValidToken(): Boolean {
        val token = getToken() ?: return false
        if (isTokenExpired(token)) {
            clearToken()
            return false
        }
        return true
    }

    fun isTokenExpired(token: String = getToken().orEmpty()): Boolean {
        if (token.isBlank()) return true
        return runCatching {
            val payload = token.split(".").getOrNull(1) ?: return true
            val json = JSONObject(String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)))
            val expSeconds = json.optLong("exp", 0L)
            expSeconds <= 0L || expSeconds * 1000L <= System.currentTimeMillis()
        }.getOrDefault(true)
    }

    fun clearToken() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_AVATAR)
            .remove(KEY_ONBOARDING_DONE)
            .remove(KEY_CURRENCY)
            .apply()
    }

    // ── Onboarding ──────────────────────────────────────────────────
    fun setOnboardingDone(done: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply()
    }

    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    // ── Currency ────────────────────────────────────────────────────
    fun setCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(): String = prefs.getString(KEY_CURRENCY, "VND") ?: "VND"

    fun saveUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    fun saveUserInfo(fullName: String, email: String, avatarUrl: String? = null) {
        prefs.edit()
            .putString(KEY_USER_NAME, fullName)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_AVATAR, avatarUrl ?: "")
            .apply()
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    fun getUserAvatar(): String = prefs.getString(KEY_USER_AVATAR, "") ?: ""
    
    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }
    
    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_CURRENCY = "currency"
    }
}
