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
import com.example.hipermarketsallingapplication.data.model.Product
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(viewModel: ProductViewModel) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showMultiAddDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }
    var showPriceTagModal by remember { mutableStateOf<Product?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        // Fixed Vertical Product Searching on Top
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchProducts(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            label = { Text("Search Products by Name, Code, Barcode", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkBlueAccent) },
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
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons Row Below Search: Add Product & Add Multi Product
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    selectedProductForEdit = null
                    showAddDialog = true
                },
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Product", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showMultiAddDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.LibraryAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Multi Product", fontWeight = FontWeight.Bold)
            }
        }

        // Products List Table
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inventory Catalog (${products.size} Items)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                }

                HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(bottom = 8.dp))

                if (products.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No items found in inventory.", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(products) { product ->
                            ProductRow(
                                product = product,
                                onEdit = {
                                    selectedProductForEdit = product
                                    showAddDialog = true
                                },
                                onDelete = { viewModel.deleteProduct(product.id) },
                                onPrintTag = { showPriceTagModal = product }
                            )
                        }
                    }
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
                viewModel.saveProduct(p)
                showAddDialog = false
            }
        )
    }

    // Add Multiple Products Dialog
    if (showMultiAddDialog) {
        AddMultiProductModal(
            onDismiss = { showMultiAddDialog = false },
            onSaveMultiple = { newProds ->
                newProds.forEach { viewModel.saveProduct(it) }
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
fun ProductRow(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPrintTag: () -> Unit
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
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = product.name,
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Code: ${product.code} | Barcode: ${product.barcode} | Type: ${product.type}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .weight(0.8f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    color = EnergyRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Stock: ${product.quantity} units",
                    color = if (product.quantity < 10) EnergyRed else SuccessGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            Row {
                IconButton(onClick = onPrintTag) {
                    Icon(Icons.Default.QrCode, contentDescription = "Price Tag", tint = DarkBlueAccent)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextLight)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EnergyRed)
                }
            }
        }
    }
}

@Composable
fun AddEditProductModal(
    existingProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var code by remember { mutableStateOf(existingProduct?.code ?: "") }
    var barcode by remember { mutableStateOf(existingProduct?.barcode ?: "") }
    var type by remember { mutableStateOf(existingProduct?.type ?: "General") }
    var price by remember { mutableStateOf(existingProduct?.price?.toString() ?: "0.0") }
    var cost by remember { mutableStateOf(existingProduct?.cost?.toString() ?: "0.0") }
    var qty by remember { mutableStateOf(existingProduct?.quantity?.toString() ?: "10") }
    var tax by remember { mutableStateOf(existingProduct?.tax?.toString() ?: "0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(
                text = if (existingProduct == null) "Add New Product" else "Edit Product",
                color = TextLight,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text("Category / Type", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                    )
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = { Text("Stock Quantity", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Selling Price ($)", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                    )
                    OutlinedTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = { Text("Cost Price ($)", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val p = Product(
                            id = existingProduct?.id ?: 0L,
                            name = name,
                            code = if (code.isBlank()) "P${(100..999).random()}" else code,
                            barcode = if (barcode.isBlank()) "${(8900000..8999999).random()}" else barcode,
                            type = type,
                            price = price.toDoubleOrNull() ?: 0.0,
                            cost = cost.toDoubleOrNull() ?: 0.0,
                            quantity = qty.toIntOrNull() ?: 0,
                            tax = tax.toDoubleOrNull() ?: 0.0
                        )
                        onSave(p)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
            ) {
                Text("Save")
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
fun AddMultiProductModal(
    onDismiss: () -> Unit,
    onSaveMultiple: (List<Product>) -> Unit
) {
    var rawText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text("Add Multiple Products (Bulk Batch)", color = TextLight, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Enter products line by line (Format: Name, Price, Quantity, Category)",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = { Text("e.g.\nMilk 1L, 2.50, 20, Dairy\nFresh Bread, 1.80, 15, Bakery\nOrange Juice, 3.20, 10, Beverages", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lines = rawText.lines().filter { it.isNotBlank() }
                    val newProducts = lines.mapNotNull { line ->
                        val parts = line.split(",").map { it.trim() }
                        if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                            val name = parts[0]
                            val price = parts.getOrNull(1)?.toDoubleOrNull() ?: 1.0
                            val qty = parts.getOrNull(2)?.toIntOrNull() ?: 10
                            val type = parts.getOrNull(3) ?: "General"
                            Product(
                                id = 0L,
                                name = name,
                                code = "P${(100..999).random()}",
                                barcode = "${(8900000..8999999).random()}",
                                type = type,
                                price = price,
                                quantity = qty
                            )
                        } else null
                    }
                    if (newProducts.isNotEmpty()) {
                        onSaveMultiple(newProducts)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
            ) {
                Text("Add All Products")
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
            Text("Price Tag Preview", color = TextLight, fontWeight = FontWeight.Bold)
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.name, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$${String.format("%.2f", product.price)}", color = Color.Red, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("*${product.barcode}*", color = Color.Black, fontSize = 14.sp)
                Text("BARCODE: ${product.barcode}", color = Color.Gray, fontSize = 10.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)) {
                Text("Print Tag")
            }
        }
    )
}
