package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProjectEntity
import com.example.python.PythonTemplates
import com.example.ui.theme.CodeBg
import com.example.ui.theme.DarkTealPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PythonEditorScreen(
    project: ProjectEntity?,
    onSaveScript: (String) -> Unit,
    onRunCalculation: (String) -> Unit,
    onAiGenerateScript: (prompt: String, onResult: (String) -> Unit) -> Unit
) {
    if (project == null) return

    var scriptText by remember(project.id) { mutableStateOf(project.pythonScript) }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }

    val lineCount = remember(scriptText) {
        scriptText.lines().size.coerceAtLeast(1)
    }

    val snippets = listOf(
        "حاشیه سود ناخالص" to "gross_margin = (gross_profit / revenue) * 100\n",
        "حاشیه سود خالص" to "net_margin = (net_income / revenue) * 100\n",
        "نسبت جاری" to "current_ratio = current_assets / current_liabilities\n",
        "بازده حقوق صاحبان سهام (ROE)" to "roe = (net_income / equity) * 100\n",
        "نسبت P/E" to "pe_ratio = stock_price / (net_income / shares_count)\n",
        "شرط سلامت مالی" to "if net_margin > 15:\n    health_score += 15\n    print('[عالی] سودآوری بالاست')\n"
    )

    Scaffold(
        containerColor = Slate900,
        topBar = {
            Surface(
                color = Slate800,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ویرایشگر دستورالعمل پایتون",
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
                            onClick = { showAiDialog = true },
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldAccent)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("دستیار فرمول", fontSize = 11.sp, color = GoldAccent)
                        }

                        IconButton(onClick = { showTemplatesDialog = true }) {
                            Icon(Icons.Default.LibraryBooks, contentDescription = "Templates", tint = Color.White)
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
        ) {
            // Snippets Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "افزودن فرمول سریع:",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                snippets.forEach { (label, code) ->
                    SuggestionChip(
                        onClick = { scriptText += "\n" + code },
                        label = { Text(label, fontSize = 11.sp, color = Color.White) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DarkTealPrimary.copy(alpha = 0.3f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkTealPrimary)
                    )
                }
            }

            // Main Code Editor Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CodeBg)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Line numbers column
                    Column(
                        modifier = Modifier
                            .width(40.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF0F172A))
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (i in 1..lineCount) {
                            Text(
                                text = "$i",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    // Main TextField for Code
                    TextField(
                        value = scriptText,
                        onValueChange = { scriptText = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CodeBg),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color(0xFF38BDF8)
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CodeBg,
                            unfocusedContainerColor = CodeBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GoldAccent
                        )
                    )
                }
            }

            // Action Bar
            Surface(
                color = Slate800,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onSaveScript(scriptText) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره کد پایتون", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onSaveScript(scriptText)
                            onRunCalculation(scriptText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره و اجرای محاسبات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showTemplatesDialog) {
        AlertDialog(
            onDismissRequest = { showTemplatesDialog = false },
            containerColor = Slate800,
            title = { Text("انتخاب قالب پایتون آماده", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PythonTemplates.templates.forEach { tmpl ->
                        Card(
                            onClick = {
                                scriptText = tmpl.script
                                showTemplatesDialog = false
                            },
                            colors = CardDefaults.cardColors(containerColor = Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(tmpl.title, fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(tmpl.description, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTemplatesDialog = false }) {
                    Text("انصراف", color = Color(0xFFCBD5E1))
                }
            }
        )
    }

    if (showAiDialog) {
        var aiPrompt by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAiDialog = false },
            containerColor = Slate800,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("دستیار فرمول‌نویسی پایتون", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "محاسبه یا الگوریتم مورد نظر خود را به فارسی بنویسید تا کد پایتون آن ساخته شود:",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        placeholder = { Text("مثال: محاسبه نسبت سود خالص و وضعیت بدهی به حقوق صاحبان سهام", fontSize = 12.sp, color = Color(0xFF64748B)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (aiPrompt.isNotBlank()) {
                            onAiGenerateScript(aiPrompt) { generated ->
                                scriptText += "\n" + generated
                                showAiDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("تولید فرمول پایتون", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiDialog = false }) {
                    Text("انصراف", color = Color(0xFFCBD5E1))
                }
            }
        )
    }
}
