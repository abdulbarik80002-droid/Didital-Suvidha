package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Worker
import com.example.data.model.formatPaiseToRupees
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerManagementScreen(
    workers: List<Worker>,
    onAddWorker: (name: String, phone: String, trade: String, dailyWage: Double, advance: Double, bank: String, ifsc: String, upi: String, site: String) -> Unit,
    onGiveAdvance: (workerId: Long, advanceRupees: Double, note: String) -> Unit,
    onDeleteWorker: (Worker) -> Unit,
    onUpdateWorker: (Worker) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSiteFilter by remember { mutableStateOf("All Sites") }
    var showAddDialog by remember { mutableStateOf(false) }
    var workerForAdvance by remember { mutableStateOf<Worker?>(null) }
    var workerForEdit by remember { mutableStateOf<Worker?>(null) }

    val sites = listOf("All Sites", "Site A - Metro Heights", "Site B - Highway Corridor", "Central Workshop")

    val filteredWorkers = workers.filter { worker ->
        (selectedSiteFilter == "All Sites" || worker.siteAssigned == selectedSiteFilter) &&
                (worker.name.contains(searchQuery, ignoreCase = true) ||
                        worker.trade.contains(searchQuery, ignoreCase = true) ||
                        worker.phone.contains(searchQuery))
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SuvidhaGold,
                contentColor = SuvidhaNavyDark,
                modifier = Modifier.testTag("fab_add_worker")
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Worker", fontWeight = FontWeight.Bold)
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
            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by worker name, trade or phone...") },
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
                    .testTag("input_search_workers"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Site Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sites) { site ->
                    FilterChip(
                        selected = selectedSiteFilter == site,
                        onClick = { selectedSiteFilter = site },
                        label = { Text(site, fontSize = 12.sp) },
                        leadingIcon = if (selectedSiteFilter == site) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registered Workforce (${filteredWorkers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Contractor Assigned",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredWorkers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Workers Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add new worker profiles to begin tracking site attendance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredWorkers, key = { it.id }) { worker ->
                        WorkerCard(
                            worker = worker,
                            onGiveAdvance = { workerForAdvance = worker },
                            onEdit = { workerForEdit = worker },
                            onDelete = { onDeleteWorker(worker) }
                        )
                    }
                }
            }
        }
    }

    // Add Worker Dialog
    if (showAddDialog) {
        AddWorkerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, trade, wage, adv, bank, ifsc, upi, site ->
                onAddWorker(name, phone, trade, wage, adv, bank, ifsc, upi, site)
                showAddDialog = false
            }
        )
    }

    // Edit Worker Dialog
    if (workerForEdit != null) {
        EditWorkerDialog(
            worker = workerForEdit!!,
            onDismiss = { workerForEdit = null },
            onConfirm = { updatedWorker ->
                onUpdateWorker(updatedWorker)
                workerForEdit = null
            }
        )
    }

    // Give Advance Dialog
    if (workerForAdvance != null) {
        GiveAdvanceDialog(
            worker = workerForAdvance!!,
            onDismiss = { workerForAdvance = null },
            onConfirm = { amount, note ->
                onGiveAdvance(workerForAdvance!!.id, amount, note)
                workerForAdvance = null
            }
        )
    }
}

