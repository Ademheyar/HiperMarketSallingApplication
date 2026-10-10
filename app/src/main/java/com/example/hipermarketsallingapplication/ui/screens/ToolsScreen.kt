package com.example.hipermarketsallingapplication.ui.screens

import java.util.Locale
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.PaymentTool
import com.example.hipermarketsallingapplication.ui.theme.*

/**
 * ToolsScreen matching Tools.py
 * Manages Payment Tools (Cash, Card, Credit, Cashout, Cashin) & System Utilities.
 */
@Composable
fun ToolsScreen() {
    val sampleTools = remember {
        mutableStateListOf(
            PaymentTool(id = 1, shopId = "1", shopName = "Main Shop", name = "Cash Payment", method = "CASH", code = "PAY-01", shortKey = "F10", accessKey = "101", enabled = true, quickPay = true, openDrawer = true, printSlip = true, changeAllowed = true),
            PaymentTool(id = 2, shopId = "1", shopName = "Main Shop", name = "Card POS Terminal", method = "CARD", code = "PAY-02", shortKey = "F11", accessKey = "102", enabled = true, quickPay = true, openDrawer = false, printSlip = true, changeAllowed = false),
            PaymentTool(id = 3, shopId = "1", shopName = "Main Shop", name = "Store Credit Sale", method = "CREADIT", code = "PAY-03", shortKey = "F12", accessKey = "103", enabled = true, quickPay = false, customerRequired = true, openDrawer = false, printSlip = true, changeAllowed = false),
            PaymentTool(id = 4, shopId = "1", shopName = "Main Shop", name = "Petty Cash Draw Out", method = "CASHOUT", code = "PAY-04", shortKey = "CTRL+D", accessKey = "104", enabled = true, quickPay = false, openDrawer = true, printSlip = true, changeAllowed = false)
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedToolInTable by remember { mutableStateOf<PaymentTool?>(null) }
    var showToolEditor by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    // Filtered tools list matching Tools.py search_tools
    val filteredTools = remember(sampleTools, searchQuery) {
        if (searchQuery.isBlank()) {
            sampleTools
        } else {
            sampleTools.filter { t ->
                t.name.contains(searchQuery, ignoreCase = true) ||
                        t.method.contains(searchQuery, ignoreCase = true) ||
                        t.code.contains(searchQuery, ignoreCase = true) ||
                        t.shortKey.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Tool Form State variables matching Tools.py
    var editId by remember { mutableStateOf(0L) }
    var editShopId by remember { mutableStateOf("1") }
    var editShopName by remember { mutableStateOf("Main Shop") }
    var editName by remember { mutableStateOf("") }
    var editMethod by remember { mutableStateOf("CASH") }
    var editCode by remember { mutableStateOf("") }
    var editShortKey by remember { mutableStateOf("") }
    var editAccessKey by remember { mutableStateOf("") }
    var editEnabled by remember { mutableStateOf(true) }
    var editQuickPay by remember { mutableStateOf(true) }
    var editMarkPad by remember { mutableStateOf(false) }
    var editCustomerRequired by remember { mutableStateOf(false) }
    var editOpenDrawer by remember { mutableStateOf(true) }
    var editPrintSlip by remember { mutableStateOf(true) }
    var editChangeAllowed by remember { mutableStateOf(true) }

    fun populateToolEditor(t: PaymentTool) {
        editId = t.id
        editShopId = t.shopId
        editShopName = t.shopName
        editName = t.name
        editMethod = t.method
        editCode = t.code
        editShortKey = t.shortKey
        editAccessKey = t.accessKey
        editEnabled = t.enabled
        editQuickPay = t.quickPay
        editMarkPad = t.markPad
        editCustomerRequired = t.customerRequired
        editOpenDrawer = t.openDrawer
        editPrintSlip = t.printSlip
        editChangeAllowed = t.changeAllowed
    }

    fun clearToolEditor() {
        editId = 0L
        editShopId = "1"
        editShopName = "Main Shop"
        editName = ""
        editMethod = "CASH"
        editCode = ""
        editShortKey = ""
        editAccessKey = ""
        editEnabled = true
        editQuickPay = true
        editMarkPad = false
        editCustomerRequired = false
        editOpenDrawer = true
        editPrintSlip = true
        editChangeAllowed = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Search & Action Buttons Bar matching Tools.py (search_frame)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Payment Tools (Tools.py)...", fontSize = 9.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkBlueAccent) },
                    modifier = Modifier.width(220.dp).defaultMinSize(minHeight = 52.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )

                // Add New button matching Tools.py
                Button(
                    onClick = {
                        clearToolEditor()
                        isEditMode = false
                        showToolEditor = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Change button matching Tools.py
                Button(
                    onClick = {
                        selectedToolInTable?.let { t ->
                            populateToolEditor(t)
                            isEditMode = true
                            showToolEditor = true
                        }
                    },
                    enabled = selectedToolInTable != null,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Delete button matching Tools.py
                Button(
                    onClick = {
                        selectedToolInTable?.let { t ->
                            sampleTools.removeAll { it.id == t.id }
                            selectedToolInTable = null
                            showToolEditor = false
                        }
                    },
                    enabled = selectedToolInTable != null,
                    colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Main Payment Tools Data Table Grid matching Tools.py (list_box)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                Text(
                    text = "Payment & Cash Drawer Tools (${filteredTools.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkBlueAccent,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Table Column Headers Row matching Tools.py
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBlueDarker, RoundedCornerShape(4.dp))
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCellHeader("Shop ID", 60.dp)
                    TableCellHeader("Shop Name", 100.dp)
                    TableCellHeader("Tool Name", 140.dp)
                    TableCellHeader("Tool Method", 100.dp)
                    TableCellHeader("Tool ID", 80.dp)
                    TableCellHeader("Tool Short cut", 100.dp)
                    TableCellHeader("Tool Acsess key", 110.dp)
                    TableCellHeader("Tool enabel", 85.dp)
                    TableCellHeader("Tool Quick_pay", 100.dp)
                    TableCellHeader("Tool Markpad", 90.dp)
                    TableCellHeader("Tool Customer_required", 130.dp)
                    TableCellHeader("Tool Open_drower", 110.dp)
                    TableCellHeader("Tool Printslip", 100.dp)
                    TableCellHeader("Change Allowed", 110.dp)
                }

                HorizontalDivider(color = DarkBlueDarker)

                if (filteredTools.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No payment tools configured.", color = TextMuted, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filteredTools) { tool ->
                            val isSelected = selectedToolInTable?.id == tool.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) DarkBlueAccent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { selectedToolInTable = tool }
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCellValue(tool.shopId, 60.dp, isBold = true)
                                TableCellValue(tool.shopName, 100.dp)
                                TableCellValue(tool.name, 140.dp, color = TextLight, isBold = true)
                                TableCellValue(tool.method, 100.dp, color = DarkBlueAccent)
                                TableCellValue(tool.code, 80.dp)
                                TableCellValue(tool.shortKey, 100.dp)
                                TableCellValue(tool.accessKey, 110.dp)
                                TableCellValue(if (tool.enabled) "1" else "0", 85.dp, color = if (tool.enabled) SuccessGreen else EnergyRed)
                                TableCellValue(if (tool.quickPay) "1" else "0", 100.dp)
                                TableCellValue(if (tool.markPad) "1" else "0", 90.dp)
                                TableCellValue(if (tool.customerRequired) "1" else "0", 130.dp)
                                TableCellValue(if (tool.openDrawer) "1" else "0", 110.dp)
                                TableCellValue(if (tool.printSlip) "1" else "0", 100.dp)
                                TableCellValue(if (tool.changeAllowed) "1" else "0", 110.dp)
                            }
                        }
                    }
                }
            }
        }

        // Tool Details & Editing Subpanel matching Tools.py (details_frame)
        if (showToolEditor) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditMode) "Edit Payment Tool (Tools.py)" else "Add Payment Tool (Tools.py)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DarkBlueAccent
                        )
                        IconButton(onClick = { showToolEditor = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider(color = DarkBlueDarker)

                    // Line 1: Tool Name & Tool ID
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Tool Name:", fontSize = 9.sp, color = TextMuted) },
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = editCode,
                            onValueChange = { editCode = it },
                            label = { Text("Tool ID:", fontSize = 9.sp, color = TextMuted) },
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Line 2: Tool Method Dropdown & Tool Shortcut
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MoreInfoDropdown(
                            label = "Tool Method:",
                            value = editMethod,
                            options = listOf("CASH", "CARD", "CREADIT", "CASHOUT", "CASHIN", "OTHER"),
                            onOptionSelected = { editMethod = it },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editShortKey,
                            onValueChange = { editShortKey = it },
                            label = { Text("Tool Short cut:", fontSize = 9.sp, color = TextMuted) },
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Line 3: Tool Access key
                    OutlinedTextField(
                        value = editAccessKey,
                        onValueChange = { editAccessKey = it },
                        label = { Text("Tool Acsess key:", fontSize = 9.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 54.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    // Checkboxes Grid matching Tools.py
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = editEnabled, onCheckedChange = { editEnabled = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                            Text("Tool enabel:", fontSize = 11.sp, color = TextLight)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = editQuickPay, onCheckedChange = { editQuickPay = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                            Text("Tool Quick payment:", fontSize = 11.sp, color = TextLight)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = editCustomerRequired, onCheckedChange = { editCustomerRequired = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                            Text("Tool Customer Required:", fontSize = 11.sp, color = TextLight)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = editPrintSlip, onCheckedChange = { editPrintSlip = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                            Text("Tool Print Receipt:", fontSize = 11.sp, color = TextLight)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = editChangeAllowed, onCheckedChange = { editChangeAllowed = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                            Text("Change Allowed:", fontSize = 11.sp, color = TextLight)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = editOpenDrawer, onCheckedChange = { editOpenDrawer = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                            Text("Tool Open Cash Drawer:", fontSize = 11.sp, color = TextLight)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = editMarkPad, onCheckedChange = { editMarkPad = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                            Text("Tool Mark Pad:", fontSize = 11.sp, color = TextLight)
                        }
                    }

                    // Action Buttons: Add / Update and Cancle matching Tools.py
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (editName.isNotBlank()) {
                                    val newTool = PaymentTool(
                                        id = if (isEditMode) editId else System.currentTimeMillis(),
                                        shopId = editShopId,
                                        shopName = editShopName,
                                        name = editName,
                                        method = editMethod,
                                        code = editCode,
                                        shortKey = editShortKey,
                                        accessKey = editAccessKey,
                                        enabled = editEnabled,
                                        quickPay = editQuickPay,
                                        markPad = editMarkPad,
                                        customerRequired = editCustomerRequired,
                                        openDrawer = editOpenDrawer,
                                        printSlip = editPrintSlip,
                                        changeAllowed = editChangeAllowed
                                    )
                                    if (isEditMode) {
                                        val idx = sampleTools.indexOfFirst { it.id == editId }
                                        if (idx >= 0) sampleTools[idx] = newTool
                                    } else {
                                        sampleTools.add(newTool)
                                    }
                                    showToolEditor = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(if (isEditMode) "Update" else "Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showToolEditor = false },
                            colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Cancle", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
