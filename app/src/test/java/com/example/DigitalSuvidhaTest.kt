package com.example

import com.example.data.ads.AdNetworkManager
import com.example.data.model.formatPaiseToRupees
import com.example.data.security.CryptoManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DigitalSuvidhaTest {

    @Test
    fun testCurrencyFormatting() {
        val paise = 8540000L // ₹85,400.00
        val formatted = formatPaiseToRupees(paise)
        assertTrue("Formatted currency should contain 85,400", formatted.contains("85,400"))
    }

    @Test
    fun testPinVerificationCrypto() {
        val pin = "1234"
        val salt = CryptoManager.generateSalt()
        val hash = CryptoManager.hashPin(pin, salt)

        assertTrue("PIN verification should succeed for correct PIN", CryptoManager.verifyPin("1234", hash, salt))
        assertFalse("PIN verification should fail for incorrect PIN", CryptoManager.verifyPin("9999", hash, salt))
    }

    @Test
    fun testAesEncryptionDecryption() {
        val sensitiveData = "AADHAAR-UID-784912049182"
        val encrypted = CryptoManager.encrypt(sensitiveData)
        assertNotNull("Ciphertext should not be null", encrypted)
        assertTrue("Ciphertext should contain colon separator", encrypted.contains(":"))

        val decrypted = CryptoManager.decrypt(encrypted)
        assertEquals("Decrypted plain text must match original", sensitiveData, decrypted)
    }

    @Test
    fun testAdNetworkRegistry() {
        val banners = AdNetworkManager.getAllBanners()
        assertTrue("Should have multiple corporate sponsor banners", banners.size >= 5)

        val banner = AdNetworkManager.getAdByIndex(0)
        assertNotNull(banner.companyName)
        assertTrue(banner.headline.isNotEmpty())
    }
}
