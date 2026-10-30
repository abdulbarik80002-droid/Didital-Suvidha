package com.example.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalletAccount
import com.example.ui.theme.*

data class BillCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val billers: List<String>,
    val consumerLabel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillPaymentScreen(
    wallet: WalletAccount?,
    onPayBill: (serviceName: String, billerName: String, consumerNo: String, amount: Double) -> Unit
) {
    val categories = listOf(
        BillCategory(
            id = "elec",
            name = "Electricity",
            icon = Icons.Default.Bolt,
            billers = listOf("Tata Power DDL", "Adani Electricity Mumbai", "BESCOM Karnataka", "Mahavitaran (MSEDCL)", "UPPCL Urban"),
            consumerLabel = "Consumer Account Number"
        ),
        BillCategory(
            id = "mobile",
            name = "Mobile",
            icon = Icons.Default.Smartphone,
            billers = listOf("Jio Prepaid / Postpaid", "Airtel Telecommunications", "Vi (Vodafone Idea)", "BSNL Mobile"),
            consumerLabel = "10-Digit Mobile Number"
        ),
        BillCategory(
            id = "water",
            name = "Water",
            icon = Icons.Default.WaterDrop,
            billers = listOf("Delhi Jal Board (DJB)", "Bangalore Water Board (BWSSB)", "MCGM Mumbai Water", "Hyderabad Water Board (HMWSSB)"),
            consumerLabel = "K Number / RR Number"
        ),
        BillCategory(
            id = "gas",
            name = "Gas Cylinder",
            icon = Icons.Default.LocalFireDepartment,
            billers = listOf("Indane Gas (Indian Oil)", "Bharat Gas (BPCL)", "HP Gas (HPCL)", "Indraprastha Gas (IGL)"),
            consumerLabel = "LPG Consumer ID / Registered Mobile"
        ),
        BillCategory(
            id = "fastag",
            name = "FASTag",
            icon = Icons.Default.DirectionsCar,
            billers = listOf("NHAI FASTag", "ICICI Bank FASTag", "Paytm Payments Bank FASTag", "HDFC Bank FASTag"),
            consumerLabel = "Vehicle VRN Number"
        ),
        BillCategory(
            id = "dth",
            name = "DTH TV",
            icon = Icons.Default.Tv,
            billers = listOf("Tata Play (Tata Sky)", "Airtel Digital TV", "Dish TV India", "Sun Direct"),
            consumerLabel = "Subscriber ID / Smart Card No"
        )
    )

    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedBiller by remember { mutableStateOf(categories[0].billers[0]) }
    var consumerNumber by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("1500") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // BBPS Header Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SuvidhaNavy)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bharat BillPay (BBPS) Terminal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Instant utility remittances with official transaction receipts",
                            color = Color(0xFFD4E5FF),
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SuvidhaGold)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("BBPS Verified", color = SuvidhaNavyDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Category Picker (2x3 Grid)
        item {
            Text(
                text = "Select Utility Service:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { cat ->
                            val isSelected = selectedCategory.id == cat.id
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedCategory = cat
                                        selectedBiller = cat.billers[0]
                                    }
                                    .testTag("bill_cat_${cat.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null,
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.name,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else SuvidhaNavy,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bill Payment Form
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${selectedCategory.name} Payment Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SuvidhaNavy
                    )

                    var expandedBiller by remember { mutableStateOf(false) }
                    Text("Select Biller Provider:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    ExposedDropdownMenuBox(
                        expanded = expandedBiller,
                        onExpandedChange = { expandedBiller = !expandedBiller }
                    ) {
                        OutlinedTextField(
                            value = selectedBiller,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBiller) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedBiller,
                            onDismissRequest = { expandedBiller = false }
                        ) {
                            selectedCategory.billers.forEach { biller ->
                                DropdownMenuItem(
                                    text = { Text(biller) },
                                    onClick = {
                                        selectedBiller = biller
                                        expandedBiller = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = consumerNumber,
                        onValueChange = { consumerNumber = it },
                        label = { Text("${selectedCategory.consumerLabel} *") },
                        modifier = Modifier.fillMaxWidth().testTag("input_bill_consumer_no"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Bill Amount (₹) *") },
                        modifier = Modifier.fillMaxWidth().testTag("input_bill_amount"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (consumerNumber.isNotBlank() && amt > 0) {
                                onPayBill(selectedCategory.name, selectedBiller, consumerNumber, amt)
                                consumerNumber = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_pay_bill_submit")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm & Pay Utility Bill", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
