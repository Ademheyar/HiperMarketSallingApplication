package com.example.hipermarketsallingapplication.ui.screens

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.Product
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.ProductViewModel
import com.example.hipermarketsallingapplication.ui.viewmodel.UserViewModel

@Composable
fun ProductsScreen(viewModel: ProductViewModel, userViewModel: UserViewModel) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    val userWorkShops by userViewModel.userWorkingShops.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("ALL PRODUCTS") }

    var isPriceFilter by remember { mutableStateOf("") }
    var isCostFilter by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var showMultiAddDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }
    var showPriceTagModal by remember { mutableStateOf<Product?>(null) }

    val activeShop = currentUser?.userShop ?: ""
    val hasSingleShop = activeShop.isNotBlank() && 
        !activeShop.equals("All Working Shops", ignoreCase = true) && 
        !activeShop.equals("All Shops", ignoreCase = true) && 
        !activeShop.equals("None", ignoreCase = true) && 
        !activeShop.equals("[]", ignoreCase = true)

    // Unique Categories extracted from products database (Treeview in Product.py)
    val categories = remember(products) {
        val list = mutableListOf("ALL PRODUCTS")
        val uniqueTypes = products.map { it.type.ifBlank { "General" } }.distinct().sorted()
        list.addAll(uniqueTypes)
        list
    }

    // Filter products based on search, category, shop, and price/cost filters
    val filteredProducts = remember(products, searchQuery, selectedCategory, isPriceFilter, isCostFilter, activeShop, userWorkShops) {
        products.filter { p ->
            val matchesCategory = (selectedCategory == "ALL PRODUCTS") || p.type.contains(selectedCategory, ignoreCase = true)
            val matchesPrice = isPriceFilter.isBlank() || p.price == isPriceFilter.toDoubleOrNull()
            val matchesCost = isCostFilter.isBlank() || p.cost == isCostFilter.toDoubleOrNull()

            val matchesShop = if (hasSingleShop) {
                p.atShop.equals(activeShop, ignoreCase = true) || p.atShop.equals("All Shops", ignoreCase = true)
            } else if (userWorkShops.isNotEmpty()) {
                userWorkShops.any { ws -> p.atShop.equals(ws.name, ignoreCase = true) } || p.atShop.equals("All Shops", ignoreCase = true)
            } else {
                true
            }

            matchesCategory && matchesPrice && matchesCost && matchesShop
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Search & Controls Header (Responsive: 1 row on wide screen, 2 rows on small screen)
            if (isWideScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchProducts(it) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp),
                        label = { Text("Search Products by Name, Code, Barcode", color = TextMuted, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchProducts("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlueAccent,
                            unfocusedBorderColor = CardBackground,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    Button(
                        onClick = {
                            selectedProductForEdit = null
                            showAddDialog = true
                        },
                        modifier = Modifier.height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Add New", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { showMultiAddDialog = true },
                        modifier = Modifier.height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.LibraryAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Multi Add", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = { viewModel.loadProducts() },
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = DarkBlueAccent)
                    }
                }
            } else {
                // Small Screen Header: Search Bar on top, Action Buttons below
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchProducts(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        label = { Text("Search Products by Name, Code, Barcode", color = TextMuted, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchProducts("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlueAccent,
                            unfocusedBorderColor = CardBackground,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                selectedProductForEdit = null
                                showAddDialog = true
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Add New", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showMultiAddDialog = true },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary),
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.LibraryAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Multi Add", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = { viewModel.loadProducts() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = DarkBlueAccent)
                        }
                    }
                }
            }

            // Two-pane or Single-pane Main Content Layout
            if (isWideScreen) {
                // Wide Screen: Left TreeView Category Navigation + Right Products Notebook
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left Category TreeView Navigation
                    Card(
                        modifier = Modifier
                            .width(210.dp)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountTree, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Product Categories", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 12.sp)
                            }
                            HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(vertical = 6.dp))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(categories) { cat ->
                                    val isSelected = cat == selectedCategory
                                    val count = if (cat == "ALL PRODUCTS") products.size else products.count { it.type.equals(cat, ignoreCase = true) }
                                    Surface(
                                        onClick = { selectedCategory = cat },
                                        color = if (isSelected) DarkBlueAccent else SurfaceDark,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cat,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else TextMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("($count)", fontSize = 10.sp, color = if (isSelected) Color.White else TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right Side Notebook (Products Tab & Products Info / Report Tab)
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        SecondaryTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = CardBackground,
                            contentColor = DarkBlueAccent
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Products Inventory (${filteredProducts.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Products Information & Report", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        when (selectedTab) {
                            0 -> ProductsListTab(
                                products = filteredProducts,
                                onSaveChange = { viewModel.saveProduct(it, userWorkShops) },
                                onFullEdit = {
                                    selectedProductForEdit = it
                                    showAddDialog = true
                                },
                                onDelete = { id -> viewModel.deleteProduct(id) },
                                onPrintTag = { showPriceTagModal = it }
                            )
                            1 -> ProductsReportView(products = products)
                        }
                    }
                }
            } else {
                // Phone / Narrow Screen Layout: Top Scrollable Category Chips + Notebook Tabs
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkBlueAccent,
                                selectedLabelColor = Color.White,
                                containerColor = CardBackground,
                                labelColor = TextMuted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardBackground,
                    contentColor = DarkBlueAccent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Products (${filteredProducts.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Products Report", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                when (selectedTab) {
                    0 -> ProductsListTab(
                        products = filteredProducts,
                        onSaveChange = { viewModel.saveProduct(it, userWorkShops) },
                        onFullEdit = {
                            selectedProductForEdit = it
                            showAddDialog = true
                        },
                        onDelete = { id -> viewModel.deleteProduct(id) },
                        onPrintTag = { showPriceTagModal = it }
                    )
                    1 -> ProductsReportView(products = products)
                }
            }
        }
    }

    // Add or Edit Single Product Dialog
    if (showAddDialog) {
        AddEditProductModal(
            existingProduct = selectedProductForEdit,
            onDismiss = { showAddDialog = false },
            onSave = { p ->
                val targetProduct = if (hasSingleShop) p.copy(atShop = activeShop) else p.copy(atShop = "All Working Shops")
                viewModel.saveProduct(targetProduct, userWorkShops)
                showAddDialog = false
            }
        )
    }

    // Add Multiple Products Dialog
    if (showMultiAddDialog) {
        AddMultiProductModal(
            onDismiss = { showMultiAddDialog = false },
            onSaveMultiple = { newProds ->
                newProds.forEach { p ->
                    val targetProduct = if (hasSingleShop) p.copy(atShop = activeShop) else p.copy(atShop = "All Working Shops")
                    viewModel.saveProduct(targetProduct, userWorkShops)
                }
                showMultiAddDialog = false
            }
        )
    }

    // Print Price Tag Modal
    showPriceTagModal?.let { prod ->
        PriceTagModal(
            product = prod,
            onDismiss = { showPriceTagModal = null }
        )
    }
}

@Composable
fun ProductsListTab(
    products: List<Product>,
    onSaveChange: (Product) -> Unit,
    onFullEdit: (Product) -> Unit,
    onDelete: (Long) -> Unit,
    onPrintTag: (Product) -> Unit
) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No products found in inventory.", color = TextMuted)
        }
    } else {
        // Group Horizontal Scroll for the entire inventory table
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(720.dp)
                    .padding(end = 16.dp)
            ) {
                items(products) { product ->
                    ProductCardRow(
                        product = product,
                        onSaveChange = onSaveChange,
                        onFullEdit = { onFullEdit(product) },
                        onDelete = { onDelete(product.id) },
                        onPrintTag = { onPrintTag(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCardRow(
    product: Product,
    onSaveChange: (Product) -> Unit,
    onFullEdit: () -> Unit,
    onDelete: () -> Unit,
    onPrintTag: (Product) -> Unit
) {
    var nameInput by remember(product) { mutableStateOf(product.name) }
    var priceInput by remember(product) { mutableStateOf(product.price.toString()) }
    var qtyInput by remember(product) { mutableStateOf(product.quantity.toString()) }
    var codeInput by remember(product) { mutableStateOf(product.code) }
    var categoryInput by remember(product) { mutableStateOf(product.type) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // LEFT SIDE: Fields arranged in a Column (Product Name on top, Price/QTY/Code/Category/Barcode on bottom)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Row: Avatar Icon + Product Name Input Box (filling full width of left side)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = DarkBlueAccent,
                        modifier = Modifier.size(36.dp)
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Product Name", fontSize = 10.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }

                // Bottom Row: Price, QTY Max, Code, Category, Barcode, Price Tag Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Price ($)", fontSize = 9.sp, color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(95.dp).height(58.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text("QTY Max", fontSize = 9.sp, color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(90.dp).height(58.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("Code", fontSize = 9.sp, color = TextMuted) },
                        modifier = Modifier.width(95.dp).height(58.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text("Category", fontSize = 9.sp, color = TextMuted) },
                        modifier = Modifier.width(105.dp).height(58.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("Barcode: ${product.barcode}", color = TextMuted, fontSize = 10.sp, maxLines = 1)
                    }

                    IconButton(onClick = { onPrintTag(product) }, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.QrCode, contentDescription = "Price Tag", tint = DarkBlueAccent)
                    }
                }
            }

            // RIGHT SIDE: 3 buttons stacked vertically in 3 rows (top: Delete, middle: Full Edit, bottom: Save)
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(110.dp)
            ) {
                // Row 1 (top): Delete Button
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().height(30.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Delete", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Row 2 (middle): Full Edit Button
                Button(
                    onClick = onFullEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().height(30.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Full Edit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Row 3 (bottom): Save Button
                Button(
                    onClick = {
                        val newPrice = priceInput.toDoubleOrNull() ?: product.price
                        val newQty = qtyInput.toIntOrNull() ?: product.quantity
                        val updated = product.copy(
                            name = nameInput,
                            price = newPrice,
                            quantity = newQty,
                            code = codeInput,
                            type = categoryInput
                        )
                        onSaveChange(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().height(30.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Save", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProductsReportView(products: List<Product>) {
    val totalItems = products.size
    val totalQty = products.sumOf { it.quantity }
    val totalCost = products.sumOf { it.cost * it.quantity }
    val totalPrice = products.sumOf { it.price * it.quantity }
    val totalProfit = totalPrice - totalCost

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Products Information & Summary Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkBlueAccent)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportRow("Total Distinct Products:", "$totalItems items", DarkBlueAccent)
                ReportRow("Total Stock Units:", "$totalQty units", SuccessGreen)
                ReportRow("Total Stock Cost Value:", "$${String.format(Locale.getDefault(), "%.2f", totalCost)}", EnergyRed)
                ReportRow("Total Stock Price Value:", "$${String.format(Locale.getDefault(), "%.2f", totalPrice)}", DarkBlueAccent)
                HorizontalDivider(color = DarkBlueDarker)
                ReportRow("Estimated Inventory Profit:", "$${String.format(Locale.getDefault(), "%.2f", totalProfit)}", SuccessGreen)
            }
        }

        Text("Category Stock Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkBlueAccent)

        val categoriesGrouped = products.groupBy { it.type.ifBlank { "General" } }
        categoriesGrouped.forEach { (catName, catProds) ->
            val catQty = catProds.sumOf { it.quantity }
            val catValue = catProds.sumOf { it.price * it.quantity }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(catName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                        Text("${catProds.size} products | $catQty total units", color = TextMuted, fontSize = 11.sp)
                    }
                    Text("$${String.format(Locale.getDefault(), "%.2f", catValue)}", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ReportRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}


