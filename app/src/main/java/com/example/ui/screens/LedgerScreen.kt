package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LedgerTransaction
import com.example.data.model.formatPaiseToRupees
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LedgerScreen(
    transactions: List<LedgerTransaction>,
    onTransactionSelect: (LedgerTransaction) -> Unit,
    onRaiseDispute: (LedgerTransaction) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var detailTxn by remember { mutableStateOf<LedgerTransaction?>(null) }

    val filterOptions = listOf(
        "ALL" to "All Entries",
        "WAGE_PAYOUT" to "Wage Payouts",
        "BANK_TRANSFER" to "Bank IMPS",
        "UPI_TRANSFER" to "UPI Remittance",
        "BILL_PAYMENT" to "Utility Bills",
        "WALLET_TOPUP" to "Capital Top-ups",
        "WORKER_ADVANCE" to "Worker Advances",
        "MARKETPLACE_PURCHASE" to "Marketplace",
        "INTERNATIONAL_TRANSFER" to "International Wire"
    )

    val filteredList = transactions.filter { txn ->
        (selectedFilter == "ALL" || txn.category == selectedFilter) &&
                (txn.title.contains(searchQuery, ignoreCase = true) ||
                        txn.referenceId.contains(searchQuery, ignoreCase = true) ||
                        txn.subtitle.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by reference ID, title or payee...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_ledger_search"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontSize = 11.5.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ledger Statistics Strip
        val totalDebits = transactions.filter { it.type == "DEBIT" }.sumOf { it.amountPaise }
        val totalCredits = transactions.filter { it.type == "CREDIT" }.sumOf { it.amountPaise }

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredList.size} Entries",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "In: +${formatPaiseToRupees(totalCredits)}",
                        color = SuvidhaGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Out: -${formatPaiseToRupees(totalDebits)}",
                        color = SuvidhaAmberDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transactions List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Transactions Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Past payments and ledger records will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredList, key = { it.id }) { txn ->
                    val isCredit = txn.type == "CREDIT"
                    val icon = when (txn.category) {
                        "WALLET_TOPUP" -> Icons.Default.AccountBalanceWallet
                        "WAGE_PAYOUT" -> Icons.Default.Payments
                        "BANK_TRANSFER" -> Icons.Default.AccountBalance
                        "UPI_TRANSFER" -> Icons.Default.Smartphone
                        "BILL_PAYMENT" -> Icons.Default.Bolt
                        "WORKER_ADVANCE" -> Icons.Default.MonetizationOn
                        "MARKETPLACE_PURCHASE" -> Icons.Default.ShoppingCart
                        "INTERNATIONAL_TRANSFER" -> Icons.Default.Public
                        else -> Icons.Default.Receipt
                    }
                    val iconColor = when {
                        isCredit -> SuvidhaGreen
                        txn.category == "INTERNATIONAL_TRANSFER" -> Color(0xFF7C4DFF)
                        txn.category == "MARKETPLACE_PURCHASE" -> SuvidhaNavy
                        else -> SuvidhaBlue
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                detailTxn = txn
                                onTransactionSelect(txn)
                            }
                            .testTag("txn_row_${txn.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(iconColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = txn.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = txn.subtitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(txn.timestamp)),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    if (txn.disputeStatus != "NONE") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFFFFF3E0))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("Disputed", fontSize = 8.5.sp, color = SuvidhaAmberDark, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = (if (isCredit) "+" else "-") + formatPaiseToRupees(txn.amountPaise),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = if (isCredit) SuvidhaGreen else SuvidhaAmberDark
                                )
                                Text(
                                    text = "Bal: ${formatPaiseToRupees(txn.balanceAfterPaise)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Detailed Transaction Audit & Dispute Trigger
    if (detailTxn != null) {
        val txn = detailTxn!!
        val isCredit = txn.type == "CREDIT"

        AlertDialog(
            onDismissRequest = { detailTxn = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isCredit) SuvidhaGreen else SuvidhaAmberDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ledger Audit Checkback", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = txn.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = txn.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Amount:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = (if (isCredit) "+" else "-") + formatPaiseToRupees(txn.amountPaise),
                            fontWeight = FontWeight.Bold,
                            color = if (isCredit) SuvidhaGreen else SuvidhaAmberDark
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Reference ID:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(txn.referenceId, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Category:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(txn.category, fontSize = 11.5.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Timestamp:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", Locale.ENGLISH).format(Date(txn.timestamp)), fontSize = 11.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Balance After:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(formatPaiseToRupees(txn.balanceAfterPaise), fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        Text("Audit Trail: ${txn.details}", fontSize = 10.5.sp)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Digital Suvidha Transaction Receipt - ${txn.referenceId}")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "DIGITAL SUVIDHA TRANSACTION RECEIPT\n" +
                                            "Ref ID: ${txn.referenceId}\n" +
                                            "Title: ${txn.title}\n" +
                                            "Amount: ${formatPaiseToRupees(txn.amountPaise)}\n" +
                                            "Details: ${txn.details}\n" +
                                            "Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date(txn.timestamp))}\n" +
                                            "Compliance: RBI Digital Payment Directions Standard"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receipt", fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = {
                            onRaiseDispute(txn)
                            detailTxn = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuvidhaAmberDark)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Raise Dispute", fontSize = 11.5.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { detailTxn = null }) {
                    Text("Close")
                }
            }
        )
    }
}
