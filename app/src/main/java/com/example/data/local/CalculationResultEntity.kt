package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_results")
data class CalculationResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val healthScore: Int = 85,
    val healthStatus: String = "عالی",
    val scriptUsed: String = "",
    val metricsJson: String = "{}",
    val logsText: String = "",
    val summaryText: String = ""
)
