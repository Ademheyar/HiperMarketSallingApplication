package com.example.hipermarketsallingapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.*
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.DocViewModel
import com.example.hipermarketsallingapplication.ui.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.hipermarketsallingapplication.ui.viewmodel.UserViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction

data class SplitPaymentRow(
    val type: String,
    val amount: Double,
    val reference: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel,
    userViewModel: UserViewModel,
    docViewModel: DocViewModel,
    activeUser: String = "admin"
) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val checkoutStatus by viewModel.checkoutSuccess.collectAsState()
    val docs by docViewModel.docs.collectAsState()
    val summary by docViewModel.summary.collectAsState()
    val savedCarts by viewModel.savedCarts.collectAsState()

    val currentUser by userViewModel.currentUser.collectAsState()
    val userWorkShops by userViewModel.userWorkingShops.collectAsState()
    val activeShop = currentUser?.userShop ?: ""

    // Automatically load products for the active logged-in shop!
    LaunchedEffect(activeShop, userWorkShops) {
        viewModel.loadProductsForShop(activeShop, userWorkShops)
    }

    var selectedProductForVariant by remember { mutableStateOf<Product?>(null) }

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showQuickPayModal by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showEndDayModal by remember { mutableStateOf(false) }
    var showCalculatorModal by remember { mutableStateOf(false) }
    var showCustomItemModal by remember { mutableStateOf(false) }
    var showSavedSalesModal by remember { mutableStateOf(false) }
    var showCustomerSelectionModal by remember { mutableStateOf(false) }
    var lastReceiptCode by remember { mutableStateOf("") }
    var barcodeInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        // Top Section 1: Reusable PosSearchEntry from SearchEntry.kt (searchbox.py)
        PosSearchEntry(
            searchQuery = searchQuery,
            onSearchQueryChange = {
                viewModel.searchProducts(it)
                barcodeInput = it
            },
            products = products,
            activeShop = activeShop,
            onAddToCart = { selectedProd, addQty, selectedColor, selectedSize, selectedShop ->
                val variantProd = selectedProd.copy(
                    atShop = selectedShop,
                    code = if (selectedColor != "Default" || selectedSize != "M") "${selectedProd.code}-$selectedColor-$selectedSize" else selectedProd.code
                )
                viewModel.addToCart(variantProd, addQty.toInt())
            },
            onClearSearch = {
                viewModel.searchProducts("")
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Top Section 2: 2-Row Horizontal Scrolling Action Bar (All buttons from Display.py)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Row 1 Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Customer Button (labeled "Customer")
                    PosTopActionButton(
                        label = if (selectedCustomer.isBlank() || selectedCustomer == "Walk-in Customer") "Customer" else "Customer: $selectedCustomer",
                        icon = Icons.Default.Person,
                        color = DarkBlueSecondary,
                        onClick = { showCustomerSelectionModal = true }
                    )

                    PosTopActionButton(
                        label = "Calcu (F1)",
                        icon = Icons.Default.Calculate,
                        color = DarkBlueSecondary,
                        onClick = { showCalculatorModal = true }
                    )

                    PosTopActionButton(
                        label = "None (F2)",
                        icon = Icons.Default.AddBox,
                        color = DarkBlueSecondary,
                        onClick = { showCustomItemModal = true }
                    )

                    PosTopActionButton(
                        label = "Activets (${savedCarts.size}) (F6)",
                        icon = Icons.Default.PendingActions,
                        color = if (savedCarts.isNotEmpty()) SuccessGreen else DarkBlueSecondary,
                        onClick = { showSavedSalesModal = true }
                    )

                    PosTopActionButton(
                        label = "Update (Ctrl+U)",
                        icon = Icons.Default.Sync,
                        color = DarkBlueSecondary,
                        onClick = { docViewModel.loadDocs() }
                    )
                }

                // Row 2 Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Quick Pay Button
                    PosTopActionButton(
                        label = "Quick Pay",
                        icon = Icons.Default.ShoppingCart,
                        color = if (cart.isNotEmpty()) SuccessGreen else SuccessGreen.copy(alpha = 0.4f),
                        enabled = cart.isNotEmpty(),
                        onClick = {
                            if (cart.isNotEmpty()) {
                                showQuickPayModal = true
                            }
                        }
                    )

                    PosTopActionButton(
                        label = "Split Pay (F10)",
                        icon = Icons.Default.Payment,
                        color = if (cart.isNotEmpty()) DarkBlueAccent else DarkBlueAccent.copy(alpha = 0.4f),
                        enabled = cart.isNotEmpty(),
                        onClick = { if (cart.isNotEmpty()) showPaymentDialog = true }
                    )

                    PosTopActionButton(
                        label = "Cash Drawer",
                        icon = Icons.Default.MeetingRoom,
                        color = DarkBlueSecondary,
                        onClick = { }
                    )

                    PosTopActionButton(
                        label = "End Day (Ctrl+E)",
                        icon = Icons.Default.Assessment,
                        color = DarkBlueSecondary,
                        onClick = {
                            docViewModel.loadDocs()
                            showEndDayModal = true
                        }
                    )
                }
            }
        }

        // Main Body Layout: Selected Items Cart List Full Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Sale Cart (${cart.size} items)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (cart.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear Cart", color = EnergyRed)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                if (cart.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.RemoveShoppingCart,
                                contentDescription = "Empty Cart",
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cart is empty", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Type or scan barcode in top search bar to add items", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(cart) { item ->
                            CartItemRow(
                                item = item,
                                onUpdateItem = { updatedItem -> viewModel.updateCartItem(updatedItem) },
                                onSplitItem = { itemToSplit -> viewModel.splitCartItem(itemToSplit) },
                                onRemove = { viewModel.removeFromCart(item) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                // Totals breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", color = TextMuted, fontSize = 13.sp)
                        Text("$${String.format("%.2f", viewModel.cartSubtotal)}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tax:", color = TextMuted, fontSize = 13.sp)
                        Text("$${String.format("%.2f", viewModel.cartTax)}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "$${String.format("%.2f", viewModel.cartTotal)}",
                            color = EnergyRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }

    // Quick Pay Tools Selection Modal
    if (showQuickPayModal) {
        QuickPayToolsModal(
            totalAmount = viewModel.cartTotal,
            onDismiss = { showQuickPayModal = false },
            onPaySelected = { method ->
                showQuickPayModal = false
                lastReceiptCode = viewModel.checkout(method, activeUser)
                docViewModel.loadDocs()
                showReceiptDialog = true
            }
        )
    }

    // Customer Selection Modal (ChooseCustemr.py)
    if (showCustomerSelectionModal) {
        CustomerSelectionModal(
            customers = customers,
            currentCustomer = selectedCustomer,
            onSelectCustomer = { name -> viewModel.setCustomer(name) },
            onCreateCustomer = { name, phone, email, address ->
                viewModel.setCustomer(name)
            },
            onDismiss = { showCustomerSelectionModal = false }
        )
    }

    // Split Payment Form Modal (Peymentsplit.py)
    if (showPaymentDialog) {
        SplitPaymentModal(
            totalAmount = viewModel.cartTotal,
            onDismiss = { showPaymentDialog = false },
            onPayConfirmed = { payMethod ->
                showPaymentDialog = false
                lastReceiptCode = viewModel.checkout(payMethod, activeUser)
                docViewModel.loadDocs()
                showReceiptDialog = true
            }
        )
    }

    // Top-Level Floating End Day Panel Modal
    if (showEndDayModal) {
        EndDaySummaryModal(
            summary = summary,
            docs = docs,
            onDismiss = { showEndDayModal = false }
        )
    }

    // Activets (F6) Hold Carts Modal
    if (showSavedSalesModal) {
        SavedSalesModal(
            savedCarts = savedCarts,
            onHoldCurrentCart = { viewModel.holdCurrentCart() },
            onSelectCart = { sc -> viewModel.restoreSavedCart(sc) },
            onDeleteCart = { sc -> viewModel.deleteSavedCart(sc) },
            onDismiss = { showSavedSalesModal = false }
        )
    }

    // Quick Calculator Modal (F1)
    if (showCalculatorModal) {
        PosCalculatorModal(
            onDismiss = { showCalculatorModal = false }
        )
    }

    // Printed Receipt Simulation Modal
    if (showReceiptDialog) {
        ReceiptPreviewModal(
            receiptCode = lastReceiptCode,
            statusText = checkoutStatus ?: "Success",
            onDismiss = {
                showReceiptDialog = false
                viewModel.clearCheckoutStatus()
            }
        )
    }

    // Custom None Item Modal (F2) matching GetVALUE.py
    if (showCustomItemModal) {
        GetValueDialog(
            titles = listOf("Enter Custom Item Price ($)", "Enter Item Quantity"),
            initialValue = "10.00",
            onValueConfirmed = { values ->
                val customPrice = values.getOrNull(0) ?: 10.0
                val customQty = values.getOrNull(1) ?: 1.0
                val customProduct = Product(
                    id = System.currentTimeMillis(),
                    name = "Custom Unregistered Item (F2)",
                    code = "NONE-${(100..999).random()}",
                    barcode = "NONE-${(1000..9999).random()}",
                    type = "Custom",
                    price = customPrice,
                    quantity = customQty.toInt()
                )
                viewModel.addToCart(customProduct, customQty.toInt())
                showCustomItemModal = false
            },
            onDismiss = { showCustomItemModal = false }
        )
    }

    // POS Item & Variant Selector Modal (searchbox.py / ItemSelectorWidget)
    selectedProductForVariant?.let { prod ->
        PosItemSelectorDialog(
            product = prod,
            activeShop = activeShop,
            onDismiss = { selectedProductForVariant = null },
            onAddToCart = { selectedProd, addQty, selectedColor, selectedSize, selectedShop ->
                val variantProd = selectedProd.copy(
                    atShop = selectedShop,
                    code = if (selectedColor != "Default" || selectedSize != "M") "${selectedProd.code}-$selectedColor-$selectedSize" else selectedProd.code
                )
                viewModel.addToCart(variantProd, addQty.toInt())
                selectedProductForVariant = null
            }
        )
    }
}

@Composable
fun PosItemSelectorDialog(
    product: Product,
    activeShop: String,
    onDismiss: () -> Unit,
    onAddToCart: (Product, Double, String, String, String) -> Unit
) {
    val moreInfoParser = remember(product.moreInfo, activeShop, product.code) {
        MoreInfoParser(
            moreInfoJson = product.moreInfo,
            fallbackShop = if (product.atShop.isNotBlank() && product.atShop != "All Shops") product.atShop else activeShop.ifBlank { "Main Shop" },
            fallbackCode = product.code.ifBlank { "P101" },
            fallbackColor = "Default",
            fallbackSize = "M"
        )
    }

    val shopOptions = remember(moreInfoParser) { moreInfoParser.getShops() }
    var selectedShop by remember(product, activeShop) { mutableStateOf(if (shopOptions.size == 1) shopOptions.first() else if (product.atShop.isNotBlank() && product.atShop != "All Shops") product.atShop else activeShop.ifBlank { shopOptions.first() }) }

    val codeOptions = remember(moreInfoParser, selectedShop) { moreInfoParser.getCodes(selectedShop) }
    var selectedCode by remember(product, selectedShop) { mutableStateOf(if (codeOptions.size == 1) codeOptions.first() else product.code.ifBlank { codeOptions.first() }) }

    val colorOptions = remember(moreInfoParser, selectedShop, selectedCode) { moreInfoParser.getColors(selectedShop, selectedCode) }
    var selectedColor by remember(product, selectedShop, selectedCode) { mutableStateOf(if (colorOptions.size == 1) colorOptions.first() else colorOptions.first()) }

    val sizeOptions = remember(moreInfoParser, selectedShop, selectedCode, selectedColor) { moreInfoParser.getSizes(selectedShop, selectedCode, selectedColor) }
    var selectedSize by remember(product, selectedShop, selectedCode, selectedColor) { mutableStateOf(if (sizeOptions.size == 1) sizeOptions.first() else sizeOptions.first()) }

    var qtyInput by remember { mutableStateOf("1.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(product.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Code: $selectedCode | Barcode: ${product.barcode}", fontSize = 11.sp, color = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Stock Available: ${product.quantity} units", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                            Text("Category: ${product.type}", fontSize = 11.sp, color = TextMuted)
                        }
                        Text("$${String.format(Locale.getDefault(), "%.2f", product.price)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlueAccent)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MoreInfoDropdown(
                        label = "Shop",
                        value = selectedShop,
                        options = shopOptions,
                        onOptionSelected = { selectedShop = it },
                        modifier = Modifier.weight(1f)
                    )

                    MoreInfoDropdown(
                        label = "Code",
                        value = selectedCode,
                        options = codeOptions,
                        onOptionSelected = { selectedCode = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MoreInfoDropdown(
                        label = "Color",
                        value = selectedColor,
                        options = colorOptions,
                        onOptionSelected = { selectedColor = it },
                        modifier = Modifier.weight(1f)
                    )

                    MoreInfoDropdown(
                        label = "Size",
                        value = selectedSize,
                        options = sizeOptions,
                        onOptionSelected = { selectedSize = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = qtyInput,
                    onValueChange = { qtyInput = it },
                    label = { Text("Add QTY", fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val addQty = qtyInput.toDoubleOrNull() ?: 1.0
                    onAddToCart(product, addQty, selectedColor, selectedSize, selectedShop)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Item To Cart")
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
fun QuickPayToolsModal(
    totalAmount: Double,
    onDismiss: () -> Unit,
    onPaySelected: (String) -> Unit
) {
    val paymentTools = listOf(
        Triple("Cash", Icons.Default.Money, SuccessGreen),
        Triple("Credit Card", Icons.Default.CreditCard, DarkBlueAccent),
        Triple("Mobile Payment", Icons.Default.QrCode, DarkBlueSecondary),
        Triple("Store Credit / Debt", Icons.Default.AccountBalanceWallet, EnergyRed),
        Triple("Voucher / Coupon", Icons.Default.CardGiftcard, DarkBlueAccent)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quick Pay - Select Payment Tool", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Total Amount Due: $${String.format("%.2f", totalAmount)}",
                    color = EnergyRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text("Choose Payment Tool:", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                paymentTools.forEach { (name, icon, color) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPaySelected(name)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = name, tint = color, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Button(
                                onClick = { onPaySelected(name) },
                                colors = ButtonDefaults.buttonColors(containerColor = color),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Pay Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CustomerSelectionModal(
    customers: List<Customer>,
    currentCustomer: String,
    onSelectCustomer: (String) -> Unit,
    onCreateCustomer: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers else customers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = DarkBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Customer", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Customer by Name, Phone...", fontSize = 11.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkBlueAccent) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Registered Customers (${filteredCustomers.size})", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create New", fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 2.dp))

                // Walk-in Customer Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectCustomer("Walk-in Customer")
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentCustomer == "Walk-in Customer" || currentCustomer.isBlank()) DarkBlueAccent else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Walk-in Customer (Default)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Customer List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCustomers) { c ->
                        val isSelected = currentCustomer == c.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCustomer(c.name)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DarkBlueAccent else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(c.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Phone: ${c.phone} • ${c.email}", color = TextMuted, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        onSelectCustomer(c.name)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Select", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary)) {
                Text("Close")
            }
        }
    )

    // Create New Customer Modal
    if (showCreateDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var newAddress by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Create New Customer Account", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                    )
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Address / City", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onCreateCustomer(newName, newPhone, newEmail, newAddress)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
                ) {
                    Text("Create & Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun SplitPaymentModal(
    totalAmount: Double,
    onDismiss: () -> Unit,
    onPayConfirmed: (String) -> Unit
) {
    var selectedPaymentType by remember { mutableStateOf("Cash") }
    var paymentAmountInput by remember { mutableStateOf("") }
    var referenceInput by remember { mutableStateOf("") }
    val paymentRows = remember { mutableStateListOf<SplitPaymentRow>() }

    val paymentTypes = listOf("Cash", "Credit Card", "Mobile Payment", "Store Credit / Debt", "Voucher")
    val totalPaid = paymentRows.sumOf { it.amount }
    val remainingBalance = (totalAmount - totalPaid).coerceAtLeast(0.0)
    val changeDue = (totalPaid - totalAmount).coerceAtLeast(0.0)

    LaunchedEffect(remainingBalance) {
        if (remainingBalance > 0 && paymentAmountInput.isBlank()) {
            paymentAmountInput = String.format("%.2f", remainingBalance)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = DarkBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payment Split Form", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Payment Add Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Payment Type Selector Dropdown
                            var showTypeMenu by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { showTypeMenu = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Text(selectedPaymentType, fontSize = 12.sp)
                                }
                                DropdownMenu(
                                    expanded = showTypeMenu,
                                    onDismissRequest = { showTypeMenu = false }
                                ) {
                                    paymentTypes.forEach { pt ->
                                        DropdownMenuItem(
                                            text = { Text(pt) },
                                            onClick = {
                                                selectedPaymentType = pt
                                                showTypeMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Reference
                            OutlinedTextField(
                                value = referenceInput,
                                onValueChange = { referenceInput = it },
                                label = { Text("Reference / Auth #", fontSize = 10.sp, color = TextMuted) },
                                modifier = Modifier.weight(1f).height(46.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = paymentAmountInput,
                                onValueChange = { paymentAmountInput = it },
                                label = { Text("Amount ($)", fontSize = 10.sp, color = TextMuted) },
                                modifier = Modifier.weight(1f).height(46.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Button(
                                onClick = {
                                    val amt = paymentAmountInput.toDoubleOrNull() ?: 0.0
                                    if (amt > 0) {
                                        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                        paymentRows.add(
                                            SplitPaymentRow(
                                                type = selectedPaymentType,
                                                amount = amt,
                                                reference = if (referenceInput.isBlank()) "Ref-${(100..999).random()}" else referenceInput,
                                                timestamp = sdf.format(Date())
                                            )
                                        )
                                        referenceInput = ""
                                        val newRemaining = (totalAmount - (totalPaid + amt)).coerceAtLeast(0.0)
                                        paymentAmountInput = if (newRemaining > 0) String.format("%.2f", newRemaining) else "0.00"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Text("Add Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Table of Partial Payments
                Text("Applied Payment Split Rows (${paymentRows.size})", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(paymentRows) { row ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${row.type} • ${row.reference}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Time: ${row.timestamp}", color = TextMuted, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$${String.format("%.2f", row.amount)}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    IconButton(
                                        onClick = { paymentRows.remove(row) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = EnergyRed)
                                    }
                                }
                            }
                        }
                    }
                }

                // Financial Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payable:", color = TextMuted, fontSize = 12.sp)
                            Text("$${String.format("%.2f", totalAmount)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid So Far:", color = TextMuted, fontSize = 12.sp)
                            Text("$${String.format("%.2f", totalPaid)}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (remainingBalance > 0) "Remaining Balance:" else "Change Due:", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (remainingBalance > 0) "$${String.format("%.2f", remainingBalance)}" else "$${String.format("%.2f", changeDue)}",
                                color = if (remainingBalance > 0) EnergyRed else SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val summaryStr = paymentRows.joinToString(" + ") { "${it.type}: $${String.format("%.2f", it.amount)}" }
                    val finalPayType = if (paymentRows.isEmpty()) "Cash" else "Split ($summaryStr)"
                    onPayConfirmed(finalPayType)
                },
                enabled = totalPaid >= totalAmount || paymentRows.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
            ) {
                Text("Continue Checkout")
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
fun EndDaySummaryModal(
    summary: EndOfDaySummary?,
    docs: List<SalesDoc>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = EnergyRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("End of Day Sales & Summary", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                summary?.let { s ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Daily Gross Revenue: $${String.format("%.2f", s.totalRevenue)}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Transactions Count: ${s.totalSalesCount} receipts", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            Text("Estimated Profit: $${String.format("%.2f", s.totalProfit)}", color = EnergyRed, fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                Text("Recent Sales Receipts", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(docs) { doc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(doc.docBarcode, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Customer: ${doc.customerId} • ${doc.docCreatedDate}", color = TextMuted, fontSize = 10.sp)
                                }
                                Text("$${String.format("%.2f", doc.price)}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
            ) {
                Text("Close Summary")
            }
        }
    )
}

@Composable
fun PosCalculatorModal(
    onDismiss: () -> Unit
) {
    GetValueDialog(
        titles = listOf("Calculator (F1)"),
        initialValue = "0",
        onDismiss = onDismiss
    )
}

@Composable
fun SavedSalesModal(
    savedCarts: List<SavedCart>,
    onHoldCurrentCart: () -> Unit,
    onSelectCart: (SavedCart) -> Unit,
    onDeleteCart: (SavedCart) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Activets (F6) - Hold Sales Charts", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onHoldCurrentCart()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
                ) {
                    Icon(Icons.Default.PauseCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hold Current Cart & Open New Chart", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                Text("Saved Active Charts (${savedCarts.size})", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                if (savedCarts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No active hold charts saved.", color = TextMuted, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(savedCarts) { sc ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectCart(sc)
                                        onDismiss()
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(sc.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Customer: ${sc.customer} • ${sc.items.size} items • ${sc.timestamp}", color = TextMuted, fontSize = 11.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = {
                                                onSelectCart(sc)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Resume", fontSize = 11.sp)
                                        }
                                        IconButton(onClick = { onDeleteCart(sc) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EnergyRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary)) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PosTopActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
fun ProductTile(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = product.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.type,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    color = EnergyRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Stock: ${product.quantity}",
                    color = if (product.quantity < 10) EnergyRed else TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onUpdateItem: (CartItem) -> Unit,
    onSplitItem: (CartItem) -> Unit = {},
    onRemove: () -> Unit
) {
    val moreInfoParser = remember(item.product.moreInfo, item.product.atShop, item.product.code) {
        MoreInfoParser(
            moreInfoJson = item.product.moreInfo,
            fallbackShop = item.product.atShop.ifBlank { "Main Shop" },
            fallbackCode = item.product.code.ifBlank { "P101" },
            fallbackColor = "Default",
            fallbackSize = "M"
        )
    }

    val shopOptions = remember(moreInfoParser) { moreInfoParser.getShops() }
    var shopInput by remember(item) { mutableStateOf(if (shopOptions.size == 1) shopOptions.first() else item.product.atShop.ifBlank { shopOptions.first() }) }

    val codeOptions = remember(moreInfoParser, shopInput) { moreInfoParser.getCodes(shopInput) }
    var codeInput by remember(item, shopInput) { mutableStateOf(if (codeOptions.size == 1) codeOptions.first() else item.product.code.ifBlank { codeOptions.first() }) }

    val colorOptions = remember(moreInfoParser, shopInput, codeInput) { moreInfoParser.getColors(shopInput, codeInput) }
    var colorInput by remember(item, shopInput, codeInput) { mutableStateOf(if (colorOptions.size == 1) colorOptions.first() else colorOptions.first()) }

    val sizeOptions = remember(moreInfoParser, shopInput, codeInput, colorInput) { moreInfoParser.getSizes(shopInput, codeInput, colorInput) }
    var sizeInput by remember(item, shopInput, codeInput, colorInput) { mutableStateOf(if (sizeOptions.size == 1) sizeOptions.first() else sizeOptions.first()) }

    var qtyInput by remember(item) { mutableStateOf(item.qty.toInt().toString()) }
    var priceInput by remember(item) { mutableStateOf(String.format(Locale.getDefault(), "%.2f", item.price)) }
    var totalPriceInput by remember(item) { mutableStateOf(String.format(Locale.getDefault(), "%.2f", item.subtotal)) }

    // Auto-update price if moreInfo specifies custom price for variant
    LaunchedEffect(shopInput, codeInput, colorInput, sizeInput) {
        val details = moreInfoParser.getItemDetails(shopInput, codeInput, colorInput, sizeInput)
        if (details?.price != null && details.price > 0 && details.price != item.price) {
            priceInput = String.format(Locale.getDefault(), "%.2f", details.price)
            totalPriceInput = String.format(Locale.getDefault(), "%.2f", details.price * item.qty)
            onUpdateItem(item.copy(price = details.price))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header Line: Avatar Icon + Product Name + Barcode + Unit Price & Total Price Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = DarkBlueAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Barcode: ${item.product.barcode} | Type: ${item.product.type}",
                            fontSize = 10.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", item.subtotal)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SuccessGreen,
                    maxLines = 1
                )
            }

            HorizontalDivider(color = DarkBlueDarker)

            // Controls Line 1: Editable QTY, Unit Price, Total Price, and Action Buttons ('-' and 'v' / '+')
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Editable QTY
                OutlinedTextField(
                    value = qtyInput,
                    onValueChange = { input ->
                        qtyInput = input
                        val newQty = input.toDoubleOrNull() ?: item.qty
                        if (newQty > 0) {
                            val newTotal = newQty * item.price
                            totalPriceInput = String.format(Locale.getDefault(), "%.2f", newTotal)
                            onUpdateItem(item.copy(qty = newQty))
                        }
                    },
                    label = { Text("QTY", fontSize = 9.sp, color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 56.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )

                // Editable Unit Price
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { input ->
                        priceInput = input
                        val newPrice = input.toDoubleOrNull() ?: item.price
                        val newTotal = item.qty * newPrice
                        totalPriceInput = String.format(Locale.getDefault(), "%.2f", newTotal)
                        onUpdateItem(item.copy(price = newPrice))
                    },
                    label = { Text("Price ($)", fontSize = 9.sp, color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.2f).defaultMinSize(minHeight = 56.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )

                // Editable Total Price
                OutlinedTextField(
                    value = totalPriceInput,
                    onValueChange = { input ->
                        totalPriceInput = input
                        val newTotal = input.toDoubleOrNull() ?: item.subtotal
                        if (item.qty > 0) {
                            val newUnitPrice = newTotal / item.qty
                            priceInput = String.format(Locale.getDefault(), "%.2f", newUnitPrice)
                            onUpdateItem(item.copy(price = newUnitPrice))
                        }
                    },
                    label = { Text("Total ($)", fontSize = 9.sp, color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.2f).defaultMinSize(minHeight = 56.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )

                // Action Buttons matching Display.py (del_button '-' and list_button 'v' or '+')
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    // '-' Button (Decrements qty or removes item)
                    Button(
                        onClick = {
                            if (item.qty > 1) {
                                onUpdateItem(item.copy(qty = item.qty - 1))
                            } else {
                                onRemove()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // 'v' or '+' Button (Display.py list_button)
                    Button(
                        onClick = {
                            if (item.qty <= 1) {
                                onUpdateItem(item.copy(qty = item.qty + 1))
                            } else {
                                onSplitItem(item)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = if (item.qty <= 1) "+" else "v",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Controls Line 2: Dropdowns for Variants (Shop, Code, Color, Size)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoreInfoDropdown(
                    label = "Shop",
                    value = shopInput,
                    options = shopOptions,
                    onOptionSelected = { shopInput = it },
                    modifier = Modifier.weight(1f)
                )

                MoreInfoDropdown(
                    label = "Code",
                    value = codeInput,
                    options = codeOptions,
                    onOptionSelected = { codeInput = it },
                    modifier = Modifier.weight(1f)
                )

                MoreInfoDropdown(
                    label = "Color",
                    value = colorInput,
                    options = colorOptions,
                    onOptionSelected = { colorInput = it },
                    modifier = Modifier.weight(1f)
                )

                MoreInfoDropdown(
                    label = "Size",
                    value = sizeInput,
                    options = sizeOptions,
                    onOptionSelected = { sizeInput = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PaymentMethodModal(
    totalAmount: Double,
    onDismiss: () -> Unit,
    onPayConfirmed: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Select Payment Method", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total Payable: $${String.format("%.2f", totalAmount)}",
                    color = EnergyRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val methods = listOf("Cash", "Credit Card", "Mobile Payment", "Store Credit / Debt")
                methods.forEach { method ->
                    Button(
                        onClick = { onPayConfirmed(method) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(method, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun ReceiptPreviewModal(
    receiptCode: String,
    statusText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sale Complete & Slip Printed", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("HIPERMARKET RECEIPT", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Shop #1 - Main Terminal", color = TextMuted, fontSize = 12.sp)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                Text(receiptCode, color = DarkBlueAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(statusText, color = SuccessGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Thank you for shopping with us!", color = TextMuted, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
            ) {
                Text("Close")
            }
        }
    )
}

fun shouldShowProductCatalog(products: List<Product>, searchQuery: String): Boolean {
    return searchQuery.isNotBlank() && products.isNotEmpty()
}

