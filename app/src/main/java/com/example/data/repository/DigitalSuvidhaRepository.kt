package com.example.data.repository

import android.os.Build
import com.example.data.local.DigitalSuvidhaDao
import com.example.data.model.*
import com.example.util.NetworkUtils
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DigitalSuvidhaRepository(private val dao: DigitalSuvidhaDao) {

    val allWorkers: Flow<List<Worker>> = dao.getAllWorkers()
    val activeWorkers: Flow<List<Worker>> = dao.getActiveWorkers()
    val allAttendance: Flow<List<Attendance>> = dao.getAllAttendance()
    val allPayroll: Flow<List<PayrollRecord>> = dao.getAllPayrollRecords()
    val allTransactions: Flow<List<LedgerTransaction>> = dao.getAllTransactions()
    val wallet: Flow<WalletAccount?> = dao.getWalletFlow()
    val allAuditLogs: Flow<List<AuditLog>> = dao.getAllAuditLogs()
    val allTickets: Flow<List<SupportTicket>> = dao.getAllTickets()
    val allMarketOrders: Flow<List<MarketOrder>> = dao.getAllMarketOrders()

    fun getAttendanceForDate(date: String): Flow<List<Attendance>> = dao.getAttendanceForDate(date)

    suspend fun getWalletDirect(): WalletAccount {
        var w = dao.getWalletDirect()
        if (w == null) {
            w = WalletAccount()
            dao.saveWallet(w)
        }
        return w
    }

    suspend fun logAuditEvent(eventType: String, details: String) {
        val ip = NetworkUtils.getDeviceIpAddress()
        val device = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
        dao.insertAuditLog(
            AuditLog(
                eventType = eventType,
                ipAddress = ip,
                timestamp = System.currentTimeMillis(),
                deviceModel = device,
                details = details
            )
        )
    }

    suspend fun seedDefaultDataIfEmpty() {
        val existingWallet = dao.getWalletDirect()
        if (existingWallet == null) {
            dao.saveWallet(
                WalletAccount(
                    id = 1,
                    balancePaise = 8540000L, // ₹85,400.00
                    accountHolder = "Sharma Construction & Infra Pvt Ltd",
                    panOrGst = "GSTIN: 07AAACS1234F1Z9",
                    isLocked = true,
                    pinHash = "1234",
                    biometricEnabled = true,
                    isAadhaarVerified = true,
                    aadhaarMasked = "XXXX-XXXX-8421",
                    digilockerRef = "DL-UIDAI-784912",
                    verifiedAt = System.currentTimeMillis() - 86400000L * 15,
                    isPanVerified = true,
                    panNumberMasked = "AAACS****F",
                    panRef = "NSDL-PAN-940218",
                    isDlVerified = true,
                    dlNumberMasked = "DL-04****2018",
                    dlRef = "SARATHI-DL-381920",
                    isPhoneVerified = true,
                    verifiedPhoneNumber = "+91 98765 43210",
                    companyName = "Sharma Construction & Infra Pvt Ltd",
                    companyAddress = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
                    companyPhone = "+91 98765 43210",
                    companyEmail = "accounts@sharmaconstruction.com",
                    gstin = "07AAACS1234F1Z9"
                )
            )
            logAuditEvent("INITIAL_SETUP", "Digital Suvidha initialized with encrypted local database.")
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val initialWorkers = listOf(
            Worker(
                name = "Ramesh Kumar",
                phone = "+91 98765 43210",
                trade = "Mason",
                dailyWagePaise = 75000L, // ₹750/day
                advanceBalancePaise = 150000L, // ₹1,500 advance
                bankAccountNo = "483901928374",
                ifscCode = "SBIN0004021",
                upiId = "ramesh.kumar@okhdfcbank",
                siteAssigned = "Site A - Metro Heights",
                status = "ACTIVE",
                contractorName = "Sharma Construction & Infra Pvt Ltd",
                contractorAddress = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
                dob = "12041992"
            ),
            Worker(
                name = "Suresh Mahto",
                phone = "+91 98111 22334",
                trade = "Helper",
                dailyWagePaise = 50000L, // ₹500/day
                advanceBalancePaise = 50000L, // ₹500 advance
                bankAccountNo = "918237465012",
                ifscCode = "PUNB0123400",
                upiId = "sureshmahto@paytm",
                siteAssigned = "Site A - Metro Heights",
                status = "ACTIVE",
                contractorName = "Sharma Construction & Infra Pvt Ltd",
                contractorAddress = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
                dob = "05111996"
            ),
            Worker(
                name = "Mohammad Aslam",
                phone = "+91 97234 56789",
                trade = "Electrician",
                dailyWagePaise = 85000L, // ₹850/day
                advanceBalancePaise = 0L,
                bankAccountNo = "501004839201",
                ifscCode = "HDFC0000128",
                upiId = "aslam.wiring@ibl",
                siteAssigned = "Site B - Highway Corridor",
                status = "ACTIVE",
                contractorName = "Sharma Construction & Infra Pvt Ltd",
                contractorAddress = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
                dob = "23081989"
            ),
            Worker(
                name = "Dharmendra Singh",
                phone = "+91 94567 89012",
                trade = "Carpenter",
                dailyWagePaise = 70000L, // ₹700/day
                advanceBalancePaise = 100000L,
                bankAccountNo = "382910384756",
                ifscCode = "ICIC0001920",
                upiId = "dharmendra@ybl",
                siteAssigned = "Site B - Highway Corridor",
                status = "ACTIVE",
                contractorName = "Sharma Construction & Infra Pvt Ltd",
                contractorAddress = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
                dob = "18021994"
            ),
            Worker(
                name = "Bablu Yadav",
                phone = "+91 99887 76655",
                trade = "Bar Bender",
                dailyWagePaise = 72000L, // ₹720/day
                advanceBalancePaise = 200000L,
                bankAccountNo = "098712345678",
                ifscCode = "BARB0DELHI",
                upiId = "bablu.steel@axl",
                siteAssigned = "Site A - Metro Heights",
                status = "ACTIVE",
                contractorName = "Sharma Construction & Infra Pvt Ltd",
                contractorAddress = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
                dob = "10101991"
            ),
            Worker(
                name = "Raju Paswan",
                phone = "+91 91234 56780",
                trade = "Plumber",
                dailyWagePaise = 68000L, // ₹680/day
                advanceBalancePaise = 0L,
                bankAccountNo = "201928374650",
                ifscCode = "UBIN0530182",
                upiId = "raju.plumbing@upi",
                siteAssigned = "Central Workshop",
                status = "ACTIVE",
                contractorName = "Sharma Construction & Infra Pvt Ltd",
                contractorAddress = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
                dob = "30061997"
            )
        )

        if (dao.getWorkerById(1L) == null) {
            val insertedIds = mutableListOf<Long>()
            for (w in initialWorkers) {
                val id = dao.insertWorker(w)
                insertedIds.add(id)
            }

            // Seed initial attendance with GPS location coordinates
            val defaultAttendance = listOf(
                Attendance(workerId = insertedIds[0], date = today, status = "PRESENT", overtimeHours = 2.0, site = "Site A - Metro Heights", gpsLatitude = 28.6280, gpsLongitude = 77.3649, gpsAddress = "Site A, Sector 62 Noida", checkInTime = "08:15 AM"),
                Attendance(workerId = insertedIds[1], date = today, status = "PRESENT", overtimeHours = 0.0, site = "Site A - Metro Heights", gpsLatitude = 28.6281, gpsLongitude = 77.3648, gpsAddress = "Site A, Sector 62 Noida", checkInTime = "08:22 AM"),
                Attendance(workerId = insertedIds[2], date = today, status = "OVERTIME", overtimeHours = 3.0, site = "Site B - Highway Corridor", gpsLatitude = 28.5355, gpsLongitude = 77.3910, gpsAddress = "Site B, Expressway Hub", checkInTime = "08:05 AM"),
                Attendance(workerId = insertedIds[3], date = today, status = "HALF_DAY", overtimeHours = 0.0, site = "Site B - Highway Corridor", gpsLatitude = 28.5356, gpsLongitude = 77.3911, gpsAddress = "Site B, Expressway Hub", checkInTime = "08:45 AM"),
                Attendance(workerId = insertedIds[4], date = today, status = "PRESENT", overtimeHours = 1.5, site = "Site A - Metro Heights", gpsLatitude = 28.6280, gpsLongitude = 77.3649, gpsAddress = "Site A, Sector 62 Noida", checkInTime = "08:10 AM"),
                Attendance(workerId = insertedIds[5], date = today, status = "ABSENT", overtimeHours = 0.0, site = "Central Workshop", gpsLatitude = 28.5700, gpsLongitude = 77.3200, gpsAddress = "Central Workshop Noida", checkInTime = "N/A")
            )
            dao.insertAttendanceList(defaultAttendance)

            // Seed initial transactions
            dao.insertTransaction(
                LedgerTransaction(
                    referenceId = "TXN-TOPUP-101",
                    type = "CREDIT",
                    category = "WALLET_TOPUP",
                    title = "Working Capital Top-up",
                    subtitle = "HDFC Bank NetBanking • Account **4912",
                    amountPaise = 10000000L, // ₹1,00,000.00
                    balanceAfterPaise = 10000000L,
                    status = "SUCCESS",
                    details = "IMPS Reference: 329182901234 | Purpose: Site Wage Pool"
                )
            )

            dao.insertTransaction(
                LedgerTransaction(
                    referenceId = "TXN-WAGE-102",
                    type = "DEBIT",
                    category = "WAGE_PAYOUT",
                    title = "Weekly Wage: Ramesh Kumar",
                    subtitle = "Voucher #DS-PAY-1001 • Direct UPI",
                    amountPaise = 420000L, // ₹4,200.00
                    balanceAfterPaise = 9580000L,
                    status = "SUCCESS",
                    details = "Settled for 6 days attendance - ₹300 advance deduction"
                )
            )

            dao.insertTransaction(
                LedgerTransaction(
                    referenceId = "TXN-BILL-103",
                    type = "DEBIT",
                    category = "BILL_PAYMENT",
                    title = "Site Electricity: Tata Power DDL",
                    subtitle = "Consumer #1002938481 • BBPS Verified",
                    amountPaise = 1040000L, // ₹10,400.00
                    balanceAfterPaise = 8540000L,
                    status = "SUCCESS",
                    details = "BBPS Ref: BBPS984320194 | Bill Period: Sep-Oct 2026"
                )
            )

            // Seed sample support ticket
            dao.insertTicket(
                SupportTicket(
                    ticketNumber = "DS-TCK-901",
                    category = "ATTENDANCE_DISCREPANCY",
                    title = "GPS Geofence Discrepancy at Site B",
                    description = "Worker Dharmendra Singh logged check-in at 08:45 AM, GPS radius recorded 65m outside boundary.",
                    referenceId = "ATT-SITEB-04",
                    status = "RESOLVED",
                    priority = "MEDIUM",
                    resolutionNotes = "Supervisor verified manual site entry log. Attendance credited."
                )
            )

            logAuditEvent("DATA_SEED", "Pre-loaded initial contractor workforce, GPS attendance, and financial records.")
        }
    }

    suspend fun insertWorker(worker: Worker): Long {
        val id = dao.insertWorker(worker)
        logAuditEvent("ADD_WORKER", "Added worker: ${worker.name} (${worker.trade}) for contractor ${worker.contractorName}")
        return id
    }

    suspend fun updateWorker(worker: Worker) {
        dao.updateWorker(worker)
        logAuditEvent("UPDATE_WORKER", "Updated profile for worker ID: ${worker.id}")
    }

    suspend fun deleteWorker(worker: Worker) {
        dao.deleteWorker(worker)
        logAuditEvent("DELETE_WORKER", "Removed worker ID: ${worker.id}")
    }

    suspend fun markAttendance(
        workerId: Long,
        date: String,
        status: String,
        overtimeHours: Double,
        site: String,
        latitude: Double = 28.6280,
        longitude: Double = 77.3649,
        address: String = "Site A, Sector 62 Noida"
    ) {
        dao.deleteAttendanceRecord(workerId, date)
        val timeNow = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        dao.insertAttendance(
            Attendance(
                workerId = workerId,
                date = date,
                status = status,
                overtimeHours = overtimeHours,
                site = site,
                gpsLatitude = latitude,
                gpsLongitude = longitude,
                gpsAddress = address,
                checkInTime = timeNow,
                isSynced = true
            )
        )
        logAuditEvent("GPS_ATTENDANCE", "Recorded attendance for worker $workerId on $date ($status) at GPS ($latitude, $longitude)")
    }

    suspend fun markAllPresent(date: String, workers: List<Worker>, siteLocation: String = "Site A, Sector 62 Noida") {
        val timeNow = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        for (w in workers) {
            dao.deleteAttendanceRecord(w.id, date)
            dao.insertAttendance(
                Attendance(
                    workerId = w.id,
                    date = date,
                    status = "PRESENT",
                    overtimeHours = 0.0,
                    site = w.siteAssigned,
                    gpsLatitude = 28.6280,
                    gpsLongitude = 77.3649,
                    gpsAddress = siteLocation,
                    checkInTime = timeNow,
                    isSynced = true
                )
            )
        }
        logAuditEvent("ATTENDANCE_BULK_GPS", "Marked all active workers present with GPS coordinates for date $date")
    }

    suspend fun recordWorkerAdvance(workerId: Long, advancePaise: Long, note: String): Boolean {
        val w = dao.getWorkerById(workerId) ?: return false
        val wallet = getWalletDirect()
        if (wallet.balancePaise < advancePaise) return false

        val newBalance = wallet.balancePaise - advancePaise
        dao.updateWalletBalance(newBalance)
        dao.updateWorkerAdvance(workerId, w.advanceBalancePaise + advancePaise)

        val txnId = "TXN-ADV-${System.currentTimeMillis() % 1000000}"
        dao.insertTransaction(
            LedgerTransaction(
                referenceId = txnId,
                type = "DEBIT",
                category = "WORKER_ADVANCE",
                title = "Advance to ${w.name}",
                subtitle = "Site: ${w.siteAssigned} • Cash/UPI Advance",
                amountPaise = advancePaise,
                balanceAfterPaise = newBalance,
                status = "SUCCESS",
                details = "Advance against future wage settlement. Note: $note"
            )
        )
        logAuditEvent("WORKER_ADVANCE", "Disbursed advance of ₹${advancePaise / 100} to ${w.name}")
        return true
    }

    suspend fun disbursePayroll(
        worker: Worker,
        daysPresent: Double,
        overtimeHours: Double,
        grossWagePaise: Long,
        advanceDeductedPaise: Long,
        netPayablePaise: Long,
        paymentMode: String,
        period: String
    ): Result<PayrollRecord> {
        val wallet = getWalletDirect()
        if (paymentMode == "WALLET" && wallet.balancePaise < netPayablePaise) {
            return Result.failure(Exception("Insufficient wallet balance. Please add funds."))
        }

        val voucherNumber = "DS-PAY-${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}-${(100..999).random()}"
        val txnRef = "TXN-PAY-${UUID.randomUUID().toString().take(8).uppercase()}"

        val payroll = PayrollRecord(
            voucherNumber = voucherNumber,
            workerId = worker.id,
            workerName = worker.name,
            workerTrade = worker.trade,
            period = period,
            daysPresent = daysPresent,
            overtimeHours = overtimeHours,
            grossWagePaise = grossWagePaise,
            advanceDeductedPaise = advanceDeductedPaise,
            netPayablePaise = netPayablePaise,
            paymentMode = paymentMode,
            paymentStatus = "DISBURSED",
            transactionRef = txnRef
        )
        dao.insertPayrollRecord(payroll)

        if (advanceDeductedPaise > 0) {
            val remainingAdvance = maxOf(0L, worker.advanceBalancePaise - advanceDeductedPaise)
            dao.updateWorkerAdvance(worker.id, remainingAdvance)
        }

        val newBalance = if (paymentMode == "WALLET") wallet.balancePaise - netPayablePaise else wallet.balancePaise
        if (paymentMode == "WALLET") {
            dao.updateWalletBalance(newBalance)
        }

        dao.insertTransaction(
            LedgerTransaction(
                referenceId = txnRef,
                type = "DEBIT",
                category = "WAGE_PAYOUT",
                title = "Wage Payout: ${worker.name}",
                subtitle = "Voucher #$voucherNumber • $paymentMode",
                amountPaise = netPayablePaise,
                balanceAfterPaise = newBalance,
                status = "SUCCESS",
                details = "Gross: ₹${grossWagePaise/100} | Advance Ded: ₹${advanceDeductedPaise/100} | Period: $period"
            )
        )

        logAuditEvent("PAYROLL_DISBURSEMENT", "Disbursed wage ₹${netPayablePaise/100} to ${worker.name} (Voucher $voucherNumber)")
        return Result.success(payroll)
    }

    suspend fun executeBankTransfer(
        accountNumber: String,
        ifsc: String,
        beneficiaryName: String,
        amountPaise: Long,
        purpose: String,
        mode: String = "IMPS"
    ): Result<LedgerTransaction> {
        val wallet = getWalletDirect()
        if (wallet.balancePaise < amountPaise) {
            return Result.failure(Exception("Insufficient wallet balance for transfer."))
        }

        val newBalance = wallet.balancePaise - amountPaise
        dao.updateWalletBalance(newBalance)

        val txnRef = "$mode-${SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())}-${(100000..999999).random()}"
        val txn = LedgerTransaction(
            referenceId = txnRef,
            type = "DEBIT",
            category = "BANK_TRANSFER",
            title = "Transfer to $beneficiaryName",
            subtitle = "A/c: ****${accountNumber.takeLast(4)} • IFSC: $ifsc ($mode)",
            amountPaise = amountPaise,
            balanceAfterPaise = newBalance,
            status = "SUCCESS",
            details = "$mode RRN: ${(100000000000L..999999999999L).random()} | Purpose: $purpose"
        )
        dao.insertTransaction(txn)
        logAuditEvent("BANK_TRANSFER", "Executed $mode transfer of ₹${amountPaise/100} to $beneficiaryName ($ifsc)")
        return Result.success(txn)
    }

    suspend fun executeInternationalTransfer(
        countryCurrency: String,
        foreignAmount: Double,
        inrAmountPaise: Long,
        swiftBic: String,
        ibanOrAccount: String,
        beneficiaryName: String,
        recipientBank: String,
        purpose: String
    ): Result<LedgerTransaction> {
        val wallet = getWalletDirect()
        if (wallet.balancePaise < inrAmountPaise) {
            return Result.failure(Exception("Insufficient wallet balance for international remittance."))
        }

        val newBalance = wallet.balancePaise - inrAmountPaise
        dao.updateWalletBalance(newBalance)

        val txnRef = "SWIFT-${(10000000..99999999).random()}"
        val txn = LedgerTransaction(
            referenceId = txnRef,
            type = "DEBIT",
            category = "INTERNATIONAL_TRANSFER",
            title = "International Remittance ($countryCurrency $foreignAmount)",
            subtitle = "$beneficiaryName • SWIFT: $swiftBic • $recipientBank",
            amountPaise = inrAmountPaise,
            balanceAfterPaise = newBalance,
            status = "SUCCESS",
            details = "SWIFT UETR: ${UUID.randomUUID()} | IBAN: $ibanOrAccount | Purpose: $purpose"
        )
        dao.insertTransaction(txn)
        logAuditEvent("INTL_TRANSFER", "Executed international wire of $countryCurrency $foreignAmount (₹${inrAmountPaise/100}) to $beneficiaryName")
        return Result.success(txn)
    }

    suspend fun executeUpiPayment(
        upiId: String,
        recipientName: String,
        amountPaise: Long,
        note: String
    ): Result<LedgerTransaction> {
        val wallet = getWalletDirect()
        if (wallet.balancePaise < amountPaise) {
            return Result.failure(Exception("Insufficient wallet balance for UPI payment."))
        }

        val newBalance = wallet.balancePaise - amountPaise
        dao.updateWalletBalance(newBalance)

        val txnRef = "UPI-${SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())}-${(100000..999999).random()}"
        val txn = LedgerTransaction(
            referenceId = txnRef,
            type = "DEBIT",
            category = "UPI_TRANSFER",
            title = "UPI Paid to $recipientName",
            subtitle = "VPA: $upiId",
            amountPaise = amountPaise,
            balanceAfterPaise = newBalance,
            status = "SUCCESS",
            details = "UPI Ref ID: ${(100000000000L..999999999999L).random()} | Note: $note"
        )
        dao.insertTransaction(txn)
        logAuditEvent("UPI_TRANSFER", "Paid ₹${amountPaise/100} to $upiId ($recipientName)")
        return Result.success(txn)
    }

    suspend fun payUtilityBill(
        serviceName: String,
        billerName: String,
        consumerNumber: String,
        amountPaise: Long
    ): Result<LedgerTransaction> {
        val wallet = getWalletDirect()
        if (wallet.balancePaise < amountPaise) {
            return Result.failure(Exception("Insufficient wallet balance to pay bill."))
        }

        val newBalance = wallet.balancePaise - amountPaise
        dao.updateWalletBalance(newBalance)

        val bbpsRef = "BBPS-${(100000000000L..999999999999L).random()}"
        val txn = LedgerTransaction(
            referenceId = bbpsRef,
            type = "DEBIT",
            category = "BILL_PAYMENT",
            title = "$serviceName: $billerName",
            subtitle = "Consumer ID: $consumerNumber • BBPS Assured",
            amountPaise = amountPaise,
            balanceAfterPaise = newBalance,
            status = "SUCCESS",
            details = "BBPS Approval Ref: $bbpsRef | Service: $serviceName"
        )
        dao.insertTransaction(txn)
        logAuditEvent("BILL_PAYMENT", "BBPS payment of ₹${amountPaise/100} for $billerName ($consumerNumber)")
        return Result.success(txn)
    }

    suspend fun purchaseMarketplaceItems(
        itemsSummary: String,
        totalPaise: Long,
        paymentMode: String,
        address: String
    ): Result<MarketOrder> {
        val wallet = getWalletDirect()
        if (paymentMode == "WALLET" && wallet.balancePaise < totalPaise) {
            return Result.failure(Exception("Insufficient wallet balance for marketplace order."))
        }

        val orderId = "ORD-DS-${(100000..999999).random()}"
        val newBalance = if (paymentMode == "WALLET") wallet.balancePaise - totalPaise else wallet.balancePaise
        if (paymentMode == "WALLET") {
            dao.updateWalletBalance(newBalance)
        }

        val order = MarketOrder(
            orderId = orderId,
            itemsSummary = itemsSummary,
            totalAmountPaise = totalPaise,
            paymentMode = paymentMode,
            deliveryAddress = address
        )
        dao.insertMarketOrder(order)

        dao.insertTransaction(
            LedgerTransaction(
                referenceId = orderId,
                type = "DEBIT",
                category = "MARKETPLACE_PURCHASE",
                title = "In-App Shopping: Site Supplies",
                subtitle = "Order #$orderId • $itemsSummary",
                amountPaise = totalPaise,
                balanceAfterPaise = newBalance,
                status = "SUCCESS",
                details = "Delivered to $address | Payment: $paymentMode"
            )
        )

        logAuditEvent("MARKETPLACE_PURCHASE", "Purchased $itemsSummary for ₹${totalPaise/100} (Order $orderId)")
        return Result.success(order)
    }

    suspend fun topUpWallet(amountPaise: Long, source: String): LedgerTransaction {
        val wallet = getWalletDirect()
        val newBalance = wallet.balancePaise + amountPaise
        dao.updateWalletBalance(newBalance)

        val txn = LedgerTransaction(
            referenceId = "TOPUP-${System.currentTimeMillis() % 1000000}",
            type = "CREDIT",
            category = "WALLET_TOPUP",
            title = "Wallet Funded (Demo)",
            subtitle = "$source • Instant Credit",
            amountPaise = amountPaise,
            balanceAfterPaise = newBalance,
            status = "SUCCESS",
            details = "Contractor Working Capital Top-up for payroll & operations"
        )
        dao.insertTransaction(txn)
        logAuditEvent("WALLET_TOPUP", "Added working capital funds of ₹${amountPaise/100} via $source")
        return txn
    }

    suspend fun setAadhaarVerified(ref: String) {
        val time = System.currentTimeMillis()
        dao.updateAadhaarStatus(true, ref, time)
        logAuditEvent("AADHAAR_VERIFIED", "Successfully completed Aadhaar verification via DigiLocker / MeriPehchaan (Ref: $ref)")
    }

    suspend fun setPanVerified(ref: String, maskedPan: String) {
        dao.updatePanStatus(true, ref, maskedPan)
        logAuditEvent("PAN_VERIFIED", "Successfully completed PAN verification via NSDL portal (Ref: $ref)")
    }

    suspend fun setDlVerified(ref: String, maskedDl: String) {
        dao.updateDlStatus(true, ref, maskedDl)
        logAuditEvent("DL_VERIFIED", "Successfully completed Driving License verification via Sarathi MoRTH (Ref: $ref)")
    }

    suspend fun setPhoneVerified(phoneNumber: String) {
        dao.updatePhoneStatus(true, phoneNumber)
        logAuditEvent("PHONE_OTP_VERIFIED", "Successfully verified mobile phone access via OTP ($phoneNumber)")
    }

    suspend fun createSupportTicket(
        category: String,
        title: String,
        description: String,
        refId: String,
        priority: String
    ): SupportTicket {
        val ticketNo = "DS-TCK-${(10000..99999).random()}"
        val ticket = SupportTicket(
            ticketNumber = ticketNo,
            category = category,
            title = title,
            description = description,
            referenceId = refId,
            priority = priority
        )
        val id = dao.insertTicket(ticket)
        if (refId.isNotEmpty()) {
            dao.updateTransactionDispute(refId, "RAISED")
        }
        logAuditEvent("SUPPORT_TICKET_RAISED", "Raised ticket $ticketNo for $category (Ref: $refId)")
        return ticket.copy(id = id)
    }

    suspend fun resolveDispute(ticketId: Long, refId: String, notes: String) {
        dao.updateTicketStatus(ticketId, "RESOLVED", notes)
        if (refId.isNotEmpty()) {
            dao.updateTransactionDispute(refId, "RESOLVED")
        }
        logAuditEvent("DISPUTE_RESOLVED", "Resolved dispute for ticket $ticketId with resolution: $notes")
    }

    suspend fun updateCorporateProfile(profile: CorporateProfile) {
        dao.updateCorporateDetails(
            name = profile.companyName,
            address = profile.physicalAddress,
            phone = profile.mobileNumber,
            email = profile.emailAddress,
            gst = profile.gstin
        )
        logAuditEvent("CORPORATE_PROFILE_UPDATED", "Updated corporate credentials for PDF generation: ${profile.companyName}")
    }

    suspend fun unlockWallet() = dao.setWalletLocked(false)
    suspend fun lockWallet() = dao.setWalletLocked(true)
    suspend fun updatePin(newPin: String) = dao.updatePin(newPin)
    suspend fun updateBiometric(enabled: Boolean) = dao.updateBiometric(enabled)

    suspend fun resetData() {
        dao.clearWorkers()
        dao.clearAttendance()
        dao.clearPayroll()
        dao.clearTransactions()
        dao.clearAuditLogs()
        dao.clearTickets()
        dao.clearMarketOrders()
        dao.updateWalletBalance(8540000L)
        dao.updateAadhaarStatus(true, "DL-UIDAI-784912", System.currentTimeMillis())
        seedDefaultDataIfEmpty()
        logAuditEvent("RESET_DATA", "Restored contractor demo dataset and cleared previous activity.")
    }
}
