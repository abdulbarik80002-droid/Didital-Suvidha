package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale

/**
 * Worker Profile Entity
 * Stores comprehensive worker master details, editable profile information,
 * contractor assignments, company credentials, bank/UPI settlement info, and DOB for encrypted PDFs.
 */
@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val trade: String,
    val dailyWagePaise: Long,
    val advanceBalancePaise: Long = 0L,
    val bankAccountNo: String = "",
    val ifscCode: String = "",
    val upiId: String = "",
    val siteAssigned: String = "Site A - Metro Heights",
    val status: String = "ACTIVE", // ACTIVE, INACTIVE
    val contractorName: String = "Sharma Construction & Infra Pvt Ltd",
    val contractorAddress: String = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
    val dob: String = "15081995" // Date of Birth format DDMMYYYY for encrypted PDF password
)

/**
 * Attendance Record Entity
 * Automated daily attendance calculation linked directly to real-time GPS location tracking.
 */
@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workerId: Long,
    val date: String, // YYYY-MM-DD
    val status: String, // PRESENT, HALF_DAY, ABSENT, OVERTIME
    val overtimeHours: Double = 0.0,
    val site: String = "",
    val gpsLatitude: Double = 28.6280, // GPS latitude coordinate
    val gpsLongitude: Double = 77.3649, // GPS longitude coordinate
    val gpsAddress: String = "Sector 62, Noida (Geofenced Site A)",
    val checkInTime: String = "08:30 AM",
    val isSynced: Boolean = true
)

/**
 * Payroll Record Entity
 * Stores wage vouchers, gross wage, advance deductions, and net payable calculations.
 */
@Entity(tableName = "payroll_records")
data class PayrollRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voucherNumber: String,
    val workerId: Long,
    val workerName: String,
    val workerTrade: String,
    val period: String,
    val daysPresent: Double,
    val overtimeHours: Double,
    val grossWagePaise: Long,
    val advanceDeductedPaise: Long,
    val netPayablePaise: Long,
    val paymentMode: String, // WALLET, BANK_TRANSFER, UPI, CASH
    val paymentStatus: String, // DISBURSED, PENDING
    val transactionRef: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Financial Ledger Transaction Entity
 * Double-entry ledger recording all debits, credits, top-ups, transfers, utility payments, and dispute statuses.
 */
@Entity(tableName = "ledger_transactions")
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val referenceId: String,
    val type: String, // CREDIT or DEBIT
    val category: String, // WAGE_PAYOUT, BANK_TRANSFER, UPI_TRANSFER, BILL_PAYMENT, WALLET_TOPUP, WORKER_ADVANCE, MARKETPLACE_PURCHASE, INTERNATIONAL_TRANSFER
    val title: String,
    val subtitle: String,
    val amountPaise: Long,
    val balanceAfterPaise: Long,
    val status: String, // SUCCESS, PENDING, FAILED, DISPUTED
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val disputeStatus: String = "NONE" // NONE, RAISED, UNDER_REVIEW, RESOLVED
)

/**
 * Corporate Wallet Account & KYC Status Entity
 * Stores available working capital, company details, and multi-document KYC statuses.
 */
@Entity(tableName = "wallet_account")
data class WalletAccount(
    @PrimaryKey
    val id: Long = 1,
    val balancePaise: Long = 8540000L, // ₹85,400.00
    val accountHolder: String = "Sharma Construction & Infra Pvt Ltd",
    val panOrGst: String = "GSTIN: 07AAACS1234F1Z9",
    val isLocked: Boolean = true,
    val pinHash: String = "1234",
    val biometricEnabled: Boolean = true,
    // Aadhaar KYC
    val isAadhaarVerified: Boolean = false,
    val aadhaarMasked: String = "XXXX-XXXX-8421",
    val digilockerRef: String = "",
    val verifiedAt: Long = 0L,
    // PAN KYC
    val isPanVerified: Boolean = false,
    val panNumberMasked: String = "AAACS****F",
    val panRef: String = "",
    // Driving License KYC
    val isDlVerified: Boolean = false,
    val dlNumberMasked: String = "DL-04****2018",
    val dlRef: String = "",
    // Mobile OTP KYC
    val isPhoneVerified: Boolean = true,
    val verifiedPhoneNumber: String = "+91 98765 43210",
    // Company Profile
    val companyName: String = "Sharma Construction & Infra Pvt Ltd",
    val companyAddress: String = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
    val companyPhone: String = "+91 98765 43210",
    val companyEmail: String = "accounts@sharmaconstruction.com",
    val gstin: String = "07AAACS1234F1Z9"
)

/**
 * Security & Audit Log Entity
 * 100% data retention and tamper-evident audit trail.
 */
@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val ipAddress: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceModel: String,
    val details: String
)

/**
 * Support Ticket & Dispute Entity
 * In-app customer support ticketing system and dispute resolution workflow.
 */
@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticketNumber: String,
    val category: String, // FAILED_TRANSACTION, ATTENDANCE_DISCREPANCY, WAGE_SETTLEMENT, BILL_PAYMENT, KYC_ISSUE
    val title: String,
    val description: String,
    val referenceId: String = "",
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val createdAt: Long = System.currentTimeMillis(),
    val resolutionNotes: String = ""
)

/**
 * Marketplace Order Entity
 * In-app shopping purchases and order tracking.
 */
@Entity(tableName = "market_orders")
data class MarketOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: String,
    val itemsSummary: String,
    val totalAmountPaise: Long,
    val paymentMode: String, // WALLET, UPI
    val deliveryAddress: String,
    val orderDate: Long = System.currentTimeMillis(),
    val status: String = "CONFIRMED" // CONFIRMED, DISPATCHED, DELIVERED
)

/**
 * Corporate Profile data class for PDF branding & official letterhead
 */
data class CorporateProfile(
    val companyName: String = "Sharma Construction & Infra Pvt Ltd",
    val physicalAddress: String = "Plot 42, Sector 62, Noida, NCR Delhi - 201309",
    val mobileNumber: String = "+91 98765 43210",
    val emailAddress: String = "accounts@sharmaconstruction.com",
    val gstin: String = "07AAACS1234F1Z9",
    val contractorName: String = "Rajesh Sharma (Principal Contractor)"
)

/**
 * E-Commerce Marketplace Product Item
 */
data class MarketItem(
    val id: String,
    val title: String,
    val category: String, // Materials, Safety & PPE, Tools & Hardware, Site Essentials
    val pricePaise: Long,
    val unit: String,
    val rating: Double,
    val stock: Int,
    val description: String,
    val badge: String = ""
)

/**
 * Shopping Cart Item
 */
data class CartItem(
    val item: MarketItem,
    val quantity: Int
)

/**
 * Support Chat Message
 */
data class TicketMessage(
    val id: String,
    val sender: String, // USER, SUPPORT_AGENT, SYSTEM
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * GPS Coordinates
 */
data class GpsLocation(
    val latitude: Double,
    val longitude: Double,
    val siteName: String,
    val accuracyMeters: Float,
    val isInsideGeofence: Boolean
)

/**
 * Utility function to format paise to Indian Rupee notation (e.g. ₹1,234.00)
 */
fun formatPaiseToRupees(paise: Long): String {
    val rupees = paise / 100.0
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return format.format(rupees)
}
