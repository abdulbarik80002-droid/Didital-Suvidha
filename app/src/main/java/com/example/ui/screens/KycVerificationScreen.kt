package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalletAccount
import com.example.ui.theme.*

@Composable
fun KycVerificationScreen(
    wallet: WalletAccount?,
    onVerifyAadhaar: (ref: String) -> Unit,
    onVerifyPan: (ref: String, maskedPan: String) -> Unit,
    onVerifyDl: (ref: String, maskedDl: String) -> Unit,
    onVerifyPhone: (phone: String) -> Unit
) {
    var showAadhaarModal by remember { mutableStateOf(false) }
    var showPanModal by remember { mutableStateOf(false) }
    var showDlModal by remember { mutableStateOf(false) }
    var showPhoneModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Statutory Compliance & PII Security Header
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SuvidhaNavyDark),
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SuvidhaGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SuvidhaGold, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("KYC & Statutory Verification Hub", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                Text("UIDAI • NSDL • MoRTH • Telecom Access", fontSize = 11.sp, color = Color(0xFFD4E5FF))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = SuvidhaGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AES-256 GCM PII Encryption Standard", fontSize = 10.5.sp, color = Color(0xFF80CBC4), fontWeight = FontWeight.Medium)
                        }
                        Text("100% Retained", fontSize = 10.sp, color = SuvidhaGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Document Verification Cards
        item {
            Text("Verification Workflows", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        // 1. Aadhaar Card
        item {
            KycDocumentCard(
                title = "Aadhaar Card Verification",
                subtitle = "DigiLocker / MeriPehchaan Government Portal",
                docNumber = wallet?.aadhaarMasked ?: "XXXX-XXXX-8421",
                isVerified = wallet?.isAadhaarVerified == true,
                refId = wallet?.digilockerRef ?: "DL-UIDAI-784912",
                icon = Icons.Default.Badge,
                onVerifyClick = { showAadhaarModal = true }
            )
        }

        // 2. PAN Card
        item {
            KycDocumentCard(
                title = "PAN Card Verification",
                subtitle = "Income Tax Department / NSDL Portal",
                docNumber = wallet?.panNumberMasked ?: "AAACS****F",
                isVerified = wallet?.isPanVerified == true,
                refId = wallet?.panRef ?: "NSDL-PAN-940218",
                icon = Icons.Default.CreditCard,
                onVerifyClick = { showPanModal = true }
            )
        }

        // 3. Driving License
        item {
            KycDocumentCard(
                title = "Driving License Verification",
                subtitle = "Ministry of Road Transport (MoRTH / Sarathi)",
                docNumber = wallet?.dlNumberMasked ?: "DL-04****2018",
                isVerified = wallet?.isDlVerified == true,
                refId = wallet?.dlRef ?: "SARATHI-DL-381920",
                icon = Icons.Default.DirectionsCar,
                onVerifyClick = { showDlModal = true }
            )
        }

        // 4. Mobile Number Access & OTP
        item {
            KycDocumentCard(
                title = "Mobile Number & OTP Access",
                subtitle = "Telecom 2FA Verification Gateway",
                docNumber = wallet?.verifiedPhoneNumber ?: "+91 98765 43210",
                isVerified = wallet?.isPhoneVerified == true,
                refId = "OTP-SMS-AUTH",
                icon = Icons.Default.Smartphone,
                onVerifyClick = { showPhoneModal = true }
            )
        }
    }

    // Modal: Aadhaar Verification
    if (showAadhaarModal) {
        var aadhaarInput by remember { mutableStateOf("8421") }
        var otpInput by remember { mutableStateOf("382910") }
        AlertDialog(
            onDismissRequest = { showAadhaarModal = false },
            title = { Text("DigiLocker Aadhaar Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Secure OTP authorization via DigiLocker Government Gateway:")
                    OutlinedTextField(
                        value = "XXXX-XXXX-$aadhaarInput",
                        onValueChange = {},
                        label = { Text("Aadhaar Number (Last 4 digits)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text("Enter 6-Digit DigiLocker OTP") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ref = "DL-UIDAI-${(100000..999999).random()}"
                        onVerifyAadhaar(ref)
                        showAadhaarModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Confirm Verification")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAadhaarModal = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: PAN Verification
    if (showPanModal) {
        var panInput by remember { mutableStateOf("AAACS1234F") }
        AlertDialog(
            onDismissRequest = { showPanModal = false },
            title = { Text("NSDL PAN Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Verify contractor Permanent Account Number with Income Tax Database:")
                    OutlinedTextField(
                        value = panInput,
                        onValueChange = { panInput = it.uppercase() },
                        label = { Text("PAN Number (10 Characters)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val masked = panInput.take(5) + "****" + panInput.takeLast(1)
                        val ref = "NSDL-PAN-${(100000..999999).random()}"
                        onVerifyPan(ref, masked)
                        showPanModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Verify with NSDL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanModal = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Driving License
    if (showDlModal) {
        var dlInput by remember { mutableStateOf("DL-0420180092341") }
        AlertDialog(
            onDismissRequest = { showDlModal = false },
            title = { Text("Sarathi Driving License Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Verify contractor ID via MoRTH Sarathi National Register:")
                    OutlinedTextField(
                        value = dlInput,
                        onValueChange = { dlInput = it.uppercase() },
                        label = { Text("DL Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val masked = dlInput.take(5) + "****" + dlInput.takeLast(4)
                        val ref = "SARATHI-DL-${(100000..999999).random()}"
                        onVerifyDl(ref, masked)
                        showDlModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Verify with MoRTH")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDlModal = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Mobile OTP
    if (showPhoneModal) {
        var phoneInput by remember { mutableStateOf("+91 98765 43210") }
        var otpInput by remember { mutableStateOf("918234") }
        AlertDialog(
            onDismissRequest = { showPhoneModal = false },
            title = { Text("Mobile Phone Access & OTP Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Verify mobile phone ownership for 2FA SMS alerts:")
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Mobile Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text("Enter 6-Digit SMS OTP") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onVerifyPhone(phoneInput)
                        showPhoneModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Verify Mobile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneModal = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun KycDocumentCard(
    title: String,
    subtitle: String,
    docNumber: String,
    isVerified: Boolean,
    refId: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onVerifyClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isVerified) SuvidhaGreen else SuvidhaAmberDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    Text(docNumber, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(subtitle, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (isVerified && refId.isNotEmpty()) {
                        Text("Ref: $refId", fontSize = 9.5.sp, color = SuvidhaGreen, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (isVerified) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Verified", color = SuvidhaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onVerifyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaAmberDark),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Verify Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
