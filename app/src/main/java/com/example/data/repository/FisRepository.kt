package com.example.data.repository

import android.content.Context
import com.example.data.local.CalculationResultEntity
import com.example.data.local.FinancialDataEntity
import com.example.data.local.FisDao
import com.example.data.local.ProjectEntity
import com.example.pdf.PdfExporter
import com.example.python.EvaluationResult
import com.example.python.PythonInterpreter
import com.example.python.PythonTemplates
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import java.io.File

class FisRepository(
    private val dao: FisDao,
    private val context: Context
) {
    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    fun getProject(id: Long): Flow<ProjectEntity?> = dao.getProjectById(id)

    suspend fun createDefaultTemplatesIfEmpty() {
        // Will be called on startup to seed default projects if DB is clean
    }

    suspend fun insertProject(project: ProjectEntity): Long {
        val scriptToUse = project.pythonScript.ifEmpty {
            PythonTemplates.templates.first().script
        }
        val projId = dao.insertProject(project.copy(pythonScript = scriptToUse))

        // Ensure default financial data exists
        val existingData = dao.getFinancialDataByProjectDirect(projId)
        if (existingData == null) {
            dao.insertFinancialData(FinancialDataEntity(projectId = projId))
        }

        return projId
    }

    suspend fun updateProject(project: ProjectEntity) {
        dao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(id: Long) {
        dao.deleteProjectById(id)
        dao.clearResultsForProject(id)
    }

    fun getFinancialData(projectId: Long): Flow<FinancialDataEntity?> {
        return dao.getFinancialDataByProject(projectId)
    }

    suspend fun saveFinancialData(data: FinancialDataEntity) {
        dao.insertFinancialData(data)
    }

    fun getCalculationResults(projectId: Long): Flow<List<CalculationResultEntity>> {
        return dao.getResultsByProject(projectId)
    }

    suspend fun runCalculation(projectId: Long, scriptOverride: String? = null): EvaluationResult {
        val project = dao.getProjectByIdDirect(projectId) ?: return EvaluationResult(
            success = false,
            healthScore = 0,
            healthStatus = "پروژه یافت نشد",
            computedMetrics = emptyMap(),
            computedStrings = emptyMap(),
            logs = listOf("پروژه مورد نظر وجود ندارد."),
            errorMessage = "پروژه یافت نشد"
        )

        var financialData = dao.getFinancialDataByProjectDirect(projectId)
        if (financialData == null) {
            financialData = FinancialDataEntity(projectId = projectId)
            dao.insertFinancialData(financialData)
        }

        val script = scriptOverride ?: project.pythonScript.ifEmpty {
            PythonTemplates.templates.first().script
        }

        // Execute Python script
        val interpreter = PythonInterpreter()
        val evalResult = interpreter.execute(script, financialData)

        if (evalResult.success) {
            // Save result entity to DB
            val metricsJsonObj = JSONObject()
            for ((k, v) in evalResult.computedMetrics) {
                metricsJsonObj.put(k, v)
            }

            val logsStr = evalResult.logs.joinToString("\n")

            val resultEntity = CalculationResultEntity(
                projectId = projectId,
                timestamp = System.currentTimeMillis(),
                healthScore = evalResult.healthScore,
                healthStatus = evalResult.healthStatus,
                scriptUsed = script,
                metricsJson = metricsJsonObj.toString(),
                logsText = logsStr,
                summaryText = "امتیاز سلامت: ${evalResult.healthScore} از ۱۰۰ - وضعیت: ${evalResult.healthStatus}"
            )

            dao.insertCalculationResult(resultEntity)
        }

        return evalResult
    }

    fun generatePdf(project: ProjectEntity, financialData: FinancialDataEntity, result: CalculationResultEntity): File? {
        val exporter = PdfExporter(context)
        return exporter.generatePdfReport(project, financialData, result)
    }

    fun openPdf(file: File) {
        val exporter = PdfExporter(context)
        exporter.openOrSharePdf(file)
    }
}
