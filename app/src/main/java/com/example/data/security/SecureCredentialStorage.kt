package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

sealed class VerificationResult {
    object Success : VerificationResult()
    data class IncorrectPin(val remainingAttempts: Int) : VerificationResult()
    data class LockedOut(val remainingSeconds: Long) : VerificationResult()
}

/**
 * Secure local credential management using SharedPreferences backed by
 * PBKDF2/SHA-256 salted hashes and brute-force attempt rate limiting.
 */
class SecureCredentialStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "digital_suvidha_secure_prefs"
        const val DEFAULT_PIN = "1234"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_SALT = "key_pin_salt"
        private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "key_lockout_until"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_AUTO_LOCK = "key_auto_lock"

        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30000L // 30 seconds lockout
    }

    init {
        // Initialize with default PIN if not present
        if (!prefs.contains(KEY_PIN_HASH)) {
            savePin(DEFAULT_PIN)
        }
    }

    /**
     * Hashes and stores the new PIN using PBKDF2 with cryptographic salt.
     */
    fun savePin(pin: String) {
        val salt = CryptoManager.generateSalt()
        val hash = CryptoManager.hashPin(pin, salt)
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)

        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_PIN_SALT, saltBase64)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
    }

    /**
     * Verifies the entered PIN against the stored cryptographic hash.
     * Enforces rate-limiting and lockout upon repeated failures.
     */
    fun verifyPin(enteredPin: String): VerificationResult {
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)

        if (now < lockoutUntil) {
            val remainingSeconds = (lockoutUntil - now + 999) / 1000
            return VerificationResult.LockedOut(remainingSeconds)
        }

        val storedHash = prefs.getString(KEY_PIN_HASH, null)
        val saltBase64 = prefs.getString(KEY_PIN_SALT, null)

        val isValid = if (storedHash != null && saltBase64 != null) {
            val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
            CryptoManager.verifyPin(enteredPin, storedHash, salt)
        } else {
            enteredPin == DEFAULT_PIN
        }

        if (isValid) {
            resetLockout()
            return VerificationResult.Success
        } else {
            val failed = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            if (failed >= MAX_FAILED_ATTEMPTS) {
                val lockoutTime = now + LOCKOUT_DURATION_MS
                prefs.edit()
                    .putInt(KEY_FAILED_ATTEMPTS, failed)
                    .putLong(KEY_LOCKOUT_UNTIL, lockoutTime)
                    .apply()
                return VerificationResult.LockedOut(LOCKOUT_DURATION_MS / 1000)
            } else {
                prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failed).apply()
                val remaining = MAX_FAILED_ATTEMPTS - failed
                return VerificationResult.IncorrectPin(remaining)
            }
        }
    }

    fun getRemainingLockoutSeconds(): Long {
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        return if (now < lockoutUntil) {
            (lockoutUntil - now + 999) / 1000
        } else {
            0L
        }
    }

    fun resetLockout() {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
    }

    fun isAutoLockEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_LOCK, true)

    fun setAutoLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LOCK, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
}
