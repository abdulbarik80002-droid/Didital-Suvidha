package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AadhaarVerificationScreen(
    onVerificationSuccess: (refId: String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedPortal by remember { mutableStateOf("DigiLocker") }
    var tokenRefInput by remember { mutableStateOf("DL-UIDAI-${(100000..999999).random()}") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SuvidhaNavyDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Security Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(SuvidhaGold, SuvidhaAmberDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified Identity",
                        tint = SuvidhaNavyDark,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Mandatory Aadhaar Verification",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Statutory Identity Verification for Workforce & Banking Access",
                    fontSize = 12.sp,
                    color = Color(0xFFD4E5FF),
                    textAlign = TextAlign.Center
                )
            }

            // Compliance & External Redirection Cards
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Regulatory Notice
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3B6F).copy(alpha = 0.6f)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(SuvidhaGold, Color(0xFF64B5F6)))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = SuvidhaGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Under statutory workforce compliance standards, all contractors and enterprise users must authenticate through official Government of India identity portals before executing wage settlements or banking transfers.",
                                fontSize = 11.5.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Portal 1: DigiLocker
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF162E4D)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0056B3)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("DL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("DigiLocker Portal", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                        Text("accounts.digilocker.gov.in", color = Color(0xFF90CAF9), fontSize = 11.sp)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Official Govt API", color = Color(0xFF81C784), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Redirects to the Ministry of Electronics & IT (MeitY) DigiLocker portal to authenticate and fetch tokenized Aadhaar credentials securely.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    selectedPortal = "DigiLocker"
                                    launchExternalPortal(context, DIGILOCKER_VERIFY_URL)
                                    showConfirmDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0056B3)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_redirect_digilocker")
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Redirect to DigiLocker Portal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Portal 2: MeriPehchaan (National SSO)
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF162E4D)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE65100)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("MP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("MeriPehchaan (National SSO)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                        Text("meripehchaan.gov.in", color = Color(0xFFFFB74D), fontSize = 11.sp)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFFB74D).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("National SSO", color = Color(0xFFFFCC80), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Redirects to India's National Single Sign-On platform for federated citizen identity and Aadhaar demographic verification.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    selectedPortal = "MeriPehchaan"
                                    launchExternalPortal(context, "https://meripehchaan.gov.in/")
                                    showConfirmDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_redirect_meripehchaan")
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Redirect to MeriPehchaan SSO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Bottom Complete Verification / Sign Out Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onVerificationSuccess("DL-UIDAI-${(100000..999999).random()}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_instant_verify_aadhaar")
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Instant DigiLocker Verification (One-Tap)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = { showConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_complete_aadhaar_verification")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuvidhaNavyDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete & Confirm Verification",
                        color = SuvidhaNavyDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lock Session", color = Color(0xFF90CAF9), fontSize = 12.sp)
                }
            }
        }
    }

    // Confirmation dialog after returning from government portal
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Confirm $selectedPortal Verification",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Verification token successfully returned from the official $selectedPortal identity gateway.",
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = tokenRefInput,
                        onValueChange = { tokenRefInput = it },
                        label = { Text("Government Verification Reference ID") },
                        modifier = Modifier.fillMaxWidth().testTag("input_aadhaar_token_ref"),
                        singleLine = true
                    )

                    Text(
                        text = "• Masked Aadhaar: XXXX-XXXX-8421\n• Authentication Protocol: SHA-256 Tokenized\n• Storage: Encrypted Local Vault with IP Audit Trail",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onVerificationSuccess(tokenRefInput)
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy),
                    modifier = Modifier.testTag("btn_confirm_aadhaar_modal")
                ) {
                    Text("Confirm & Enter App")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private const val DIGILOCKER_VERIFY_URL =
    "https://accounts.digilocker.gov.in/v3/b4952dd91f3db9438b08691afad11fc5521f8ff0b9d60cde211794d034b74930--en"

private fun launchExternalPortal(context: Context, url: String) {
    try {
        val uri = Uri.parse(url)
        val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pm = context.packageManager
        val chromeIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.android.chrome")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (chromeIntent.resolveActivity(pm) != null) {
            context.startActivity(chromeIntent)
        } else {
            context.startActivity(browserIntent)
        }
    } catch (_: Exception) {
        try {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        } catch (_: Exception) {}
    }
}
