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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.PromotionAction
import com.example.hipermarketsallingapplication.ui.theme.*

/**
 * PromotionsScreen matching Actions.py
 * Manages promotional actions, discount triggers ("If :"), and offer application ("Do :").
 */
@Composable
fun PromotionsScreen() {
    val currentDateStr = remember { SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).format(Date()) }

    val sampleActions = remember {
        mutableStateListOf(
            PromotionAction(
                id = 1,
                code = "ACT-101",
                title = "10% Discount on Total Bill > $100",
                createdDate = currentDateStr,
                fromDate = currentDateStr,
                toDate = currentDateStr,
                ifTotalPrice = 100.0,
                ifTotalDiscount = 0.0,
                ifTotalQty = 1.0,
                doMakeTotalDiscount = 10.0
            ),
            PromotionAction(
                id = 2,
                code = "ACT-102",
                title = "Buy 3 Items Get $5 Off",
                createdDate = currentDateStr,
                fromDate = currentDateStr,
                toDate = currentDateStr,
                ifTotalPrice = 0.0,
                ifTotalQty = 3.0,
                doMakeTotalDiscount = 5.0
            )
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedActionInTable by remember { mutableStateOf<PromotionAction?>(null) }
    var showActionEditor by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    // Filtered actions list matching Actions.py search_products
    val filteredActions = remember(sampleActions, searchQuery) {
        if (searchQuery.isBlank()) {
            sampleActions
        } else {
            sampleActions.filter { a ->
                a.code.contains(searchQuery, ignoreCase = true) ||
                        a.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Action Form State variables matching Actions.py
    var editId by remember { mutableStateOf(0L) }
    var editCode by remember { mutableStateOf("") }
    var editLabel by remember { mutableStateOf("") }
    var editFromDate by remember { mutableStateOf(currentDateStr) }
    var editToDate by remember { mutableStateOf(currentDateStr) }

    // "If :" conditions state
    var editIfTotalPrice by remember { mutableStateOf("0.0") }
    var editIfTotalDiscount by remember { mutableStateOf("0.0") }
    var editIfTotalQty by remember { mutableStateOf("0.0") }
    var editIfTotalProfit by remember { mutableStateOf("0.0") }

    // "Do :" actions state
    var editDoMakePrice by remember { mutableStateOf("0.0") }
    var editDoMakeDiscount by remember { mutableStateOf("0.0") }

    fun populateActionEditor(a: PromotionAction) {
        editId = a.id
        editCode = a.code
        editLabel = a.title
        editFromDate = a.fromDate
        editToDate = a.toDate
        editIfTotalPrice = a.ifTotalPrice.toString()
        editIfTotalDiscount = a.ifTotalDiscount.toString()
        editIfTotalQty = a.ifTotalQty.toString()
        editIfTotalProfit = a.ifTotalProfit.toString()
        editDoMakePrice = a.doMakeTotalPrice.toString()
        editDoMakeDiscount = a.doMakeTotalDiscount.toString()
    }

    fun clearActionEditor() {
        editId = 0L
        editCode = "ACT-${(100..999).random()}"
        editLabel = ""
        editFromDate = currentDateStr
        editToDate = currentDateStr
        editIfTotalPrice = "0.0"
        editIfTotalDiscount = "0.0"
        editIfTotalQty = "0.0"
        editIfTotalProfit = "0.0"
        editDoMakePrice = "0.0"
        editDoMakeDiscount = "0.0"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Search & Action Buttons Bar matching Actions.py
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
                    label = { Text("Search Actions (Actions.py)...", fontSize = 9.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkBlueAccent) },
                    modifier = Modifier.width(220.dp).defaultMinSize(minHeight = 52.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )

                // New Action button matching Actions.py
                Button(
                    onClick = {
                        clearActionEditor()
                        isEditMode = false
                        showActionEditor = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Action", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Change button matching Actions.py
                Button(
                    onClick = {
                        selectedActionInTable?.let { a ->
                            populateActionEditor(a)
                            isEditMode = true
                            showActionEditor = true
                        }
                    },
                    enabled = selectedActionInTable != null,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Delete button matching Actions.py
                Button(
                    onClick = {
                        selectedActionInTable?.let { a ->
                            sampleActions.removeAll { it.id == a.id }
                            selectedActionInTable = null
                            showActionEditor = false
                        }
                    },
                    enabled = selectedActionInTable != null,
                    colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Refresh button matching Actions.py
                Button(
                    onClick = { searchQuery = "" },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Main Actions Data Table Grid matching Actions.py (list_box)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                Text(
                    text = "Promotional Actions List (${filteredActions.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkBlueAccent,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Table Column Headers Row matching Actions.py
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBlueDarker, RoundedCornerShape(4.dp))
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCellHeader("Shop_Actions Code", 140.dp)
                    TableCellHeader("Shop_Actions Titel", 220.dp)
                    TableCellHeader("Created Date", 120.dp)
                    TableCellHeader("From", 110.dp)
                    TableCellHeader("To", 110.dp)
                }

                HorizontalDivider(color = DarkBlueDarker)

                if (filteredActions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No promotional actions configured.", color = TextMuted, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filteredActions) { action ->
                            val isSelected = selectedActionInTable?.id == action.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) DarkBlueAccent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { selectedActionInTable = action }
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCellValue(action.code, 140.dp, isBold = true)
                                TableCellValue(action.title, 220.dp, color = TextLight, isBold = true)
                                TableCellValue(action.createdDate, 120.dp)
                                TableCellValue(action.fromDate, 110.dp, color = SuccessGreen)
                                TableCellValue(action.toDate, 110.dp, color = EnergyRed)
                            }
                        }
                    }
                }
            }
        }

        // Action Details & Editing Subpanel Notebook matching Actions.py (notebook_frame)
        if (showActionEditor) {
            var actionSubTab by remember { mutableStateOf(0) } // 0: Details, 1: If :, 2: Do :

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditMode) "Edit Action (Actions.py)" else "New Action (Actions.py)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DarkBlueAccent
                        )
                        IconButton(onClick = { showActionEditor = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    TabRow(
                        selectedTabIndex = actionSubTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = TextLight
                    ) {
                        Tab(selected = actionSubTab == 0, onClick = { actionSubTab = 0 }) {
                            Text("Action Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
                        }
                        Tab(selected = actionSubTab == 1, onClick = { actionSubTab = 1 }) {
                            Text("If : (Trigger)", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
                        }
                        Tab(selected = actionSubTab == 2, onClick = { actionSubTab = 2 }) {
                            Text("Do : (Offer)", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
                        }
                    }

                    when (actionSubTab) {
                        0 -> {
                            // Tab 1: Action Details (Actions.py)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = editCode,
                                        onValueChange = { editCode = it },
                                        label = { Text("Action Code :", fontSize = 9.sp, color = TextMuted) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    OutlinedTextField(
                                        value = editLabel,
                                        onValueChange = { editLabel = it },
                                        label = { Text("Action Label :", fontSize = 9.sp, color = TextMuted) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = editFromDate,
                                        onValueChange = { editFromDate = it },
                                        label = { Text("From_Date:", fontSize = 9.sp, color = TextMuted) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    OutlinedTextField(
                                        value = editToDate,
                                        onValueChange = { editToDate = it },
                                        label = { Text("TO_Date:", fontSize = 9.sp, color = TextMuted) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Tab 2: "If :" Conditions (Actions.py)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("IF Condition Thresholds (Actions.py)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = editIfTotalPrice,
                                        onValueChange = { editIfTotalPrice = it },
                                        label = { Text("IF Total price :", fontSize = 9.sp, color = TextMuted) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    OutlinedTextField(
                                        value = editIfTotalDiscount,
                                        onValueChange = { editIfTotalDiscount = it },
                                        label = { Text("If Total Discount :", fontSize = 9.sp, color = TextMuted) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = editIfTotalQty,
                                        onValueChange = { editIfTotalQty = it },
                                        label = { Text("If Total Items QTY :", fontSize = 9.sp, color = TextMuted) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    OutlinedTextField(
                                        value = editIfTotalProfit,
                                        onValueChange = { editIfTotalProfit = it },
                                        label = { Text("If Total Profite :", fontSize = 9.sp, color = TextMuted) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        2 -> {
                            // Tab 3: "Do :" Makes & Offers (Actions.py)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("DO Action Execution Offers (Actions.py)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = editDoMakePrice,
                                        onValueChange = { editDoMakePrice = it },
                                        label = { Text("Make Total price :", fontSize = 9.sp, color = TextMuted) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    OutlinedTextField(
                                        value = editDoMakeDiscount,
                                        onValueChange = { editDoMakeDiscount = it },
                                        label = { Text("Make Total Discount :", fontSize = 9.sp, color = TextMuted) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons: Add / Save and Cancle matching Actions.py
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (editCode.isNotBlank()) {
                                    val newAct = PromotionAction(
                                        id = if (isEditMode) editId else System.currentTimeMillis(),
                                        code = editCode,
                                        title = editLabel.ifBlank { "Action $editCode" },
                                        createdDate = currentDateStr,
                                        fromDate = editFromDate,
                                        toDate = editToDate,
                                        ifTotalPrice = editIfTotalPrice.toDoubleOrNull() ?: 0.0,
                                        ifTotalDiscount = editIfTotalDiscount.toDoubleOrNull() ?: 0.0,
                                        ifTotalQty = editIfTotalQty.toDoubleOrNull() ?: 0.0,
                                        ifTotalProfit = editIfTotalProfit.toDoubleOrNull() ?: 0.0,
                                        doMakeTotalPrice = editDoMakePrice.toDoubleOrNull() ?: 0.0,
                                        doMakeTotalDiscount = editDoMakeDiscount.toDoubleOrNull() ?: 0.0
                                    )
                                    if (isEditMode) {
                                        val idx = sampleActions.indexOfFirst { it.id == editId }
                                        if (idx >= 0) sampleActions[idx] = newAct
                                    } else {
                                        sampleActions.add(newAct)
                                    }
                                    showActionEditor = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(if (isEditMode) "Save" else "Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showActionEditor = false },
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
