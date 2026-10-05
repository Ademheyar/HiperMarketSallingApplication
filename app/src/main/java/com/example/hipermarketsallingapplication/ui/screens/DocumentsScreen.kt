package com.example.hipermarketsallingapplication.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.SalesDoc
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.DocViewModel

@Composable
fun DocumentsScreen(viewModel: DocViewModel) {
    val docs by viewModel.docs.collectAsState()
    val summary by viewModel.summary.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var selectedDocForDetail by remember { mutableStateOf<SalesDoc?>(null) }
    var dateSearchQuery by remember { mutableStateOf("") }

    val filteredDocs = remember(docs, dateSearchQuery) {
        if (dateSearchQuery.isBlank()) {
            docs
        } else {
            docs.filter {
                it.docCreatedDate.contains(dateSearchQuery, ignoreCase = true) ||
                        it.docBarcode.contains(dateSearchQuery, ignoreCase = true) ||
                        it.customerId.contains(dateSearchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        // Tab row: Sales History vs End of Day Summary
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardBackground,
            contentColor = TextLight,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Manager Documents (${docs.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("End of Day Summary & Stats", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // Manager Documents Panel with Date Searching
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Date Search Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dateSearchQuery,
                            onValueChange = { dateSearchQuery = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Search Documents by Date (e.g. YYYY-MM-DD or receipt code)", color = TextMuted, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = DarkBlueAccent) },
                            trailingIcon = {
                                if (dateSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { dateSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkBlueAccent,
                                unfocusedBorderColor = CardBackground,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            singleLine = true
                        )

                        IconButton(onClick = { viewModel.loadDocs() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = DarkBlueAccent)
                        }
                    }

                    HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(bottom = 8.dp))

                    if (filteredDocs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (dateSearchQuery.isBlank()) "No sales receipts recorded yet." else "No documents found matching \"$dateSearchQuery\"",
                                color = TextMuted
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredDocs) { doc ->
                                SalesDocRow(
                                    doc = doc,
                                    onView = { selectedDocForDetail = doc },
                                    onDelete = { viewModel.deleteDoc(doc.id) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // End of Day Summary Card
            summary?.let { s ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Daily Sales Summary Report",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SummaryStat(title = "Total Gross Revenue", value = "$${String.format("%.2f", s.totalRevenue)}", color = SuccessGreen)
                                SummaryStat(title = "Total Receipt Count", value = "${s.totalSalesCount}", color = DarkBlueAccent)
                                SummaryStat(title = "Total Estimated Profit", value = "$${String.format("%.2f", s.totalProfit)}", color = EnergyRed)
                            }
                        }
                    }
                }
            }
        }
    }

    // Document Detail Modal
    selectedDocForDetail?.let { doc ->
        AlertDialog(
            onDismissRequest = { selectedDocForDetail = null },
            containerColor = CardBackground,
            title = {
                Text("Document #${doc.docBarcode}", color = TextLight, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Date: ${doc.docCreatedDate}", color = TextMuted, fontSize = 12.sp)
                    Text("Customer: ${doc.customerId}", color = TextLight, fontWeight = FontWeight.SemiBold)
                    Text("Items: ${doc.item}", color = TextLight, fontSize = 13.sp)
                    Text("Total Price: $${String.format("%.2f", doc.price)}", color = EnergyRed, fontWeight = FontWeight.Bold)
                    Text("Payment Method: ${doc.payments}", color = SuccessGreen, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedDocForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SummaryStat(
    title: String,
    value: String,
    color: Color
) {
    Column {
        Text(title, color = TextMuted, fontSize = 11.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun SalesDocRow(
    doc: SalesDoc,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(doc.docBarcode, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(doc.docCreatedDate, color = TextMuted, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Customer: ${doc.customerId} • Items: ${doc.item}", color = TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$${String.format("%.2f", doc.price)}",
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onView) {
                    Icon(Icons.Default.Visibility, contentDescription = "View", tint = DarkBlueAccent)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EnergyRed)
                }
            }
        }
    }
}
