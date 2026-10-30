package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.AuditLog
import com.example.data.model.WalletAccount
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    wallet: WalletAccount?,
    auditLogs: List<AuditLog>,
    autoLockEnabled: Boolean = true,
    onToggleAutoLock: (Boolean) -> Unit = {},
    onChangePin: (String) -> Unit,
    onLockNow: () -> Unit,
    onResetData: () -> Unit
) {
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showAuditLogsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Aadhaar Compliance & Identity Verification Card (Requirement 1)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aadhaar Compliance Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SuvidhaNavy
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (wallet?.isAadhaarVerified == true) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (wallet?.isAadhaarVerified == true) "Verified" else "Verification Pending",
                                color = if (wallet?.isAadhaarVerified == true) SuvidhaGreen else SuvidhaAmberDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (wallet?.isAadhaarVerified == true) SuvidhaGreen else SuvidhaAmberDark,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Aadhaar UID: ${wallet?.aadhaarMasked ?: "XXXX-XXXX-8421"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (wallet?.isAadhaarVerified == true) "Gateway: DigiLocker / MeriPehchaan (${wallet.digilockerRef})" else "Requires verification via official DigiLocker portal",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. IP Audit Trail & Security Logs Card (Requirement 2)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Security Audit Trail & IP Logging",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SuvidhaNavy
                        )
                        TextButton(
                            onClick = { showAuditLogsDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("btn_view_audit_trail")
                        ) {
                            Text("View All (${auditLogs.size})", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "System automatically captures and logs device IP address during active sessions to satisfy statutory audit standards.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Recent audit entry preview
                    val latest = auditLogs.firstOrNull()
                    if (latest != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = latest.eventType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SuvidhaNavy
                                    )
                                    Text(
                                        text = "IP: ${latest.ipAddress}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = SuvidhaBlue
                                    )
                                }
                                Text(
                                    text = latest.details,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.ENGLISH).format(Date(latest.timestamp)),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Security Settings Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Security & PIN Authentication",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SuvidhaNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showChangePinDialog = true }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pin, contentDescription = null, tint = SuvidhaNavy)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Change Security PIN", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Current demo PIN: 1234", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = SuvidhaNavy)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Auto-Lock on Screen Off / Minimized", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Immediately secures interface when leaving app", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Switch(
                            checked = autoLockEnabled,
                            onCheckedChange = onToggleAutoLock,
                            modifier = Modifier.testTag("switch_auto_lock")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onLockNow() }
                            .testTag("btn_lock_session_now")
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = SuvidhaAmberDark)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Lock Session Now", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Secures application interface immediately", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // 4. Offline Data & Reset Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Management & Offline Engine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SuvidhaNavy
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = SuvidhaGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Encrypted Room Database", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("100% offline persistence on device", fontSize = 11.sp, color = SuvidhaGreen)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Active", color = SuvidhaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SuvidhaRed),
                        modifier = Modifier.fillMaxWidth().testTag("btn_reset_sample_data"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Demo Data to Initial State")
                    }
                }
            }
        }

        // 5. About & Compliance Specification
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = SuvidhaNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Digital Suvidha Specification Compliance", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Mandatory Identity Verification: DigiLocker & MeriPehchaan redirection\n" +
                                "• Localization Standard: Exclusively English interface\n" +
                                "• Data Protection: Encrypted offline Room DB & automatic IP audit trail\n" +
                                "• Ad Network: Dynamic sponsored banners from 500+ corporate partner registry\n" +
                                "• Document Dispatch: Outgoing PDF generation with editable corporate header/footer credentials",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Set 4-Digit Security PIN") },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it },
                    label = { Text("New 4-Digit PIN") },
                    modifier = Modifier.fillMaxWidth().testTag("input_new_pin"),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            onChangePin(newPin)
                            showChangePinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Update PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Application Data?") },
            text = {
                Text("This will restore default contractor workforce records, reset the working balance to ₹85,400.00, and clear previous audit logs.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaRed)
                ) {
                    Text("Reset Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Full Audit Trail Log Viewer Dialog
    if (showAuditLogsDialog) {
        AlertDialog(
            onDismissRequest = { showAuditLogsDialog = false },
            title = {
                Text("Security Audit Trail (IP Logged Sessions)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(auditLogs) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = log.eventType, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuvidhaNavy)
                                    Text(text = "IP: ${log.ipAddress}", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = SuvidhaBlue)
                                }
                                Text(text = log.details, fontSize = 11.sp)
                                Text(
                                    text = "${SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.ENGLISH).format(Date(log.timestamp))} • ${log.deviceModel}",
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAuditLogsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
