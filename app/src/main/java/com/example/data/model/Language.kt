package com.example.data.model

enum class AppLanguage(val code: String, val displayName: String, val scriptName: String) {
    ENGLISH("en", "English", "English")
}

object Localization {
    fun getString(key: String, lang: AppLanguage = AppLanguage.ENGLISH): String {
        return translations[key] ?: key
    }

    private val translations = mapOf(
        "app_name" to "Digital Suvidha",
        "tagline" to "Workforce Management & Enterprise Banking",
        "enter_pin" to "Enter Secure Security PIN",
        "pin_subtitle" to "Digital Suvidha wallet & ledger are locked",
        "biometric_unlock" to "Unlock with Biometrics",
        "dashboard" to "Dashboard",
        "workers" to "Workers",
        "attendance" to "Attendance",
        "payroll" to "Payroll",
        "banking" to "Banking",
        "bill_pay" to "Bill Pay",
        "ledger" to "Ledger",
        "reports" to "Reports",
        "settings" to "Settings",
        "wallet_balance" to "Available Balance (Demo Wallet)",
        "demo_notice" to "Training & Construction Ledger Sandbox",
        "add_worker" to "Add Worker",
        "mark_attendance" to "Mark Attendance",
        "disburse_wage" to "Disburse Wage",
        "bank_transfer" to "Bank Transfer (IMPS)",
        "upi_pay" to "UPI Payment",
        "offline_ready" to "Offline Ready - Records Saved Locally"
    )
}
