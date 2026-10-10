package com.example.hipermarketsallingapplication.ui.screens

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.hipermarketsallingapplication.data.model.*
import com.example.hipermarketsallingapplication.ui.theme.*

data class SearchSelectionItem(
    val product: Product,
    val selectedShop: String = "Main Shop",
    val selectedCode: String = "",
    val selectedColor: String = "Default",
    val selectedSize: String = "M",
    val qty: Double = 1.0,
    val barcode: String = ""
)

@Composable
fun PosSearchEntry(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    products: List<Product>,
    activeShop: String,
    onAddToCart: (Product, Double, String, String, String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchSelections = remember { mutableStateMapOf<Long, SearchSelectionItem>() }

    val doneTotalQty = searchSelections.values.sumOf { it.qty }
    val doneTotalPrice = searchSelections.values.sumOf { it.qty * it.product.price }

    fun completeSearchAndAddToCart() {
        searchSelections.values.forEach { sel ->
            onAddToCart(
                sel.product,
                sel.qty,
                sel.selectedColor,
                sel.selectedSize,
                sel.selectedShop.ifBlank { activeShop.ifBlank { "Main Shop" } }
            )
        }
        searchSelections.clear()
        onClearSearch()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            label = { Text("Scan Barcode or Search Item (searchbox.py)", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = DarkBlueAccent) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchSelections.clear(); onClearSearch() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkBlueAccent,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        // Floating Popup Panel directly below search entry (renders on front of everything without covering entry!)
        if (searchQuery.isNotBlank()) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, 160),
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (products.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("No products match \"$searchQuery\"", color = TextMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(products) { product ->
                                    val isHeld = searchSelections.containsKey(product.id)
                                    val heldItem = searchSelections[product.id]

                                    SearchItemRow(
                                        product = product,
                                        isHeld = isHeld,
                                        heldQty = heldItem?.qty ?: 0.0,
                                        defaultShop = if (product.atShop.isNotBlank() && product.atShop != "All Shops") product.atShop else activeShop.ifBlank { "Main Shop" },
                                        onAdd = { sel ->
                                            searchSelections[product.id] = sel
                                        },
                                        onRemove = {
                                            searchSelections.remove(product.id)
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = DarkBlueDarker)

                        // Bottom Control Bar matching searchbox.py (Done / Cancel buttons with live summary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    searchSelections.clear()
                                    onClearSearch()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Cancel", color = TextMuted, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { completeSearchAndAddToCart() },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (searchSelections.isNotEmpty())
                                        "Done (${doneTotalQty.toInt()} items - $${String.format(Locale.getDefault(), "%.2f", doneTotalPrice)})"
                                    else "Done",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreInfoDropdown(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Auto-select if there is only 1 option available (matching searchbox.py & Display.py)
    LaunchedEffect(options) {
        if (options.isNotEmpty()) {
            if (options.size == 1 && value != options.first()) {
                onOptionSelected(options.first())
            } else if (value.isBlank() || value !in options) {
                onOptionSelected(options.first())
            }
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value.ifBlank { if (options.isNotEmpty()) options.first() else "" },
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 9.sp, color = TextMuted) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = label,
                        modifier = Modifier.size(18.dp),
                        tint = DarkBlueAccent
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = DarkBlueAccent,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = !expanded }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            fontWeight = if (option == value) FontWeight.Bold else FontWeight.Normal,
                            color = if (option == value) DarkBlueAccent else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SearchItemRow(
    product: Product,
    isHeld: Boolean,
    heldQty: Double,
    defaultShop: String,
    onAdd: (SearchSelectionItem) -> Unit,
    onRemove: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(isHeld) }

    val moreInfoParser = remember(product.moreInfo, defaultShop, product.code) {
        MoreInfoParser(
            moreInfoJson = product.moreInfo,
            fallbackShop = defaultShop.ifBlank { "Main Shop" },
            fallbackCode = product.code.ifBlank { "P101" },
            fallbackColor = "Default",
            fallbackSize = "M"
        )
    }

    val shopOptions = remember(moreInfoParser) { moreInfoParser.getShops() }
    var selectedShop by remember(product, defaultShop) { mutableStateOf(if (shopOptions.size == 1) shopOptions.first() else defaultShop.ifBlank { shopOptions.first() }) }

    val codeOptions = remember(moreInfoParser, selectedShop) { moreInfoParser.getCodes(selectedShop) }
    var selectedCode by remember(product, selectedShop) { mutableStateOf(if (codeOptions.size == 1) codeOptions.first() else product.code.ifBlank { codeOptions.first() }) }

    val colorOptions = remember(moreInfoParser, selectedShop, selectedCode) { moreInfoParser.getColors(selectedShop, selectedCode) }
    var selectedColor by remember(product, selectedShop, selectedCode) { mutableStateOf(if (colorOptions.size == 1) colorOptions.first() else colorOptions.first()) }

    val sizeOptions = remember(moreInfoParser, selectedShop, selectedCode, selectedColor) { moreInfoParser.getSizes(selectedShop, selectedCode, selectedColor) }
    var selectedSize by remember(product, selectedShop, selectedCode, selectedColor) { mutableStateOf(if (sizeOptions.size == 1) sizeOptions.first() else sizeOptions.first()) }

    var qtyInput by remember { mutableStateOf("1") }

    val qtyFocusRequester = remember { FocusRequester() }

    // Focus QTY field first when expanded (searchbox.py)
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            qtyFocusRequester.requestFocus()
        }
    }

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
            // Main Item Line: Checkbox + Avatar Icon + Name + Price + Stock Left
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Checkbox(
                    checked = isExpanded,
                    onCheckedChange = { isExpanded = it },
                    colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent)
                )

                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = DarkBlueAccent,
                    modifier = Modifier.size(28.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Code: $selectedCode | Stock Left: ${product.quantity} units", fontSize = 10.sp, color = TextMuted)
                    if (isHeld) {
                        Text("✓ Held in Cart: ${heldQty.toInt()} units", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }

                Text("$${String.format(Locale.getDefault(), "%.2f", product.price)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SuccessGreen)
            }

            // Expanded Subpanel: Horizontally scrollable row with Dropdowns for Shop, Code, Color, Size
            if (isExpanded) {
                HorizontalDivider(color = DarkBlueDarker)

                Text("Variant Dropdowns & Quantity Control (searchbox.py)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MoreInfoDropdown(
                        label = "Shop",
                        value = selectedShop,
                        options = shopOptions,
                        onOptionSelected = { selectedShop = it },
                        modifier = Modifier.width(115.dp)
                    )

                    MoreInfoDropdown(
                        label = "Code",
                        value = selectedCode,
                        options = codeOptions,
                        onOptionSelected = { selectedCode = it },
                        modifier = Modifier.width(110.dp)
                    )

                    MoreInfoDropdown(
                        label = "Color",
                        value = selectedColor,
                        options = colorOptions,
                        onOptionSelected = { selectedColor = it },
                        modifier = Modifier.width(105.dp)
                    )

                    MoreInfoDropdown(
                        label = "Size",
                        value = selectedSize,
                        options = sizeOptions,
                        onOptionSelected = { selectedSize = it },
                        modifier = Modifier.width(100.dp)
                    )

                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text("QTY", fontSize = 9.sp, color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val q = qtyInput.toDoubleOrNull() ?: 1.0
                                onAdd(
                                    SearchSelectionItem(
                                        product = product,
                                        selectedShop = selectedShop,
                                        selectedCode = selectedCode,
                                        selectedColor = selectedColor,
                                        selectedSize = selectedSize,
                                        qty = q,
                                        barcode = product.barcode
                                    )
                                )
                            }
                        ),
                        modifier = Modifier
                            .width(85.dp)
                            .defaultMinSize(minHeight = 56.dp)
                            .focusRequester(qtyFocusRequester),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    // Explicit Add & Remove Buttons matching searchbox.py
                    Button(
                        onClick = {
                            val q = qtyInput.toDoubleOrNull() ?: 1.0
                            onAdd(
                                SearchSelectionItem(
                                    product = product,
                                    selectedShop = selectedShop,
                                    selectedCode = selectedCode,
                                    selectedColor = selectedColor,
                                    selectedSize = selectedSize,
                                    qty = q,
                                    barcode = product.barcode
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            onRemove()
                            isExpanded = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}