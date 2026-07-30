package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CalculationResultEntity
import com.example.data.local.FinancialDataEntity
import com.example.data.local.ProjectEntity
import com.example.python.EvaluationResult
import com.example.ui.components.HealthScoreCard
import com.example.ui.theme.CodeBg
import com.example.ui.theme.DarkTealPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationScreen(
    project: ProjectEntity?,
    lastResult: EvaluationResult?,
    historyResults: List<CalculationResultEntity>,
    financialData: FinancialDataEntity?,
    onRunCalculation: () -> Unit,
    onExportPdf: (CalculationResultEntity) -> Unit
) {
    if (project == null) return

    val currentScore = lastResult?.healthScore ?: historyResults.firstOrNull()?.healthScore ?: 50
    val currentStatus = lastResult?.healthStatus ?: historyResults.firstOrNull()?.healthStatus ?: "ارزیابی اولیه"

    val metricsMap = remember(lastResult, historyResults) {
        lastResult?.computedMetrics ?: parseMetricsJson(historyResults.firstOrNull()?.metricsJson ?: "{}")
    }

    val logsList = remember(lastResult, historyResults) {
        lastResult?.logs ?: historyResults.firstOrNull()?.logsText?.lines() ?: emptyList()
    }

    Scaffold(
        containerColor = Slate900,
        topBar = {
            Surface(color = Slate800) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "نتایج محاسبات پایتون",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "پروژه: ${project.name}",
                            fontSize = 11.sp,
                            color = GoldAccent
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onRunCalculation,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("محاسبه مجدد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        val latestEntity = historyResults.firstOrNull()
                        if (latestEntity != null) {
                            Button(
                                onClick = { onExportPdf(latestEntity) },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkTealPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("دانلود PDF", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Health Score Card Gauge
            HealthScoreCard(score = currentScore, status = currentStatus)

            // Computed Ratios Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نسبت‌های مالی محاسبه شده (پایتون)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = DarkTealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (metricsMap.isEmpty()) {
                        Text(
                            text = "هنوز محاسباتی انجام نشده است. دکمه 'محاسبه مجدد' را لمس کنید.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    } else {
                        metricsMap.entries.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (entry in rowItems) {
                                    MetricChipCard(
                                        name = entry.key,
                                        value = entry.value,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Console Logs Terminal Output
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CodeBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "کنسول خروجی دستورالعمل پایتون (Logs)",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = GoldAccent)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Divider(color = Slate700)

                    Spacer(modifier = Modifier.height(10.dp))

                    if (logsList.isEmpty()) {
                        Text(
                            text = "کنسول خالی است.",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )
                    } else {
                        logsList.forEach { log ->
                            Text(
                                text = "> $log",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (log.contains("هشدار") || log.contains("خطا")) Color(0xFFF87171) else Color(0xFF38BDF8),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun MetricChipCard(
    name: String,
    value: Double,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate700),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = name,
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(4.dp))
            val valStr = if (value == value.toLong().toDouble()) {
                String.format("%,d", value.toLong())
            } else {
                String.format("%.2f", value)
            }
            Text(
                text = valStr,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
        }
    }
}

private fun parseMetricsJson(jsonStr: String): Map<String, Double> {
    val map = mutableMapOf<String, Double>()
    try {
        val json = org.json.JSONObject(jsonStr)
        val keys = json.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k] = json.optDouble(k, 0.0)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return map
}
