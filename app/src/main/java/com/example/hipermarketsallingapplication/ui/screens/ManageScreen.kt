package com.example.hipermarketsallingapplication.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.*

enum class ManageSubDestination(val label: String, val icon: ImageVector, val description: String) {
    INVENTORY("Inventory", Icons.Default.Inventory, "Manage products, stock & catalog"),
    STAFF("Staff", Icons.Default.Group, "Manage staff users & access control"),
    DOCUMENTS("Documents", Icons.Default.ReceiptLong, "Sales history, receipts & invoices"),
    TOOLS("Tools", Icons.Default.Build, "POS utilities & system tools"),
    PROMOTIONS("Promotions", Icons.Default.LocalOffer, "Actions, discounts & special offers")
}

@Composable
fun ManageScreen(
    productViewModel: ProductViewModel,
    docViewModel: DocViewModel,
    userViewModel: UserViewModel,
    settingsViewModel: SettingsViewModel,
    initialSubDestination: ManageSubDestination = ManageSubDestination.INVENTORY
) {
    var selectedSubDestination by remember { mutableStateOf(initialSubDestination) }
    var isPanelCollapsed by remember { mutableStateOf(true) } // Collapsed to icon-size by default to maximize right display space

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left Notepad / Sidebar Navigation List (Icon-Sized when collapsed)
        Card(
            modifier = Modifier
                .width(if (isPanelCollapsed) 68.dp else 240.dp)
                .fillMaxHeight()
                .animateContentSize(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = if (isPanelCollapsed) Alignment.CenterHorizontally else Alignment.Start
            ) {
                // Header with Toggle Expand/Collapse Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isPanelCollapsed) Arrangement.Center else Arrangement.SpaceBetween
                ) {
                    if (!isPanelCollapsed) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = "Manage Panel",
                                tint = DarkBlueAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Manage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                        }
                    }

                    IconButton(
                        onClick = { isPanelCollapsed = !isPanelCollapsed },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPanelCollapsed) Icons.Default.Menu else Icons.Default.MenuOpen,
                            contentDescription = if (isPanelCollapsed) "Expand Panel" else "Collapse Panel",
                            tint = DarkBlueAccent
                        )
                    }
                }

                HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(bottom = 8.dp))

                // List of Notepad Sub-Destinations
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = if (isPanelCollapsed) Alignment.CenterHorizontally else Alignment.Start
                ) {
                    items(ManageSubDestination.entries) { item ->
                        val isSelected = selectedSubDestination == item
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSubDestination = item
                                    isPanelCollapsed = true // Automatically icon-size on selection to maximize display area
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DarkBlueAccent else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isPanelCollapsed) {
                                // Icon-only compact item
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) TextLight else DarkBlueAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                // Full item layout
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) TextLight else DarkBlueAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.label,
                                            fontWeight = FontWeight.Bold,
                                            color = TextLight,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = item.description,
                                            color = if (isSelected) TextLight.copy(alpha = 0.8f) else TextMuted,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right Main Content Display (Maximized Space)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (selectedSubDestination) {
                ManageSubDestination.INVENTORY -> ProductsScreen(viewModel = productViewModel)
                ManageSubDestination.STAFF -> UsersScreen(viewModel = userViewModel)
                ManageSubDestination.DOCUMENTS -> DocumentsScreen(viewModel = docViewModel)
                ManageSubDestination.TOOLS -> ToolsScreen()
                ManageSubDestination.PROMOTIONS -> PromotionsScreen()
            }
        }
    }
}

@Composable
fun ToolsScreen() {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("POS System Utilities & Tools", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextLight)
            Divider(color = DarkBlueDarker)
            Text("• Thermal Printer Diagnostics & Test Print", color = TextMuted, fontSize = 14.sp)
            Text("• Barcode Scanner Calibration", color = TextMuted, fontSize = 14.sp)
            Text("• Database Backup & Cloud Sync Test", color = TextMuted, fontSize = 14.sp)
            Text("• Cash Drawer Remote Kick Test", color = TextMuted, fontSize = 14.sp)
        }
    }
}

@Composable
fun PromotionsScreen() {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Active Promotions & Discounts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextLight)
            Divider(color = DarkBlueDarker)
            Text("• Happy Hour 10% Off Storewide", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("• Buy 2 Get 1 Free on Selected Beverages", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("• VIP Member Double Loyalty Points", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
