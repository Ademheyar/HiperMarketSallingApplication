package com.example.hipermarketsallingapplication.ui.screens

import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.User
import com.example.hipermarketsallingapplication.ui.auth.LocationData
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.DocViewModel
import com.example.hipermarketsallingapplication.ui.viewmodel.SettingsViewModel
import com.example.hipermarketsallingapplication.ui.viewmodel.UserViewModel

/**
 * ShopSettingsScreen for Manager Setting Panel
 * Encompasses:
 * 1. Shop Info & Preferences (Shop_Setting.py)
 * 2. Shop Expenses Manager (Shop_Expenses.py)
 * 3. Shop Workers & Security Access Control (Shop_Workers.py)
 * 4. App & Printer Preferences (Setting.py)
 */
@Composable
fun ShopSettingsScreen(
    viewModel: SettingsViewModel,
    userViewModel: UserViewModel? = null,
    docViewModel: DocViewModel? = null
) {
    var mainSettingsTab by remember { mutableStateOf(0) } // 0: Shop Info (Shop_Setting.py), 1: Expenses (Shop_Expenses.py), 2: Workers & Access (Shop_Workers.py), 3: App & Printer (Setting.py)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Manager Settings Sub-Tabs matching Shop_Setting.py, Shop_Expenses.py, Shop_Workers.py, Setting.py
        TabRow(
            selectedTabIndex = mainSettingsTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = TextLight
        ) {
            Tab(selected = mainSettingsTab == 0, onClick = { mainSettingsTab = 0 }) {
                Text("🏬 Shop Info", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            }
            Tab(selected = mainSettingsTab == 1, onClick = { mainSettingsTab = 1 }) {
                Text("💸 Expenses", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            }
            Tab(selected = mainSettingsTab == 2, onClick = { mainSettingsTab = 2 }) {
                Text("👥 Workers & Access", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            }
            Tab(selected = mainSettingsTab == 3, onClick = { mainSettingsTab = 3 }) {
                Text("⚙️ Printer & Preferences", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        when (mainSettingsTab) {
            0 -> ShopInfoSettingsTab(userViewModel = userViewModel)
            1 -> ShopExpensesSettingsTab()
            2 -> ShopWorkersSettingsTab(userViewModel = userViewModel)
            3 -> AppAndPrinterSettingsTab(viewModel = viewModel)
        }
    }
}
