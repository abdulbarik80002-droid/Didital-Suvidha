package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attendance
import com.example.data.model.CorporateProfile
import com.example.data.model.PayrollRecord
import com.example.data.model.Worker
import com.example.data.model.formatPaiseToRupees
import com.example.ui.components.MetricCard
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
    corporateProfile: CorporateProfile,
    workers: List<Worker>,
    attendanceList: List<Attendance>,
    payrollList: List<PayrollRecord>,
    onUpdateCorporateProfile: (CorporateProfile) -> Unit,
    onGenerateAndSendPdf: (CorporateProfile, recipientName: String, recipientEmail: String, recipientDob: String) -> Unit,
    onExportCsv: () -> Unit
) {
    val totalWagesPaidPaise = payrollList.sumOf { it.netPayablePaise }
    val totalAdvancesDuePaise = workers.sumOf { it.advanceBalancePaise }
    val sitesGroup = workers.groupBy { it.siteAssigned }
    val tradesGroup = workers.groupBy { it.trade }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEmailDispatchDialog by remember { mutableStateOf(false) }

    var recipientName by remember { mutableStateOf("Site Accounts & Finance Controller") }
    var recipientEmail by remember { mutableStateOf(corporateProfile.emailAddress) }
    var recipientDob by remember { mutableStateOf("15081995") } // DDMMYYYY format

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Corporate Header & PDF Generation Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SuvidhaNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Corporate Document Dispatch",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Monthly PDF reports encrypted with recipient Date of Birth (DDMMYYYY)",
                                color = Color(0xFFD4E5FF),
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = SuvidhaGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Current Corporate Details Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF163252)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = corporateProfile.companyName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                TextButton(
                                    onClick = { showEditProfileDialog = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Edit Credentials", color = SuvidhaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "Contractor: ${corporateProfile.contractorName}  |  GSTIN: ${corporateProfile.gstin}",
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.5.sp
                            )
                            Text(
                                text = "Address: ${corporateProfile.physicalAddress}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Phone: ${corporateProfile.mobileNumber}  |  Email: ${corporateProfile.emailAddress}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dispatch Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showEmailDispatchDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SuvidhaGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_email_encrypted_pdf")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = SuvidhaNavyDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Email Encrypted PDF", color = SuvidhaNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = onExportCsv,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_csv")
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV (BOM)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Financial & Attendance Aggregates
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Consolidated Monthly Aggregates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Wages Disbursed",
                        value = formatPaiseToRupees(totalWagesPaidPaise),
                        icon = Icons.Default.ReceiptLong,
                        iconTint = SuvidhaGreen,
                        modifier = Modifier.weight(1f),
                        subtitle = "${payrollList.size} Vouchers Issued"
                    )

                    MetricCard(
                        title = "Outstanding Advances",
                        value = formatPaiseToRupees(totalAdvancesDuePaise),
                        icon = Icons.Default.MoneyOff,
                        iconTint = SuvidhaAmberDark,
                        modifier = Modifier.weight(1f),
                        subtitle = "Across Active Crew"
                    )
                }
            }
        }

        // 3. Deployment Breakdown by Site
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Deployment & Wage Allocation by Site",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    sitesGroup.forEach { (site, siteWorkers) ->
                        val siteWageSum = siteWorkers.sumOf { it.dailyWagePaise }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = site, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(
                                    text = "${siteWorkers.size} workers deployed",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${formatPaiseToRupees(siteWageSum)} / day",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = SuvidhaNavy
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // 4. Trade Classification
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Workforce Skill Composition",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    tradesGroup.forEach { (trade, tradeWorkers) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = trade, fontSize = 13.sp)
                            Text(
                                text = "${tradeWorkers.size} workers",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Edit Corporate Header/Footer Credentials
    if (showEditProfileDialog) {
        var companyName by remember { mutableStateOf(corporateProfile.companyName) }
        var contractorName by remember { mutableStateOf(corporateProfile.contractorName) }
        var address by remember { mutableStateOf(corporateProfile.physicalAddress) }
        var phone by remember { mutableStateOf(corporateProfile.mobileNumber) }
        var email by remember { mutableStateOf(corporateProfile.emailAddress) }
        var gstin by remember { mutableStateOf(corporateProfile.gstin) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Contractor & Company Branding",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contractorName,
                        onValueChange = { contractorName = it },
                        label = { Text("Contractor Name / Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Official Physical Address *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Official Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it },
                        label = { Text("GSTIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = CorporateProfile(
                            companyName = companyName,
                            contractorName = contractorName,
                            physicalAddress = address,
                            mobileNumber = phone,
                            emailAddress = email,
                            gstin = gstin
                        )
                        onUpdateCorporateProfile(updated)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Save Branding")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Dispatch Encrypted PDF with Recipient DOB
    if (showEmailDispatchDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDispatchDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SuvidhaAmberDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Encrypted PDF Email Dispatch", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "As per Statutory Compliance Blueprint, monthly PDF reports are encrypted with a password format matching the recipient's Date of Birth (DDMMYYYY).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Recipient Designation / Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recipientEmail,
                        onValueChange = { recipientEmail = it },
                        label = { Text("Recipient Email Address *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recipientDob,
                        onValueChange = { if (it.length <= 8) recipientDob = it },
                        label = { Text("Recipient Date of Birth (DDMMYYYY) *") },
                        placeholder = { Text("e.g. 15081995") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "PDF Password will be: $recipientDob\nThe document will be attached to your system email composer pre-populated with statutory contractor credentials.",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onGenerateAndSendPdf(corporateProfile, recipientName, recipientEmail, recipientDob)
                        showEmailDispatchDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Generate & Email PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailDispatchDialog = false }) { Text("Cancel") }
            }
        )
    }
}
