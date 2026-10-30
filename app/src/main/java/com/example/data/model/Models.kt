package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val phone: String = "",
    val trade: String = "",
    val dailyWagePaise: Long = 0L,
    val advanceBalancePaise: Long = 0L,
    val bankAccountNo: String = "",
    val ifscCode: String = "",
    val upiId: String = "",
    val siteAssigned: String = "",
    val status: String = "ACTIVE"
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val workerId: Long = 0L,
    val date: String = "",
    val status: String = "PRESENT",
    val overtimeHours: Double = 0.0,
    val site: String = "",
    val isSynced: Boolean = true
)

@Entity(tableName = "payroll_records")
data class PayrollRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val voucherNumber: String = "",
    val workerId: Long = 0L,
    val workerName: String = "",
    val workerTrade: String = "",
    val period: String = "",
    val daysPresent: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val grossWagePaise: Long = 0L,
    val advanceDeductedPaise: Long = 0L,
    val netPayablePaise: Long = 0L,
    val paymentMode: String = "WALLET",
    val paymentStatus: String = "DISBURSED",
    val transactionRef: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ledger_transactions")
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val referenceId: String = "",
    val type: String = "CREDIT",
    val category: String = "WALLET_TOPUP",
    val title: String = "",
    val subtitle: String = "",
    val amountPaise: Long = 0L,
    val balanceAfterPaise: Long = 0L,
    val status: String = "SUCCESS",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_account")
data class WalletAccount(
    @PrimaryKey
    val id: Long = 1L,
    val balancePaise: Long = 8540000L,
    val accountHolder: String = "Sharma Construction & Infra Pvt Ltd",
    val panOrGst: String = "GSTIN: 07AAACS1234F1Z9",
    val isLocked: Boolean = true,
    val pinHash: String = "1234",
    val biometricEnabled: Boolean = true,
    val isAadhaarVerified: Boolean = false,
    val aadhaarMasked: String = "XXXX-XXXX-8421",
    val digilockerRef: String = "",
    val verifiedAt: Long = 0L,
    val companyName: String = "Sharma Construction & Infra Pvt Ltd",
    val companyAddress: String = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
    val companyPhone: String = "+91 98765 43210",
    val companyEmail: String = "accounts@sharmaconstruction.com",
    val gstin: String = "07AAACS1234F1Z9"
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val eventType: String = "",
    val ipAddress: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deviceModel: String = "",
    val details: String = ""
)

data class CorporateProfile(
    val companyName: String = "Sharma Construction & Infra Pvt Ltd",
    val physicalAddress: String = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
    val mobileNumber: String = "+91 98765 43210",
    val emailAddress: String = "accounts@sharmaconstruction.com",
    val gstin: String = "07AAACS1234F1Z9"
)

fun formatPaiseToRupees(paise: Long): String {
    val rupees = paise / 100.0
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return format.format(rupees)
}
