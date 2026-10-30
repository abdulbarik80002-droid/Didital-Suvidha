package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.security.SecureCredentialStorage
import com.example.data.security.VerificationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PinLockUiState(
    val pinBuffer: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isUnlocked: Boolean = false,
    val remainingLockoutSeconds: Long = 0L,
    val isBiometricEnabled: Boolean = true,
    val isScrambledKeypad: Boolean = false,
    val shakeErrorTrigger: Long = 0L,
    val randomizedKeypad: List<String> = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
)

/**
 * ViewModel responsible for credential verification, local storage encryption coordination,
 * brute-force lockout handling, and screen lock state transitions.
 */
class PinLockViewModel(application: Application) : AndroidViewModel(application) {

    private val secureStorage = SecureCredentialStorage(application.applicationContext)

    private val _uiState = MutableStateFlow(
        PinLockUiState(
            isBiometricEnabled = secureStorage.isBiometricEnabled(),
            remainingLockoutSeconds = secureStorage.getRemainingLockoutSeconds()
        )
    )
    val uiState: StateFlow<PinLockUiState> = _uiState.asStateFlow()

    private var lockoutCountdownJob: Job? = null

    init {
        val remaining = secureStorage.getRemainingLockoutSeconds()
        if (remaining > 0) {
            startLockoutCountdown(remaining)
        }
    }

    fun onDigit(digit: String) {
        if (_uiState.value.isUnlocked) return
        if (_uiState.value.remainingLockoutSeconds > 0) return

        if (_uiState.value.pinBuffer.length < 4) {
            val newPin = _uiState.value.pinBuffer + digit
            _uiState.update { it.copy(pinBuffer = newPin, errorMessage = null) }

            if (newPin.length == 4) {
                verifyPin(newPin)
            }
        }
    }

    fun onBackspace() {
        if (_uiState.value.pinBuffer.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    pinBuffer = it.pinBuffer.dropLast(1),
                    errorMessage = null
                )
            }
        }
    }

    fun onClear() {
        _uiState.update { it.copy(pinBuffer = "", errorMessage = null) }
    }

    fun submitPin() {
        val currentPin = _uiState.value.pinBuffer
        if (currentPin.length == 4) {
            verifyPin(currentPin)
        } else if (currentPin.isEmpty()) {
            // Default demo fallback if submitted blank
            verifyPin(SecureCredentialStorage.DEFAULT_PIN)
        } else {
            _uiState.update {
                it.copy(
                    errorMessage = "Please enter all 4 digits",
                    shakeErrorTrigger = System.currentTimeMillis()
                )
            }
        }
    }

    private fun verifyPin(pin: String) {
        val result = secureStorage.verifyPin(pin)
        when (result) {
            is VerificationResult.Success -> {
                _uiState.update {
                    it.copy(
                        isUnlocked = true,
                        pinBuffer = "",
                        errorMessage = null,
                        remainingLockoutSeconds = 0L
                    )
                }
            }
            is VerificationResult.IncorrectPin -> {
                _uiState.update {
                    it.copy(
                        pinBuffer = "",
                        errorMessage = "Incorrect PIN (${result.remainingAttempts} attempts remaining). Default: 1234",
                        shakeErrorTrigger = System.currentTimeMillis()
                    )
                }
            }
            is VerificationResult.LockedOut -> {
                _uiState.update {
                    it.copy(
                        pinBuffer = "",
                        errorMessage = "Too many failed attempts. Locked for ${result.remainingSeconds}s",
                        remainingLockoutSeconds = result.remainingSeconds,
                        shakeErrorTrigger = System.currentTimeMillis()
                    )
                }
                startLockoutCountdown(result.remainingSeconds)
            }
        }
    }

    private fun startLockoutCountdown(seconds: Long) {
        lockoutCountdownJob?.cancel()
        lockoutCountdownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                _uiState.update { it.copy(remainingLockoutSeconds = remaining) }
                delay(1000L)
                remaining--
            }
            secureStorage.resetLockout()
            _uiState.update {
                it.copy(
                    remainingLockoutSeconds = 0L,
                    errorMessage = null
                )
            }
        }
    }

    fun quickDemoUnlock() {
        _uiState.update {
            it.copy(
                isUnlocked = true,
                pinBuffer = "",
                errorMessage = null,
                remainingLockoutSeconds = 0L
            )
        }
    }

    fun unlockWithBiometrics() {
        if (_uiState.value.remainingLockoutSeconds > 0) return
        _uiState.update {
            it.copy(
                isUnlocked = true,
                pinBuffer = "",
                errorMessage = null
            )
        }
    }

    fun lockScreen() {
        _uiState.update {
            it.copy(
                isUnlocked = false,
                pinBuffer = "",
                errorMessage = null
            )
        }
    }

    fun toggleKeypadScramble() {
        _uiState.update { current ->
            val shouldScramble = !current.isScrambledKeypad
            val digits = if (shouldScramble) {
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").shuffled()
            } else {
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            }
            current.copy(
                isScrambledKeypad = shouldScramble,
                randomizedKeypad = digits
            )
        }
    }

    fun changePin(newPin: String): Boolean {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            secureStorage.savePin(newPin)
            return true
        }
        return false
    }

    fun setBiometricEnabled(enabled: Boolean) {
        secureStorage.setBiometricEnabled(enabled)
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }
}
