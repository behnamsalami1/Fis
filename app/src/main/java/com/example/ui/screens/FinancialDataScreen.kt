package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FinancialDataEntity
import com.example.data.local.ProjectEntity
import com.example.ui.theme.DarkTealPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialDataScreen(
    project: ProjectEntity?,
    initialData: FinancialDataEntity?,
    onSaveFinancialData: (FinancialDataEntity) -> Unit
) {
    if (project == null) return

    val data = initialData ?: FinancialDataEntity(projectId = project.id)

    var revenue by remember(data) { mutableStateOf(data.revenue.toString()) }
    var cogs by remember(data) { mutableStateOf(data.cogs.toString()) }
    var grossProfit by remember(data) { mutableStateOf(data.grossProfit.toString()) }
    var opex by remember(data) { mutableStateOf(data.opex.toString()) }
    var operatingProfit by remember(data) { mutableStateOf(data.operatingProfit.toString()) }
    var interestExpense by remember(data) { mutableStateOf(data.interestExpense.toString()) }
    var taxExpense by remember(data) { mutableStateOf(data.taxExpense.toString()) }
    var netIncome by remember(data) { mutableStateOf(data.netIncome.toString()) }

    var currentAssets by remember(data) { mutableStateOf(data.currentAssets.toString()) }
    var cash by remember(data) { mutableStateOf(data.cash.toString()) }
    var receivables by remember(data) { mutableStateOf(data.receivables.toString()) }
    var inventory by remember(data) { mutableStateOf(data.inventory.toString()) }
    var nonCurrentAssets by remember(data) { mutableStateOf(data.nonCurrentAssets.toString()) }
    var totalAssets by remember(data) { mutableStateOf(data.totalAssets.toString()) }

    var currentLiabilities by remember(data) { mutableStateOf(data.currentLiabilities.toString()) }
    var longTermDebt by remember(data) { mutableStateOf(data.longTermDebt.toString()) }
    var totalLiabilities by remember(data) { mutableStateOf(data.totalLiabilities.toString()) }
    var equity by remember(data) { mutableStateOf(data.equity.toString()) }

    var operatingCashFlow by remember(data) { mutableStateOf(data.operatingCashFlow.toString()) }
    var investingCashFlow by remember(data) { mutableStateOf(data.investingCashFlow.toString()) }
    var financingCashFlow by remember(data) { mutableStateOf(data.financingCashFlow.toString()) }
    var freeCashFlow by remember(data) { mutableStateOf(data.freeCashFlow.toString()) }

    var sharesCount by remember(data) { mutableStateOf(data.sharesCount.toString()) }
    var stockPrice by remember(data) { mutableStateOf(data.stockPrice.toString()) }

    var activeTab by remember { mutableIntStateOf(0) }

    fun autoBalance() {
        val rev = revenue.toDoubleOrNull() ?: 0.0
        val cg = cogs.toDoubleOrNull() ?: 0.0
        val gp = rev - cg
        grossProfit = gp.toString()

        val opx = opex.toDoubleOrNull() ?: 0.0
        val ebit = gp - opx
        operatingProfit = ebit.toString()

        val intr = interestExpense.toDoubleOrNull() ?: 0.0
        val tx = taxExpense.toDoubleOrNull() ?: 0.0
        val net = ebit - intr - tx
        netIncome = net.toString()

        val ca = currentAssets.toDoubleOrNull() ?: 0.0
        val nca = nonCurrentAssets.toDoubleOrNull() ?: 0.0
        val ta = ca + nca
        totalAssets = ta.toString()

        val cl = currentLiabilities.toDoubleOrNull() ?: 0.0
        val ltd = longTermDebt.toDoubleOrNull() ?: 0.0
        val tl = cl + ltd
        totalLiabilities = tl.toString()

        val eq = ta - tl
        equity = eq.toString()

        val ocf = operatingCashFlow.toDoubleOrNull() ?: 0.0
        freeCashFlow = (ocf - (cg * 0.05)).toString()
    }

    Scaffold(
        containerColor = Slate900,
        topBar = {
            Column(modifier = Modifier.background(Slate800)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ورود داده‌های صورتهای مالی",
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

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { autoBalance() },
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkTealPrimary)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = DarkTealPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("محاسبه خودکار", fontSize = 11.sp, color = DarkTealPrimary)
                        }

                        Button(
                            onClick = {
                                val updatedEntity = data.copy(
                                    revenue = revenue.toDoubleOrNull() ?: 0.0,
                                    cogs = cogs.toDoubleOrNull() ?: 0.0,
                                    grossProfit = grossProfit.toDoubleOrNull() ?: 0.0,
                                    opex = opex.toDoubleOrNull() ?: 0.0,
                                    operatingProfit = operatingProfit.toDoubleOrNull() ?: 0.0,
                                    interestExpense = interestExpense.toDoubleOrNull() ?: 0.0,
                                    taxExpense = taxExpense.toDoubleOrNull() ?: 0.0,
                                    netIncome = netIncome.toDoubleOrNull() ?: 0.0,
                                    currentAssets = currentAssets.toDoubleOrNull() ?: 0.0,
                                    cash = cash.toDoubleOrNull() ?: 0.0,
                                    receivables = receivables.toDoubleOrNull() ?: 0.0,
                                    inventory = inventory.toDoubleOrNull() ?: 0.0,
                                    nonCurrentAssets = nonCurrentAssets.toDoubleOrNull() ?: 0.0,
                                    totalAssets = totalAssets.toDoubleOrNull() ?: 0.0,
                                    currentLiabilities = currentLiabilities.toDoubleOrNull() ?: 0.0,
                                    longTermDebt = longTermDebt.toDoubleOrNull() ?: 0.0,
                                    totalLiabilities = totalLiabilities.toDoubleOrNull() ?: 0.0,
                                    equity = equity.toDoubleOrNull() ?: 0.0,
                                    operatingCashFlow = operatingCashFlow.toDoubleOrNull() ?: 0.0,
                                    investingCashFlow = investingCashFlow.toDoubleOrNull() ?: 0.0,
                                    financingCashFlow = financingCashFlow.toDoubleOrNull() ?: 0.0,
                                    freeCashFlow = freeCashFlow.toDoubleOrNull() ?: 0.0,
                                    sharesCount = sharesCount.toDoubleOrNull() ?: 1.0,
                                    stockPrice = stockPrice.toDoubleOrNull() ?: 0.0
                                )
                                onSaveFinancialData(updatedEntity)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ذخیره", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Slate800,
                    contentColor = GoldAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            color = GoldAccent,
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab])
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("صورت سود و زیان", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("ترازنامه", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("جریان نقد و بازار", fontSize = 11.sp) }
                    )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (activeTab) {
                0 -> {
                    // Income Statement
                    FinancialInputGroupTitle("اقلام صورت سود و زیان (میلیون ریال/تومان)")

                    FinancialInputField("درآمد عملیاتی (Revenue)", revenue) { revenue = it }
                    FinancialInputField("بهای تمام شده فروش (COGS)", cogs) { cogs = it }
                    FinancialInputField("سود ناخالص (Gross Profit)", grossProfit) { grossProfit = it }
                    FinancialInputField("هزینه‌های عمومی و اداری (OPEX)", opex) { opex = it }
                    FinancialInputField("سود عملیاتی (EBIT)", operatingProfit) { operatingProfit = it }
                    FinancialInputField("هزینه‌های مالی (Interest Expense)", interestExpense) { interestExpense = it }
                    FinancialInputField("مالیات بر درآمد (Tax)", taxExpense) { taxExpense = it }
                    FinancialInputField("سود خالص (Net Income)", netIncome) { netIncome = it }
                }
                1 -> {
                    // Balance Sheet
                    FinancialInputGroupTitle("اقلام ترازنامه و دارایی/بدهی")

                    FinancialInputField("دارایی‌های جاری (Current Assets)", currentAssets) { currentAssets = it }
                    FinancialInputField("موجودی نقد و بانک (Cash)", cash) { cash = it }
                    FinancialInputField("حساب‌ها و اسناد دریافتنی (Receivables)", receivables) { receivables = it }
                    FinancialInputField("موجودی کالا و مواد (Inventory)", inventory) { inventory = it }
                    FinancialInputField("دارایی‌های غیرجاری (Non-Current Assets)", nonCurrentAssets) { nonCurrentAssets = it }
                    FinancialInputField("مجموع دارایی‌ها (Total Assets)", totalAssets) { totalAssets = it }

                    Divider(color = Slate700, modifier = Modifier.padding(vertical = 8.dp))

                    FinancialInputField("بدهی‌های جاری (Current Liabilities)", currentLiabilities) { currentLiabilities = it }
                    FinancialInputField("بدهی‌های بلندمدت (Long-term Debt)", longTermDebt) { longTermDebt = it }
                    FinancialInputField("مجموع بدهی‌ها (Total Liabilities)", totalLiabilities) { totalLiabilities = it }
                    FinancialInputField("حقوق صاحبان سهام (Equity)", equity) { equity = it }
                }
                2 -> {
                    // Cash Flow & Market Info
                    FinancialInputGroupTitle("صورت جریان وجوه نقد و مشخصات سهم")

                    FinancialInputField("جریان نقد حاصل از فعالیت‌های عملیاتی (OCF)", operatingCashFlow) { operatingCashFlow = it }
                    FinancialInputField("جریان نقد حاصل از سرمایه‌گذاری", investingCashFlow) { investingCashFlow = it }
                    FinancialInputField("جریان نقد حاصل از تأمین مالی", financingCashFlow) { financingCashFlow = it }
                    FinancialInputField("جریان نقد آزاد (Free Cash Flow - FCF)", freeCashFlow) { freeCashFlow = it }

                    Divider(color = Slate700, modifier = Modifier.padding(vertical = 8.dp))

                    FinancialInputGroupTitle("اطلاعات بازار و سهام")

                    FinancialInputField("تعداد کل سهام شرکت (میلیون سهم)", sharesCount) { sharesCount = it }
                    FinancialInputField("قیمت روز هر سهم بازار", stockPrice) { stockPrice = it }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FinancialInputGroupTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = GoldAccent,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun FinancialInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = Modifier.width(140.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DarkTealPrimary,
                    unfocusedBorderColor = Slate700
                )
            )
        }
    }
}
