package com.example.python

import com.example.data.local.FinancialDataEntity
import java.util.Locale
import kotlin.math.*

data class EvaluationResult(
    val success: Boolean,
    val healthScore: Int,
    val healthStatus: String,
    val computedMetrics: Map<String, Double>,
    val computedStrings: Map<String, String>,
    val logs: List<String>,
    val errorMessage: String? = null
)

class PythonInterpreter {

    fun execute(script: String, data: FinancialDataEntity): EvaluationResult {
        val variables = mutableMapOf<String, Any>()
        val logs = mutableListOf<String>()

        // 1. Populate initial context from FinancialDataEntity
        populateInitialVariables(variables, data)

        // 2. Parse and execute line by line
        val lines = script.lines()
        var currentLineIndex = 0
        var defaultHealthScore = 50

        try {
            var i = 0
            while (i < lines.size) {
                val rawLine = lines[i]
                val trimmed = rawLine.trim()

                // Skip comments and empty lines
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    i++
                    continue
                }

                // Check for IF block
                if (trimmed.startsWith("if ")) {
                    val (nextIndex, returnedHealth) = handleIfBlock(
                        lines = lines,
                        startIndex = i,
                        variables = variables,
                        logs = logs
                    )
                    i = nextIndex
                    continue
                }

                // Handle single statement
                executeStatement(trimmed, variables, logs)
                i++
            }

            // Extract results
            val computedMetrics = mutableMapOf<String, Double>()
            val computedStrings = mutableMapOf<String, String>()

            for ((key, value) in variables) {
                // Ignore system inputs in final metrics list to emphasize calculated results
                if (isSystemVariable(key)) continue
                when (value) {
                    is Number -> computedMetrics[key] = value.toDouble()
                    is Boolean -> computedMetrics[key] = if (value) 1.0 else 0.0
                    is String -> computedStrings[key] = value
                }
            }

            // Determine final health score
            val rawScore = (variables["health_score"] as? Number)?.toInt()
                ?: (variables["score"] as? Number)?.toInt()
                ?: defaultHealthScore

            val finalScore = rawScore.coerceIn(0, 100)
            val status = when {
                finalScore >= 80 -> "عالی (ممتاز)"
                finalScore >= 60 -> "مناسب (پایدار)"
                finalScore >= 45 -> "ریسک متوسط"
                else -> "ریسک بالا (هشدار)"
            }

            return EvaluationResult(
                success = true,
                healthScore = finalScore,
                healthStatus = status,
                computedMetrics = computedMetrics,
                computedStrings = computedStrings,
                logs = logs
            )

        } catch (e: Exception) {
            logs.add("خطا در اجرای کد پایتون: ${e.message}")
            return EvaluationResult(
                success = false,
                healthScore = 0,
                healthStatus = "خطا در کد",
                computedMetrics = emptyMap(),
                computedStrings = emptyMap(),
                logs = logs,
                errorMessage = e.message ?: "خطای ناشناخته در پایتون"
            )
        }
    }

    private fun populateInitialVariables(map: MutableMap<String, Any>, d: FinancialDataEntity) {
        val marketCap = d.stockPrice * d.sharesCount
        val epsVal = if (d.sharesCount > 0) d.netIncome / d.sharesCount else 0.0
        val peVal = if (epsVal > 0) d.stockPrice / epsVal else 0.0
        val bvps = if (d.sharesCount > 0) d.equity / d.sharesCount else 0.0
        val pbVal = if (bvps > 0) d.stockPrice / bvps else 0.0
        val psVal = if (d.revenue > 0) marketCap / d.revenue else 0.0
        val paVal = if (d.totalAssets > 0) marketCap / d.totalAssets else 0.0

        // Market Cap and Computed valuation ratios
        map["market_cap"] = marketCap
        map["eps"] = epsVal
        map["pe_ratio"] = peVal
        map["pb_ratio"] = pbVal
        map["ps_ratio"] = psVal
        map["pa_ratio"] = paVal

        // Persian Aliases for Market Ratios
        map["ارزش_بازار"] = marketCap
        map["سود_هر_سهم"] = epsVal
        map["نسبت_pe"] = peVal
        map["نسبت_pb"] = pbVal
        map["نسبت_ps"] = psVal
        map["نسبت_pa"] = paVal

        // English aliases
        map["revenue"] = d.revenue
        map["cogs"] = d.cogs
        map["gross_profit"] = d.grossProfit
        map["opex"] = d.opex
        map["operating_profit"] = d.operatingProfit
        map["ebit"] = d.operatingProfit
        map["interest_expense"] = d.interestExpense
        map["tax_expense"] = d.taxExpense
        map["net_income"] = d.netIncome

        map["current_assets"] = d.currentAssets
        map["cash"] = d.cash
        map["receivables"] = d.receivables
        map["inventory"] = d.inventory
        map["non_current_assets"] = d.nonCurrentAssets
        map["total_assets"] = d.totalAssets

        map["current_liabilities"] = d.currentLiabilities
        map["long_term_debt"] = d.longTermDebt
        map["total_liabilities"] = d.totalLiabilities
        map["equity"] = d.equity

        map["operating_cash_flow"] = d.operatingCashFlow
        map["ocf"] = d.operatingCashFlow
        map["investing_cash_flow"] = d.investingCashFlow
        map["financing_cash_flow"] = d.financingCashFlow
        map["free_cash_flow"] = d.freeCashFlow
        map["fcf"] = d.freeCashFlow

        map["shares_count"] = d.sharesCount
        map["stock_price"] = d.stockPrice

        // Persian aliases
        map["درآمد"] = d.revenue
        map["بهای_تمام_شده"] = d.cogs
        map["سود_ناخالص"] = d.grossProfit
        map["سود_عملیاتی"] = d.operatingProfit
        map["سود_خالص"] = d.netIncome
        map["دارایی_جاری"] = d.currentAssets
        map["موجودی_نقد"] = d.cash
        map["کل_دارایی"] = d.totalAssets
        map["بدهی_جاری"] = d.currentLiabilities
        map["کل_بدهی"] = d.totalLiabilities
        map["حقوق_صاحبان_سهام"] = d.equity
        map["قیمت_سهم"] = d.stockPrice

        map["health_score"] = 50
    }

    private fun isSystemVariable(key: String): Boolean {
        val sysKeys = setOf(
            "revenue", "cogs", "gross_profit", "opex", "operating_profit", "ebit",
            "interest_expense", "tax_expense", "net_income", "current_assets", "cash",
            "receivables", "inventory", "non_current_assets", "total_assets", "current_liabilities",
            "long_term_debt", "total_liabilities", "equity", "operating_cash_flow", "ocf",
            "investing_cash_flow", "financing_cash_flow", "free_cash_flow", "fcf", "shares_count",
            "stock_price", "درآمد", "بهای_تمام_شده", "سود_ناخالص", "سود_عملیاتی", "سود_خالص",
            "دارایی_جاری", "موجودی_نقد", "کل_دارایی", "بدهی_جاری", "کل_بدهی", "حقوق_صاحبان_سهام", "قیمت_سهم"
        )
        return sysKeys.contains(key)
    }

    private fun executeStatement(statement: String, variables: MutableMap<String, Any>, logs: MutableList<String>) {
        if (statement.startsWith("print(")) {
            val content = statement.substringAfter("print(").substringBeforeLast(")")
            val printed = evaluatePrintArgs(content, variables)
            logs.add(printed)
            return
        }

        if (statement.contains(" += ")) {
            val parts = statement.split(" += ", limit = 2)
            val varName = parts[0].trim()
            val expr = parts[1].trim()
            val currentVal = (variables[varName] as? Number)?.toDouble() ?: 0.0
            val addedVal = evaluateExpression(expr, variables)
            variables[varName] = currentVal + addedVal
            return
        }

        if (statement.contains(" -= ")) {
            val parts = statement.split(" -= ", limit = 2)
            val varName = parts[0].trim()
            val expr = parts[1].trim()
            val currentVal = (variables[varName] as? Number)?.toDouble() ?: 0.0
            val subVal = evaluateExpression(expr, variables)
            variables[varName] = currentVal - subVal
            return
        }

        if (statement.contains(" = ")) {
            val parts = statement.split(" = ", limit = 2)
            val varName = parts[0].trim()
            val expr = parts[1].trim()
            val valResult = evaluateExpression(expr, variables)
            variables[varName] = valResult
            return
        }
    }

    private fun handleIfBlock(
        lines: List<String>,
        startIndex: Int,
        variables: MutableMap<String, Any>,
        logs: MutableList<String>
    ): Pair<Int, Int> {
        val firstLine = lines[startIndex].trim()
        val conditionStr = firstLine.substringAfter("if ").substringBefore(":")
        val conditionResult = evaluateCondition(conditionStr, variables)

        var i = startIndex + 1
        val ifBody = mutableListOf<String>()
        val elifElseBlocks = mutableListOf<Pair<String?, List<String>>>()

        // Gather IF body
        while (i < lines.size) {
            val l = lines[i]
            val tr = l.trim()
            if (tr.isEmpty()) {
                i++
                continue
            }
            if (l.startsWith("    ") || l.startsWith("\t")) {
                ifBody.add(tr)
                i++
            } else {
                break
            }
        }

        var currentBranchExecuted = false
        if (conditionResult) {
            for (stmt in ifBody) {
                executeStatement(stmt, variables, logs)
            }
            currentBranchExecuted = true
        }

        // Check for elif or else
        while (i < lines.size) {
            val tr = lines[i].trim()
            if (tr.startsWith("elif ")) {
                val elifCondStr = tr.substringAfter("elif ").substringBefore(":")
                i++
                val elifBody = mutableListOf<String>()
                while (i < lines.size) {
                    val l = lines[i]
                    if (l.startsWith("    ") || l.startsWith("\t")) {
                        elifBody.add(l.trim())
                        i++
                    } else break
                }
                if (!currentBranchExecuted && evaluateCondition(elifCondStr, variables)) {
                    for (stmt in elifBody) {
                        executeStatement(stmt, variables, logs)
                    }
                    currentBranchExecuted = true
                }
            } else if (tr.startsWith("else:")) {
                i++
                val elseBody = mutableListOf<String>()
                while (i < lines.size) {
                    val l = lines[i]
                    if (l.startsWith("    ") || l.startsWith("\t")) {
                        elseBody.add(l.trim())
                        i++
                    } else break
                }
                if (!currentBranchExecuted) {
                    for (stmt in elseBody) {
                        executeStatement(stmt, variables, logs)
                    }
                    currentBranchExecuted = true
                }
            } else {
                break
            }
        }

        return Pair(i, 0)
    }

    private fun evaluateCondition(condStr: String, variables: Map<String, Any>): Boolean {
        var s = condStr.trim()
        val operators = listOf(">=", "<=", "==", "!=", ">", "<")

        for (op in operators) {
            if (s.contains(op)) {
                val parts = s.split(op, limit = 2)
                val left = evaluateExpression(parts[0].trim(), variables)
                val right = evaluateExpression(parts[1].trim(), variables)
                return when (op) {
                    ">=" -> left >= right
                    "<=" -> left <= right
                    "==" -> abs(left - right) < 1e-6
                    "!=" -> abs(left - right) >= 1e-6
                    ">" -> left > right
                    "<" -> left < right
                    else -> false
                }
            }
        }
        val num = evaluateExpression(s, variables)
        return num != 0.0
    }

    private fun evaluatePrintArgs(argStr: String, variables: Map<String, Any>): String {
        val parts = argStr.split(",")
        val sb = StringBuilder()
        for (part in parts) {
            val p = part.trim()
            if ((p.startsWith("\"") && p.endsWith("\"")) || (p.startsWith("'") && p.endsWith("'"))) {
                sb.append(p.substring(1, p.length - 1)).append(" ")
            } else {
                val numVal = try {
                    evaluateExpression(p, variables)
                } catch (e: Exception) {
                    variables[p]?.toString() ?: p
                }
                if (numVal is Double) {
                    if (numVal == numVal.toLong().toDouble()) {
                        sb.append(numVal.toLong()).append(" ")
                    } else {
                        sb.append(String.format(Locale.US, "%.2f", numVal)).append(" ")
                    }
                } else {
                    sb.append(numVal).append(" ")
                }
            }
        }
        return sb.toString().trim()
    }

    fun evaluateExpression(exprStr: String, variables: Map<String, Any>): Double {
        var expr = exprStr.trim()

        // Handle inline ternary: `val if cond else default`
        if (expr.contains(" if ") && expr.contains(" else ")) {
            val ifPart = expr.substringBefore(" if ")
            val rest = expr.substringAfter(" if ")
            val condPart = rest.substringBefore(" else ")
            val elsePart = rest.substringAfter(" else ")
            return if (evaluateCondition(condPart, variables)) {
                evaluateExpression(ifPart, variables)
            } else {
                evaluateExpression(elsePart, variables)
            }
        }

        // Handle functions round(x, d)
        if (expr.startsWith("round(") && expr.endsWith(")")) {
            val inner = expr.substring(6, expr.length - 1)
            val parts = inner.split(",")
            val valToRound = evaluateExpression(parts[0], variables)
            val decimals = if (parts.size > 1) evaluateExpression(parts[1], variables).toInt() else 0
            val factor = 10.0.pow(decimals)
            return round(valToRound * factor) / factor
        }

        // Handle abs(x)
        if (expr.startsWith("abs(") && expr.endsWith(")")) {
            val inner = expr.substring(4, expr.length - 1)
            return abs(evaluateExpression(inner, variables))
        }

        // Handle sqrt(x)
        if (expr.startsWith("sqrt(") && expr.endsWith(")")) {
            val inner = expr.substring(5, expr.length - 1)
            return sqrt(evaluateExpression(inner, variables))
        }

        // Handle simple mathematical expression parsing
        return SimpleMathParser(expr, variables).parse()
    }

    private class SimpleMathParser(val str: String, val vars: Map<String, Any>) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) {
                // Ignore leftover spaces
                while (ch == ' '.code) nextChar()
            }
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) {
                    if (ch == '*'.code) {
                        // Handle Power **
                        nextChar()
                        x = x.pow(parseFactor())
                    } else {
                        x *= parseFactor()
                    }
                } else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    x = if (divisor != 0.0) x / divisor else 0.0
                } else if (eat('%'.code)) {
                    x %= parseFactor()
                } else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch.toChar().isLetter() || ch == '_'.code || ch > 128) { // unicode for Persian
                while (ch.toChar().isLetterOrDigit() || ch == '_'.code || ch > 128) nextChar()
                val varName = str.substring(startPos, pos)
                val rawVar = vars[varName]
                x = when (rawVar) {
                    is Number -> rawVar.toDouble()
                    is Boolean -> if (rawVar) 1.0 else 0.0
                    else -> 0.0
                }
            } else {
                x = 0.0
            }
            return x
        }
    }
}
