package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalletAccount
import com.example.data.model.formatPaiseToRupees
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankingTerminalScreen(
    wallet: WalletAccount?,
    onBankTransfer: (account: String, ifsc: String, name: String, amount: Double, purpose: String) -> Unit,
    onUpiTransfer: (upiId: String, name: String, amount: Double, note: String) -> Unit,
    onTopUp: (amount: Double, source: String) -> Unit,
    onInternationalTransfer: (currency: String, foreignAmt: Double, inrAmt: Double, swift: String, iban: String, name: String, bank: String, purpose: String) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Domestic IMPS, 1 = UPI Pay & QR, 2 = International FX
    var showTopUpDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Digital Suvidha Corporate Capital Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_banking_wallet_terminal"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SuvidhaNavyDark, SuvidhaNavy, SuvidhaBlue)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Corporate Banking Terminal",
                                    color = Color(0xFFD4E5FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = wallet?.companyName ?: "Sharma Construction & Infra Pvt Ltd",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuvidhaGold.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Multi-Factor Auth (MFA)", color = SuvidhaGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Available Working Capital Pool",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.5.sp
                        )
                        Text(
                            text = formatPaiseToRupees(wallet?.balancePaise ?: 0L),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showTopUpDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SuvidhaGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_topup_capital")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = SuvidhaNavyDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Top-up Working Capital", color = SuvidhaNavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // 2. Transfer Mode Tabs (Domestic, UPI, International)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Domestic (IMPS)", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("UPI Remittance", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("International Wire", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp) }
                )
            }
        }

        // 3. Tab Contents
        item {
            when (selectedTab) {
                0 -> DomesticTransferSection(onBankTransfer = onBankTransfer)
                1 -> UpiTransferSection(wallet = wallet, onUpiTransfer = onUpiTransfer)
                2 -> InternationalTransferSection(onInternationalTransfer = onInternationalTransfer)
            }
        }
    }

    // Top-up Dialog
    if (showTopUpDialog) {
        var topUpAmountStr by remember { mutableStateOf("50000") }
        var topUpSource by remember { mutableStateOf("HDFC Bank NetBanking") }

        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = { Text("Top-up Working Capital Pool", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter amount to add to contractor operational wallet:")
                    OutlinedTextField(
                        value = topUpAmountStr,
                        onValueChange = { topUpAmountStr = it },
                        label = { Text("Amount (INR)") },
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Select Funding Channel:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    listOf("HDFC Bank NetBanking (Corporate)", "ICICI Bank Corporate Pool", "UPI AutoPay Pool", "SBI Corporate Gateway").forEach { src ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { topUpSource = src }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = topUpSource == src, onClick = { topUpSource = src })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(src, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = topUpAmountStr.toDoubleOrNull() ?: 50000.0
                        onTopUp(amt, topUpSource)
                        showTopUpDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Authorize Credit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DomesticTransferSection(
    onBankTransfer: (account: String, ifsc: String, name: String, amount: Double, purpose: String) -> Unit
) {
    var beneficiaryName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("Subcontractor Site Material Payment") }
    var transferType by remember { mutableStateOf("IMPS") } // IMPS, NEFT, RTGS

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Instant Domestic Bank Transfer",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Transfer to any Indian scheduled commercial bank via IMPS, NEFT, or RTGS with instant double-entry ledger entry.",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Transfer mode selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("IMPS", "NEFT", "RTGS").forEach { mode ->
                    FilterChip(
                        selected = transferType == mode,
                        onClick = { transferType = mode },
                        label = { Text(mode, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = beneficiaryName,
                onValueChange = { beneficiaryName = it },
                label = { Text("Beneficiary Name *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_bank_beneficiary_name")
            )

            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Bank Account Number *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_bank_account_number")
            )

            OutlinedTextField(
                value = ifscCode,
                onValueChange = { ifscCode = it.uppercase() },
                label = { Text("IFSC Code (11 digits, e.g. HDFC0000128) *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_bank_ifsc")
            )

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Transfer Amount (INR) *") },
                leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_bank_amount")
            )

            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Payment Purpose / Site Reference") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (beneficiaryName.isNotBlank() && accountNumber.isNotBlank() && ifscCode.isNotBlank() && amt > 0) {
                        onBankTransfer(accountNumber, ifscCode, beneficiaryName, amt, purpose)
                        beneficiaryName = ""
                        accountNumber = ""
                        ifscCode = ""
                        amountStr = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_execute_bank_transfer")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Execute $transferType Transfer", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }
    }
}

@Composable
fun UpiTransferSection(
    wallet: WalletAccount?,
    onUpiTransfer: (upiId: String, name: String, amount: Double, note: String) -> Unit
) {
    var upiId by remember { mutableStateOf("") }
    var payeeName by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("Site Payment") }
    var showQrModal by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Unified Payments Interface (UPI)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Real-time P2P and Merchant settlement",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = { showQrModal = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_show_upi_qr")
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("My QR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it.lowercase().trim() },
                label = { Text("Recipient UPI ID / VPA (e.g. sharma@okaxis) *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_upi_id")
            )

            OutlinedTextField(
                value = payeeName,
                onValueChange = { payeeName = it },
                label = { Text("Recipient / Merchant Name *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_upi_payee_name")
            )

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Amount (INR) *") },
                leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_upi_amount")
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Transaction Remark / Voucher ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (upiId.isNotBlank() && payeeName.isNotBlank() && amt > 0) {
                        onUpiTransfer(upiId, payeeName, amt, note)
                        upiId = ""
                        payeeName = ""
                        amountStr = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_execute_upi_pay")
            ) {
                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send via UPI Instant Remittance", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showQrModal) {
        val contractorUpi = "digitalsuvidha.${wallet?.gstin?.take(10) ?: "sharma"}@bank"
        AlertDialog(
            onDismissRequest = { showQrModal = false },
            title = {
                Text("Contractor UPI Collection QR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "UPI QR Code",
                                tint = SuvidhaNavyDark,
                                modifier = Modifier.size(120.dp)
                            )
                            Text("UPI SCAN & PAY", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = SuvidhaNavyDark)
                        }
                    }

                    Text(
                        text = wallet?.companyName ?: "Sharma Construction & Infra Pvt Ltd",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "VPA: $contractorUpi",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Scan from Google Pay, PhonePe, Paytm, or BHIM to accept payments directly into contractor capital wallet.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQrModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Close QR")
                }
            }
        )
    }
}

@Composable
fun InternationalTransferSection(
    onInternationalTransfer: (currency: String, foreignAmt: Double, inrAmt: Double, swift: String, iban: String, name: String, bank: String, purpose: String) -> Unit
) {
    val currencies = listOf(
        "USD" to 84.50,
        "AED" to 23.00,
        "EUR" to 91.20,
        "GBP" to 109.80,
        "SGD" to 63.80
    )

    var selectedCurrency by remember { mutableStateOf(currencies[0]) }
    var foreignAmountStr by remember { mutableStateOf("1000") }
    var beneficiaryName by remember { mutableStateOf("") }
    var swiftBic by remember { mutableStateOf("") }
    var ibanOrAccount by remember { mutableStateOf("") }
    var recipientBank by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("Construction Machinery Import / Engineering Consultant Fee") }

    var showMfaModal by remember { mutableStateOf(false) }
    var mfaOtp by remember { mutableStateOf("") }

    val foreignAmt = foreignAmountStr.toDoubleOrNull() ?: 0.0
    val calculatedInr = foreignAmt * selectedCurrency.second

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "International Wire & FX Transfer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEDE7F6))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("SWIFT / BIC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E35B1))
                }
            }

            Text(
                text = "Statutory outward cross-border money transfer compliant with FEMA regulations, SWIFT gpi tracking, and mandatory MFA validation.",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Currency row
            Text("Select Destination Currency & FX Rate:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currencies.forEach { (curr, rate) ->
                    FilterChip(
                        selected = selectedCurrency.first == curr,
                        onClick = { selectedCurrency = curr to rate },
                        label = { Text("$curr (₹$rate)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = foreignAmountStr,
                    onValueChange = { foreignAmountStr = it },
                    label = { Text("Amount (${selectedCurrency.first}) *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formatPaiseToRupees((calculatedInr * 100).toLong()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("INR Equivalent") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = beneficiaryName,
                onValueChange = { beneficiaryName = it },
                label = { Text("Overseas Beneficiary / Company Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = swiftBic,
                    onValueChange = { swiftBic = it.uppercase() },
                    label = { Text("SWIFT / BIC Code *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ibanOrAccount,
                    onValueChange = { ibanOrAccount = it.uppercase() },
                    label = { Text("IBAN / Account Number *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = recipientBank,
                onValueChange = { recipientBank = it },
                label = { Text("Recipient Bank & Country *") },
                placeholder = { Text("e.g. Emirates NBD, Dubai, UAE") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("FEMA Remittance Purpose Code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (beneficiaryName.isNotBlank() && swiftBic.isNotBlank() && ibanOrAccount.isNotBlank() && foreignAmt > 0) {
                        showMfaModal = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_initiate_intl_wire")
            ) {
                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Proceed to MFA Authorization", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // MFA Verification Modal Dialog
    if (showMfaModal) {
        AlertDialog(
            onDismissRequest = { showMfaModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SuvidhaAmberDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Multi-Factor Authentication (MFA)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "International remittances require high-security step-up 2FA. Enter the 6-digit corporate verification OTP sent to +91 98765 43210 (Demo OTP: 482910):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = mfaOtp,
                        onValueChange = { if (it.length <= 6) mfaOtp = it },
                        label = { Text("Enter 6-Digit MFA Token") },
                        placeholder = { Text("482910") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Remittance: ${selectedCurrency.first} $foreignAmountStr ≈ ${formatPaiseToRupees((calculatedInr * 100).toLong())}\nBeneficiary: $beneficiaryName (SWIFT: $swiftBic)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onInternationalTransfer(
                            selectedCurrency.first,
                            foreignAmt,
                            calculatedInr,
                            swiftBic,
                            ibanOrAccount,
                            beneficiaryName,
                            recipientBank,
                            purpose
                        )
                        showMfaModal = false
                        beneficiaryName = ""
                        swiftBic = ""
                        ibanOrAccount = ""
                        mfaOtp = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Authorize Remittance")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMfaModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
