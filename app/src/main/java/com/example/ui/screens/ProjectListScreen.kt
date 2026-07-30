package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProjectEntity
import com.example.python.PythonTemplates
import com.example.ui.theme.DarkTealPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    projects: List<ProjectEntity>,
    selectedProjectId: Long?,
    onSelectProject: (Long) -> Unit,
    onCreateProject: (name: String, company: String, year: String, industry: String, desc: String, templateScript: String?) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onTogglePinProject: (ProjectEntity) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableIntStateOf(0) } // 0: همه, 1: ایجادشده توسط شما, 2: اشتراک‌گذاری‌شده با شما

    val filteredProjects = remember(projects, searchQuery, activeFilter) {
        var result = if (searchQuery.isBlank()) projects
        else projects.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.companyName.contains(searchQuery, ignoreCase = true) ||
            it.industry.contains(searchQuery, ignoreCase = true)
        }

        if (activeFilter == 2) {
            // Shared filter sample
            result = result.filter { it.description.contains("اشتراک") }
        }
        result
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Surface(
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "پروژه‌ها",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "جدید",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar (Matching screenshot 1)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                placeholder = { Text("جستجوی پروژه‌ها", color = Color(0xFF64748B), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFF94A3B8))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF334155),
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips (Matching screenshot 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeFilter == 0,
                    onClick = { activeFilter = 0 },
                    label = { Text("همه", fontSize = 12.sp) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF334155),
                        selectedLabelColor = Color.White,
                        containerColor = Color.Transparent,
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null
                )

                FilterChip(
                    selected = activeFilter == 1,
                    onClick = { activeFilter = 1 },
                    label = { Text("ایجادشده توسط شما", fontSize = 12.sp) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF334155),
                        selectedLabelColor = Color.White,
                        containerColor = Color.Transparent,
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null
                )

                FilterChip(
                    selected = activeFilter == 2,
                    onClick = { activeFilter = 2 },
                    label = { Text("اشتراک‌گذاری‌شده با شما", fontSize = 12.sp) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF334155),
                        selectedLabelColor = Color.White,
                        containerColor = Color.Transparent,
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "نام",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "هیچ پروژه‌ای وجود ندارد" else "پروژه‌ای پیدا نشد",
                            fontSize = 14.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "برای افزودن، دکمه 'جدید' را بزنید.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredProjects, key = { it.id }) { project ->
                        ProjectItemRow(
                            project = project,
                            isSelected = selectedProjectId == project.id,
                            onSelect = { onSelectProject(project.id) },
                            onDelete = { onDeleteProject(project.id) },
                            onTogglePin = { onTogglePinProject(project) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, company, year, industry, desc, script ->
                onCreateProject(name, company, year, industry, desc, script)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ProjectItemRow(
    project: ProjectEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val formattedDate = remember(project.createdAt) {
        val sdf = SimpleDateFormat("dd MMMM", Locale("fa"))
        sdf.format(Date(project.createdAt))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Folder Icon Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = project.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (project.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin",
                        tint = if (project.isPinned) GoldAccent else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Slate800)
                    ) {
                        DropdownMenuItem(
                            text = { Text("ورود به پروژه", color = Color.White, fontSize = 12.sp) },
                            onClick = {
                                showMenu = false
                                onSelect()
                            },
                            leadingIcon = { Icon(Icons.Default.ArrowForward, contentDescription = null, tint = GoldAccent) }
                        )

                        DropdownMenuItem(
                            text = { Text("حذف پروژه", color = Color(0xFFEF4444), fontSize = 12.sp) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, company: String, year: String, industry: String, desc: String, script: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("1403") }
    var industry by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var initialPythonCode by remember { mutableStateOf(PythonTemplates.templates[0].script) }
    var selectedTemplateIndex by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate800,
        title = {
            Text("ایجاد پروژه مالی جدید", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .background(Slate800),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام پروژه (مثال: phayton تحلیل شرکتهای تولیدی) *", color = Color(0xFFCBD5E1), fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("نام نماد / شرکت", color = Color(0xFFCBD5E1), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("سال مالی", color = Color(0xFFCBD5E1), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.width(90.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Text("کادر دستورات اولیه پایتون و فرمول‌ها:", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = initialPythonCode,
                    onValueChange = { initialPythonCode = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8)
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("انتخاب فرمول‌های پیش‌فرض آماده:", fontSize = 11.sp, color = Color(0xFF94A3B8))

                PythonTemplates.templates.forEachIndexed { idx, tmpl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTemplateIndex = idx
                                initialPythonCode = tmpl.script
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTemplateIndex == idx,
                            onClick = {
                                selectedTemplateIndex = idx
                                initialPythonCode = tmpl.script
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                        )
                        Text(tmpl.title, fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, company, year, industry, desc, initialPythonCode)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
            ) {
                Text("ایجاد و اجرای دستورات", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = Color(0xFFCBD5E1))
            }
        }
    )
}

