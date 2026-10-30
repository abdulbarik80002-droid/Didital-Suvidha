package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.Attendance
import com.example.data.model.Worker
import com.example.data.model.formatPaiseToRupees
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    workers: List<Worker>,
    attendanceList: List<Attendance>,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onMarkAttendance: (workerId: Long, status: String, otHours: Double, site: String) -> Unit,
    onMarkAllPresent: () -> Unit
) {
    var selectedSiteFilter by remember { mutableStateOf("All Sites") }
    var showGpsInfoModal by remember { mutableStateOf(false) }

    val sites = listOf("All Sites", "Site A - Metro Heights", "Site B - Highway Corridor", "Central Workshop")

    // Filter workers based on status and site
    val activeWorkers = workers.filter { it.status == "ACTIVE" }
    val filteredWorkers = activeWorkers.filter {
        selectedSiteFilter == "All Sites" || it.siteAssigned == selectedSiteFilter
    }

    // Attendance stats for selected date
    val dateAttendance = attendanceList.filter { it.date == selectedDate }
    val presentCount = dateAttendance.count { it.status == "PRESENT" || it.status == "OVERTIME" }
    val halfDayCount = dateAttendance.count { it.status == "HALF_DAY" }
    val absentCount = dateAttendance.count { it.status == "ABSENT" }
    val totalOtHours = dateAttendance.sumOf { it.overtimeHours }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Real-time GPS Location & Contractor Site Header
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SuvidhaNavyDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGpsInfoModal = true }
                    .testTag("card_gps_attendance_status")
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
                                    .background(SuvidhaGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "GPS Geofencing Active",
                                    tint = SuvidhaGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Real-Time GPS Location Tracking",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF10B981))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text("Geofenced", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Text(
                                    text = "Sharma Construction • Plot 42, Sector 62, Noida",
                                    fontSize = 11.sp,
                                    color = Color(0xFFD4E5FF)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Details",
                            tint = Color(0xFF90CAF9),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Site Coordinates: 28.6280° N, 77.3649° E (Accurate to 3.2m)",
                            fontSize = 10.5.sp,
                            color = Color(0xFF80CBC4),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tap for Map Audit",
                            fontSize = 10.sp,
                            color = SuvidhaGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Date Selector & Roll-Call Summary Bar
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
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
                                text = "Attendance Date",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedDate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                    onDateChange(today)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Today", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onMarkAllPresent,
                                colors = ButtonDefaults.buttonColors(containerColor = SuvidhaGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_mark_all_present")
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("All Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Statistics Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatPill("Present", "$presentCount", SuvidhaGreen, Modifier.weight(1f))
                        StatPill("Half-Day", "$halfDayCount", SuvidhaAmber, Modifier.weight(1f))
                        StatPill("Absent", "$absentCount", Color(0xFFE53935), Modifier.weight(1f))
                        StatPill("OT Hours", "${totalOtHours}h", Color(0xFF7C4DFF), Modifier.weight(1f))
                    }
                }
            }
        }

        // 3. Site Filter Chips
        item {
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
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // 4. Workforce Roll-Call List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Site Crew Roll-Call (${filteredWorkers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GPS Auto-Calculated",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        items(filteredWorkers, key = { it.id }) { worker ->
            val att = dateAttendance.firstOrNull { it.workerId == worker.id }
            val currentStatus = att?.status ?: "PENDING"
            val currentOt = att?.overtimeHours ?: 0.0

            WorkerAttendanceCard(
                worker = worker,
                currentStatus = currentStatus,
                overtimeHours = currentOt,
                gpsDetails = att?.let { "${it.checkInTime} • ${it.gpsAddress}" } ?: "Check-in at Site A (28.6280, 77.3649)",
                onStatusChange = { newStatus, newOt ->
                    onMarkAttendance(worker.id, newStatus, newOt, worker.siteAssigned)
                }
            )
        }
    }

    // Modal: GPS Geofence details
    if (showGpsInfoModal) {
        AlertDialog(
            onDismissRequest = { showGpsInfoModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PinDrop, contentDescription = null, tint = SuvidhaNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GPS Site Geofencing Audit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Automated daily attendance calculation linked directly to real-time GPS location tracking as per Statutory Compliance Blueprint.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Assigned Site: Site A - Metro Heights", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Geofence Radius: 250 meters", fontSize = 11.sp)
                            Text("Center Coordinates: 28.6280° N, 77.3649° E", fontSize = 11.sp)
                            Text("Contractor: Sharma Construction & Infra Pvt Ltd", fontSize = 11.sp)
                            Text("Official Address: Plot 42, Sector 62, Noida NCR", fontSize = 11.sp)
                            Text("Audit Compliance: Tamper-proof GPS timestamping", fontSize = 11.sp, color = SuvidhaGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showGpsInfoModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = color)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun WorkerAttendanceCard(
    worker: Worker,
    currentStatus: String,
    overtimeHours: Double,
    gpsDetails: String,
    onStatusChange: (status: String, otHours: Double) -> Unit
) {
    var showOtDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_row_${worker.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Worker details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SuvidhaNavy.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = worker.name.take(2).uppercase(Locale.ROOT),
                            fontWeight = FontWeight.Bold,
                            color = SuvidhaNavy,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = worker.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${worker.trade} • ${worker.siteAssigned}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = currentStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // GPS location stamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SuvidhaBlue,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = gpsDetails,
                    fontSize = 10.5.sp,
                    color = Color(0xFF334155),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Roll-Call Action Buttons (Present, Half-Day, Overtime, Absent)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Present Button
                AttendanceActionButton(
                    label = "Present",
                    isSelected = currentStatus == "PRESENT",
                    activeColor = SuvidhaGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusChange("PRESENT", 0.0) }
                )

                // Half Day Button
                AttendanceActionButton(
                    label = "Half-Day",
                    isSelected = currentStatus == "HALF_DAY",
                    activeColor = SuvidhaAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusChange("HALF_DAY", 0.0) }
                )

                // Overtime Button
                AttendanceActionButton(
                    label = if (overtimeHours > 0) "OT (${overtimeHours.toInt()}h)" else "Overtime",
                    isSelected = currentStatus == "OVERTIME" || overtimeHours > 0,
                    activeColor = Color(0xFF7C4DFF),
                    modifier = Modifier.weight(1f),
                    onClick = { showOtDialog = true }
                )

                // Absent Button
                AttendanceActionButton(
                    label = "Absent",
                    isSelected = currentStatus == "ABSENT",
                    activeColor = Color(0xFFE53935),
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusChange("ABSENT", 0.0) }
                )
            }
        }
    }

    if (showOtDialog) {
        var selectedHours by remember { mutableStateOf(if (overtimeHours > 0) overtimeHours else 2.0) }

        AlertDialog(
            onDismissRequest = { showOtDialog = false },
            title = {
                Text("Set Overtime for ${worker.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Daily Wage: ${formatPaiseToRupees(worker.dailyWagePaise)} (8hr standard)")
                    Text("Overtime Hours: ${selectedHours} hours", fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF))
                    Slider(
                        value = selectedHours.toFloat(),
                        onValueChange = { selectedHours = Math.round(it * 2) / 2.0 },
                        valueRange = 0.5f..8f,
                        steps = 14
                    )
                    val otRateHourly = (worker.dailyWagePaise / 8.0) * 1.5 // 1.5x overtime statutory rate
                    val estimatedOtPaise = (otRateHourly * selectedHours).toLong()
                    Text(
                        text = "Estimated OT Pay (1.5x rate): ${formatPaiseToRupees(estimatedOtPaise)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStatusChange("OVERTIME", selectedHours)
                        showOtDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text("Apply Overtime")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AttendanceActionButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
