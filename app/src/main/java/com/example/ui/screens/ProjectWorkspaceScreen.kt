package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CalculationResultEntity
import com.example.data.local.FinancialDataEntity
import com.example.data.local.ProjectEntity
import com.example.python.EvaluationResult
import com.example.ui.theme.GoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectWorkspaceScreen(
    project: ProjectEntity,
    financialData: FinancialDataEntity?,
    lastResult: EvaluationResult?,
    onBackToProjects: () -> Unit,
    onSaveScript: (String) -> Unit,
    onRunCalculation: (script: String) -> Unit,
    onSaveFinancialData: (FinancialDataEntity) -> Unit,
    onExportPdf: (CalculationResultEntity) -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0: پایتون و محاسبات, 1: صورتهای مالی, 2: گزارش PDF
    var scriptText by remember(project.id) { mutableStateOf(project.pythonScript) }

    // Financial Data input states
    var revenueText by remember(financialData) { mutableStateOf((financialData?.revenue ?: 100000.0).toString()) }
    var cogsText by remember(financialData) { mutableStateOf((financialData?.cogs ?: 65000.0).toString()) }
    var netIncomeText by remember(financialData) { mutableStateOf((financialData?.netIncome ?: 22000.0).toString()) }
    var stockPriceText by remember(financialData) { mutableStateOf((financialData?.stockPrice ?: 1200.0).toString()) }
    var sharesCountText by remember(financialData) { mutableStateOf((financialData?.sharesCount ?: 1000.0).toString()) }
    var assetsText by remember(financialData) { mutableStateOf((financialData?.totalAssets ?: 250000.0).toString()) }
    var liabilitiesText by remember(financialData) { mutableStateOf((financialData?.totalLiabilities ?: 110000.0).toString()) }
    var equityText by remember(financialData) { mutableStateOf((financialData?.equity ?: 140000.0).toString()) }

    val peRatio = lastResult?.computedMetrics?.get("pe_ratio") ?: lastResult?.computedMetrics?.get("نسبت_pe") ?: 0.0
    val paRatio = lastResult?.computedMetrics?.get("pa_ratio") ?: lastResult?.computedMetrics?.get("نسبت_pa") ?: 0.0
    val psRatio = lastResult?.computedMetrics?.get("ps_ratio") ?: lastResult?.computedMetrics?.get("نسبت_ps") ?: 0.0
    val pbRatio = lastResult?.computedMetrics?.get("pb_ratio") ?: lastResult?.computedMetrics?.get("نسبت_pb") ?: 0.0
    val marketCap = lastResult?.computedMetrics?.get("market_cap") ?: lastResult?.computedMetrics?.get("ارزش_بازار") ?: 0.0
    val eps = lastResult?.computedMetrics?.get("eps") ?: lastResult?.computedMetrics?.get("سود_هر_سهم") ?: 0.0
    val outputLogs = lastResult?.logs?.joinToString("\n") ?: ""

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Surface(
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Header Bar (Matching screenshot 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = onBackToProjects) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "بازگشت",
                                    tint = Color.White
                                )
                            }

                            Column {
                                Text(
                                    text = project.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (project.companyName.isNotEmpty()) {
                                    Text(
                                        text = project.companyName + if (project.fiscalYear.isNotEmpty()) " - سال ${project.fiscalYear}" else "",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { /* Menu options */ }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "تنظیمات",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sub Header Navigation Pills (Matching screenshot 2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            label = { Text("دستورات پایتون و محاسبات", fontSize = 12.sp) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF334155),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            border = null
                        )

                        FilterChip(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            label = { Text("ورود صورتهای مالی", fontSize = 12.sp) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF334155),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            border = null
                        )

                        FilterChip(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            label = { Text("گزارش PDF", fontSize = 12.sp) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF334155),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            border = null
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            when (activeTab) {
                0 -> {
                    // Python Editor & Execution Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Snippets Bar
                        Text(
                            text = "فرمول‌های آماده پایتون:",
                            fontSize = 12.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val snippets = listOf(
                                "نسبت P/E" to "\n# محاسبه P/E\npe_ratio = stock_price / (net_income / shares_count)\nprint(f'P/E: {pe_ratio:.2f}')\n",
                                "نسبت P/A" to "\n# محاسبه P/A (ارزش به دارایی)\npa_ratio = (stock_price * shares_count) / total_assets\nprint(f'P/A: {pa_ratio:.2f}')\n",
                                "نسبت P/S" to "\n# محاسبه P/S (ارزش به فروش)\nps_ratio = (stock_price * shares_count) / revenue\nprint(f'P/S: {ps_ratio:.2f}')\n",
                                "نسبت P/B" to "\n# محاسبه P/B (ارزش به ارزش دفتری)\npb_ratio = stock_price / (equity / shares_count)\nprint(f'P/B: {pb_ratio:.2f}')\n",
                                "ارزش بازار" to "\n# ارزش بازار کل\nmarket_cap = stock_price * shares_count\nprint(f'Market Cap: {market_cap:,.0f}')\n",
                                "حاشیه سود" to "\ngross_margin = (gross_profit / revenue) * 100\nnet_margin = (net_income / revenue) * 100\n"
                            )

                            snippets.forEach { (label, snippetCode) ->
                                SuggestionChip(
                                    onClick = { scriptText += snippetCode },
                                    label = { Text(label, fontSize = 11.sp, color = Color.White) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF1E293B)),
                                    border = null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Python Script Input Box
                        OutlinedTextField(
                            value = scriptText,
                            onValueChange = {
                                scriptText = it
                                onSaveScript(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFF38BDF8)
                            ),
                            placeholder = { Text("# کد پایتون خود را اینجا بنویسید...", color = Color(0xFF64748B), fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF334155),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Run Button
                        Button(
                            onClick = {
                                onSaveScript(scriptText)
                                onRunCalculation(scriptText)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اجرای پایتون و محاسبه نسبت‌های مالی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Results Cards Section
                        if (lastResult != null) {
                            Text(
                                text = "نتایج محاسبات مالی:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(title = "P/E", value = String.format("%.2f", peRatio), modifier = Modifier.weight(1f))
                                StatCard(title = "P/A", value = String.format("%.2f", paRatio), modifier = Modifier.weight(1f))
                                StatCard(title = "P/S", value = String.format("%.2f", psRatio), modifier = Modifier.weight(1f))
                                StatCard(title = "P/B", value = String.format("%.2f", pbRatio), modifier = Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(title = "ارزش بازار", value = String.format("%,.0f", marketCap), modifier = Modifier.weight(1f))
                                StatCard(title = "سود هر سهم EPS", value = String.format("%.1f", eps), modifier = Modifier.weight(1f))
                            }

                            if (outputLogs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("خروجی ترمینال پایتون:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = outputLogs,
                                        modifier = Modifier.padding(12.dp),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color(0xFF4ADE80)
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Financial Data Entry Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("ورود و ویرایش صورتهای مالی شرکت:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("ارقام به تومان یا میلیون تومان به انتخاب شما", fontSize = 11.sp, color = Color(0xFF94A3B8))

                        Spacer(modifier = Modifier.height(4.dp))

                        NumberInputRow(label = "درآمد کل / فروش:", value = revenueText, onValueChange = { revenueText = it })
                        NumberInputRow(label = "بهای تمام شده کالای فروش رفته (COGS):", value = cogsText, onValueChange = { cogsText = it })
                        NumberInputRow(label = "سود خالص دوره:", value = netIncomeText, onValueChange = { netIncomeText = it })
                        NumberInputRow(label = "قیمت پایانی هر سهم:", value = stockPriceText, onValueChange = { stockPriceText = it })
                        NumberInputRow(label = "تعداد کل سهام شرکت:", value = sharesCountText, onValueChange = { sharesCountText = it })
                        NumberInputRow(label = "جمع کل دارایی‌ها:", value = assetsText, onValueChange = { assetsText = it })
                        NumberInputRow(label = "جمع کل بدهی‌ها:", value = liabilitiesText, onValueChange = { liabilitiesText = it })
                        NumberInputRow(label = "حقوق صاحبان سهام:", value = equityText, onValueChange = { equityText = it })

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val updatedData = (financialData ?: FinancialDataEntity(projectId = project.id)).copy(
                                    revenue = revenueText.toDoubleOrNull() ?: 0.0,
                                    cogs = cogsText.toDoubleOrNull() ?: 0.0,
                                    netIncome = netIncomeText.toDoubleOrNull() ?: 0.0,
                                    stockPrice = stockPriceText.toDoubleOrNull() ?: 0.0,
                                    sharesCount = sharesCountText.toDoubleOrNull() ?: 0.0,
                                    totalAssets = assetsText.toDoubleOrNull() ?: 0.0,
                                    totalLiabilities = liabilitiesText.toDoubleOrNull() ?: 0.0,
                                    equity = equityText.toDoubleOrNull() ?: 0.0
                                )
                                onSaveFinancialData(updatedData)
                                onRunCalculation(scriptText)
                                activeTab = 0
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("ذخیره و اجرای مجدد محاسبات", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                2 -> {
                    // PDF Report Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("ایجاد گزارش رسمی PDF پروژه", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "گزارش شامل صورتهای مالی و نتایج محاسبات نسبت‌های P/E، P/A، P/S و P/B می‌باشد.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val calcRes = CalculationResultEntity(
                                    projectId = project.id,
                                    healthScore = lastResult?.healthScore ?: 85,
                                    healthStatus = lastResult?.healthStatus ?: "عالی",
                                    scriptUsed = scriptText,
                                    logsText = outputLogs,
                                    summaryText = "نسبت P/E: ${String.format("%.2f", peRatio)} | P/A: ${String.format("%.2f", paRatio)}"
                                )
                                onExportPdf(calcRes)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("دانلود و مشاهده PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun NumberInputRow(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 11.sp, color = Color(0xFFCBD5E1))
        Spacer(modifier = Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF334155),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

