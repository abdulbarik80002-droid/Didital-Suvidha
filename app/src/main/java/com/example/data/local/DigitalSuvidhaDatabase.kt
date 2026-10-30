package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Attendance
import com.example.data.model.AuditLog
import com.example.data.model.LedgerTransaction
import com.example.data.model.MarketOrder
import com.example.data.model.PayrollRecord
import com.example.data.model.SupportTicket
import com.example.data.model.WalletAccount
import com.example.data.model.Worker

@Database(
    entities = [
        Worker::class,
        Attendance::class,
        PayrollRecord::class,
        LedgerTransaction::class,
        WalletAccount::class,
        AuditLog::class,
        SupportTicket::class,
        MarketOrder::class
    ],
    version = 3,
    exportSchema = false
)
abstract class DigitalSuvidhaDatabase : RoomDatabase() {
    abstract fun dao(): DigitalSuvidhaDao

    companion object {
        @Volatile
        private var INSTANCE: DigitalSuvidhaDatabase? = null

        fun getDatabase(context: Context): DigitalSuvidhaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DigitalSuvidhaDatabase::class.java,
                    "digital_suvidha.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
