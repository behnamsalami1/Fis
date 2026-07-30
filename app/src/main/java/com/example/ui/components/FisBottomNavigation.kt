package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppTab

@Composable
fun FisBottomNavigation(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    Surface(
        color = Color(0xFF0F172A),
        tonalElevation = 8.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationBarItem(
                selected = currentTab == AppTab.PROJECTS,
                onClick = { onTabSelected(AppTab.PROJECTS) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "پروژه‌ها",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = "پروژه‌ها",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = Color(0xFF334155),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                )
            )
        }
    }
}

