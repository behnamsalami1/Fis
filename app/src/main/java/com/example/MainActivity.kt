package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FisBottomNavigation
import com.example.ui.screens.*
import com.example.ui.theme.FisTradingTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.FisViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FisTradingTheme(darkTheme = true) {
                // Persian / RTL Layout Direction
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    FisAppMainScreen()
                }
            }
        }
    }
}

@Composable
fun FisAppMainScreen(
    viewModel: FisViewModel = viewModel()
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val selectedProjectId by viewModel.selectedProjectId.collectAsStateWithLifecycle()
    val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
    val financialData by viewModel.financialData.collectAsStateWithLifecycle()
    val lastEvalResult by viewModel.lastEvalResult.collectAsStateWithLifecycle()
    val currentTab by viewModel.activeTab.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            FisBottomNavigation(
                currentTab = currentTab,
                onTabSelected = {
                    viewModel.selectProject(null)
                    viewModel.setTab(AppTab.PROJECTS)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentProject = selectedProject

            AnimatedContent(
                targetState = currentProject != null,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "workspaceTransition"
            ) { isProjectSelected ->
                if (isProjectSelected && currentProject != null) {
                    ProjectWorkspaceScreen(
                        project = currentProject,
                        financialData = financialData,
                        lastResult = lastEvalResult,
                        onBackToProjects = { viewModel.selectProject(null) },
                        onSaveScript = { script -> viewModel.updateProjectScript(script) },
                        onRunCalculation = { script -> viewModel.runCalculation(script) },
                        onSaveFinancialData = { data -> viewModel.updateFinancialData(data) },
                        onExportPdf = { res -> viewModel.generateAndOpenPdf(res) }
                    )
                } else {
                    ProjectListScreen(
                        projects = projects,
                        selectedProjectId = selectedProjectId,
                        onSelectProject = { id -> viewModel.selectProject(id) },
                        onCreateProject = { name, company, year, ind, desc, script ->
                            viewModel.createProject(name, company, year, ind, desc, script)
                        },
                        onDeleteProject = { id -> viewModel.deleteProject(id) },
                        onTogglePinProject = { proj -> viewModel.togglePinProject(proj) }
                    )
                }
            }
        }
    }
}

