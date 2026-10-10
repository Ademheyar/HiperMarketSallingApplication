package com.example.hipermarketsallingapplication.ui.screens

import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.User
import com.example.hipermarketsallingapplication.ui.auth.LocationData
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.DocViewModel
import com.example.hipermarketsallingapplication.ui.viewmodel.SettingsViewModel
import com.example.hipermarketsallingapplication.ui.viewmodel.UserViewModel

data class AppThemeOption(
    val name: String,
    val description: String,
    val backgroundColor: Color,
    val cardColor: Color,
    val buttonColor: Color,
    val accentColor: Color,
    val textColor: Color
)

data class ShopExpense(
    val id: Long,
    val title: String,
    val description: String,
    val amount: Double,
    val category: String,
    val date: String,
    val ref: String,
    val notes: String = "",
    val repeats: Boolean = false,
    val freq: String = "Monthly",
    val interval: Int = 1,
    val endDate: String = ""
)

data class ShopWorkerInfo(
    val id: Long,
    val fullName: String,
    val userName: String,
    val role: String,
    val shopName: String,
    val brandName: String,
    val accessLevel: Int
)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    AppAndPrinterSettingsTab(viewModel = viewModel)
}

@Composable
fun AppAndPrinterSettingsTab(viewModel: SettingsViewModel) {
    val themeOptions = listOf(
        AppThemeOption("Dark Blue Accent (Default)", "Professional dark mode with blue & green accents", SurfaceDark, CardBackground, DarkBlueAccent, SuccessGreen, TextLight),
        AppThemeOption("Cyberpunk Energy", "High contrast dark mode with energy red & bright highlights", Color(0xFF121212), Color(0xFF1E1E1E), EnergyRed, Color(0xFFFFD700), Color.White),
        AppThemeOption("Emerald Wealth", "Financial success theme with emerald green & dark slate", Color(0xFF0D1B1E), Color(0xFF1B2A32), SuccessGreen, DarkBlueAccent, Color(0xFFE0F2FE)),
        AppThemeOption("Modern Light Surface", "Clean bright theme for well-lit retail environments", Color(0xFFF1F5F9), Color(0xFFFFFFFF), DarkBlueAccent, DarkBlueAccent, Color(0xFF0F172A))
    )

    val savedThemeName by viewModel.selectedTheme.collectAsState()
    var selectedThemeName by remember(savedThemeName) { mutableStateOf(savedThemeName) }
    var showThemeSavedMessage by remember { mutableStateOf(false) }

    var appLanguage by remember { mutableStateOf("English") }
    var rememberPrinterChoice by remember { mutableStateOf(true) }
    var creditPrintoutCount by remember { mutableStateOf("1") }
    var autoPrintCreditSlips by remember { mutableStateOf(true) }
    var autoPrintAllSlips by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("📱 Application & Printer Settings (Setting.py)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            HorizontalDivider(color = DarkBlueDarker)

            // Theme Selector with Visual Swatches (Setting.py)
            Text("Select App Theme (Visual Preview):", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                themeOptions.forEach { option ->
                    val isSelected = selectedThemeName == option.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedThemeName = option.name },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) option.cardColor else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = if (isSelected) BorderStroke(2.dp, option.buttonColor) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(option.name, color = option.textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(option.description, color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    ColorSwatch("BG", option.backgroundColor)
                                    ColorSwatch("Card", option.cardColor)
                                    ColorSwatch("Button", option.buttonColor)
                                    ColorSwatch("Selected", option.accentColor)
                                    ColorSwatch("Text", option.textColor)
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = option.buttonColor)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.saveTheme(selectedThemeName)
                    showThemeSavedMessage = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply & Save Theme", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            if (showThemeSavedMessage) {
                Text("Theme applied and saved successfully!", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(color = DarkBlueDarker)

            // Printer Preferences matching Setting.py
            Text("🖨️ Printer & Slip Preferences (Setting.py)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkBlueAccent)

            MoreInfoDropdown(
                label = "Language:",
                value = appLanguage,
                options = listOf("English", "Afrikaans", "isiZulu", "Amharic", "Oromo"),
                onOptionSelected = { appLanguage = it },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = rememberPrinterChoice, onCheckedChange = { rememberPrinterChoice = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                Text("Remamber Printer Choice", fontSize = 12.sp, color = TextLight)
            }

            OutlinedTextField(
                value = creditPrintoutCount,
                onValueChange = { creditPrintoutCount = it },
                label = { Text("Allow Creadit Slips To be Printed Times:", fontSize = 10.sp, color = TextMuted) },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 54.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = autoPrintCreditSlips, onCheckedChange = { autoPrintCreditSlips = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                Text("Auto Print Creadit Slips", fontSize = 12.sp, color = TextLight)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = autoPrintAllSlips, onCheckedChange = { autoPrintAllSlips = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                Text("Auto Print All Slips", fontSize = 12.sp, color = TextLight)
            }
        }
    }
}

@Composable
fun ShopInfoSettingsTab(userViewModel: UserViewModel?) {
    val currentUser = userViewModel?.currentUser?.collectAsState()?.value
    val usersList = userViewModel?.users?.collectAsState()?.value ?: emptyList()

    var shopName by remember { mutableStateOf(currentUser?.userShop?.ifBlank { "Main Shop" } ?: "Main Shop") }
    var shopBrandName by remember { mutableStateOf("BELLEMA FASHION") }
    var shopType by remember { mutableStateOf("Clothing & Accessories Store") }
    var shopOwner by remember { mutableStateOf(currentUser?.userName ?: "AH Adem") }
    var shopEmail by remember { mutableStateOf(currentUser?.email?.ifBlank { "info@shop.com" } ?: "info@shop.com") }
    var shopCountry by remember { mutableStateOf(currentUser?.country?.ifBlank { "Ethiopia" } ?: "Ethiopia") }
    var shopContact by remember { mutableStateOf(currentUser?.phoneNum?.ifBlank { "+251900000000" } ?: "+251900000000") }
    var shopCurrency by remember { mutableStateOf("USD $") }
    var isShopEnabled by remember { mutableStateOf(true) }

    val shopPhoneNumbers = remember { mutableStateListOf("+251911121314", "+251922232425") }
    var newPhoneInput by remember { mutableStateOf("") }

    val shopTypesList = listOf(
        "Grocery & Food Store", "Clothing & Accessories Store", "Electronics & Tech Store", "Health & Beauty Store",
        "Home & Living Store", "Food Service Store", "Supermarket", "Convenience Store", "Boutique", "Pharmacy", "Restaurant", "Other"
    )

    val currenciesList = listOf("USD $", "EUR €", "GBP £", "ETB Br", "ZAR R", "KES KSh", "NGN ₦", "GHS GH₵")
    val userOptions = remember(usersList) { listOf("") + usersList.map { it.userName }.distinct() }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🏬 Shop Information & Preferences (Shop_Setting.py)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            HorizontalDivider(color = DarkBlueDarker)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isShopEnabled, onCheckedChange = { isShopEnabled = it }, colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent))
                Text("Shop Is Enabled", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextLight)
            }

            // Shop Name & Brand Name
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop Name:", fontSize = 9.sp, color = TextMuted) },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = shopBrandName,
                    onValueChange = { shopBrandName = it },
                    label = { Text("Shop Brand Name:", fontSize = 9.sp, color = TextMuted) },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            // Shop Type & Shop Owner
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MoreInfoDropdown(
                    label = "Shop Type:",
                    value = shopType,
                    options = shopTypesList,
                    onOptionSelected = { shopType = it },
                    modifier = Modifier.weight(1f)
                )

                MoreInfoDropdown(
                    label = "Shop Owner:",
                    value = shopOwner,
                    options = if (userOptions.contains(shopOwner)) userOptions else listOf(shopOwner) + userOptions,
                    onOptionSelected = { shopOwner = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Email & Contact
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = shopEmail,
                    onValueChange = { shopEmail = it },
                    label = { Text("Shop Email:", fontSize = 9.sp, color = TextMuted) },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = shopContact,
                    onValueChange = { shopContact = it },
                    label = { Text("Shop Contact:", fontSize = 9.sp, color = TextMuted) },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            // Country & Currency Dropdown
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = shopCountry,
                    onValueChange = { shopCountry = it },
                    label = { Text("Shop Country:", fontSize = 9.sp, color = TextMuted) },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
                MoreInfoDropdown(
                    label = "Shop Currency:",
                    value = shopCurrency,
                    options = currenciesList,
                    onOptionSelected = { shopCurrency = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Phone Numbers List (Shop_Setting.py)
            Text("Shop Phone Numbers:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkBlueAccent)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                shopPhoneNumbers.forEach { pNo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• $pNo", fontSize = 12.sp, color = TextLight)
                        IconButton(onClick = { shopPhoneNumbers.remove(pNo) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = EnergyRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newPhoneInput,
                        onValueChange = { newPhoneInput = it },
                        label = { Text("Add Phone No", fontSize = 8.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = {
                            if (newPhoneInput.isNotBlank()) {
                                shopPhoneNumbers.add(newPhoneInput)
                                newPhoneInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Add", fontSize = 11.sp)
                    }
                }
            }

            Button(
                onClick = { /* Save shop settings */ },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Shop Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ShopExpensesSettingsTab() {
    val expensesList = remember {
        mutableStateListOf(
            ShopExpense(1, "Store Rent", "Monthly commercial building rent", 1200.0, "Rent", "2026-10-01", "REF-801", "Paid via Bank Transfer"),
            ShopExpense(2, "Electricity Bill", "Monthly utility power bill", 340.0, "Utilities", "2026-10-05", "REF-802", "Paid Cash"),
            ShopExpense(3, "Staff Salaries", "Monthly staff wage payout", 2800.0, "Salary", "2026-10-01", "REF-803", "Payroll"),
            ShopExpense(4, "Thermal Paper Rolls", "50 rolls 80mm receipt paper", 85.0, "Supplies", "2026-10-08", "REF-804", "Receipt rolls")
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedExpenseInTable by remember { mutableStateOf<ShopExpense?>(null) }
    var showExpenseEditor by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    val filteredExpenses = remember(expensesList, searchQuery) {
        if (searchQuery.isBlank()) {
            expensesList
        } else {
            expensesList.filter { e ->
                e.title.contains(searchQuery, ignoreCase = true) ||
                        e.category.contains(searchQuery, ignoreCase = true) ||
                        e.description.contains(searchQuery, ignoreCase = true) ||
                        e.ref.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var editId by remember { mutableStateOf(0L) }
    var editTitle by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editAmount by remember { mutableStateOf("") }
    var editCategory by remember { mutableStateOf("Rent") }
    var editDate by remember { mutableStateOf("2026-10-10") }
    var editRef by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }

    val categoriesList = listOf("Rent", "Salary", "Utilities", "Supplies", "Maintenance", "Marketing", "Other")

    fun populateExpenseEditor(e: ShopExpense) {
        editId = e.id
        editTitle = e.title
        editDescription = e.description
        editAmount = e.amount.toString()
        editCategory = e.category
        editDate = e.date
        editRef = e.ref
        editNotes = e.notes
    }

    fun clearExpenseEditor() {
        editId = 0L
        editTitle = ""
        editDescription = ""
        editAmount = ""
        editCategory = "Rent"
        editDate = "2026-10-10"
        editRef = "REF-${(100..999).random()}"
        editNotes = ""
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Expenses (Shop_Expenses.py)...", fontSize = 9.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkBlueAccent) },
                    modifier = Modifier.width(220.dp).defaultMinSize(minHeight = 52.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )

                Button(
                    onClick = {
                        clearExpenseEditor()
                        isEditMode = false
                        showExpenseEditor = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Expense", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        selectedExpenseInTable?.let { e ->
                            populateExpenseEditor(e)
                            isEditMode = true
                            showExpenseEditor = true
                        }
                    },
                    enabled = selectedExpenseInTable != null,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        selectedExpenseInTable?.let { e ->
                            expensesList.removeAll { it.id == e.id }
                            selectedExpenseInTable = null
                            showExpenseEditor = false
                        }
                    },
                    enabled = selectedExpenseInTable != null,
                    colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = DarkBlueDarker)

            // Table Grid matching Shop_Expenses.py
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
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
                        TableCellHeader("Title", 150.dp)
                        TableCellHeader("Amount", 90.dp)
                        TableCellHeader("Category", 100.dp)
                        TableCellHeader("Date", 100.dp)
                        TableCellHeader("Ref", 100.dp)
                        TableCellHeader("Description", 200.dp)
                    }

                    HorizontalDivider(color = DarkBlueDarker)

                    if (filteredExpenses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No expenses recorded.", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(filteredExpenses) { expense ->
                                val isSelected = selectedExpenseInTable?.id == expense.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) DarkBlueAccent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { selectedExpenseInTable = expense }
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCellValue(expense.id.toString(), 50.dp, isBold = true)
                                    TableCellValue(expense.title, 150.dp, color = TextLight, isBold = true)
                                    TableCellValue("$${String.format(Locale.getDefault(), "%.2f", expense.amount)}", 90.dp, color = EnergyRed, isBold = true)
                                    TableCellValue(expense.category, 100.dp, color = DarkBlueAccent)
                                    TableCellValue(expense.date, 100.dp)
                                    TableCellValue(expense.ref, 100.dp)
                                    TableCellValue(expense.description, 200.dp)
                                }
                            }
                        }
                    }
                }
            }

            if (showExpenseEditor) {
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(if (isEditMode) "Edit Expense (Shop_Expenses.py)" else "Add New Expense (Shop_Expenses.py)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkBlueAccent)

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Title:", fontSize = 9.sp, color = TextMuted) }, modifier = Modifier.weight(1f).defaultMinSize(minHeight = 52.dp), singleLine = true)
                            OutlinedTextField(value = editAmount, onValueChange = { editAmount = it }, label = { Text("Amount ($):", fontSize = 9.sp, color = TextMuted) }, modifier = Modifier.weight(1f).defaultMinSize(minHeight = 52.dp), singleLine = true)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MoreInfoDropdown(label = "Category:", value = editCategory, options = categoriesList, onOptionSelected = { editCategory = it }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = editDate, onValueChange = { editDate = it }, label = { Text("Date (YYYY-MM-DD):", fontSize = 9.sp, color = TextMuted) }, modifier = Modifier.weight(1f).defaultMinSize(minHeight = 52.dp), singleLine = true)
                        }

                        OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text("Description:", fontSize = 9.sp, color = TextMuted) }, modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp), singleLine = true)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    if (editTitle.isNotBlank() && editAmount.isNotBlank()) {
                                        val newExp = ShopExpense(
                                            id = if (isEditMode) editId else System.currentTimeMillis(),
                                            title = editTitle,
                                            description = editDescription,
                                            amount = editAmount.toDoubleOrNull() ?: 0.0,
                                            category = editCategory,
                                            date = editDate,
                                            ref = editRef,
                                            notes = editNotes
                                        )
                                        if (isEditMode) {
                                            val idx = expensesList.indexOfFirst { it.id == editId }
                                            if (idx >= 0) expensesList[idx] = newExp
                                        } else {
                                            expensesList.add(newExp)
                                        }
                                        showExpenseEditor = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) { Text(if (isEditMode) "Update" else "Add", fontWeight = FontWeight.Bold) }

                            Button(onClick = { showExpenseEditor = false }, colors = ButtonDefaults.buttonColors(containerColor = EnergyRed), modifier = Modifier.weight(1f).height(40.dp)) { Text("Cancel", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopWorkersSettingsTab(userViewModel: UserViewModel?) {
    val usersList = userViewModel?.users?.collectAsState()?.value ?: emptyList()

    val workersList = remember {
        mutableStateListOf(
            ShopWorkerInfo(1, "System Admin", "admin", "OWNER", "Main Shop", "BELLEMA FASHION", 10),
            ShopWorkerInfo(2, "Cashier One", "cashier1", "WORKER", "Main Shop", "BELLEMA FASHION", 2)
        )
    }

    var selectedWorkerInTable by remember { mutableStateOf<ShopWorkerInfo?>(null) }
    var selectedWorkerToAdd by remember { mutableStateOf("") }
    var editAccessLevel by remember { mutableStateOf("2") }

    val userOptions = remember(usersList) { listOf("") + usersList.map { it.userName }.distinct() }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("👥 Workers & Security Access Control (Shop_Workers.py)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            HorizontalDivider(color = DarkBlueDarker)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoreInfoDropdown(
                    label = "Select Worker To Add:",
                    value = selectedWorkerToAdd,
                    options = userOptions,
                    onOptionSelected = { selectedWorkerToAdd = it },
                    modifier = Modifier.width(180.dp)
                )

                Button(
                    onClick = {
                        if (selectedWorkerToAdd.isNotBlank() && workersList.none { it.userName == selectedWorkerToAdd }) {
                            workersList.add(
                                ShopWorkerInfo(
                                    id = System.currentTimeMillis(),
                                    fullName = selectedWorkerToAdd,
                                    userName = selectedWorkerToAdd,
                                    role = "WORKER",
                                    shopName = "Main Shop",
                                    brandName = "BELLEMA FASHION",
                                    accessLevel = 2
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Join Worker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        selectedWorkerInTable?.let { w ->
                            val lvl = editAccessLevel.toIntOrNull() ?: 2
                            val idx = workersList.indexOfFirst { it.id == w.id }
                            if (idx >= 0) {
                                workersList[idx] = w.copy(accessLevel = lvl)
                            }
                        }
                    },
                    enabled = selectedWorkerInTable != null,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change Access Level", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        selectedWorkerInTable?.let { w ->
                            workersList.removeAll { it.id == w.id }
                            selectedWorkerInTable = null
                        }
                    },
                    enabled = selectedWorkerInTable != null,
                    colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Worker Has Left", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = DarkBlueDarker)

            // Data Table Grid matching Shop_Workers.py
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
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
                        TableCellHeader("User Full Name", 140.dp)
                        TableCellHeader("User Name", 110.dp)
                        TableCellHeader("Type", 90.dp)
                        TableCellHeader("Shop Name", 110.dp)
                        TableCellHeader("Shop Brand Name", 130.dp)
                        TableCellHeader("ACSSES Level", 100.dp)
                    }

                    HorizontalDivider(color = DarkBlueDarker)

                    if (workersList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No workers registered for this shop.", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(workersList) { worker ->
                                val isSelected = selectedWorkerInTable?.id == worker.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) DarkBlueAccent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            selectedWorkerInTable = worker
                                            editAccessLevel = worker.accessLevel.toString()
                                        }
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCellValue(worker.id.toString(), 50.dp, isBold = true)
                                    TableCellValue(worker.fullName, 140.dp, color = TextLight, isBold = true)
                                    TableCellValue(worker.userName, 110.dp)
                                    TableCellValue(worker.role, 90.dp, color = DarkBlueAccent)
                                    TableCellValue(worker.shopName, 110.dp)
                                    TableCellValue(worker.brandName, 130.dp)
                                    TableCellValue(worker.accessLevel.toString(), 100.dp, color = SuccessGreen, isBold = true)
                                }
                            }
                        }
                    }
                }
            }

            selectedWorkerInTable?.let { w ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Selected Worker: ${w.fullName} (@${w.userName})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkBlueAccent)
                        OutlinedTextField(
                            value = editAccessLevel,
                            onValueChange = { editAccessLevel = it },
                            label = { Text("Access Level (0-10):", fontSize = 8.sp, color = TextMuted) },
                            modifier = Modifier.width(130.dp).defaultMinSize(minHeight = 48.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSwatch(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, CircleShape)
        ) {}
        Text(label, color = TextMuted, fontSize = 9.sp)
    }
}
