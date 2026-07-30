package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_data")
data class FinancialDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val revenue: Double = 100000.0,
    val cogs: Double = 60000.0,
    val grossProfit: Double = 40000.0,
    val opex: Double = 15000.0,
    val operatingProfit: Double = 25000.0,
    val interestExpense: Double = 3000.0,
    val taxExpense: Double = 4400.0,
    val netIncome: Double = 17600.0,
    val currentAssets: Double = 50000.0,
    val cash: Double = 15000.0,
    val receivables: Double = 20000.0,
    val inventory: Double = 15000.0,
    val nonCurrentAssets: Double = 70000.0,
    val totalAssets: Double = 120000.0,
    val currentLiabilities: Double = 30000.0,
    val longTermDebt: Double = 20000.0,
    val totalLiabilities: Double = 50000.0,
    val equity: Double = 70000.0,
    val operatingCashFlow: Double = 22000.0,
    val investingCashFlow: Double = -10000.0,
    val financingCashFlow: Double = -5000.0,
    val freeCashFlow: Double = 12000.0,
    val sharesCount: Double = 1000.0,
    val stockPrice: Double = 250.0,
    val customFieldsJson: String = "{}"
)
