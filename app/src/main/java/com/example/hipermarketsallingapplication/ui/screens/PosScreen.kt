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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.CartItem
import com.example.hipermarketsallingapplication.data.model.Customer
import com.example.hipermarketsallingapplication.data.model.Product
import com.example.hipermarketsallingapplication.data.model.SalesDoc
import com.example.hipermarketsallingapplication.data.model.EndOfDaySummary
import com.example.hipermarketsallingapplication.data.model.SavedCart
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.DocViewModel
import com.example.hipermarketsallingapplication.ui.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        // Top Section 1: Fixed Search Bar on Top with Inline Search Results Panel Listed Down Below
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    viewModel.searchProducts(it)
                    barcodeInput = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                label = { Text("Scan Barcode or Search Item", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = DarkBlueAccent) },
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

            // Inline Search Results Panel listed directly down below search bar
            if (searchQuery.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .padding(top = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Search Results for \"$searchQuery\" (${products.size})",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            IconButton(onClick = { viewModel.searchProducts("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Results", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }

                        HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(vertical = 4.dp))

                        if (products.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("No products match \"$searchQuery\"", color = TextMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 130.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(products) { product ->
                                    ProductTile(
                                        product = product,
                                        onClick = {
                                            viewModel.addToCart(product)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

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
            colors = CardDefaults.cardColors(containerColor = CardBackground),
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
                        color = TextLight
                    )
                    if (cart.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear Cart", color = EnergyRed)
                        }
                    }
                }

                HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(vertical = 4.dp))

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
                                onQtyChange = { newQty -> viewModel.updateCartQty(item, newQty) },
                                onRemove = { viewModel.removeFromCart(item) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(vertical = 8.dp))

                // Totals breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBlueDarker, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", color = TextMuted, fontSize = 13.sp)
                        Text("$${String.format("%.2f", viewModel.cartSubtotal)}", color = TextLight, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tax:", color = TextMuted, fontSize = 13.sp)
                        Text("$${String.format("%.2f", viewModel.cartTax)}", color = TextLight, fontSize = 13.sp)
                    }
                    HorizontalDivider(color = CardBackground, modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

    // Custom None Item Modal (F2)
    if (showCustomItemModal) {
        AlertDialog(
            onDismissRequest = { showCustomItemModal = false },
            containerColor = CardBackground,
            title = { Text("Add Non-Catalog Custom Item (F2)", color = TextLight, fontWeight = FontWeight.Bold) },
            text = { Text("Enter custom item details to add directly to sale.", color = TextMuted) },
            confirmButton = {
                Button(onClick = { showCustomItemModal = false }) { Text("Add Custom Item") }
            }
        )
    }
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
        containerColor = CardBackground,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quick Pay - Select Payment Tool", color = TextLight, fontWeight = FontWeight.Bold)
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

                HorizontalDivider(color = DarkBlueDarker)

                Text("Choose Payment Tool:", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                paymentTools.forEach { (name, icon, color) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPaySelected(name)
                            },
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = name, tint = color, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(name, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
        containerColor = CardBackground,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = DarkBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Customer", color = TextLight, fontWeight = FontWeight.Bold)
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
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
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

                HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(vertical = 2.dp))

                // Walk-in Customer Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectCustomer("Walk-in Customer")
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentCustomer == "Walk-in Customer" || currentCustomer.isBlank()) DarkBlueAccent else SurfaceDark
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = TextLight)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Walk-in Customer (Default)", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                containerColor = if (isSelected) DarkBlueAccent else SurfaceDark
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
                                    Text(c.name, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            containerColor = CardBackground,
            title = { Text("Create New Customer Account", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                    )
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Address / City", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
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
        containerColor = CardBackground,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = DarkBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payment Split Form", color = TextLight, fontWeight = FontWeight.Bold)
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
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
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
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
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
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
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
                    colors = CardDefaults.cardColors(containerColor = DarkBlueDarker),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payable:", color = TextMuted, fontSize = 12.sp)
                            Text("$${String.format("%.2f", totalAmount)}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid So Far:", color = TextMuted, fontSize = 12.sp)
                            Text("$${String.format("%.2f", totalPaid)}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        HorizontalDivider(color = CardBackground, modifier = Modifier.padding(vertical = 2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (remainingBalance > 0) "Remaining Balance:" else "Change Due:", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
        containerColor = CardBackground,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = EnergyRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("End of Day Sales & Summary", color = TextLight, fontWeight = FontWeight.Bold)
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
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Daily Gross Revenue: $${String.format("%.2f", s.totalRevenue)}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Transactions Count: ${s.totalSalesCount} receipts", color = TextLight, fontSize = 12.sp)
                            Text("Estimated Profit: $${String.format("%.2f", s.totalProfit)}", color = EnergyRed, fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(vertical = 4.dp))
                Text("Recent Sales Receipts", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(docs) { doc ->
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
                                    Text(doc.docBarcode, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
    var displayExpression by remember { mutableStateOf("0") }

    fun appendChar(char: String) {
        if (displayExpression == "0" && char != ".") {
            displayExpression = char
        } else {
            displayExpression += char
        }
    }

    fun calculateResult() {
        try {
            val sanitized = displayExpression.replace("×", "*").replace("÷", "/")
            val result = evaluateSimpleExpression(sanitized)
            displayExpression = String.format("%.2f", result).removeSuffix(".00")
        } catch (e: Exception) {
            displayExpression = "Error"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Calculator (F1)", color = TextLight, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Calculator Display
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = displayExpression,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = TextLight,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }

                // Calculator Keypad
                val keys = listOf(
                    listOf("C", "÷", "×", "⌫"),
                    listOf("7", "8", "9", "-"),
                    listOf("4", "5", "6", "+"),
                    listOf("1", "2", "3", "="),
                    listOf("0", ".", "", "")
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    keys.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { key ->
                                if (key.isNotEmpty()) {
                                    Button(
                                        onClick = {
                                            when (key) {
                                                "C" -> displayExpression = "0"
                                                "⌫" -> {
                                                    displayExpression = if (displayExpression.length > 1) displayExpression.dropLast(1) else "0"
                                                }
                                                "=" -> calculateResult()
                                                else -> appendChar(key)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = when (key) {
                                                "=", "C" -> DarkBlueAccent
                                                "+", "-", "×", "÷" -> DarkBlueSecondary
                                                else -> SurfaceDark
                                            }
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(key, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)) {
                Text("Close")
            }
        }
    )
}

fun evaluateSimpleExpression(expression: String): Double {
    val tokens = expression.split(Regex("(?<=[+*-/])|(?=[+*-/])")).map { it.trim() }.filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return 0.0
    var current = tokens[0].toDoubleOrNull() ?: 0.0
    var i = 1
    while (i < tokens.size - 1) {
        val op = tokens[i]
        val nextVal = tokens[i + 1].toDoubleOrNull() ?: 0.0
        when (op) {
            "+" -> current += nextVal
            "-" -> current -= nextVal
            "*" -> current *= nextVal
            "/" -> if (nextVal != 0.0) current /= nextVal
        }
        i += 2
    }
    return current
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
        containerColor = CardBackground,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Activets (F6) - Hold Sales Charts", color = TextLight, fontWeight = FontWeight.Bold)
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

                HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(vertical = 4.dp))

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
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
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
                                        Text(sc.name, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
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
                    color = TextLight,
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
    onQtyChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(6.dp))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.name,
                color = TextLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$${String.format("%.2f", item.price)} x ${item.qty.toInt()} = $${String.format("%.2f", item.subtotal)}",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = { onQtyChange(item.qty - 1) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = DarkBlueAccent)
            }

            Text(
                text = "${item.qty.toInt()}",
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(
                onClick = { onQtyChange(item.qty + 1) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = DarkBlueAccent)
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = EnergyRed)
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
        containerColor = CardBackground,
        title = {
            Text("Select Payment Method", color = TextLight, fontWeight = FontWeight.Bold)
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
                        Text(method, color = TextLight, fontWeight = FontWeight.SemiBold)
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
        containerColor = CardBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sale Complete & Slip Printed", color = TextLight, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("HIPERMARKET RECEIPT", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Shop #1 - Main Terminal", color = TextMuted, fontSize = 12.sp)
                HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(vertical = 8.dp))
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

