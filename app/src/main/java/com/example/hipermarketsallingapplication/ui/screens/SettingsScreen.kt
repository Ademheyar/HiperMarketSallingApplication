package com.example.hipermarketsallingapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.hipermarketsallingapplication.data.model.Customer
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.SettingsViewModel

data class AppThemeOption(
    val name: String,
    val description: String,
    val backgroundColor: Color,
    val cardColor: Color,
    val buttonColor: Color,
    val accentColor: Color,
    val textColor: Color
)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    var showCustomerModal by remember { mutableStateOf(false) }

    val themeOptions = listOf(
        AppThemeOption(
            name = "Dark Blue Accent (Default)",
            description = "Professional dark mode with blue & green accents",
            backgroundColor = SurfaceDark,
            cardColor = CardBackground,
            buttonColor = DarkBlueAccent,
            accentColor = SuccessGreen,
            textColor = TextLight
        ),
        AppThemeOption(
            name = "Cyberpunk Energy",
            description = "High contrast dark mode with energy red & bright highlights",
            backgroundColor = Color(0xFF121212),
            cardColor = Color(0xFF1E1E1E),
            buttonColor = EnergyRed,
            accentColor = Color(0xFFFFD700),
            textColor = Color.White
        ),
        AppThemeOption(
            name = "Emerald Wealth",
            description = "Financial success theme with emerald green & dark slate",
            backgroundColor = Color(0xFF0D1B1E),
            cardColor = Color(0xFF1B2A32),
            buttonColor = SuccessGreen,
            accentColor = DarkBlueAccent,
            textColor = Color(0xFFE0F2FE)
        ),
        AppThemeOption(
            name = "Modern Light Surface",
            description = "Clean bright theme for well-lit retail environments",
            backgroundColor = Color(0xFFF1F5F9),
            cardColor = Color(0xFFFFFFFF),
            buttonColor = DarkBlueAccent,
            accentColor = SuccessGreen,
            textColor = Color(0xFF0F172A)
        )
    )

    val savedThemeName by viewModel.selectedTheme.collectAsState()
    var selectedThemeName by remember(savedThemeName) { mutableStateOf(savedThemeName) }
    var showThemeSavedMessage by remember { mutableStateOf(false) }

    var appLanguage by remember { mutableStateOf("English (US)") }
    var printerDeviceName by remember { mutableStateOf("POS-80 Thermal Receipt Printer") }
    var cutAfterPrint by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Application & Printer Settings Card with Visual Theme Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = DarkBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Application & Printer Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextLight)
                }
                Divider(color = DarkBlueDarker)

                Text("Select App Theme (Visual Preview):", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(SurfaceDark, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(themeOptions) { option ->
                        val isSelected = selectedThemeName == option.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedThemeName = option.name },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) option.cardColor else SurfaceDark
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
                                    // Visual color swatches
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

                Spacer(modifier = Modifier.height(4.dp))

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
                    Text("Apply & Save Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                if (showThemeSavedMessage) {
                    Text(
                        text = "Theme applied and saved successfully!",
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = appLanguage,
                        onValueChange = { appLanguage = it },
                        label = { Text("Language", color = TextMuted, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = printerDeviceName,
                        onValueChange = { printerDeviceName = it },
                        label = { Text("Thermal Printer Device", color = TextMuted, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cut After Print", color = TextLight, fontSize = 13.sp)
                        Switch(
                            checked = cutAfterPrint,
                            onCheckedChange = { cutAfterPrint = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SuccessGreen)
                        )
                    }
                }
            }
        }

        Button(
            onClick = { showCustomerModal = true },
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register New Customer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }

    if (showCustomerModal) {
        AddCustomerModal(
            onDismiss = { showCustomerModal = false },
            onSave = { cust ->
                viewModel.addCustomer(cust)
                showCustomerModal = false
            }
        )
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

@Composable
fun AddCustomerModal(
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text("Register New Customer", color = TextLight, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Customer Full Name", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        val c = Customer(name = nameInput, phone = phone, email = email)
                        onSave(c)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
            ) {
                Text("Register Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
