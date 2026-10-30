package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SuvidhaAmber
import com.example.ui.theme.SuvidhaAmberDark
import com.example.ui.theme.SuvidhaGold
import com.example.ui.theme.SuvidhaNavy
import com.example.ui.theme.SuvidhaNavyDark
import kotlin.math.roundToInt

/**
 * Secure PIN Entry UI Component:
 * - Highly responsive touch screen number pad with instant ripple feedback
 * - 4-digit animated PIN dot indicator with shake feedback on error
 * - Local storage encryption badge (AES-256 & PBKDF2)
 * - Rate-limited lockout warning card with Quick Unlock bypass
 * - Scramble keypad option for shoulder-surfing defense
 * - Support for both touch screen and hardware keyboard input
 */
@Composable
fun SecurePinEntry(
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
    onQuickUnlock: () -> Unit,
    onSubmitPin: () -> Unit,
    onToggleScramble: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isLockedOut = remainingLockoutSeconds > 0
    val focusRequester = remember { FocusRequester() }

    // Request keyboard focus for emulator / physical keyboard
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Shake animation effect for incorrect PIN
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            for (i in 0..5) {
                val target = if (i % 2 == 0) -14f else 14f
                shakeOffset.animateTo(target, animationSpec = tween(durationMillis = 40))
            }
            shakeOffset.animateTo(0f, animationSpec = tween(durationMillis = 35))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        AndroidKeyEvent.KEYCODE_0, AndroidKeyEvent.KEYCODE_NUMPAD_0 -> {
                            onDigitClick("0")
                            true
                        }
                        AndroidKeyEvent.KEYCODE_1, AndroidKeyEvent.KEYCODE_NUMPAD_1 -> {
                            onDigitClick("1")
                            true
                        }
                        AndroidKeyEvent.KEYCODE_2, AndroidKeyEvent.KEYCODE_NUMPAD_2 -> {
                            onDigitClick("2")
                            true
                        }
                        AndroidKeyEvent.KEYCODE_3, AndroidKeyEvent.KEYCODE_NUMPAD_3 -> {
                            onDigitClick("3")
                            true
                        }
                        AndroidKeyEvent.KEYCODE_4, AndroidKeyEvent.KEYCODE_NUMPAD_4 -> {
                            onDigitClick("4")
                            true
                        }
                        AndroidKeyEvent.KEYCODE_5, AndroidKeyEvent.KEYCODE_NUMPAD_5 -> {
                            onDigitClick("5")
                            true
                        }
                        AndroidKeyEvent.KEYCODE_6, AndroidKeyEvent.KEYCODE_NUMPAD_6 -> {
                            onDigitClick("6")
                            true
                        }
                        AndroidKeyEvent.KEYCODE_7, AndroidKeyEvent.KEYCODE_NUMPAD_7 -> {
                            onDigitClick("7")
                            true
                        }
                        AndroidKeyEvent.KEYCODE_8, AndroidKeyEvent.KEYCODE_NUMPAD_8 -> {
                            onDigitClick("8")
                            true
                        }
                        AndroidKeyEvent.KEYCODE_9, AndroidKeyEvent.KEYCODE_NUMPAD_9 -> {
                            onDigitClick("9")
                            true
                        }
                        AndroidKeyEvent.KEYCODE_DEL -> {
                            onBackspaceClick()
                            true
                        }
                        AndroidKeyEvent.KEYCODE_ENTER, AndroidKeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            onSubmitPin()
                            true
                        }
                        else -> false
                    }
                } else false
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Security badge
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF132A4A).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B4D78)),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Encrypted Local Storage",
                    tint = SuvidhaGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Encrypted Local Storage (AES-256 & PBKDF2)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD4E5FF)
                )
            }
        }

        // Lock header and icon
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(SuvidhaGold, SuvidhaAmber)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLockedOut) Icons.Default.LockClock else Icons.Default.Lock,
                    contentDescription = "Security Screen Lock",
                    tint = SuvidhaNavyDark,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Digital Suvidha",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Secure Workforce & Corporate Banking Terminal",
                fontSize = 11.5.sp,
                color = Color(0xFF90CAF9),
                textAlign = TextAlign.Center
            )
        }

        // Rate-limit lockout alert if active
        AnimatedVisibility(visible = isLockedOut) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1F1F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE57373)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LockClock,
                        contentDescription = "Lockout",
                        tint = Color(0xFFFF8A80),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Brute-Force Protection Active",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Please wait ${remainingLockoutSeconds}s or tap Quick Unlock below.",
                            color = Color(0xFFFFCDD2),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // PIN Dots section with shake offset
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset { IntOffset(x = shakeOffset.value.roundToInt(), y = 0) }
        ) {
            Text(
                text = if (isLockedOut) "Terminal Temporarily Locked" else "Enter 4-Digit Security PIN",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isLockedOut) Color(0xFFFF8A80) else Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 16.dp)
            ) {
                repeat(4) { index ->
                    val isFilled = index < pinBuffer.length
                    val dotSize by animateDpAsState(
                        targetValue = if (isFilled) 18.dp else 14.dp,
                        animationSpec = tween(durationMillis = 150),
                        label = "dot_size_$index"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = when {
                            errorMessage != null -> Color(0xFFFF5252)
                            isFilled -> SuvidhaGold
                            else -> Color.Transparent
                        },
                        animationSpec = tween(durationMillis = 150),
                        label = "dot_color_$index"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = when {
                            errorMessage != null -> Color(0xFFFF5252)
                            isFilled -> SuvidhaGold
                            else -> Color(0xFF607D8B)
                        },
                        animationSpec = tween(durationMillis = 150),
                        label = "border_color_$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(width = 2.dp, color = borderColor, shape = CircleShape)
                            .testTag("pin_dot_$index")
                    )
                }
            }

            // Error message or Default PIN hint
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFFF8A80),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("pin_error_text")
                )
            }
            if (errorMessage == null && !isLockedOut) {
                Text(
                    text = "Default PIN: 1234 • Tap numbers or Quick Unlock",
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.5.sp
                )
            }
        }

        // Numeric Keypad Layout with Responsive Touch Screen Buttons
        val digits = if (keypadDigits.size >= 10) keypadDigits else listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val row1 = listOf(digits[0], digits[1], digits[2])
        val row2 = listOf(digits[3], digits[4], digits[5])
        val row3 = listOf(digits[6], digits[7], digits[8])
        val row4 = listOf("BIO", digits[9], "DEL")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in listOf(row1, row2, row3, row4)) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (key in row) {
                        when (key) {
                            "BIO" -> {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B3B6F))
                                        .border(1.5.dp, Color(0xFF2B4D78), CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true, color = SuvidhaGold),
                                            role = Role.Button,
                                            onClick = onBiometricClick
                                        )
                                        .testTag("pin_key_bio"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Biometric Authentication",
                                        tint = SuvidhaGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            "DEL" -> {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B3B6F))
                                        .border(1.5.dp, Color(0xFF2B4D78), CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true, color = Color.White),
                                            role = Role.Button,
                                            onClick = onBackspaceClick
                                        )
                                        .testTag("btn_pin_del"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete digit",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E3A5F))
                                        .border(1.5.dp, Color(0xFF2D5180), CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true, color = SuvidhaGold),
                                            role = Role.Button,
                                            onClick = { onDigitClick(key) }
                                        )
                                        .testTag("pin_key_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick actions & Scramble keypad button
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onSubmitPin,
                colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_submit_pin")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Unlock (1234)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            }

            Button(
                onClick = onQuickUnlock,
                colors = ButtonDefaults.buttonColors(containerColor = SuvidhaGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_quick_demo_unlock")
            ) {
                Text("Quick Unlock", color = SuvidhaNavyDark, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onToggleScramble,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isScrambledKeypad) SuvidhaGold else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("btn_scramble_keypad")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Scramble Keypad",
                    tint = if (isScrambledKeypad) SuvidhaGold else Color(0xFF90CAF9),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
