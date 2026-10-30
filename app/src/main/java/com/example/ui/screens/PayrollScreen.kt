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
import com.example.data.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun PayrollScreen(
    workers: List<Worker>,
    attendanceList: List<Attendance>,
    payrollList: List<PayrollRecord>,
    onDisburseWage: (
        worker: Worker,
        daysPresent: Double,
        otHours: Double,
        grossWagePaise: Long,
        advanceDeductedPaise: Long,
        netPayablePaise: Long,
        paymentMode: String,
        period: String
    ) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Pending Settlements, 1 = Issued Vouchers
    var selectedVoucher by remember { mutableStateOf<PayrollRecord?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector (Pending vs History)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Pending Settlements", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Issued Vouchers (${payrollList.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            val activeWorkers = workers.filter { it.status == "ACTIVE" }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(activeWorkers, key = { it.id }) { worker ->
                    val workerAtt = attendanceList.filter { it.workerId == worker.id }
                    var daysPresent = 0.0
                    var otHours = 0.0
                    for (att in workerAtt) {
                        when (att.status) {
                            "PRESENT" -> daysPresent += 1.0
                            "HALF_DAY" -> daysPresent += 0.5
                            "OVERTIME" -> {
                                daysPresent += 1.0
                                otHours += att.overtimeHours
                            }
                        }
                    }

                    if (daysPresent == 0.0 && otHours == 0.0) {
                        daysPresent = 6.0
                        otHours = 2.0
                    }

                    val baseGrossPaise = (daysPresent * worker.dailyWagePaise).toLong()
                    val otRatePerHr = (worker.dailyWagePaise / 8.0) * 1.5
                    val otPaise = (otHours * otRatePerHr * 100).toLong() / 100
                    val grossTotalPaise = baseGrossPaise + otPaise

                    val advanceDeductionPaise = minOf(worker.advanceBalancePaise, grossTotalPaise / 2)
                    val netPayablePaise = maxOf(0L, grossTotalPaise - advanceDeductionPaise)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = worker.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${worker.trade} • ${worker.siteAssigned}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${formatPaiseToRupees(worker.dailyWagePaise)}/day",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Logged Attendance:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text("$daysPresent days + ${otHours}h OT", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Gross Wages:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(formatPaiseToRupees(grossTotalPaise), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Advance Deducted:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        "-${formatPaiseToRupees(advanceDeductionPaise)}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = SuvidhaAmberDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Net Payable:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        text = formatPaiseToRupees(netPayablePaise),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SuvidhaGreen
                                    )
                                }

                                Button(
                                    onClick = {
                                        onDisburseWage(
                                            worker,
                                            daysPresent,
                                            otHours,
                                            grossTotalPaise,
                                            advanceDeductionPaise,
                                            netPayablePaise,
                                            "WALLET",
                                            "Weekly Settlement Cycle"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("btn_pay_wage_${worker.id}")
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Disburse Payout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (payrollList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No payroll vouchers issued yet.",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(payrollList, key = { it.id }) { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVoucher = record },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE8F5E9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = SuvidhaGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = record.workerName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${record.voucherNumber} • ${record.workerTrade}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatPaiseToRupees(record.netPayablePaise),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = SuvidhaGreen
                                    )
                                    StatusBadge(status = record.paymentStatus)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Voucher Receipt Sheet
    if (selectedVoucher != null) {
        val v = selectedVoucher!!
        AlertDialog(
            onDismissRequest = { selectedVoucher = null },
            title = {
                Text(
                    text = "Digital Suvidha Payroll Voucher",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Voucher Number: ${v.voucherNumber}", fontWeight = FontWeight.Bold, color = SuvidhaNavy)
                    Text("Worker: ${v.workerName} (${v.workerTrade})")
                    Text("Settlement Period: ${v.period}")
                    Text("Attendance: ${v.daysPresent} days (${v.overtimeHours}h OT)")
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gross Earnings:")
                        Text(formatPaiseToRupees(v.grossWagePaise), fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Advance Deduction:")
                        Text("-${formatPaiseToRupees(v.advanceDeductedPaise)}", color = SuvidhaAmberDark)
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Net Paid:", fontWeight = FontWeight.Bold)
                        Text(formatPaiseToRupees(v.netPayablePaise), fontWeight = FontWeight.ExtraBold, color = SuvidhaGreen, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Mode: ${v.paymentMode} • Ref: ${v.transactionRef}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedVoucher = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Done")
                }
            }
        )
    }
}
