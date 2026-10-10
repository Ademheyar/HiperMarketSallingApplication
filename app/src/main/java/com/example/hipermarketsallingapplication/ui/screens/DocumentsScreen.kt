package com.example.hipermarketsallingapplication.ui.screens

import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.SalesDoc
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.DocViewModel
import com.example.hipermarketsallingapplication.ui.viewmodel.UserViewModel

@Composable
fun DocumentsScreen(
    viewModel: DocViewModel,
    userViewModel: UserViewModel? = null
) {
    val docs by viewModel.docs.collectAsState()
    val currentUser = userViewModel?.currentUser?.collectAsState()?.value
    val usersList = userViewModel?.users?.collectAsState()?.value ?: emptyList()
    val userWorkShops = userViewModel?.userWorkingShops?.collectAsState()?.value ?: emptyList()

    val currentDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedSubNotebookTab by remember { mutableStateOf(0) } // 0: List Docs (Doc.py), 1: Report (Doc.py)
    var selectedDocInTable by remember { mutableStateOf<SalesDoc?>(null) }
    var showDocDetailModal by remember { mutableStateOf<SalesDoc?>(null) }

    // Filter Form Inputs matching Doc.py
    var docIdQuery by remember { mutableStateOf("") }
    var docTypeQuery by remember { mutableStateOf("") }
    var docBarcodeQuery by remember { mutableStateOf("") }
    var extensionBarcodeQuery by remember { mutableStateOf("") }
    var itemQuery by remember { mutableStateOf("") }
    var soldItemInfoQuery by remember { mutableStateOf("") }
    var discountQuery by remember { mutableStateOf("") }

    var selectedUserId by remember { mutableStateOf(currentUser?.userName ?: "") }
    var selectedAtShop by remember { mutableStateOf(currentUser?.userShop?.ifBlank { "Main Shop" } ?: "Main Shop") }
    var selectedCustomerId by remember { mutableStateOf("") }
    var selectedSellerId by remember { mutableStateOf("") }

    var createdDateEnabled by remember { mutableStateOf(true) }
    var expireDateEnabled by remember { mutableStateOf(true) }
    var updatedDateEnabled by remember { mutableStateOf(true) }

    var dateFromQuery by remember { mutableStateOf(currentDateStr) }
    var dateToQuery by remember { mutableStateOf(currentDateStr) }

    val userOptions = remember(usersList) { listOf("") + usersList.map { it.userName }.distinct() }
    val shopOptions = remember(userWorkShops) { listOf("All Shops", "Main Shop") + userWorkShops.map { it.name }.distinct() }

    // Filtered Documents matching Doc.py perform_search
    val filteredDocs = remember(
        docs, docIdQuery, docTypeQuery, docBarcodeQuery, extensionBarcodeQuery,
        itemQuery, soldItemInfoQuery, discountQuery, selectedUserId, selectedAtShop,
        selectedCustomerId, selectedSellerId, createdDateEnabled, dateFromQuery, dateToQuery
    ) {
        docs.filter { doc ->
            val matchesId = docIdQuery.isBlank() || doc.id.toString().contains(docIdQuery)
            val matchesType = docTypeQuery.isBlank() || doc.type.contains(docTypeQuery, ignoreCase = true)
            val matchesBarcode = docBarcodeQuery.isBlank() || doc.docBarcode.contains(docBarcodeQuery, ignoreCase = true)
            val matchesExtBarcode = extensionBarcodeQuery.isBlank() || doc.extensionBarcode.contains(extensionBarcodeQuery, ignoreCase = true)
            val matchesItem = itemQuery.isBlank() || doc.item.contains(itemQuery, ignoreCase = true)
            val matchesSoldItem = soldItemInfoQuery.isBlank() || doc.item.contains(soldItemInfoQuery, ignoreCase = true)
            val matchesDiscount = discountQuery.isBlank() || doc.discount.toString().contains(discountQuery)
            val matchesUser = selectedUserId.isBlank() || doc.userId.contains(selectedUserId, ignoreCase = true)
            val matchesShop = selectedAtShop.isBlank() || selectedAtShop == "All Shops" || doc.atShopId.contains(selectedAtShop, ignoreCase = true)
            val matchesCustomer = selectedCustomerId.isBlank() || doc.customerId.contains(selectedCustomerId, ignoreCase = true)
            val matchesSeller = selectedSellerId.isBlank() || doc.sellerId.contains(selectedSellerId, ignoreCase = true)

            val matchesDate = if (createdDateEnabled) {
                (dateFromQuery.isBlank() || doc.docCreatedDate >= dateFromQuery) &&
                        (dateToQuery.isBlank() || doc.docCreatedDate <= dateToQuery)
            } else true

            matchesId && matchesType && matchesBarcode && matchesExtBarcode &&
                    matchesItem && matchesSoldItem && matchesDiscount && matchesUser &&
                    matchesShop && matchesCustomer && matchesSeller && matchesDate
        }
    }

    // Calculated Totals for Header Bar (Doc.py)
    val totalDocCount = filteredDocs.size
    val totalItemCount = filteredDocs.sumOf { it.qty.toInt() }
    val totalGrandTotal = filteredDocs.sumOf { it.price }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top Green Header Tag matching Doc.py
        Surface(
            color = SuccessGreen,
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
        ) {
            Text(
                text = "Documents",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // Search Filter Grid Panel matching Doc.py
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1 Filter Inputs (Doc ID, Type, Barcode, Ext Barcode, Item, Sold Item Info, Discount)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactFilterField(label = "Document ID:", value = docIdQuery, onValueChange = { docIdQuery = it }, width = 110.dp)
                    CompactFilterField(label = "Document Type:", value = docTypeQuery, onValueChange = { docTypeQuery = it }, width = 120.dp)
                    CompactFilterField(label = "Document Barcode:", value = docBarcodeQuery, onValueChange = { docBarcodeQuery = it }, width = 130.dp)
                    CompactFilterField(label = "Extension Barcode:", value = extensionBarcodeQuery, onValueChange = { extensionBarcodeQuery = it }, width = 130.dp)
                    CompactFilterField(label = "Item:", value = itemQuery, onValueChange = { itemQuery = it }, width = 110.dp)
                    CompactFilterField(label = "Sold Item Info:", value = soldItemInfoQuery, onValueChange = { soldItemInfoQuery = it }, width = 120.dp)
                    CompactFilterField(label = "Discount:", value = discountQuery, onValueChange = { discountQuery = it }, width = 100.dp)
                }

                // Row 2 Filter Inputs (User ID, At Shop, Customer ID, Seller ID, Date Checkboxes & Inputs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MoreInfoDropdown(
                        label = "User ID:",
                        value = selectedUserId,
                        options = if (userOptions.contains(selectedUserId)) userOptions else listOf(selectedUserId) + userOptions,
                        onOptionSelected = { selectedUserId = it },
                        modifier = Modifier.width(130.dp)
                    )

                    MoreInfoDropdown(
                        label = "At Shop:",
                        value = selectedAtShop,
                        options = shopOptions,
                        onOptionSelected = { selectedAtShop = it },
                        modifier = Modifier.width(120.dp)
                    )

                    MoreInfoDropdown(
                        label = "Customer ID:",
                        value = selectedCustomerId,
                        options = userOptions,
                        onOptionSelected = { selectedCustomerId = it },
                        modifier = Modifier.width(130.dp)
                    )

                    MoreInfoDropdown(
                        label = "Seller ID:",
                        value = selectedSellerId,
                        options = userOptions,
                        onOptionSelected = { selectedSellerId = it },
                        modifier = Modifier.width(120.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = createdDateEnabled,
                            onCheckedChange = { createdDateEnabled = it },
                            colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent)
                        )
                        Text("Created Date", fontSize = 10.sp, color = TextLight)
                    }

                    CompactFilterField(label = "", value = dateFromQuery, onValueChange = { dateFromQuery = it }, width = 100.dp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = expireDateEnabled,
                            onCheckedChange = { expireDateEnabled = it },
                            colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent)
                        )
                        Text("Expire Date", fontSize = 10.sp, color = TextLight)
                    }

                    CompactFilterField(label = "", value = dateToQuery, onValueChange = { dateToQuery = it }, width = 100.dp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = updatedDateEnabled,
                            onCheckedChange = { updatedDateEnabled = it },
                            colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent)
                        )
                        Text("Updated Date", fontSize = 10.sp, color = TextLight)
                    }
                }

                // Row 3 Blue Action Buttons Bar matching Doc.py (Search, Veiw, Delet, Undo, GetDate)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DocActionButton("Search", DarkBlueAccent, Modifier.weight(1f)) {
                        viewModel.loadDocs()
                    }

                    DocActionButton("Veiw", DarkBlueAccent, Modifier.weight(1f)) {
                        selectedDocInTable?.let { showDocDetailModal = it }
                    }

                    DocActionButton("Delet", EnergyRed, Modifier.weight(1f)) {
                        selectedDocInTable?.let {
                            viewModel.deleteDoc(it.id)
                            selectedDocInTable = null
                        }
                    }

                    DocActionButton("Undo", DarkBlueSecondary, Modifier.weight(1f)) {
                        viewModel.loadDocs()
                    }

                    DocActionButton("GetDate", SuccessGreen, Modifier.weight(1f)) {
                        dateFromQuery = currentDateStr
                        dateToQuery = currentDateStr
                    }
                }
            }
        }

        // User Badge & Sub-Notebook Bar matching Doc.py (AH Adem / List Docs / Report)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = SuccessGreen,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = currentUser?.userName ?: "AH Adem",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            TabRow(
                selectedTabIndex = selectedSubNotebookTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TextLight,
                modifier = Modifier.weight(1f)
            ) {
                Tab(
                    selected = selectedSubNotebookTab == 0,
                    onClick = { selectedSubNotebookTab = 0 }
                ) {
                    Text("List Docs", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp), color = if (selectedSubNotebookTab == 0) SuccessGreen else TextMuted)
                }

                Tab(
                    selected = selectedSubNotebookTab == 1,
                    onClick = { selectedSubNotebookTab = 1 }
                ) {
                    Text("Report", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp), color = if (selectedSubNotebookTab == 1) SuccessGreen else TextMuted)
                }
            }
        }

        // Live Summary Header Line matching Doc.py (TOTAL DOC : X ITEMS : Y COUNTED Grand Total : Z)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL DOC : $totalDocCount   ITEMS : $totalItemCount   COUNTED Grand Total : $${String.format(Locale.getDefault(), "%.2f", totalGrandTotal)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = DarkBlueAccent
                )
            }
        }

        // Sub-Notebook Tab Content
        if (selectedSubNotebookTab == 0) {
            // Data Table Grid matching Doc.py (List Docs)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    // Table Column Headers Row matching Doc.py
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBlueDarker, RoundedCornerShape(4.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCellHeader("ID", 50.dp)
                        TableCellHeader("doc_barcode", 110.dp)
                        TableCellHeader("extension_barcode", 120.dp)
                        TableCellHeader("user_id", 80.dp)
                        TableCellHeader("customer_id", 90.dp)
                        TableCellHeader("Type", 75.dp)
                        TableCellHeader("Items", 140.dp)
                        TableCellHeader("Qty", 55.dp)
                        TableCellHeader("price", 75.dp)
                        TableCellHeader("disc", 60.dp)
                        TableCellHeader("tax", 60.dp)
                        TableCellHeader("Payment", 85.dp)
                        TableCellHeader("doc_created_date", 120.dp)
                        TableCellHeader("doc_expire_date", 120.dp)
                    }

                    HorizontalDivider(color = DarkBlueDarker)

                    if (filteredDocs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No documents recorded.", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(filteredDocs) { doc ->
                                val isSelected = selectedDocInTable?.id == doc.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) DarkBlueAccent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { selectedDocInTable = doc }
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCellValue(doc.id.toString(), 50.dp, isBold = true)
                                    TableCellValue(doc.docBarcode, 110.dp, color = TextLight)
                                    TableCellValue(doc.extensionBarcode.ifBlank { "-" }, 120.dp)
                                    TableCellValue(doc.userId, 80.dp)
                                    TableCellValue(doc.customerId, 90.dp)
                                    TableCellValue(doc.type, 75.dp, color = DarkBlueAccent)
                                    TableCellValue(doc.item, 140.dp)
                                    TableCellValue(doc.qty.toInt().toString(), 55.dp)
                                    TableCellValue("$${String.format(Locale.getDefault(), "%.2f", doc.price)}", 75.dp, color = SuccessGreen, isBold = true)
                                    TableCellValue("$${String.format(Locale.getDefault(), "%.2f", doc.discount)}", 60.dp)
                                    TableCellValue("$${String.format(Locale.getDefault(), "%.2f", doc.tax)}", 60.dp)
                                    TableCellValue(doc.payments, 85.dp)
                                    TableCellValue(doc.docCreatedDate, 120.dp)
                                    TableCellValue(doc.docExpireDate.ifBlank { "-" }, 120.dp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Financial Report Tab matching Doc.py creat_info / home_tab
            val totalRev = filteredDocs.sumOf { it.price }
            val totalProfit = filteredDocs.sumOf { it.profit }
            val totalTax = filteredDocs.sumOf { it.tax }
            val totalDiscounts = filteredDocs.sumOf { it.discount }
            val cashPaid = filteredDocs.filter { it.payments.contains("Cash", ignoreCase = true) }.sumOf { it.price }
            val cardPaid = filteredDocs.filter { it.payments.contains("Card", ignoreCase = true) || it.payments.contains("Visa", ignoreCase = true) }.sumOf { it.price }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📊 Sales & Financial Summary Report (Doc.py)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextLight)
                    HorizontalDivider(color = DarkBlueDarker)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryStat("Total Gross Revenue", "$${String.format(Locale.getDefault(), "%.2f", totalRev)}", SuccessGreen)
                        SummaryStat("Total Documents", "$totalDocCount", DarkBlueAccent)
                        SummaryStat("Estimated Net Profit", "$${String.format(Locale.getDefault(), "%.2f", totalProfit)}", EnergyRed)
                    }

                    HorizontalDivider(color = DarkBlueDarker)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryStat("Cash Paid", "$${String.format(Locale.getDefault(), "%.2f", cashPaid)}", TextLight)
                        SummaryStat("Card Paid", "$${String.format(Locale.getDefault(), "%.2f", cardPaid)}", TextLight)
                        SummaryStat("Total Discounts", "$${String.format(Locale.getDefault(), "%.2f", totalDiscounts)}", TextMuted)
                        SummaryStat("Total Tax", "$${String.format(Locale.getDefault(), "%.2f", totalTax)}", TextMuted)
                    }
                }
            }
        }
    }

    // Document View Detail Modal (Doc.py perform_veiw)
    showDocDetailModal?.let { doc ->
        AlertDialog(
            onDismissRequest = { showDocDetailModal = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = DarkBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Document #${doc.docBarcode}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Document ID: ${doc.id}", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 12.sp)
                    Text("Type: ${doc.type}", fontWeight = FontWeight.Bold, color = DarkBlueAccent, fontSize = 13.sp)
                    Text("Date Created: ${doc.docCreatedDate}", color = TextMuted, fontSize = 12.sp)
                    Text("Customer ID: ${doc.customerId}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Text("User/Seller ID: ${doc.userId} / ${doc.sellerId}", color = TextMuted, fontSize = 12.sp)
                    Text("At Shop ID: ${doc.atShopId}", color = TextMuted, fontSize = 12.sp)
                    Text("Items Summary: ${doc.item}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    HorizontalDivider(color = DarkBlueDarker)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:", color = TextMuted, fontSize = 12.sp)
                        Text("$${String.format(Locale.getDefault(), "%.2f", doc.price)}", color = TextLight, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount:", color = TextMuted, fontSize = 12.sp)
                        Text("$${String.format(Locale.getDefault(), "%.2f", doc.discount)}", color = TextMuted, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Price:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("$${String.format(Locale.getDefault(), "%.2f", doc.price - doc.discount)}", color = EnergyRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text("Payment Method: ${doc.payments}", color = SuccessGreen, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDocDetailModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CompactFilterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    width: androidx.compose.ui.unit.Dp
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = if (label.isNotBlank()) { { Text(label, fontSize = 8.sp, color = TextMuted) } } else null,
        modifier = Modifier
            .width(width)
            .defaultMinSize(minHeight = 52.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = DarkBlueAccent,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
    )
}

@Composable
fun DocActionButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(38.dp)
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
fun TableCellHeader(
    text: String,
    width: androidx.compose.ui.unit.Dp
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = DarkBlueAccent,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun TableCellValue(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    color: Color = TextLight,
    isBold: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        fontSize = 11.sp,
        color = color,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
