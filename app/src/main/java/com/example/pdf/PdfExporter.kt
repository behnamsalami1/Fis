package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.data.local.CalculationResultEntity
import com.example.data.local.FinancialDataEntity
import com.example.data.local.ProjectEntity
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExporter(private val context: Context) {

    fun generatePdfReport(
        project: ProjectEntity,
        financialData: FinancialDataEntity,
        result: CalculationResultEntity
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            paint.isAntiAlias = true

            var currentY = 0f

            // 1. Draw Header Bar (Dark Teal)
            paint.color = Color.parseColor("#0F172A")
            canvas.drawRect(0f, 0f, 595f, 90f, paint)

            paint.color = Color.parseColor("#0D9488")
            canvas.drawRect(0f, 85f, 595f, 90f, paint)

            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.isFakeBoldText = true
            canvas.drawText("FIS Trading System - سیستم معاملاتی FIS", 25f, 45f, paint)

            paint.textSize = 13f
            paint.color = Color.parseColor("#CBD5E1")
            canvas.drawText("گزارش تحلیل صورتهای مالی و خروجی محاسبات پایتون", 25f, 70f, paint)

            currentY = 115f

            // 2. Project Information Box
            paint.color = Color.parseColor("#F1F5F9")
            val infoBox = RectF(20f, currentY, 575f, currentY + 110f)
            canvas.drawRoundRect(infoBox, 8f, 8f, paint)

            paint.color = Color.parseColor("#334155")
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("اطلاعات پروژه: ${project.name}", 35f, currentY + 30f, paint)

            paint.textSize = 11f
            paint.isFakeBoldText = false
            paint.color = Color.parseColor("#475569")
            canvas.drawText("نام شرکت / نماد: ${project.companyName.ifEmpty { "تعریف نشده" }}", 35f, currentY + 55f, paint)
            canvas.drawText("سال مالی: ${project.fiscalYear} | صنعت: ${project.industry.ifEmpty { "عمومی" }}", 35f, currentY + 75f, paint)

            val dateStr = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date(result.timestamp))
            canvas.drawText("تاریخ اجرای محاسبات: $dateStr", 35f, currentY + 95f, paint)

            // Health Score Card (Right aligned in box)
            val scoreColor = when {
                result.healthScore >= 80 -> Color.parseColor("#10B981")
                result.healthScore >= 60 -> Color.parseColor("#0D9488")
                result.healthScore >= 45 -> Color.parseColor("#F59E0B")
                else -> Color.parseColor("#EF4444")
            }

            paint.color = scoreColor
            val scoreBox = RectF(430f, currentY + 20f, 560f, currentY + 90f)
            canvas.drawRoundRect(scoreBox, 10f, 10f, paint)

            paint.color = Color.WHITE
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("${result.healthScore} / 100", 450f, currentY + 55f, paint)
            paint.textSize = 11f
            canvas.drawText(result.healthStatus, 450f, currentY + 75f, paint)

            currentY += 135f

            // 3. Calculated Metrics Section Title
            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 15f
            paint.isFakeBoldText = true
            canvas.drawText("نتایج و نسبت‌های مالی استخراج شده از کد پایتون:", 25f, currentY, paint)
            currentY += 15f

            // Table Header
            paint.color = Color.parseColor("#1E293B")
            canvas.drawRect(20f, currentY, 575f, currentY + 25f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11f
            canvas.drawText("نام شاخص / متغیر", 35f, currentY + 17f, paint)
            canvas.drawText("مقدار محاسبه شده", 380f, currentY + 17f, paint)

            currentY += 25f

            // Metrics Items
            val metricsMap = parseMetricsJson(result.metricsJson)
            paint.textSize = 10f
            paint.isFakeBoldText = false

            var isRowAlt = false
            for ((key, value) in metricsMap) {
                if (currentY > 750f) break

                paint.color = if (isRowAlt) Color.parseColor("#F8FAFC") else Color.WHITE
                canvas.drawRect(20f, currentY, 575f, currentY + 22f, paint)

                paint.color = Color.parseColor("#334155")
                canvas.drawText(key, 35f, currentY + 15f, paint)

                paint.color = Color.parseColor("#0D9488")
                paint.isFakeBoldText = true
                canvas.drawText(formatDouble(value), 380f, currentY + 15f, paint)
                paint.isFakeBoldText = false

                currentY += 22f
                isRowAlt = !isRowAlt
            }

            currentY += 20f

            // 4. Financial Statements Data Summary Title
            if (currentY < 720f) {
                paint.color = Color.parseColor("#0F172A")
                paint.textSize = 14f
                paint.isFakeBoldText = true
                canvas.drawText("خلاصه صورتهای مالی ورودی:", 25f, currentY, paint)
                currentY += 15f

                val fnItems = listOf(
                    "درآمد عملیاتی (Revenue)" to financialData.revenue,
                    "سود خالص (Net Income)" to financialData.netIncome,
                    "کل دارایی‌ها (Total Assets)" to financialData.totalAssets,
                    "کل بدهی‌ها (Total Liabilities)" to financialData.totalLiabilities,
                    "جریان نقد عملیاتی (OCF)" to financialData.operatingCashFlow
                )

                for ((label, valNum) in fnItems) {
                    if (currentY > 780f) break
                    paint.color = Color.parseColor("#475569")
                    paint.textSize = 10f
                    paint.isFakeBoldText = false
                    canvas.drawText(label, 35f, currentY, paint)
                    paint.color = Color.parseColor("#0F172A")
                    canvas.drawText(formatDouble(valNum), 380f, currentY, paint)
                    currentY += 18f
                }
            }

            // Footer
            paint.color = Color.parseColor("#94A3B8")
            paint.textSize = 9f
            canvas.drawLine(20f, 810f, 575f, 810f, paint)
            canvas.drawText("صفحه ۱ از ۱ | تولید شده توسط سیستم معاملاتی FIS - نسخه هوشمند", 25f, 825f, paint)

            pdfDocument.finishPage(page)

            // Save File to Storage
            val fileName = "FIS_Report_${project.name}_${System.currentTimeMillis()}.pdf"
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)

            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun parseMetricsJson(jsonStr: String): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        try {
            val json = JSONObject(jsonStr)
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

    private fun formatDouble(valNum: Double): String {
        return if (valNum == valNum.toLong().toDouble()) {
            String.format(Locale.US, "%,d", valNum.toLong())
        } else {
            String.format(Locale.US, "%,.2f", valNum)
        }
    }

    fun openOrSharePdf(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "باز کردن گزارش PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
