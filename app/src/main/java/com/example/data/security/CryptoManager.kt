package com.example.data.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Handles cryptographic operations for local credential protection:
 * - PBKDF2/SHA-256 password-based key derivation with cryptographic salt
 * - Constant-time comparison to prevent timing attacks
 * - AES-256-GCM symmetric encryption & decryption for credentials in local storage
 */
object CryptoManager {

    private const val ALGORITHM_AES = "AES"
    private const val CIPHER_AES_GCM = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SIZE_BYTES = 12
    private const val SALT_SIZE_BYTES = 16
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH_BITS = 256

    private val secureRandom = SecureRandom()

    // Master seed for local symmetric encryption
    private val LOCAL_STORAGE_SECRET: ByteArray by lazy {
        val masterSeed = "DigitalSuvidhaSecuredCredentialsMasterKey2026!#"
        val digest = MessageDigest.getInstance("SHA-256")
        digest.digest(masterSeed.toByteArray(Charsets.UTF_8))
    }

    /**
     * Generates a cryptographically strong random salt.
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_SIZE_BYTES)
        secureRandom.nextBytes(salt)
        return salt
    }

    /**
     * Hashes a PIN with a given salt using PBKDF2 with HMAC-SHA256 (or SHA-256 iterative digest fallback).
     */
    fun hashPin(pin: String, salt: ByteArray): String {
        return try {
            val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
            val skf = try {
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            } catch (_: Exception) {
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            }
            val hash = skf.generateSecret(spec).encoded
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (_: Exception) {
            // High-security iterative SHA-256 fallback
            val md = MessageDigest.getInstance("SHA-256")
            md.update(salt)
            var current = md.digest(pin.toByteArray(Charsets.UTF_8))
            for (i in 1..PBKDF2_ITERATIONS) {
                md.reset()
                md.update(current)
                md.update(salt)
                current = md.digest()
            }
            Base64.encodeToString(current, Base64.NO_WRAP)
        }
    }

    /**
     * Verifies an entered PIN against a stored hash using constant-time comparison
     * to eliminate timing side-channel vulnerabilities.
     */
    fun verifyPin(enteredPin: String, storedHashBase64: String, salt: ByteArray): Boolean {
        val calculatedHashBase64 = hashPin(enteredPin, salt)
        val calculatedBytes = calculatedHashBase64.toByteArray(Charsets.UTF_8)
        val storedBytes = storedHashBase64.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(calculatedBytes, storedBytes)
    }

    /**
     * Encrypts plaintext bytes using AES-256-GCM with a fresh random IV.
     * Returns a string containing IV + ":" + Base64 ciphertext.
     */
    fun encrypt(plainText: String): String {
        val iv = ByteArray(IV_SIZE_BYTES)
        secureRandom.nextBytes(iv)

        val secretKey: SecretKey = SecretKeySpec(LOCAL_STORAGE_SECRET, ALGORITHM_AES)
        val cipher = Cipher.getInstance(CIPHER_AES_GCM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherBase64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)
        return "$ivBase64:$cipherBase64"
    }

    /**
     * Decrypts an encrypted string created by encrypt().
     */
    fun decrypt(encryptedString: String): String? {
        return try {
            val parts = encryptedString.split(":")
            if (parts.size != 2) return null

            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)

            val secretKey: SecretKey = SecretKeySpec(LOCAL_STORAGE_SECRET, ALGORITHM_AES)
            val cipher = Cipher.getInstance(CIPHER_AES_GCM)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(cipherText)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }
}
