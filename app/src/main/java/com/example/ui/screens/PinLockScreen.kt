package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.SecurePinEntry
import com.example.ui.theme.SuvidhaNavyDark

/**
 * Screen lock display using SecurePinEntry with encrypted local storage protection.
 * Supports touch screen keypad, hardware keyboard, biometric unlock, and quick demo unlock.
 */
@Composable
fun PinLockScreen(
    pinBuffer: String,
    errorMessage: String?,
    remainingLockoutSeconds: Long = 0L,
    isBiometricAvailable: Boolean = true,
    isScrambledKeypad: Boolean = false,
    keypadDigits: List<String> = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
    shakeTrigger: Long = 0L,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: () -> Unit,
    onQuickUnlock: () -> Unit = {},
    onSubmitPin: () -> Unit = {},
    onToggleScramble: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SuvidhaNavyDark
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SecurePinEntry(
                    pinBuffer = pinBuffer,
                    errorMessage = errorMessage,
                    remainingLockoutSeconds = remainingLockoutSeconds,
                    isBiometricAvailable = isBiometricAvailable,
                    isScrambledKeypad = isScrambledKeypad,
                    keypadDigits = keypadDigits,
                    shakeTrigger = shakeTrigger,
                    onDigitClick = onDigitClick,
                    onBackspaceClick = onBackspaceClick,
                    onBiometricClick = onBiometricClick,
                    onQuickUnlock = onQuickUnlock,
                    onSubmitPin = onSubmitPin,
                    onToggleScramble = onToggleScramble,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
