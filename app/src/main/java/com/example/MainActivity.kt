package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DigitalSuvidhaHeader
import com.example.ui.screens.*
import com.example.ui.theme.DigitalSuvidhaTheme
import com.example.ui.theme.SuvidhaGold
import com.example.ui.theme.SuvidhaNavyDark
import com.example.ui.viewmodel.DigitalSuvidhaViewModel
import com.example.ui.viewmodel.NavTab
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigitalSuvidhaTheme {
                DigitalSuvidhaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalSuvidhaApp(
    viewModel: DigitalSuvidhaViewModel = viewModel()
) {
    val context = LocalContext.current
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val pinBuffer by viewModel.pinBuffer.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val attendanceList by viewModel.attendanceList.collectAsStateWithLifecycle()
    val payrollList by viewModel.payrollList.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val tickets by viewModel.tickets.collectAsStateWithLifecycle()
    val marketOrders by viewModel.marketOrders.collectAsStateWithLifecycle()
    val currentAd by viewModel.currentAd.collectAsStateWithLifecycle()

    val autoLockEnabled by viewModel.autoLockEnabled.collectAsStateWithLifecycle()
    val remainingLockoutSeconds by viewModel.remainingLockoutSeconds.collectAsStateWithLifecycle()
    val isScrambledKeypad by viewModel.isScrambledKeypad.collectAsStateWithLifecycle()
    val keypadDigits by viewModel.keypadDigits.collectAsStateWithLifecycle()
    val shakeTrigger by viewModel.shakeTrigger.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showServicesSheet by remember { mutableStateOf(false) }

    // Auto-dismiss user messages
    LaunchedEffect(userMessage) {
        if (userMessage != null) {
            delay(3500)
            viewModel.clearUserMessage()
        }
    }

    // Step 1: Secure PIN Lock Gate
    if (!isUnlocked) {
        PinLockScreen(
            pinBuffer = pinBuffer,
            errorMessage = pinError,
            remainingLockoutSeconds = remainingLockoutSeconds,
            isScrambledKeypad = isScrambledKeypad,
            keypadDigits = keypadDigits,
            shakeTrigger = shakeTrigger,
            onDigitClick = { viewModel.onPinDigit(it) },
            onBackspaceClick = { viewModel.onPinBackspace() },
            onBiometricClick = { viewModel.unlockWithBiometrics() },
            onQuickUnlock = { viewModel.quickDemoUnlock() },
            onSubmitPin = { viewModel.submitCurrentPin() },
            onToggleScramble = { viewModel.toggleScrambleKeypad() }
        )
    }
    // Step 2: Mandatory Aadhaar Verification Gate (if unverified)
    else if (wallet != null && !wallet!!.isAadhaarVerified) {
        AadhaarVerificationScreen(
            onVerificationSuccess = { refId ->
                viewModel.confirmAadhaarVerification(refId)
            },
            onSignOut = { viewModel.lockApp() }
        )
    }
    // Step 3: Full Application Access
    else {
        if (activeTab != NavTab.DASHBOARD) {
            BackHandler {
                viewModel.navigateTo(NavTab.DASHBOARD)
            }
        }

        val corporateProfile = viewModel.getCorporateProfile()

        Scaffold(
            topBar = {
                val title = when (activeTab) {
                    NavTab.DASHBOARD -> "Digital Suvidha"
                    NavTab.WORKERS -> "Workforce Directory"
                    NavTab.ATTENDANCE -> "GPS Site Attendance"
                    NavTab.PAYROLL -> "Payroll & Vouchers"
                    NavTab.BANKING -> "Banking & Transfers"
                    NavTab.BILL_PAY -> "Bharat BillPay (BBPS)"
                    NavTab.MARKETPLACE -> "In-App Marketplace"
                    NavTab.LEDGER -> "Enterprise Ledger"
                    NavTab.KYC -> "KYC Verification Hub"
                    NavTab.REPORTS -> "Reports & PDF Dispatch"
                    NavTab.SUPPORT -> "Support & Disputes"
                    NavTab.SETTINGS -> "Settings & Audit"
                }

                val subtitle = when (activeTab) {
                    NavTab.DASHBOARD -> "Workforce Management & Enterprise Banking"
                    NavTab.WORKERS -> "Worker master profiles & contractor logs"
                    NavTab.ATTENDANCE -> "Real-time GPS tracking & roll-call"
                    NavTab.PAYROLL -> "Wage calculation & settlement"
                    NavTab.BANKING -> "IMPS, UPI & International remittances"
                    NavTab.BILL_PAY -> "Electricity, gas, water & telecom utilities"
                    NavTab.MARKETPLACE -> "Construction materials, PPE & tools"
                    NavTab.LEDGER -> "Double-entry financial records & audit checkback"
                    NavTab.KYC -> "Aadhaar, PAN, DL & Mobile 2FA compliance"
                    NavTab.REPORTS -> "Encrypted monthly PDF dispatch (DOB password)"
                    NavTab.SUPPORT -> "Customer helpdesk, dispute resolution & legal policies"
                    NavTab.SETTINGS -> "Local DB security & tamper-evident audit"
                }

                DigitalSuvidhaHeader(
                    title = title,
                    subtitle = subtitle,
                    isAadhaarVerified = wallet?.isAadhaarVerified ?: false,
                    onLockClick = { viewModel.lockApp() }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == NavTab.DASHBOARD,
                        onClick = { viewModel.navigateTo(NavTab.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = activeTab == NavTab.WORKERS,
                        onClick = { viewModel.navigateTo(NavTab.WORKERS) },
                        icon = { Icon(Icons.Default.Engineering, contentDescription = "Workers") },
                        label = { Text("Workers", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_workers")
                    )

                    NavigationBarItem(
                        selected = activeTab == NavTab.ATTENDANCE,
                        onClick = { viewModel.navigateTo(NavTab.ATTENDANCE) },
                        icon = { Icon(Icons.Default.FactCheck, contentDescription = "Attendance") },
                        label = { Text("Attendance", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_attendance")
                    )

                    NavigationBarItem(
                        selected = activeTab == NavTab.PAYROLL,
                        onClick = { viewModel.navigateTo(NavTab.PAYROLL) },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Payroll") },
                        label = { Text("Payroll", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_payroll")
                    )

                    NavigationBarItem(
                        selected = activeTab !in listOf(NavTab.DASHBOARD, NavTab.WORKERS, NavTab.ATTENDANCE, NavTab.PAYROLL),
                        onClick = { showServicesSheet = true },
                        icon = { Icon(Icons.Default.Widgets, contentDescription = "Services") },
                        label = { Text("Services", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_services")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (activeTab) {
                    NavTab.DASHBOARD -> DashboardScreen(
                        wallet = wallet,
                        workers = workers,
                        attendanceList = attendanceList,
                        transactions = transactions,
                        currentAd = currentAd,
                        onRotateAd = { viewModel.rotateNextAd() },
                        onNavigate = { viewModel.navigateTo(it) },
                        onQuickAddMoney = { viewModel.navigateTo(NavTab.BANKING) },
                        onTransactionClick = { viewModel.navigateTo(NavTab.LEDGER) }
                    )

                    NavTab.WORKERS -> WorkerManagementScreen(
                        workers = workers,
                        onAddWorker = { name, phone, trade, wage, adv, bank, ifsc, upi, site ->
                            viewModel.addWorker(name, phone, trade, wage, adv, bank, ifsc, upi, site)
                        },
                        onGiveAdvance = { id, amt, note ->
                            viewModel.giveWorkerAdvance(id, amt, note)
                        },
                        onDeleteWorker = { viewModel.deleteWorker(it) },
                        onUpdateWorker = { viewModel.updateWorker(it) }
                    )

                    NavTab.ATTENDANCE -> AttendanceScreen(
                        workers = workers,
                        attendanceList = attendanceList,
                        selectedDate = selectedDate,
                        onDateChange = { viewModel.setSelectedDate(it) },
                        onMarkAttendance = { workerId, status, ot, site ->
                            viewModel.markAttendance(workerId, status, ot, site)
                        },
                        onMarkAllPresent = { viewModel.markAllPresent() }
                    )

                    NavTab.PAYROLL -> PayrollScreen(
                        workers = workers,
                        attendanceList = attendanceList,
                        payrollList = payrollList,
                        onDisburseWage = { worker, days, ot, gross, adv, net, mode, period ->
                            viewModel.disburseWage(worker, days, ot, gross, adv, net, mode, period)
                        }
                    )

                    NavTab.BANKING -> BankingTerminalScreen(
                        wallet = wallet,
                        onBankTransfer = { account, ifsc, name, amt, purpose ->
                            viewModel.sendBankTransfer(account, ifsc, name, amt, purpose)
                        },
                        onUpiTransfer = { upiId, name, amt, note ->
                            viewModel.sendUpiTransfer(upiId, name, amt, note)
                        },
                        onTopUp = { amt, src ->
                            viewModel.addMoneyToWallet(amt, src)
                        },
                        onInternationalTransfer = { curr, foreignAmt, inrAmt, swift, iban, name, bank, purpose ->
                            viewModel.sendInternationalTransfer(curr, foreignAmt, inrAmt, swift, iban, name, bank, purpose)
                        }
                    )

                    NavTab.BILL_PAY -> BillPaymentScreen(
                        wallet = wallet,
                        onPayBill = { service, biller, consumer, amt ->
                            viewModel.payBill(service, biller, consumer, amt)
                        }
                    )

                    NavTab.MARKETPLACE -> MarketplaceScreen(
                        wallet = wallet,
                        orders = marketOrders,
                        onPurchaseItems = { summary, totalPaise, mode, address ->
                            viewModel.purchaseMarketplaceItems(summary, totalPaise, mode, address)
                        }
                    )

                    NavTab.LEDGER -> LedgerScreen(
                        transactions = transactions,
                        onTransactionSelect = {},
                        onRaiseDispute = { txn ->
                            viewModel.raiseSupportTicket(
                                category = "FAILED_TRANSACTION",
                                title = "Dispute for ${txn.title}",
                                description = "Transaction audit checkback initiated for ${txn.referenceId} (${txn.details}).",
                                refId = txn.referenceId,
                                priority = "HIGH"
                            )
                        }
                    )

                    NavTab.KYC -> KycVerificationScreen(
                        wallet = wallet,
                        onVerifyAadhaar = { ref -> viewModel.confirmAadhaarVerification(ref) },
                        onVerifyPan = { ref, masked -> viewModel.verifyPan(ref, masked) },
                        onVerifyDl = { ref, masked -> viewModel.verifyDl(ref, masked) },
                        onVerifyPhone = { phone -> viewModel.verifyPhone(phone) }
                    )

                    NavTab.REPORTS -> ReportsScreen(
                        corporateProfile = corporateProfile,
                        workers = workers,
                        attendanceList = attendanceList,
                        payrollList = payrollList,
                        onUpdateCorporateProfile = { viewModel.updateCorporateProfile(it) },
                        onGenerateAndSendPdf = { profile, recName, recEmail, recDob ->
                            viewModel.generateAndSharePdfReport(context, profile, recName, recEmail, recDob)
                        },
                        onExportCsv = { viewModel.exportReportCsv() }
                    )

                    NavTab.SUPPORT -> SupportAndDisputesScreen(
                        tickets = tickets,
                        onRaiseTicket = { cat, title, desc, refId, pri ->
                            viewModel.raiseSupportTicket(cat, title, desc, refId, pri)
                        },
                        onResolveTicket = { id, refId, notes ->
                            viewModel.resolveSupportTicket(id, refId, notes)
                        }
                    )

                    NavTab.SETTINGS -> SettingsScreen(
                        wallet = wallet,
                        auditLogs = auditLogs,
                        autoLockEnabled = autoLockEnabled,
                        onToggleAutoLock = { viewModel.setAutoLockEnabled(it) },
                        onChangePin = { viewModel.updatePin(it) },
                        onLockNow = { viewModel.lockApp() },
                        onResetData = { viewModel.resetData() }
                    )
                }

                // Global Notification Banner
                AnimatedVisibility(
                    visible = userMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SuvidhaNavyDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuvidhaGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = userMessage ?: "",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // More Services Navigation Bottom Sheet
    if (showServicesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showServicesSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Operational Services & Enterprise Modules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val services = listOf(
                    Triple(NavTab.BANKING, "Banking Terminal (IMPS, UPI & SWIFT Wire)", Icons.Default.AccountBalance),
                    Triple(NavTab.BILL_PAY, "Bharat BillPay (BBPS Utilities)", Icons.Default.Bolt),
                    Triple(NavTab.MARKETPLACE, "In-App Shopping & Site Supplies", Icons.Default.ShoppingCart),
                    Triple(NavTab.LEDGER, "Financial Activity Ledger & Audit", Icons.Default.MenuBook),
                    Triple(NavTab.KYC, "KYC & Identity Verification Hub", Icons.Default.VerifiedUser),
                    Triple(NavTab.REPORTS, "Reports & Encrypted PDF Dispatch (DOB)", Icons.Default.PictureAsPdf),
                    Triple(NavTab.SUPPORT, "Customer Help, Disputes & Legal Compliance", Icons.Default.SupportAgent),
                    Triple(NavTab.SETTINGS, "Aadhaar Compliance & Security Audit", Icons.Default.Security)
                )

                services.forEach { (tab, label, icon) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.navigateTo(tab)
                                showServicesSheet = false
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeTab == tab) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = label,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}
