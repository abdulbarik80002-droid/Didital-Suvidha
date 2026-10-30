package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Attendance
import com.example.data.model.AuditLog
import com.example.data.model.LedgerTransaction
import com.example.data.model.MarketOrder
import com.example.data.model.PayrollRecord
import com.example.data.model.SupportTicket
import com.example.data.model.WalletAccount
import com.example.data.model.Worker
import kotlinx.coroutines.flow.Flow

@Dao
interface DigitalSuvidhaDao {

    // --- Workers ---
    @Query("SELECT * FROM workers ORDER BY id DESC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun getActiveWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE id = :workerId LIMIT 1")
    suspend fun getWorkerById(workerId: Long): Worker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)

    @Query("UPDATE workers SET advanceBalancePaise = :newAdvance WHERE id = :workerId")
    suspend fun updateWorkerAdvance(workerId: Long, newAdvance: Long)

    // --- Attendance with GPS Location Tracking ---
    @Query("SELECT * FROM attendance ORDER BY date DESC, id DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getAttendanceListForDate(date: String): List<Attendance>

    @Query("SELECT * FROM attendance WHERE workerId = :workerId ORDER BY date DESC")
    fun getAttendanceForWorker(workerId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE workerId = :workerId AND date >= :startDate AND date <= :endDate")
    suspend fun getAttendanceBetween(workerId: Long, startDate: String, endDate: String): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(attendances: List<Attendance>)

    @Query("DELETE FROM attendance WHERE workerId = :workerId AND date = :date")
    suspend fun deleteAttendanceRecord(workerId: Long, date: String)

    // --- Payroll ---
    @Query("SELECT * FROM payroll_records ORDER BY timestamp DESC")
    fun getAllPayrollRecords(): Flow<List<PayrollRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayrollRecord(record: PayrollRecord): Long

    // --- Transactions / Ledger & Disputes ---
    @Query("SELECT * FROM ledger_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions WHERE referenceId = :refId LIMIT 1")
    suspend fun getTransactionByRef(refId: String): LedgerTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LedgerTransaction): Long

    @Query("UPDATE ledger_transactions SET disputeStatus = :status WHERE referenceId = :refId")
    suspend fun updateTransactionDispute(refId: String, status: String)

    // --- Wallet & Corporate Account ---
    @Query("SELECT * FROM wallet_account WHERE id = 1 LIMIT 1")
    fun getWalletFlow(): Flow<WalletAccount?>

    @Query("SELECT * FROM wallet_account WHERE id = 1 LIMIT 1")
    suspend fun getWalletDirect(): WalletAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWallet(wallet: WalletAccount)

    @Query("UPDATE wallet_account SET balancePaise = :newBalance WHERE id = 1")
    suspend fun updateWalletBalance(newBalance: Long)

    @Query("UPDATE wallet_account SET isLocked = :locked WHERE id = 1")
    suspend fun setWalletLocked(locked: Boolean)

    @Query("UPDATE wallet_account SET pinHash = :pin WHERE id = 1")
    suspend fun updatePin(pin: String)

    @Query("UPDATE wallet_account SET biometricEnabled = :enabled WHERE id = 1")
    suspend fun updateBiometric(enabled: Boolean)

    @Query("UPDATE wallet_account SET isAadhaarVerified = :verified, digilockerRef = :ref, verifiedAt = :time WHERE id = 1")
    suspend fun updateAadhaarStatus(verified: Boolean, ref: String, time: Long)

    @Query("UPDATE wallet_account SET isPanVerified = :verified, panRef = :ref, panNumberMasked = :masked WHERE id = 1")
    suspend fun updatePanStatus(verified: Boolean, ref: String, masked: String)

    @Query("UPDATE wallet_account SET isDlVerified = :verified, dlRef = :ref, dlNumberMasked = :masked WHERE id = 1")
    suspend fun updateDlStatus(verified: Boolean, ref: String, masked: String)

    @Query("UPDATE wallet_account SET isPhoneVerified = :verified, verifiedPhoneNumber = :phone WHERE id = 1")
    suspend fun updatePhoneStatus(verified: Boolean, phone: String)

    @Query("UPDATE wallet_account SET companyName = :name, companyAddress = :address, companyPhone = :phone, companyEmail = :email, gstin = :gst WHERE id = 1")
    suspend fun updateCorporateDetails(name: String, address: String, phone: String, email: String, gst: String)

    // --- Security Audit Trail ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog): Long

    // --- Support Tickets & Disputes ---
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicket): Long

    @Query("UPDATE support_tickets SET status = :status, resolutionNotes = :notes WHERE id = :ticketId")
    suspend fun updateTicketStatus(ticketId: Long, status: String, notes: String)

    // --- Marketplace Orders ---
    @Query("SELECT * FROM market_orders ORDER BY orderDate DESC")
    fun getAllMarketOrders(): Flow<List<MarketOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketOrder(order: MarketOrder): Long

    // Reset operations
    @Query("DELETE FROM workers")
    suspend fun clearWorkers()

    @Query("DELETE FROM attendance")
    suspend fun clearAttendance()

    @Query("DELETE FROM payroll_records")
    suspend fun clearPayroll()

    @Query("DELETE FROM ledger_transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM audit_logs")
    suspend fun clearAuditLogs()

    @Query("DELETE FROM support_tickets")
    suspend fun clearTickets()

    @Query("DELETE FROM market_orders")
    suspend fun clearMarketOrders()
}
