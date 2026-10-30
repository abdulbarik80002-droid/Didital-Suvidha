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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ads.CompanyBannerAd
import com.example.data.model.*
import com.example.ui.components.CompanyBannerAdCard
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavTab

@Composable
fun DashboardScreen(
    wallet: WalletAccount?,
    workers: List<Worker>,
    attendanceList: List<Attendance>,
    transactions: List<LedgerTransaction>,
    currentAd: CompanyBannerAd,
    onRotateAd: () -> Unit,
    onNavigate: (NavTab) -> Unit,
    onQuickAddMoney: () -> Unit,
    onTransactionClick: (LedgerTransaction) -> Unit
) {
    val totalWorkers = workers.size
    val todayAttendance = attendanceList.filter { it.date == attendanceList.firstOrNull()?.date }
    val presentCount = todayAttendance.count { it.status == "PRESENT" || it.status == "OVERTIME" }
    val halfDayCount = todayAttendance.count { it.status == "HALF_DAY" }
    val totalOtHours = todayAttendance.sumOf { it.overtimeHours }
    val totalAdvances = workers.sumOf { it.advanceBalancePaise }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // 1. Digital Suvidha Corporate Wallet Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_wallet_balance"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SuvidhaNavyDark, SuvidhaBlue, SuvidhaTeal)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = wallet?.companyName ?: "Sharma Construction & Infra Pvt Ltd",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "GSTIN: ${wallet?.gstin ?: "07AAACS1234F1Z9"}",
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (wallet?.isAadhaarVerified == true) Color(0xFF10B981).copy(alpha = 0.25f)
                                        else Color(0xFFF57F17).copy(alpha = 0.25f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (wallet?.isAadhaarVerified == true) "Aadhaar Verified" else "Aadhaar Pending",
                                    color = if (wallet?.isAadhaarVerified == true) Color(0xFF80CBC4) else Color(0xFFFFD54F),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Available Working Capital Balance",
                            color = Color(0xFFD4E5FF),
                            fontSize = 12.sp
                        )

                        Text(
                            text = formatPaiseToRupees(wallet?.balancePaise ?: 0L),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Actions on Wallet Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onQuickAddMoney,
                                colors = ButtonDefaults.buttonColors(containerColor = SuvidhaGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_topup_wallet")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = SuvidhaNavyDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Top-up Funds",
                                    color = SuvidhaNavyDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalButton(
                                onClick = { onNavigate(NavTab.PAYROLL) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_pay_wage_quick")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Disburse Wage",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. DYNAMIC BANNER AD NETWORK INTEGRATION (Requirement 3: 500+ Companies with Official Logo Display)
        item {
            CompanyBannerAdCard(
                currentAd = currentAd,
                onRotateNext = onRotateAd
            )
        }

        // 3. Workforce Metrics Summary
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Construction Workforce Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Crew",
                        value = "$totalWorkers",
                        icon = Icons.Default.People,
                        iconTint = SuvidhaBlue,
                        modifier = Modifier.weight(1f),
                        subtitle = "Active Workers"
                    )
                    MetricCard(
                        title = "Present Today",
                        value = "$presentCount",
                        icon = Icons.Default.CheckCircle,
                        iconTint = SuvidhaGreen,
                        modifier = Modifier.weight(1f),
                        subtitle = "+$halfDayCount Half-Day"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Overtime Hours",
                        value = "${totalOtHours}h",
                        icon = Icons.Default.AccessTime,
                        iconTint = Color(0xFF7C4DFF),
                        modifier = Modifier.weight(1f),
                        subtitle = "Site OT Today"
                    )
                    MetricCard(
                        title = "Worker Advances",
                        value = formatPaiseToRupees(totalAdvances),
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = SuvidhaAmber,
                        modifier = Modifier.weight(1f),
                        subtitle = "Settlement Dues"
                    )
                }
            }
        }

        // 4. Quick Service Navigation Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Operational Services",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val services = listOf(
                    Triple(NavTab.WORKERS, "Workforce\nProfiles", Icons.Default.Engineering),
                    Triple(NavTab.ATTENDANCE, "Daily Site\nAttendance", Icons.Default.FactCheck),
                    Triple(NavTab.PAYROLL, "Payroll &\nSlips", Icons.Default.ReceiptLong),
                    Triple(NavTab.BANKING, "Bank IMPS\nTransfers", Icons.Default.AccountBalance),
                    Triple(NavTab.BILL_PAY, "BBPS Utility\nPayments", Icons.Default.Bolt),
                    Triple(NavTab.LEDGER, "Financial\nLedger", Icons.Default.MenuBook)
                )

                for (chunk in services.chunked(3)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (item in chunk) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(item.first) }
                                    .testTag("nav_service_${item.first.name.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.third,
                                            contentDescription = item.second,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.second,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { onNavigate(NavTab.LEDGER) },
                    modifier = Modifier.testTag("btn_view_all_ledger")
                ) {
                    Text("View Ledger")
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent transactions found.",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(transactions.take(5)) { txn ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTransactionClick(txn) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (txn.type == "CREDIT") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (txn.type == "CREDIT") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (txn.type == "CREDIT") SuvidhaGreen else SuvidhaRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = txn.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = txn.subtitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = (if (txn.type == "CREDIT") "+" else "-") + formatPaiseToRupees(txn.amountPaise),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (txn.type == "CREDIT") SuvidhaGreen else MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(status = txn.status)
                        }
                    }
                }
            }
        }
    }
}
