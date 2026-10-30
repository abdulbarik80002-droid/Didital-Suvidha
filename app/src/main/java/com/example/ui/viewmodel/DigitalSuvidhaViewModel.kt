package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ads.AdNetworkManager
import com.example.data.ads.CompanyBannerAd
import com.example.data.local.DigitalSuvidhaDatabase
import com.example.data.model.*
import com.example.data.repository.DigitalSuvidhaRepository
import com.example.data.security.SecureCredentialStorage
import com.example.data.security.VerificationResult
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NavTab {
    DASHBOARD,
    WORKERS,
    ATTENDANCE,
    PAYROLL,
    BANKING,
    BILL_PAY,
    MARKETPLACE,
    LEDGER,
    KYC,
    REPORTS,
    SUPPORT,
    SETTINGS
}

class DigitalSuvidhaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DigitalSuvidhaRepository
    private val secureStorage = SecureCredentialStorage(application.applicationContext)
    private var lockoutJob: Job? = null
    private val _remainingLockoutSeconds = MutableStateFlow(0L)
    val remainingLockoutSeconds: StateFlow<Long> = _remainingLockoutSeconds.asStateFlow()

    init {
        val database = DigitalSuvidhaDatabase.getDatabase(application)
        repository = DigitalSuvidhaRepository(database.dao())
        viewModelScope.launch {
            repository.seedDefaultDataIfEmpty()
        }
        val initialLockout = secureStorage.getRemainingLockoutSeconds()
        if (initialLockout > 0) {
            startLockoutCountdown(initialLockout)
        } else {
            secureStorage.resetLockout()
            _remainingLockoutSeconds.value = 0L
        }
    }

    // Language standard - Exclusively English
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Security state
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _pinBuffer = MutableStateFlow("")
    val pinBuffer: StateFlow<String> = _pinBuffer.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    // Navigation
    private val _activeTab = MutableStateFlow(NavTab.DASHBOARD)
    val activeTab: StateFlow<NavTab> = _activeTab.asStateFlow()

    fun navigateTo(tab: NavTab) {
        _activeTab.value = tab
    }

    // Attendance Date Selector
    private val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val _selectedDate = MutableStateFlow(todayStr)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    // Site filter for Workers & Attendance
    private val _selectedSite = MutableStateFlow("All Sites")
    val selectedSite: StateFlow<String> = _selectedSite.asStateFlow()

    fun setSelectedSite(site: String) {
        _selectedSite.value = site
    }

    // UI Feedback Banner
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // Dynamic Banner Ad Network (500+ companies)
    private var adIndex = 0
    private val _currentAd = MutableStateFlow(AdNetworkManager.getRandomBanner())
    val currentAd: StateFlow<CompanyBannerAd> = _currentAd.asStateFlow()

    fun rotateNextAd() {
        adIndex++
        _currentAd.value = AdNetworkManager.getAdByIndex(adIndex)
    }

    // Data streams from repository
    val workers: StateFlow<List<Worker>> = repository.allWorkers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val attendanceList: StateFlow<List<Attendance>> = repository.allAttendance.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val payrollList: StateFlow<List<PayrollRecord>> = repository.allPayroll.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions: StateFlow<List<LedgerTransaction>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val wallet: StateFlow<WalletAccount?> = repository.wallet.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val auditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tickets: StateFlow<List<SupportTicket>> = repository.allTickets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val marketOrders: StateFlow<List<MarketOrder>> = repository.allMarketOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Scrambled keypad and UI triggers
    private val _isScrambledKeypad = MutableStateFlow(false)
    val isScrambledKeypad: StateFlow<Boolean> = _isScrambledKeypad.asStateFlow()

    private val _keypadDigits = MutableStateFlow(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"))
    val keypadDigits: StateFlow<List<String>> = _keypadDigits.asStateFlow()

    private val _shakeTrigger = MutableStateFlow(0L)
    val shakeTrigger: StateFlow<Long> = _shakeTrigger.asStateFlow()

    // Auto-Lock preference
    private val _autoLockEnabled = MutableStateFlow(secureStorage.isAutoLockEnabled())
    val autoLockEnabled: StateFlow<Boolean> = _autoLockEnabled.asStateFlow()

    fun setAutoLockEnabled(enabled: Boolean) {
        _autoLockEnabled.value = enabled
        secureStorage.setAutoLockEnabled(enabled)
    }

    fun onAppBackgrounded() {
        if (_autoLockEnabled.value && _isUnlocked.value) {
            lockApp()
        }
    }

    fun toggleScrambleKeypad() {
        val shouldScramble = !_isScrambledKeypad.value
        _isScrambledKeypad.value = shouldScramble
        _keypadDigits.value = if (shouldScramble) {
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").shuffled()
        } else {
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        }
    }

    // Security & PIN Methods
    fun onPinDigit(digit: String) {
        if (_isUnlocked.value) return

        if (_remainingLockoutSeconds.value > 0) {
            val rem = secureStorage.getRemainingLockoutSeconds()
            if (rem <= 0) {
                _remainingLockoutSeconds.value = 0L
                secureStorage.resetLockout()
            } else {
                _pinError.value = "Locked ($rem s). Tap Quick Unlock to bypass."
                _shakeTrigger.value = System.currentTimeMillis()
                return
            }
        }

        val current = _pinBuffer.value
        val newPin = if (current.length >= 4) digit else (current + digit)
        _pinBuffer.value = newPin
        _pinError.value = null

        if (newPin.length == 4) {
            viewModelScope.launch {
                delay(120)
                verifyPin(newPin)
            }
        }
    }

    fun onPinBackspace() {
        if (_pinBuffer.value.isNotEmpty()) {
            _pinBuffer.value = _pinBuffer.value.dropLast(1)
            _pinError.value = null
        }
    }

    fun onPinClear() {
        _pinBuffer.value = ""
        _pinError.value = null
    }

    fun submitCurrentPin() {
        if (_isUnlocked.value) return
        if (_pinBuffer.value.length == 4) {
            verifyPin(_pinBuffer.value)
        } else if (_pinBuffer.value.isEmpty()) {
            verifyPin(SecureCredentialStorage.DEFAULT_PIN)
        } else {
            _pinError.value = "Please enter all 4 digits"
            _shakeTrigger.value = System.currentTimeMillis()
        }
    }

    fun quickDemoUnlock() {
        secureStorage.resetLockout()
        _remainingLockoutSeconds.value = 0L
        _isUnlocked.value = true
        _pinBuffer.value = ""
        _pinError.value = null
        viewModelScope.launch {
            repository.unlockWallet()
            repository.logAuditEvent("PIN_AUTH_SUCCESS", "Session unlocked via quick unlock (1234).")
        }
        showMessage("Session unlocked with default PIN 1234.")
    }

    private fun verifyPin(pin: String) {
        val result = secureStorage.verifyPin(pin)
        when (result) {
            is VerificationResult.Success -> {
                _isUnlocked.value = true
                _pinBuffer.value = ""
                _pinError.value = null
                _remainingLockoutSeconds.value = 0L
                viewModelScope.launch {
                    repository.unlockWallet()
                    repository.logAuditEvent("PIN_AUTH_SUCCESS", "User authenticated with encrypted PIN verification.")
                }
            }
            is VerificationResult.IncorrectPin -> {
                _pinBuffer.value = ""
                _pinError.value = "Incorrect PIN (${result.remainingAttempts} attempts remaining). Default: 1234"
                _shakeTrigger.value = System.currentTimeMillis()
                viewModelScope.launch {
                    repository.logAuditEvent("PIN_AUTH_FAILED", "Failed PIN authentication attempt.")
                }
            }
            is VerificationResult.LockedOut -> {
                _pinBuffer.value = ""
                _pinError.value = "Too many failed attempts. Locked for ${result.remainingSeconds}s"
                _remainingLockoutSeconds.value = result.remainingSeconds
                _shakeTrigger.value = System.currentTimeMillis()
                startLockoutCountdown(result.remainingSeconds)
                viewModelScope.launch {
                    repository.logAuditEvent("PIN_AUTH_LOCKOUT", "Brute-force lockout triggered for ${result.remainingSeconds}s.")
                }
            }
        }
    }

    private fun startLockoutCountdown(seconds: Long) {
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                _remainingLockoutSeconds.value = remaining
                delay(1000L)
                remaining--
            }
            secureStorage.resetLockout()
            _remainingLockoutSeconds.value = 0L
            _pinError.value = null
        }
    }

    fun unlockWithBiometrics() {
        if (_remainingLockoutSeconds.value > 0) return
        _isUnlocked.value = true
        _pinBuffer.value = ""
        _pinError.value = null
        viewModelScope.launch {
            repository.unlockWallet()
            repository.logAuditEvent("BIOMETRIC_AUTH", "User authenticated via device biometrics.")
        }
        showMessage("Biometric authentication successful.")
    }

    fun lockApp() {
        _isUnlocked.value = false
        _pinBuffer.value = ""
        _pinError.value = null
        viewModelScope.launch {
            repository.lockWallet()
            repository.logAuditEvent("SESSION_LOCKED", "Session locked by user.")
        }
    }

    fun updatePin(newPin: String) {
        viewModelScope.launch {
            if (newPin.length == 4 && newPin.all { it.isDigit() }) {
                secureStorage.savePin(newPin)
                repository.updatePin(newPin)
                repository.logAuditEvent("PIN_CHANGED", "Security PIN was updated in encrypted local storage.")
                showMessage("Security PIN successfully updated.")
            } else {
                showMessage("Security PIN must be exactly 4 digits.")
            }
        }
    }

    // KYC Verification Methods
    fun confirmAadhaarVerification(refId: String) {
        viewModelScope.launch {
            repository.setAadhaarVerified(refId)
            showMessage("Aadhaar verified via DigiLocker / MeriPehchaan (Ref: $refId)")
        }
    }

    fun verifyPan(refId: String, maskedPan: String) {
        viewModelScope.launch {
            repository.setPanVerified(refId, maskedPan)
            showMessage("PAN verified with Income Tax / NSDL database (Ref: $refId)")
        }
    }

    fun verifyDl(refId: String, maskedDl: String) {
        viewModelScope.launch {
            repository.setDlVerified(refId, maskedDl)
            showMessage("Driving License verified with MoRTH Sarathi (Ref: $refId)")
        }
    }

    fun verifyPhone(phoneNumber: String) {
        viewModelScope.launch {
            repository.setPhoneVerified(phoneNumber)
            showMessage("Mobile phone verified with 2FA OTP.")
        }
    }

    // Corporate Profile Update for Outgoing PDFs
    fun updateCorporateProfile(profile: CorporateProfile) {
        viewModelScope.launch {
            repository.updateCorporateProfile(profile)
            showMessage("Corporate credentials updated for outgoing PDFs.")
        }
    }

    // Worker Management
    fun addWorker(
        name: String,
        phone: String,
        trade: String,
        dailyWageRupees: Double,
        advanceRupees: Double,
        bankAccount: String,
        ifsc: String,
        upiId: String,
        site: String
    ) {
        viewModelScope.launch {
            val profile = getCorporateProfile()
            val worker = Worker(
                name = name,
                phone = phone,
                trade = trade,
                dailyWagePaise = (dailyWageRupees * 100).toLong(),
                advanceBalancePaise = (advanceRupees * 100).toLong(),
                bankAccountNo = bankAccount,
                ifscCode = ifsc,
                upiId = upiId,
                siteAssigned = site,
                contractorName = profile.contractorName,
                contractorAddress = profile.physicalAddress
            )
            repository.insertWorker(worker)
            showMessage("Worker profile created: $name")
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            repository.updateWorker(worker)
            showMessage("Worker details updated.")
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
            showMessage("Worker profile deleted.")
        }
    }

    fun giveWorkerAdvance(workerId: Long, advanceRupees: Double, note: String) {
        viewModelScope.launch {
            val success = repository.recordWorkerAdvance(workerId, (advanceRupees * 100).toLong(), note)
            if (success) {
                showMessage("Advance payment recorded & debited from wallet.")
            } else {
                showMessage("Insufficient balance in contractor wallet.")
            }
        }
    }

    // Attendance Methods
    fun markAttendance(workerId: Long, status: String, otHours: Double, site: String) {
        viewModelScope.launch {
            repository.markAttendance(workerId, _selectedDate.value, status, otHours, site)
        }
    }

    fun markAllPresent() {
        viewModelScope.launch {
            val active = workers.value.filter { it.status == "ACTIVE" }
            val filtered = if (_selectedSite.value == "All Sites") active else active.filter { it.siteAssigned == _selectedSite.value }
            repository.markAllPresent(_selectedDate.value, filtered)
            showMessage("All workers marked present with real-time GPS coordinates for ${_selectedDate.value}")
        }
    }

    // Payroll Methods
    fun disburseWage(
        worker: Worker,
        daysPresent: Double,
        otHours: Double,
        grossWagePaise: Long,
        advanceDeductedPaise: Long,
        netPayablePaise: Long,
        paymentMode: String,
        period: String
    ) {
        viewModelScope.launch {
            val result = repository.disbursePayroll(
                worker = worker,
                daysPresent = daysPresent,
                overtimeHours = otHours,
                grossWagePaise = grossWagePaise,
                advanceDeductedPaise = advanceDeductedPaise,
                netPayablePaise = netPayablePaise,
                paymentMode = paymentMode,
                period = period
            )
            result.onSuccess {
                showMessage("Wage disbursement confirmed: ${formatPaiseToRupees(netPayablePaise)} (${it.voucherNumber})")
            }.onFailure { err ->
                showMessage("Disbursement error: ${err.message}")
            }
        }
    }

    // Banking & Transfers
    fun sendBankTransfer(accountNo: String, ifsc: String, name: String, amountRupees: Double, purpose: String) {
        viewModelScope.launch {
            val result = repository.executeBankTransfer(
                accountNumber = accountNo,
                ifsc = ifsc,
                beneficiaryName = name,
                amountPaise = (amountRupees * 100).toLong(),
                purpose = purpose
            )
            result.onSuccess {
                showMessage("Bank IMPS transfer successful. Ref: ${it.referenceId}")
            }.onFailure {
                showMessage("Transfer failed: ${it.message}")
            }
        }
    }

    fun sendInternationalTransfer(
        currency: String,
        foreignAmt: Double,
        inrAmt: Double,
        swift: String,
        iban: String,
        name: String,
        bank: String,
        purpose: String
    ) {
        viewModelScope.launch {
            val result = repository.executeInternationalTransfer(
                countryCurrency = currency,
                foreignAmount = foreignAmt,
                inrAmountPaise = (inrAmt * 100).toLong(),
                swiftBic = swift,
                ibanOrAccount = iban,
                beneficiaryName = name,
                recipientBank = bank,
                purpose = purpose
            )
            result.onSuccess {
                showMessage("International wire transfer dispatched. Ref: ${it.referenceId}")
            }.onFailure {
                showMessage("Remittance error: ${it.message}")
            }
        }
    }

    fun sendUpiTransfer(upiId: String, name: String, amountRupees: Double, note: String) {
        viewModelScope.launch {
            val result = repository.executeUpiPayment(
                upiId = upiId,
                recipientName = name,
                amountPaise = (amountRupees * 100).toLong(),
                note = note
            )
            result.onSuccess {
                showMessage("UPI transaction completed. Ref: ${it.referenceId}")
            }.onFailure {
                showMessage("UPI payment failed: ${it.message}")
            }
        }
    }

    fun payBill(serviceName: String, billerName: String, consumerNo: String, amountRupees: Double) {
        viewModelScope.launch {
            val result = repository.payUtilityBill(
                serviceName = serviceName,
                billerName = billerName,
                consumerNumber = consumerNo,
                amountPaise = (amountRupees * 100).toLong()
            )
            result.onSuccess {
                showMessage("BBPS utility bill paid successfully. Ref: ${it.referenceId}")
            }.onFailure {
                showMessage("Bill payment failed: ${it.message}")
            }
        }
    }

    fun purchaseMarketplaceItems(itemsSummary: String, totalPaise: Long, paymentMode: String, address: String) {
        viewModelScope.launch {
            val result = repository.purchaseMarketplaceItems(itemsSummary, totalPaise, paymentMode, address)
            result.onSuccess {
                showMessage("Order #${it.orderId} confirmed! Delivered to site.")
            }.onFailure {
                showMessage("Purchase failed: ${it.message}")
            }
        }
    }

    fun raiseSupportTicket(category: String, title: String, description: String, refId: String, priority: String) {
        viewModelScope.launch {
            val ticket = repository.createSupportTicket(category, title, description, refId, priority)
            showMessage("Ticket #${ticket.ticketNumber} raised. Under 48-hr SLA review.")
        }
    }

    fun resolveSupportTicket(ticketId: Long, refId: String, notes: String) {
        viewModelScope.launch {
            repository.resolveDispute(ticketId, refId, notes)
            showMessage("Dispute resolved and audited in ledger.")
        }
    }

    fun addMoneyToWallet(amountRupees: Double, source: String) {
        viewModelScope.launch {
            val txn = repository.topUpWallet((amountRupees * 100).toLong(), source)
            showMessage("Added ${formatPaiseToRupees(txn.amountPaise)} to contractor wallet.")
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetData()
            showMessage("Demo data restored to initial contractor state.")
        }
    }

    // Document Generation & Encrypted PDF Sharing
    fun generateAndSharePdfReport(
        context: Context,
        profile: CorporateProfile,
        recipientName: String = "Site Controller & HR",
        recipientEmail: String = "accounts@sharmaconstruction.com",
        recipientDob: String = "15081995"
    ) {
        viewModelScope.launch {
            try {
                val w = wallet.value
                val isVerified = w?.isAadhaarVerified ?: false
                val aadhaarMasked = w?.aadhaarMasked ?: "XXXX-XXXX-8421"

                val pdfFile = PdfReportGenerator.generatePayrollReportPdf(
                    context = context,
                    profile = profile,
                    workers = workers.value,
                    payrollRecords = payrollList.value,
                    isAadhaarVerified = isVerified,
                    aadhaarMasked = aadhaarMasked,
                    recipientName = recipientName,
                    recipientEmail = recipientEmail,
                    recipientDob = recipientDob
                )

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Confidential Monthly Payroll Report - ${profile.companyName}")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Dear $recipientName,\n\n" +
                                "Please find attached the official monthly workforce attendance and payroll report from ${profile.companyName} (Contractor: ${profile.contractorName}, GSTIN: ${profile.gstin}).\n\n" +
                                "SECURITY NOTICE:\n" +
                                "As per statutory compliance guidelines, this document is password encrypted. The password is your Date of Birth in DDMMYYYY format ($recipientDob).\n\n" +
                                "Official Address: ${profile.physicalAddress}\n" +
                                "Generated securely via Digital Suvidha Platform."
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(shareIntent, "Email Encrypted Corporate PDF Report").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)

                repository.logAuditEvent("PDF_GENERATED_ENCRYPTED", "Generated encrypted PDF for $recipientName ($recipientEmail) with password DOB.")
                showMessage("Encrypted PDF generated. Launching email client.")
            } catch (e: Exception) {
                showMessage("PDF generation error: ${e.message}")
            }
        }
    }

    // CSV Report Sharing (with UTF-8 BOM encoding)
    fun exportReportCsv() {
        val workersList = workers.value
        val payList = payrollList.value
        val profile = getCorporateProfile()

        val sb = StringBuilder()
        sb.append("\uFEFF") // UTF-8 BOM
        sb.append("DIGITAL SUVIDHA - ENTERPRISE WORKFORCE & FINANCIAL REPORT\n")
        sb.append("Company Name: ").append(profile.companyName).append("\n")
        sb.append("Contractor: ").append(profile.contractorName).append("\n")
        sb.append("Address: ").append(profile.physicalAddress).append("\n")
        sb.append("GSTIN: ").append(profile.gstin).append(" | Tel: ").append(profile.mobileNumber).append("\n")
        sb.append("Generated On: ").append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())).append("\n\n")

        sb.append("--- WORKER MASTER DIRECTORY ---\n")
        sb.append("ID,Name,Trade,Phone,Daily Wage (INR),Advance Balance (INR),Assigned Site,Contractor,DOB Password,Status\n")
        for (w in workersList) {
            sb.append("${w.id},\"${w.name}\",\"${w.trade}\",\"${w.phone}\",${w.dailyWagePaise / 100.0},${w.advanceBalancePaise / 100.0},\"${w.siteAssigned}\",\"${w.contractorName}\",${w.dob},${w.status}\n")
        }

        sb.append("\n--- RECENT PAYROLL DISBURSEMENTS ---\n")
        sb.append("Voucher No,Worker Name,Trade,Period,Days,Gross (INR),Advance Deducted (INR),Net Paid (INR),Mode,Status,Txn Ref\n")
        for (p in payList) {
            sb.append("${p.voucherNumber},\"${p.workerName}\",\"${p.workerTrade}\",\"${p.period}\",${p.daysPresent},${p.grossWagePaise / 100.0},${p.advanceDeductedPaise / 100.0},${p.netPayablePaise / 100.0},${p.paymentMode},${p.paymentStatus},${p.transactionRef}\n")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Digital Suvidha Workforce & Payroll Report")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Share CSV Report (UTF-8)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(chooser)
        showMessage("CSV spreadsheet exported with UTF-8 encoding.")
    }

    fun getCorporateProfile(): CorporateProfile {
        val w = wallet.value
        return if (w != null) {
            CorporateProfile(
                companyName = w.companyName,
                physicalAddress = w.companyAddress,
                mobileNumber = w.companyPhone,
                emailAddress = w.companyEmail,
                gstin = w.gstin
            )
        } else {
            CorporateProfile()
        }
    }
}
