package com.example.hipermarketsallingapplication.ui.screens

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.Product
import com.example.hipermarketsallingapplication.ui.theme.*

@Composable
fun CategoryTreeSelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMainGroup by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf("MANS") }
    var selectedSubGroup by remember { mutableStateOf("TOP") }
    var selectedItemType by remember { mutableStateOf("T-SHIRT") }

    val mainGroups = listOf("MANS", "WOMANS", "KIDS", "FOREVERYONE")
    val subGroups = when (selectedGroup) {
        "WOMANS" -> listOf("ACCESSORIES", "TROUSER", "SHOES", "TOP", "BOTTOM", "DRESS", "OUTERWEAR", "ACTIVEWEAR")
        "KIDS" -> listOf("GIRLS", "BOYS", "FORKIDS", "ACCESSORIES", "TOP", "BOTTOM", "DRESS", "OUTERWEAR")
        "FOREVERYONE" -> listOf("ACCESSORIES", "SHOES", "TROUSER", "TOP", "BOTTOM", "OUTERWEAR", "ACTIVEWEAR")
        else -> listOf("ACCESSORIES", "TROUSER", "SHOES", "TOP", "BOTTOM", "OUTERWEAR", "ACTIVEWEAR")
    }

    val itemTypes = when (selectedSubGroup) {
        "TOP" -> listOf("T-SHIRT", "SHIRT", "BLOUSES", "POLO SHIRT", "TANK", "SWEATER", "HOODIES", "JACKET", "BLAZER")
        "BOTTOM" -> listOf("JEAN", "PANT", "SHORT", "SKIRT", "LEGGING", "CULOTTE")
        "DRESS" -> listOf("SUNDRESS", "COCKTAIL", "MAXI", "SHIFT", "BODYCON", "A-LINE")
        "OUTERWEAR" -> listOf("COAT", "TRENCH COAT", "RIN COAT", "PARKA", "WINDBREAKER")
        "ACTIVEWEAR" -> listOf("SPORTBRA", "ATHLETICSHORTS", "JOGGINGPANT", "SPORTJACKET", "YOGAPANT", "PREFORMANCETOP")
        else -> listOf("GENERAL")
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = onCategorySelected,
            label = { Text("Category / Type", color = TextMuted, fontSize = 10.sp) },
            trailingIcon = {
                IconButton(onClick = { expandedMainGroup = !expandedMainGroup }) {
                    Icon(Icons.Default.AccountTree, contentDescription = "Select Category Tree", tint = DarkBlueAccent)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (expandedMainGroup) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Hierarchical Category Picker (selecttype.py)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)

                    // Level 1: Main Group
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        mainGroups.forEach { grp ->
                            FilterChip(
                                selected = selectedGroup == grp,
                                onClick = {
                                    selectedGroup = grp
                                    val catPath = "$selectedGroup > $selectedSubGroup > $selectedItemType"
                                    onCategorySelected(catPath)
                                },
                                label = { Text(grp, fontSize = 9.sp) }
                            )
                        }
                    }

                    // Level 2: Sub Group
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        subGroups.forEach { subGrp ->
                            FilterChip(
                                selected = selectedSubGroup == subGrp,
                                onClick = {
                                    selectedSubGroup = subGrp
                                    val catPath = "$selectedGroup > $selectedSubGroup > $selectedItemType"
                                    onCategorySelected(catPath)
                                },
                                label = { Text(subGrp, fontSize = 9.sp) }
                            )
                        }
                    }

                    // Level 3: Item Type
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemTypes.forEach { itemTp ->
                            FilterChip(
                                selected = selectedItemType == itemTp,
                                onClick = {
                                    selectedItemType = itemTp
                                    val catPath = "$selectedGroup > $selectedSubGroup > $selectedItemType"
                                    onCategorySelected(catPath)
                                },
                                label = { Text(itemTp, fontSize = 9.sp) }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun buildProductEdtionNestedJson(
    shopStockList: List<ShopStockItem>,
    productCode: String,
    productBarcode: String,
    productType: String
): String {
    val groupedByShop = shopStockList.groupBy { it.shopName }
    val sb = StringBuilder()
    sb.append("[\n")
    groupedByShop.entries.forEachIndexed { sIdx, (shopName, shopItems) ->
        sb.append("  [\"$shopName\", [\n")
        val groupedByCode = shopItems.groupBy { it.code.ifBlank { productCode } }
        groupedByCode.entries.forEachIndexed { cIdx, (cCode, codeItems) ->
            sb.append("    [\"$cCode\", [\n")
            val groupedByColor = codeItems.groupBy { it.color.ifBlank { "Default" } }
            groupedByColor.entries.forEachIndexed { clrIdx, (cColor, colorItems) ->
                sb.append("      [\"$cColor\", [\n")
                val groupedBySize = colorItems.groupBy { it.size.ifBlank { "M" } }
                groupedBySize.entries.forEachIndexed { szIdx, (cSize, sizeItems) ->
                    sb.append("        [\"$cSize\", [\n")
                    sizeItems.forEachIndexed { iIdx, item ->
                        val bCode = item.barcode.ifBlank { productBarcode }
                        val tType = productType.ifBlank { "General" }
                        sb.append("          [\"$bCode\", \"$tType\", ${item.singlePrice}, ${item.qty}, ${item.qty}, \"\", \"\", \"\", \"\"]")
                        if (iIdx < sizeItems.size - 1) sb.append(",")
                        sb.append("\n")
                    }
                    sb.append("        ]]")
                    if (szIdx < groupedBySize.size - 1) sb.append(",")
                    sb.append("\n")
                }
                sb.append("      ]]")
                if (clrIdx < groupedByColor.size - 1) sb.append(",")
                sb.append("\n")
            }
            sb.append("    ]]")
            if (cIdx < groupedByCode.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ]]")
        if (sIdx < groupedByShop.size - 1) sb.append(",")
        sb.append("\n")
    }
    sb.append("]")
    return sb.toString()
}

data class ShopStockItem(
    val shopName: String = "Main Shop",
    val code: String = "",
    val color: String = "",
    val size: String = "M",
    val barcode: String = "",
    val qty: Int = 10,
    val singlePrice: Double = 0.0
)

@Composable
fun AddEditProductModal(
    existingProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var modalTab by remember { mutableIntStateOf(0) }

    // Main Info States
    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var code by remember { mutableStateOf(existingProduct?.code ?: "") }
    var barcode by remember { mutableStateOf(existingProduct?.barcode ?: "") }
    var type by remember { mutableStateOf(existingProduct?.type ?: "General") }
    var price by remember { mutableStateOf(existingProduct?.price?.toString() ?: "0.0") }
    var cost by remember { mutableStateOf(existingProduct?.cost?.toString() ?: "0.0") }
    var mark by remember { mutableStateOf("0.0") }
    var qty by remember { mutableStateOf(existingProduct?.quantity?.toString() ?: "10") }
    var tax by remember { mutableStateOf(existingProduct?.tax?.toString() ?: "0.0") }
    var description by remember { mutableStateOf("") }

    // Flags matching ProductFullEditionForm
    var isService by remember { mutableStateOf(false) }
    var isDefaultQty by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(true) }
    var includeTax by remember { mutableStateOf(false) }
    var priceChange by remember { mutableStateOf(false) }

    // Stock & Shop Info States
    val shopStockList = remember {
        mutableStateListOf(
            ShopStockItem(shopName = "Main Shop", code = existingProduct?.code ?: "P101", color = "Default", size = "M", qty = existingProduct?.quantity ?: 10, singlePrice = existingProduct?.price ?: 0.0)
        )
    }
    var currentShopName by remember { mutableStateOf("Main Shop") }
    var currentStockColor by remember { mutableStateOf("Default") }
    var currentStockSize by remember { mutableStateOf("M") }
    var currentStockQty by remember { mutableStateOf("10") }
    var currentStockPrice by remember { mutableStateOf(price) }
    var selectedSizingPreset by remember { mutableStateOf("Clothing Sizes") }

    // Doc Info States
    var dateFrom by remember { mutableStateOf("2025-01-01") }
    var dateTo by remember { mutableStateOf("2025-12-31") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = DarkBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingProduct == null) "Add New Product (Full Edition)" else "Full Product Edition Form",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Notebook TabRow matching ProductFullEditionForm (Main Info, Stock, Doc Info)
                SecondaryTabRow(
                    selectedTabIndex = modalTab,
                    containerColor = CardBackground,
                    contentColor = DarkBlueAccent
                ) {
                    Tab(
                        selected = modalTab == 0,
                        onClick = { modalTab = 0 },
                        text = { Text("Main Info", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = modalTab == 1,
                        onClick = { modalTab = 1 },
                        text = { Text("Stock & Shop Info", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = modalTab == 2,
                        onClick = { modalTab = 2 },
                        text = { Text("Doc Info", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                when (modalTab) {
                    0 -> {
                        // TAB 0: Main Info
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Product Name", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = price,
                                    onValueChange = { price = it },
                                    label = { Text("Price ($)", color = TextMuted) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                                )
                                OutlinedTextField(
                                    value = cost,
                                    onValueChange = { cost = it },
                                    label = { Text("Cost ($)", color = TextMuted) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                                )
                                OutlinedTextField(
                                    value = mark,
                                    onValueChange = { mark = it },
                                    label = { Text("Mark ($)", color = TextMuted) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = tax,
                                    onValueChange = { tax = it },
                                    label = { Text("Tax ($)", color = TextMuted) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                                )
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Description", color = TextMuted) },
                                    modifier = Modifier.weight(2f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                                )
                            }

                            Text("Product Flags & Configuration", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(selected = isActive, onClick = { isActive = !isActive }, label = { Text("Active") })
                                FilterChip(selected = isService, onClick = { isService = !isService }, label = { Text("Service") })
                                FilterChip(selected = includeTax, onClick = { includeTax = !includeTax }, label = { Text("Include Tax") })
                                FilterChip(selected = priceChange, onClick = { priceChange = !priceChange }, label = { Text("Allow Price Change") })
                                FilterChip(selected = isDefaultQty, onClick = { isDefaultQty = !isDefaultQty }, label = { Text("Default QTY") })
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: Stock & Shop Info (Shop Stock Tree & Sizing Generator)
                        val currentPathJson = remember(shopStockList, code, barcode, type) {
                            buildProductEdtionNestedJson(
                                shopStockList = shopStockList,
                                productCode = code,
                                productBarcode = barcode,
                                productType = type
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Sizing Preset Generator", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val sizingPresets = listOf("Clothing Sizes", "Trouser Sizes", "Shoe Sizes")
                                sizingPresets.forEach { preset ->
                                    FilterChip(
                                        selected = selectedSizingPreset == preset,
                                        onClick = { selectedSizingPreset = preset },
                                        label = { Text(preset, fontSize = 10.sp) }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val generatedSizes = when (selectedSizingPreset) {
                                        "Trouser Sizes" -> listOf("28", "30", "32", "34", "36")
                                        "Shoe Sizes" -> listOf("39", "40", "41", "42", "43", "44")
                                        else -> listOf("S", "M", "L", "XL", "2XL")
                                    }
                                    generatedSizes.forEach { sz ->
                                        if (shopStockList.none { it.size == sz }) {
                                            shopStockList.add(
                                                ShopStockItem(
                                                    shopName = currentShopName,
                                                    code = code.ifBlank { "P101" },
                                                    color = currentStockColor,
                                                    size = sz,
                                                    barcode = barcode.ifBlank { "8900101" },
                                                    qty = 10,
                                                    singlePrice = price.toDoubleOrNull() ?: 0.0
                                                )
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary)
                            ) {
                                Text("Generate Size Variant Entries ($selectedSizingPreset)", fontSize = 10.sp)
                            }

                            HorizontalDivider(color = DarkBlueDarker)

                            Text("Add / Edit Shop Stock Entry", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)

                            // Row 1: Shop Name & Barcode Or Code
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = currentShopName,
                                    onValueChange = { currentShopName = it },
                                    label = { Text("Shop Name", fontSize = 9.sp) },
                                    modifier = Modifier.weight(1f).height(58.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = code,
                                    onValueChange = {
                                        code = it
                                        barcode = it
                                    },
                                    label = { Text("Barcode Or Code", fontSize = 9.sp) },
                                    modifier = Modifier.weight(1f).height(58.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            // Row 2: Color, Size, Quantity, Single Price
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = currentStockColor,
                                    onValueChange = { currentStockColor = it },
                                    label = { Text("Color", fontSize = 9.sp) },
                                    modifier = Modifier.weight(1f).height(58.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = currentStockSize,
                                    onValueChange = { currentStockSize = it },
                                    label = { Text("Size", fontSize = 9.sp) },
                                    modifier = Modifier.weight(1f).height(58.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = currentStockQty,
                                    onValueChange = { currentStockQty = it },
                                    label = { Text("Quantity", fontSize = 9.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).height(58.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = currentStockPrice,
                                    onValueChange = { currentStockPrice = it },
                                    label = { Text("Single Price ($)", fontSize = 9.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).height(58.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            // Category Selector directly below Size & Stock controls
                            CategoryTreeSelector(
                                selectedCategory = type,
                                onCategorySelected = { type = it },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    shopStockList.add(
                                        ShopStockItem(
                                            shopName = currentShopName,
                                            code = code,
                                            color = currentStockColor,
                                            size = currentStockSize,
                                            barcode = barcode,
                                            qty = currentStockQty.toIntOrNull() ?: 10,
                                            singlePrice = currentStockPrice.toDoubleOrNull() ?: 0.0
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Text("+ Add Shop Stock Variant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Developer Path Inspector Card (matching ProductEdtion.py show_selected_path_label & more_info_label)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Developer Path Inspector (ProductEdtion.py nested_list):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)
                                    Text(
                                        text = currentPathJson,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = SuccessGreen
                                    )
                                }
                            }

                            Text("Shop Stock TreeView Records (${shopStockList.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)

                            // TreeView Structure grouped by Shop Name -> Code -> Color -> Size
                            val groupedByShop = shopStockList.groupBy { it.shopName }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                groupedByShop.forEach { (shopName, shopItems) ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Store, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Shop Node: $shopName", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }

                                            HorizontalDivider(color = DarkBlueDarker)

                                            val groupedByCode = shopItems.groupBy { it.code.ifBlank { code } }
                                            groupedByCode.forEach { (cCode, codeItems) ->
                                                Text("  └─ Code Node: $cCode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)

                                                val groupedByColor = codeItems.groupBy { it.color.ifBlank { "Default" } }
                                                groupedByColor.forEach { (cColor, colorItems) ->
                                                    Text("      └─ Color Node: $cColor", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLight)

                                                    colorItems.forEach { stk ->
                                                        val leafPath = "Size: ${stk.size} > Barcode: ${stk.barcode.ifBlank { barcode }} > QTY: ${stk.qty} > Price: $${stk.singlePrice}"
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("          └─ Leaf: $leafPath", fontSize = 9.sp, color = TextMuted)
                                                            IconButton(
                                                                onClick = { if (shopStockList.size > 1) shopStockList.remove(stk) },
                                                                modifier = Modifier.size(24.dp)
                                                            ) {
                                                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = EnergyRed)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: Doc Info (Document Transaction Log History)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Transaction Document History Filter", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = dateFrom,
                                    onValueChange = { dateFrom = it },
                                    label = { Text("Date From", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).height(58.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = dateTo,
                                    onValueChange = { dateTo = it },
                                    label = { Text("Date To", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).height(58.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Total Related Documents: 0", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextLight)
                                    Text("Total Historical Sales QTY: 0 units", fontSize = 10.sp, color = SuccessGreen)
                                    Text("No matching document records for date range $dateFrom to $dateTo", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val totalCalculatedQty = if (shopStockList.isNotEmpty()) shopStockList.sumOf { it.qty } else (qty.toIntOrNull() ?: 10)
                        val constructedMoreInfo = buildProductEdtionNestedJson(
                            shopStockList = shopStockList,
                            productCode = code,
                            productBarcode = barcode,
                            productType = type
                        )
                        val p = Product(
                            id = existingProduct?.id ?: 0L,
                            name = name,
                            code = code.ifBlank { "P${(100..999).random()}" },
                            barcode = barcode.ifBlank { "${(8900000..8999999).random()}" },
                            type = type.ifBlank { "General" },
                            price = price.toDoubleOrNull() ?: 0.0,
                            cost = cost.toDoubleOrNull() ?: 0.0,
                            quantity = totalCalculatedQty,
                            tax = tax.toDoubleOrNull() ?: 0.0,
                            moreInfo = constructedMoreInfo
                        )
                        onSave(p)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

data class QuickProductEntry(
    var id: Long = 0L,
    var name: String = "",
    var code: String = "",
    var description: String = "",
    var category: String = "General",
    var qty: String = "10",
    var cost: String = "0.0",
    var price: String = "0.0",
    var active: Boolean = true,
    var defaultQty: Boolean = false,
    var priceChange: Boolean = false
)

@Composable
fun AddMultiProductModal(
    onDismiss: () -> Unit,
    onSaveMultiple: (List<Product>) -> Unit
) {
    val entries = remember {
        mutableStateListOf(
            QuickProductEntry(name = "", price = "2.50", cost = "1.50", qty = "20", category = "Dairy"),
            QuickProductEntry(name = "", price = "1.80", cost = "1.00", qty = "15", category = "Bakery")
        )
    }

    var cashAmount by remember { mutableStateOf("0.0") }
    var cardAmount by remember { mutableStateOf("0.0") }

    val totalCount = entries.size
    val totalQty = entries.sumOf { it.qty.toIntOrNull() ?: 0 }
    val totalCost = entries.sumOf { (it.cost.toDoubleOrNull() ?: 0.0) * (it.qty.toIntOrNull() ?: 0) }
    val totalPrice = entries.sumOf { (it.price.toDoubleOrNull() ?: 0.0) * (it.qty.toIntOrNull() ?: 0) }
    val totalProfit = totalPrice - totalCost
    val creditAmount = (totalCost - (cashAmount.toDoubleOrNull() ?: 0.0) - (cardAmount.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = DarkBlueAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Product Quick Edition (Multi Add)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Prominent "+ Add New Item Row" button placed right ON TOP OF THE LIST
                Button(
                    onClick = {
                        entries.add(QuickProductEntry(name = "", price = "0.0", cost = "0.0", qty = "10", category = "General"))
                    },
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add New Item Row", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Horizontal Scrollable Container for Item Draft Cards (matching ProductQueckEditionForm)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .width(720.dp)
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(entries) { index, item ->
                            val itemCost = (item.cost.toDoubleOrNull() ?: 0.0) * (item.qty.toIntOrNull() ?: 0)
                            val itemPrice = (item.price.toDoubleOrNull() ?: 0.0) * (item.qty.toIntOrNull() ?: 0)
                            val itemProfit = itemPrice - itemCost

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Row 1: Product Avatar Icon + Name + CODE + Description
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ShoppingBag,
                                            contentDescription = null,
                                            tint = DarkBlueAccent,
                                            modifier = Modifier.size(32.dp)
                                        )

                                        OutlinedTextField(
                                            value = item.name,
                                            onValueChange = { entries[index] = item.copy(name = it) },
                                            label = { Text("Product Name #${index + 1}", fontSize = 9.sp, color = TextMuted) },
                                            modifier = Modifier.weight(1f).height(58.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )

                                        OutlinedTextField(
                                            value = item.code,
                                            onValueChange = { entries[index] = item.copy(code = it) },
                                            label = { Text("CODE / Barcode", fontSize = 9.sp, color = TextMuted) },
                                            modifier = Modifier.width(130.dp).height(58.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )

                                        OutlinedTextField(
                                            value = item.description,
                                            onValueChange = { entries[index] = item.copy(description = it) },
                                            label = { Text("Description", fontSize = 9.sp, color = TextMuted) },
                                            modifier = Modifier.width(150.dp).height(58.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    // Row 2: Category/Type + QTY + Cost + Price + Row Total Summary
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        CategoryTreeSelector(
                                            selectedCategory = item.category,
                                            onCategorySelected = { entries[index] = item.copy(category = it) },
                                            modifier = Modifier.width(160.dp)
                                        )

                                        OutlinedTextField(
                                            value = item.qty,
                                            onValueChange = { entries[index] = item.copy(qty = it) },
                                            label = { Text("QTY", fontSize = 9.sp, color = TextMuted) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.width(85.dp).height(58.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )

                                        OutlinedTextField(
                                            value = item.cost,
                                            onValueChange = { entries[index] = item.copy(cost = it) },
                                            label = { Text("Cost ($)", fontSize = 9.sp, color = TextMuted) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.width(90.dp).height(58.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )

                                        OutlinedTextField(
                                            value = item.price,
                                            onValueChange = { entries[index] = item.copy(price = it) },
                                            label = { Text("Price ($)", fontSize = 9.sp, color = TextMuted) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.width(90.dp).height(58.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text("Total Cost: $${String.format(Locale.getDefault(), "%.2f", itemCost)}", fontSize = 9.sp, color = EnergyRed)
                                            Text("Total Price: $${String.format(Locale.getDefault(), "%.2f", itemPrice)}", fontSize = 9.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                            Text("Profit: $${String.format(Locale.getDefault(), "%.2f", itemProfit)}", fontSize = 9.sp, color = DarkBlueAccent)
                                        }
                                    }

                                    // Row 3: Checkboxes & Item Row Action Buttons (Copy, Clear, Cancel)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            FilterChip(
                                                selected = item.active,
                                                onClick = { entries[index] = item.copy(active = !item.active) },
                                                label = { Text("Active", fontSize = 9.sp) }
                                            )
                                            FilterChip(
                                                selected = item.defaultQty,
                                                onClick = { entries[index] = item.copy(defaultQty = !item.defaultQty) },
                                                label = { Text("Default QTY", fontSize = 9.sp) }
                                            )
                                            FilterChip(
                                                selected = item.priceChange,
                                                onClick = { entries[index] = item.copy(priceChange = !item.priceChange) },
                                                label = { Text("Price Change", fontSize = 9.sp) }
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Button(
                                                onClick = {
                                                    entries.add(item.copy(id = 0L, name = "${item.name} (Copy)"))
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Copy", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    entries[index] = item.copy(name = "", price = "0.0", cost = "0.0", qty = "10")
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Clear", fontSize = 9.sp, color = TextMuted)
                                            }

                                            Button(
                                                onClick = {
                                                    if (entries.size > 1) entries.removeAt(index)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Cancel", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DarkBlueDarker)

                // BOTTOM SECTION: Payment Breakdown & Total Summary Information Panel
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Payments Allocation (ProductQueckEditionForm)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = cashAmount,
                            onValueChange = { cashAmount = it },
                            label = { Text("Cash ($)", fontSize = 9.sp, color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = cardAmount,
                            onValueChange = { cardAmount = it },
                            label = { Text("Card ($)", fontSize = 9.sp, color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = String.format(Locale.getDefault(), "%.2f", creditAmount),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Credit ($)", fontSize = 9.sp, color = TextMuted) },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Total Summary Information Panel at the BOTTOM
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Items Count: $totalCount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                Text("Total QTY: $totalQty units", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Cost: $${String.format(Locale.getDefault(), "%.2f", totalCost)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EnergyRed)
                                Text("Total Price: $${String.format(Locale.getDefault(), "%.2f", totalPrice)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)
                                Text("Profit: $${String.format(Locale.getDefault(), "%.2f", totalProfit)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validProducts = entries.mapNotNull { item ->
                        if (item.name.isNotBlank()) {
                            Product(
                                id = 0L,
                                name = item.name,
                                code = item.code.ifBlank { "P${(100..999).random()}" },
                                barcode = item.code.ifBlank { "${(8900000..8999999).random()}" },
                                type = item.category.ifBlank { "General" },
                                price = item.price.toDoubleOrNull() ?: 0.0,
                                cost = item.cost.toDoubleOrNull() ?: 0.0,
                                quantity = item.qty.toIntOrNull() ?: 10,
                                description = item.description,
                                active = item.active,
                                defaultQuantity = if (item.defaultQty) 1 else 0,
                                priceChange = item.priceChange
                            )
                        } else null
                    }
                    if (validProducts.isNotEmpty()) {
                        onSaveMultiple(validProducts)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
            ) {
                Text("Process & Save All (${entries.count { it.name.isNotBlank() }})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun PriceTagModal(
    product: Product,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text("Price Tag Preview", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("HIPERMARKET", color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(product.name, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$${String.format(Locale.getDefault(), "%.2f", product.price)}", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.Black, modifier = Modifier.size(60.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("CODE: ${product.code} | ${product.type}", color = Color.Gray, fontSize = 10.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
            ) {
                Text("Close / Print")
            }
        }
    )
}