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
import com.example.data.model.CartItem
import com.example.data.model.MarketItem
import com.example.data.model.MarketOrder
import com.example.data.model.WalletAccount
import com.example.data.model.formatPaiseToRupees
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    wallet: WalletAccount?,
    orders: List<MarketOrder>,
    onPurchaseItems: (itemsSummary: String, totalPaise: Long, paymentMode: String, address: String) -> Unit
) {
    val catalog = remember {
        listOf(
            MarketItem("p1", "UltraTech Grade 53 OPC Cement", "Materials", 38000L, "Bag (50kg)", 4.8, 1200, "High early strength hydraulic cement for heavy reinforced concrete.", "Bulk Discount"),
            MarketItem("p2", "Tata Tiscon 550D Rebars 12mm", "Materials", 7200000L, "Tonne (1000kg)", 4.9, 45, "Super ductile thermo-mechanically treated steel bars.", "Verified Partner"),
            MarketItem("p3", "Karam Industrial Safety Helmet (ISI)", "Safety & PPE", 32000L, "Piece", 4.7, 350, "Heavy impact ABS shell with 6-point textile suspension harness.", "ISI Approved"),
            MarketItem("p4", "Liberty Steel-Toe Safety Boots", "Safety & PPE", 125000L, "Pair", 4.6, 210, "Oil, acid, and anti-skid resistant genuine leather site footwear.", "Best Seller"),
            MarketItem("p5", "Bosch GWS Professional Angle Grinder", "Tools & Hardware", 285000L, "Unit", 4.8, 80, "Heavy-duty 750W motor with restart protection and burst-proof guard.", "1-Yr Warranty"),
            MarketItem("p6", "Hilti TE-2 SDS-Plus Rotary Hammer Drill", "Tools & Hardware", 840000L, "Unit", 4.9, 30, "Dual-mode drilling and hammer drilling for concrete and masonry.", "Pro Series"),
            MarketItem("p7", "Dr. Fixit Super Latex Waterproofing", "Materials", 240000L, "Can (20L)", 4.7, 140, "Styrene butadiene copolymer latex for structural repair & waterproofing.", "Top Choice"),
            MarketItem("p8", "Havells Heavy Duty Extension Board 16A", "Site Essentials", 95000L, "Unit", 4.5, 95, "Surge protected 4-socket contractor board with 10m industrial cord.", "Commercial"),
            MarketItem("p9", "Industrial First Aid Kit (BOCW Norms)", "Safety & PPE", 145000L, "Kit Box", 4.9, 110, "Fully stocked emergency trauma response kit compliant with labor laws.", "Statutory"),
            MarketItem("p10", "LED Rechargeable Site Floodlight 100W", "Site Essentials", 220000L, "Unit", 4.6, 60, "IP66 waterproof aluminum housing for night shifts and emergency works.", "IP66 Rated")
        )
    }

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var cart by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showCartSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Catalog, 1 = Orders History

    val categories = listOf("All", "Materials", "Safety & PPE", "Tools & Hardware", "Site Essentials")

    val filteredItems = catalog.filter { item ->
        (selectedCategory == "All" || item.category == selectedCategory) &&
                (item.title.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true))
    }

    val cartTotalPaise = cart.entries.sumOf { (id, qty) ->
        val price = catalog.firstOrNull { it.id == id }?.pricePaise ?: 0L
        price * qty
    }
    val cartCount = cart.values.sum()

    Scaffold(
        floatingActionButton = {
            if (cartCount > 0) {
                ExtendedFloatingActionButton(
                    onClick = { showCartSheet = true },
                    containerColor = SuvidhaGold,
                    contentColor = SuvidhaNavyDark,
                    modifier = Modifier.testTag("fab_open_cart")
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cart ($cartCount) • ${formatPaiseToRupees(cartTotalPaise)}", fontWeight = FontWeight.Bold)
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

            // Navigation Tab Row (Catalog vs Orders)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("E-Commerce Catalog", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Purchases & Invoices (${orders.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search materials, PPE, power tools...") },
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
                        .testTag("input_marketplace_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product items
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val qty = cart[item.id] ?: 0
                        ProductCard(
                            item = item,
                            quantity = qty,
                            onAddToCart = {
                                cart = cart + (item.id to (qty + 1))
                            },
                            onRemoveFromCart = {
                                if (qty > 1) {
                                    cart = cart + (item.id to (qty - 1))
                                } else {
                                    cart = cart - item.id
                                }
                            }
                        )
                    }
                }
            } else {
                // Orders History View
                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Past Purchases", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Orders placed in the marketplace will appear here with tax invoices.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(orders, key = { it.id }) { order ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFE8F5E9))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(order.status, color = SuvidhaGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(text = order.itemsSummary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(text = "Delivering to: ${order.deliveryAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Payment: ${order.paymentMode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = formatPaiseToRupees(order.totalAmountPaise), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = SuvidhaNavy)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Cart and Checkout Bottom Sheet
    if (showCartSheet) {
        var deliveryAddress by remember { mutableStateOf(wallet?.companyAddress ?: "Plot 42, Sector 62, Noida NCR") }
        var paymentMode by remember { mutableStateOf("WALLET") }

        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Site Supplies Order Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextButton(onClick = { cart = emptyMap(); showCartSheet = false }) {
                        Text("Clear Cart", color = Color(0xFFE53935))
                    }
                }

                // Cart item summary
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cart.forEach { (id, qty) ->
                        val item = catalog.firstOrNull { it.id == id } ?: return@forEach
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${formatPaiseToRupees(item.pricePaise)} × $qty ${item.unit}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(formatPaiseToRupees(item.pricePaise * qty), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Site Delivery Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paymentMode == "WALLET",
                        onClick = { paymentMode = "WALLET" },
                        label = { Text("Pay from Capital Wallet") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = paymentMode == "UPI",
                        onClick = { paymentMode = "UPI" },
                        label = { Text("Pay via UPI") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Payable:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(formatPaiseToRupees(cartTotalPaise), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = SuvidhaNavy)
                }

                Button(
                    onClick = {
                        val summary = cart.entries.joinToString(", ") { (id, qty) ->
                            val title = catalog.firstOrNull { it.id == id }?.title ?: id
                            "$title ($qty)"
                        }
                        onPurchaseItems(summary, cartTotalPaise, paymentMode, deliveryAddress)
                        cart = emptyMap()
                        showCartSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_order")
                ) {
                    Text("Confirm Order & Pay", color = SuvidhaNavyDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    item: MarketItem,
    quantity: Int,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (item.badge.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SuvidhaNavy.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(item.badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuvidhaNavy)
                            }
                        }
                    }
                    Text(
                        text = item.description,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
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
                    Text(
                        text = formatPaiseToRupees(item.pricePaise),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = SuvidhaNavy
                    )
                    Text(
                        text = "per ${item.unit} • ★ ${item.rating}",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (quantity == 0) {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add to Cart", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(onClick = onRemoveFromCart, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "$quantity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = onAddToCart, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
