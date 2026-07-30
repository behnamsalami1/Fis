package com.example.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CalculationResultEntity
import com.example.data.local.FinancialDataEntity
import com.example.data.local.FisDatabase
import com.example.data.local.ProjectEntity
import com.example.data.repository.FisRepository
import com.example.python.EvaluationResult
import com.example.python.PythonTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.launch
import java.io.File

enum class AppTab {
    PROJECTS,
    PYTHON_EDITOR,
    FINANCIAL_DATA,
    CALCULATIONS,
    PDF_REPORT
}

class FisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FisRepository

    init {
        val dao = FisDatabase.getDatabase(application).fisDao()
        repository = FisRepository(dao, application)
        seedSampleDataIfEmpty()
    }

    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId.asStateFlow()

    val selectedProject: StateFlow<ProjectEntity?> = _selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getProject(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val financialData: StateFlow<FinancialDataEntity?> = _selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getFinancialData(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val calculationResults: StateFlow<List<CalculationResultEntity>> = _selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getCalculationResults(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeTab = MutableStateFlow(AppTab.PROJECTS)
    val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

    private val _lastEvalResult = MutableStateFlow<EvaluationResult?>(null)
    val lastEvalResult: StateFlow<EvaluationResult?> = _lastEvalResult.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    private val _pdfFile = MutableStateFlow<File?>(null)
    val pdfFile: StateFlow<File?> = _pdfFile.asStateFlow()

    private fun seedSampleDataIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = FisDatabase.getDatabase(getApplication()).fisDao()
            val existing = db.getAllProjects()
            // Check if db is empty initially to provide starter sample project
            viewModelScope.launch {
                projects.collect { list ->
                    if (list.isEmpty()) {
                        val sample1 = ProjectEntity(
                            name = "پروژه تحلیل شرکت پتروشیمی (نمونه)",
                            companyName = "پتروشیمی نوری",
                            fiscalYear = "1403",
                            industry = "شیمیایی و پتروشیمی",
                            description = "بررسی نسبت‌های سودآوری، جریان نقد آزاد و سلامت مالی",
                            pythonScript = PythonTemplates.templates[0].script
                        )
                        val id1 = repository.insertProject(sample1)
                        _selectedProjectId.value = id1
                    }
                }
            }
        }
    }

    fun selectProject(id: Long?) {
        _selectedProjectId.value = id
    }

    fun setTab(tab: AppTab) {
        _activeTab.value = tab
    }

    fun createProject(name: String, companyName: String, fiscalYear: String, industry: String, description: String, templateScript: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val scriptToUse = templateScript ?: PythonTemplates.templates.first().script
            val newProj = ProjectEntity(
                name = name,
                companyName = companyName,
                fiscalYear = fiscalYear,
                industry = industry,
                description = description,
                pythonScript = scriptToUse
            )
            val newId = repository.insertProject(newProj)
            _selectedProjectId.value = newId
            _activeTab.value = AppTab.PYTHON_EDITOR
        }
    }

    fun updateProjectScript(script: String) {
        val proj = selectedProject.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProject(proj.copy(pythonScript = script))
        }
    }

    fun updateFinancialData(updated: FinancialDataEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveFinancialData(updated)
        }
    }

    fun togglePinProject(project: ProjectEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProject(project.copy(isPinned = !project.isPinned))
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProject(id)
            if (_selectedProjectId.value == id) {
                _selectedProjectId.value = null
                _activeTab.value = AppTab.PROJECTS
            }
        }
    }

    fun runCalculation(scriptOverride: String? = null) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isCalculating.value = true
            val result = repository.runCalculation(projId, scriptOverride)
            _lastEvalResult.value = result
            _isCalculating.value = false
            _activeTab.value = AppTab.CALCULATIONS
        }
    }

    fun generateAndOpenPdf(result: CalculationResultEntity) {
        val proj = selectedProject.value ?: return
        val fin = financialData.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val file = repository.generatePdf(proj, fin, result)
            _pdfFile.value = file
            if (file != null) {
                repository.openPdf(file)
            } else {
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "خطا در ساخت فایل PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun generateAiPythonScript(promptDescription: String, onGenerated: (String) -> Unit) {
        // AI assistance helper for Python formula generation
        val template = """# --- فرمول تولید شده توسط دستیار هوشمند FIS ---
# توضیحات: $promptDescription

# 1. محاسبه متغیرها
gross_margin = (gross_profit / revenue) * 100
net_margin = (net_income / revenue) * 100
roa = (net_income / total_assets) * 100
roe = (net_income / equity) * 100
current_ratio = current_assets / current_liabilities

# 2. ارزیابی شرطی
health_score = 60
if roe > 20.0:
    health_score += 15
    print("[ممتاز] بازده حقوق صاحبان سهام فوق‌العاده است:", roe, "%")

if current_ratio < 1.0:
    health_score -= 15
    print("[هشدار] ریسک عدم نقدینگی در کوتاه مدت وجود دارد.")

print("محاسبه فرمول اختصاصی با موفقیت به پایان رسید.")
"""
        onGenerated(template)
    }
}