@Composable
fun WorkerCard(
    worker: Worker,
    onGiveAdvance: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SuvidhaNavy.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = worker.name.take(1),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuvidhaNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = worker.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = worker.phone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(status = worker.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contractor & Official Address banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Column {
                    Text(
                        text = "Contractor: ${worker.contractorName}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = SuvidhaNavy
                    )
                    Text(
                        text = "Address: ${worker.contractorAddress}",
                        fontSize = 10.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(worker.trade, fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(worker.siteAssigned, fontSize = 11.sp) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Base Wage:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${formatPaiseToRupees(worker.dailyWagePaise)} / day",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuvidhaBlue
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Advance Due:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = formatPaiseToRupees(worker.advanceBalancePaise),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (worker.advanceBalancePaise > 0) SuvidhaAmberDark else SuvidhaGreen
                    )
                }
            }

            if (worker.upiId.isNotEmpty() || worker.bankAccountNo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (worker.upiId.isNotEmpty()) "UPI: ${worker.upiId}" else "Bank A/c: ${worker.bankAccountNo} (${worker.ifscCode})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 11.5.sp)
                }

                OutlinedButton(
                    onClick = onGiveAdvance,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SuvidhaAmberDark),
                    modifier = Modifier.testTag("btn_give_advance_${worker.id}")
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Give Advance", fontSize = 11.5.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, trade: String, dailyWage: Double, advance: Double, bank: String, ifsc: String, upi: String, site: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var trade by remember { mutableStateOf("Mason") }
    var dailyWageStr by remember { mutableStateOf("700") }
    var initialAdvanceStr by remember { mutableStateOf("0") }
    var bankAccount by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("Site A - Metro Heights") }

    val trades = listOf("Mason", "Helper", "Carpenter", "Electrician", "Plumber", "Bar Bender", "Painter", "Welder", "Foreman")
    val sites = listOf("Site A - Metro Heights", "Site B - Highway Corridor", "Central Workshop")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add New Worker Profile", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Worker Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_worker_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Phone Number *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_worker_phone")
                    )
                }

                item {
                    Text("Trade / Skill Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(trades) { t ->
                            FilterChip(
                                selected = trade == t,
                                onClick = { trade = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = dailyWageStr,
                        onValueChange = { dailyWageStr = it },
                        label = { Text("Daily Wage Rate (INR) *") },
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_worker_wage")
                    )
                }

                item {
                    OutlinedTextField(
                        value = initialAdvanceStr,
                        onValueChange = { initialAdvanceStr = it },
                        label = { Text("Initial Advance Given (INR)") },
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Assigned Construction Site:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(sites) { s ->
                            FilterChip(
                                selected = site == s,
                                onClick = { site = s },
                                label = { Text(s, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("Worker UPI ID (for direct wage settlement)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = bankAccount,
                        onValueChange = { bankAccount = it },
                        label = { Text("Bank Account Number (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = ifsc,
                        onValueChange = { ifsc = it.uppercase() },
                        label = { Text("Bank IFSC Code (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wage = dailyWageStr.toDoubleOrNull() ?: 700.0
                    val adv = initialAdvanceStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(name, phone, trade, wage, adv, bankAccount, ifsc, upiId, site)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy),
                modifier = Modifier.testTag("btn_save_worker")
            ) {
                Text("Save Worker")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditWorkerDialog(
    worker: Worker,
    onDismiss: () -> Unit,
    onConfirm: (Worker) -> Unit
) {
    var name by remember { mutableStateOf(worker.name) }
    var phone by remember { mutableStateOf(worker.phone) }
    var trade by remember { mutableStateOf(worker.trade) }
    var wageStr by remember { mutableStateOf((worker.dailyWagePaise / 100.0).toString()) }
    var site by remember { mutableStateOf(worker.siteAssigned) }
    var contractorName by remember { mutableStateOf(worker.contractorName) }
    var contractorAddress by remember { mutableStateOf(worker.contractorAddress) }
    var upiId by remember { mutableStateOf(worker.upiId) }
    var dob by remember { mutableStateOf(worker.dob) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Worker Profile & Contractor", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number *") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = trade, onValueChange = { trade = it }, label = { Text("Trade / Role *") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = wageStr, onValueChange = { wageStr = it }, label = { Text("Daily Wage (INR)") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = site, onValueChange = { site = it }, label = { Text("Assigned Site") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = contractorName, onValueChange = { contractorName = it }, label = { Text("Contractor Name") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = contractorAddress, onValueChange = { contractorAddress = it }, label = { Text("Contractor Address") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("UPI ID") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("Date of Birth (DDMMYYYY)") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wage = (wageStr.toDoubleOrNull() ?: (worker.dailyWagePaise / 100.0)) * 100
                    onConfirm(
                        worker.copy(
                            name = name,
                            phone = phone,
                            trade = trade,
                            dailyWagePaise = wage.toLong(),
                            siteAssigned = site,
                            contractorName = contractorName,
                            contractorAddress = contractorAddress,
                            upiId = upiId,
                            dob = dob
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun GiveAdvanceDialog(
    worker: Worker,
    onDismiss: () -> Unit,
    onConfirm: (amountRupees: Double, note: String) -> Unit
) {
    var advanceStr by remember { mutableStateOf("1000") }
    var note by remember { mutableStateOf("Medical / Emergency Advance") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Issue Worker Advance")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Worker: ${worker.name} (${worker.trade})",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Current Advance Due: ${formatPaiseToRupees(worker.advanceBalancePaise)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = advanceStr,
                    onValueChange = { advanceStr = it },
                    label = { Text("Advance Amount (INR) *") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_advance_amount")
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Voucher Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = advanceStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuvidhaAmberDark),
                modifier = Modifier.testTag("btn_confirm_advance")
            ) {
                Text("Disburse Advance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
