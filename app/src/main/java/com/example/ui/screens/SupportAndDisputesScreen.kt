package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.SupportTicket
import com.example.data.model.TicketMessage
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportAndDisputesScreen(
    tickets: List<SupportTicket>,
    onRaiseTicket: (category: String, title: String, description: String, refId: String, priority: String) -> Unit,
    onResolveTicket: (ticketId: Long, refId: String, notes: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Tickets & Disputes, 1 = Live Help Desk, 2 = Legal & Compliance
    var showNewTicketDialog by remember { mutableStateOf(false) }
    var selectedTicketForDetails by remember { mutableStateOf<SupportTicket?>(null) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showNewTicketDialog = true },
                    containerColor = SuvidhaGold,
                    contentColor = SuvidhaNavyDark,
                    modifier = Modifier.testTag("fab_raise_ticket")
                ) {
                    Icon(Icons.Default.AddComment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Raise Dispute / Ticket", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Tickets & Disputes (${tickets.size})", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Live Help Desk", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Legal & Compliance", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> TicketsListTab(
                    tickets = tickets,
                    onSelectTicket = { selectedTicketForDetails = it }
                )
                1 -> LiveHelpDeskTab()
                2 -> LegalComplianceTab()
            }
        }
    }

    // Modal: Raise New Support Ticket / Dispute
    if (showNewTicketDialog) {
        val categories = listOf(
            "FAILED_TRANSACTION" to "Failed Financial Transaction",
            "ATTENDANCE_DISCREPANCY" to "Attendance & GPS Geofence Dispute",
            "WAGE_SETTLEMENT" to "Payroll / Wage Calculation Discrepancy",
            "BILL_PAYMENT" to "Utility BBPS Bill Issue",
            "KYC_ISSUE" to "Identity Verification Failure"
        )
        var selectedCat by remember { mutableStateOf(categories[0].first) }
        var ticketTitle by remember { mutableStateOf("") }
        var ticketDescription by remember { mutableStateOf("") }
        var referenceId by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("HIGH") }

        AlertDialog(
            onDismissRequest = { showNewTicketDialog = false },
            title = { Text("Raise Support Ticket / Dispute", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Issue Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    categories.forEach { (catKey, catLabel) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCat = catKey }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(selected = selectedCat == catKey, onClick = { selectedCat = catKey })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(catLabel, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = ticketTitle,
                        onValueChange = { ticketTitle = it },
                        label = { Text("Summary / Subject *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = referenceId,
                        onValueChange = { referenceId = it },
                        label = { Text("Related Reference ID (e.g. TXN-101 / Worker ID)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ticketDescription,
                        onValueChange = { ticketDescription = it },
                        label = { Text("Detailed Explanation of Discrepancy *") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ticketTitle.isNotBlank() && ticketDescription.isNotBlank()) {
                            onRaiseTicket(selectedCat, ticketTitle, ticketDescription, referenceId, priority)
                            showNewTicketDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Submit Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTicketDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Ticket Details & Dispute Resolution
    if (selectedTicketForDetails != null) {
        val t = selectedTicketForDetails!!
        var resolutionInput by remember { mutableStateOf(t.resolutionNotes) }

        AlertDialog(
            onDismissRequest = { selectedTicketForDetails = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ticket #${t.ticketNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = t.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Category: ${t.category}  |  Priority: ${t.priority}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (t.referenceId.isNotEmpty()) {
                        Text(text = "Reference ID: ${t.referenceId}", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(text = t.description, fontSize = 12.sp)

                    HorizontalDivider()

                    Text("Structured Dispute Resolution:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = resolutionInput,
                        onValueChange = { resolutionInput = it },
                        label = { Text("Resolution Findings & Audit Settlement Notes") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                if (t.status != "RESOLVED") {
                    Button(
                        onClick = {
                            val notes = if (resolutionInput.isNotBlank()) resolutionInput else "Resolved in accordance with statutory audit trail."
                            onResolveTicket(t.id, t.referenceId, notes)
                            selectedTicketForDetails = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuvidhaGreen)
                    ) {
                        Text("Mark Resolved")
                    }
                } else {
                    Button(onClick = { selectedTicketForDetails = null }, colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)) {
                        Text("Close")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTicketForDetails = null }) { Text("Dismiss") }
            }
        )
    }
}

@Composable
fun TicketsListTab(
    tickets: List<SupportTicket>,
    onSelectTicket: (SupportTicket) -> Unit
) {
    if (tickets.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("No Active Disputes or Tickets", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("All attendance roll-calls and transactions are verified clean.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(tickets, key = { it.id }) { ticket ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTicket(ticket) }
                        .testTag("ticket_item_${ticket.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Ticket #${ticket.ticketNumber}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            val isResolved = ticket.status == "RESOLVED"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isResolved) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = ticket.status,
                                    color = if (isResolved) SuvidhaGreen else SuvidhaAmberDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(text = ticket.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(text = ticket.description, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

                        if (ticket.resolutionNotes.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F8E9))
                                    .padding(8.dp)
                            ) {
                                Text(text = "Resolution: ${ticket.resolutionNotes}", fontSize = 11.sp, color = Color(0xFF33691E))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveHelpDeskTab() {
    var messageInput by remember { mutableStateOf("") }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                TicketMessage("1", "SUPPORT_AGENT", "Welcome to Digital Suvidha 24/7 Corporate Support. How can we assist with your workforce, payroll, or banking transactions today?"),
                TicketMessage("2", "SYSTEM", "Standard SLA: 48-Hour resolution guaranteed for financial chargebacks under RBI Master Directions.")
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(chatMessages, key = { it.id }) { msg ->
                val isUser = msg.sender == "USER"
                val isSystem = msg.sender == "SYSTEM"

                if (isSystem) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = msg.message,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isUser) SuvidhaNavy else MaterialTheme.colorScheme.surface)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg.message,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Chat Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                placeholder = { Text("Ask support assistant...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            IconButton(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        val userText = messageInput
                        val newMsg = TicketMessage(System.currentTimeMillis().toString(), "USER", userText)
                        val botReply = TicketMessage(
                            (System.currentTimeMillis() + 1).toString(),
                            "SUPPORT_AGENT",
                            "Your query regarding \"$userText\" has been logged with Priority 1. Our nodal dispute officer has verified the corresponding audit log entry."
                        )
                        chatMessages = chatMessages + newMsg + botReply
                        messageInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SuvidhaNavy)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun LegalComplianceTab() {
    var selectedDoc by remember { mutableStateOf(0) } // 0 = TOS, 1 = Privacy Policy, 2 = Refund Policy

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedDoc == 0,
                onClick = { selectedDoc = 0 },
                label = { Text("Terms of Service", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedDoc == 1,
                onClick = { selectedDoc = 1 },
                label = { Text("Privacy Policy (DPDP)", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedDoc == 2,
                onClick = { selectedDoc = 2 },
                label = { Text("Refund & Disputes", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (selectedDoc) {
                    0 -> {
                        item {
                            Text("Terms of Service & Contractor Agreement", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SuvidhaNavy)
                            Text("Effective: Current Operational Term • Version 2.4", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("1. Scope of Platform: Digital Suvidha provides workforce registration, GPS attendance auditing, automated payroll voucher issuance, and enterprise financial transactions exclusively for authorized construction contractors and commercial enterprises.", fontSize = 12.sp)
                            Text("2. Contractor Obligations: The principal contractor agrees to maintain complete, accurate, and up-to-date worker master records, including statutory wage rates, advance balances, and site assignments.", fontSize = 12.sp)
                            Text("3. GPS Attendance Tracking: Roll-calls recorded via real-time GPS coordinates are deemed legally binding operational logs. Any manual overrides require documented justification.", fontSize = 12.sp)
                            Text("4. Financial Services: All domestic IMPS, UPI, and international remittances processed through the Digital Suvidha wallet are subject to Multi-Factor Authentication and double-entry ledger auditing.", fontSize = 12.sp)
                        }
                    }
                    1 -> {
                        item {
                            Text("Privacy Policy & DPDP Act 2023 Compliance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SuvidhaNavy)
                            Text("Data Protection Framework • 100% Retention Standard", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("1. Personally Identifiable Information (PII): All worker and contractor PII, including Aadhaar UIDs, PAN cards, driving licenses, and contact credentials, are encrypted end-to-end using AES-256 GCM symmetric cryptography.", fontSize = 12.sp)
                            Text("2. 100% Data Retention Policy: Historical workforce attendance sheets, payroll vouchers, and financial transaction ledger records are maintained in permanent, tamper-evident local database storage.", fontSize = 12.sp)
                            Text("3. DigiLocker & MeriPehchaan Integration: Identity verification tokens obtained through government identity portals are utilized solely for statutory compliance auditing.", fontSize = 12.sp)
                            Text("4. Confidential PDF Encryption: All generated payroll reports dispatched via electronic mail are password-protected with the recipient's Date of Birth (DDMMYYYY).", fontSize = 12.sp)
                        }
                    }
                    2 -> {
                        item {
                            Text("Refund, Chargeback & Dispute Policy", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SuvidhaNavy)
                            Text("RBI Digital Payment Guidelines • Structured Resolution", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("1. Failed Financial Transactions: In the event of a debited transaction where settlement fails at the beneficiary bank, the system automatically reverses the amount back into the contractor's working capital pool within T+1 working days.", fontSize = 12.sp)
                            Text("2. In-App Dispute Mechanism: Users may trigger immediate dispute resolution directly from the transaction ledger or Support tab.", fontSize = 12.sp)
                            Text("3. BBPS Utility Bill Reversals: Biller acknowledgment failures are auto-flagged and investigated by our automated reconciliation engine.", fontSize = 12.sp)
                            Text("4. Wage Discrepancies: Any attendance or wage disbursement discrepancy raised by workers or supervisors triggers a formal review workflow with site GPS audit proof.", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
