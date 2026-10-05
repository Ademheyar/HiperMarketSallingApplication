package com.example.hipermarketsallingapplication.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.User
import com.example.hipermarketsallingapplication.ui.auth.LocationData
import com.example.hipermarketsallingapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateShopPanel(
    currentUser: User?,
    onShopCreated: (Array<String>) -> Unit,
    onBack: () -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var shopBrand by remember { mutableStateOf("") }
    
    // Auto-fill from logged-in user data
    var shopPhone by remember { mutableStateOf(currentUser?.phoneNum ?: "+1 800-555-0199") }
    var shopEmail by remember { mutableStateOf(currentUser?.email ?: "shop@hipermarket.com") }

    val countries = LocationData.countries.map { it.name }
    var selectedCountry by remember { mutableStateOf(currentUser?.country ?: countries.first()) }
    var currentCities by remember { mutableStateOf(LocationData.getCities(selectedCountry)) }
    var selectedCity by remember { mutableStateOf(currentCities.first()) }
    var shopCurrency by remember { mutableStateOf(LocationData.getCurrency(selectedCountry)) }

    var shopCategory by remember { mutableStateOf("General Retail") }
    var operatingHours by remember { mutableStateOf("08:00 - 22:00") }
    var msg by remember { mutableStateOf<String?>(null) }

    var countryExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    val ownerManagerName = currentUser?.userName ?: currentUser?.fName ?: "Admin"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Create New Shop",
            color = DarkBlueAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        // Manager Name / Owner ID auto-filled from logged-in user data
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Owner ID / Manager Name:", color = TextMuted, fontSize = 10.sp)
                Text(ownerManagerName, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("Shop Name", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = shopBrand,
            onValueChange = { shopBrand = it },
            label = { Text("Shop Brand", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Country Dropdown
        ExposedDropdownMenuBox(
            expanded = countryExpanded,
            onExpandedChange = { countryExpanded = !countryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCountry,
                onValueChange = {},
                readOnly = true,
                label = { Text("Country", color = TextMuted) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = countryExpanded,
                onDismissRequest = { countryExpanded = false },
                modifier = Modifier.background(CardBackground)
            ) {
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country, color = TextLight) },
                        onClick = {
                            selectedCountry = country
                            currentCities = LocationData.getCities(country)
                            selectedCity = currentCities.first()
                            shopCurrency = LocationData.getCurrency(country)
                            countryExpanded = false
                        },
                        colors = MenuDefaults.itemColors(textColor = TextLight)
                    )
                }
            }
        }

        // City Dropdown
        ExposedDropdownMenuBox(
            expanded = cityExpanded,
            onExpandedChange = { cityExpanded = !cityExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCity,
                onValueChange = {},
                readOnly = true,
                label = { Text("City / Location", color = TextMuted) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = cityExpanded,
                onDismissRequest = { cityExpanded = false },
                modifier = Modifier.background(CardBackground)
            ) {
                currentCities.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city, color = TextLight) },
                        onClick = {
                            selectedCity = city
                            cityExpanded = false
                        },
                        colors = MenuDefaults.itemColors(textColor = TextLight)
                    )
                }
            }
        }

        OutlinedTextField(
            value = shopPhone,
            onValueChange = { shopPhone = it },
            label = { Text("Phone Number", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = shopEmail,
            onValueChange = { shopEmail = it },
            label = { Text("Email", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = shopCurrency,
            onValueChange = { shopCurrency = it },
            label = { Text("Currency (Auto-set from Country)", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = shopCategory,
            onValueChange = { shopCategory = it },
            label = { Text("Shop Category / Department", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = operatingHours,
            onValueChange = { operatingHours = it },
            label = { Text("Operating Hours", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                if (shopName.isNotBlank()) {
                    onShopCreated(arrayOf(shopName, shopBrand, selectedCountry, selectedCity, shopPhone, shopEmail, shopCategory, operatingHours))
                    msg = "Shop '$shopName' (Brand: $shopBrand) registered successfully in $selectedCity, $selectedCountry!"
                } else {
                    msg = "Enter a valid shop name."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register Shop")
        }
        msg?.let { Text(it, color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        TextButton(onClick = onBack) {
            Text("← Back to Shop Selection", color = DarkBlueAccent)
        }
    }
}
