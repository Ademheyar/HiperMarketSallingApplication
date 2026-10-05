package com.example.hipermarketsallingapplication.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.User
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserPanel(
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    var regFname by remember { mutableStateOf("") }
    var regLname by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }

    // Country & City data using LocationData
    val countries = LocationData.countries.map { it.name }
    var selectedCountry by remember { mutableStateOf(countries.first()) }
    var currentCities by remember { mutableStateOf(LocationData.getCities(selectedCountry)) }
    var selectedCity by remember { mutableStateOf(currentCities.firstOrNull() ?: "") }

    // Gender data
    val genders = listOf("Male", "Female", "Prefer Not To Say")
    var selectedGender by remember { mutableStateOf(genders.first()) }

    // Dropdown expanded states
    var countryExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    var regSuccessMsg by remember { mutableStateOf<String?>(null) }

    // Automatic username generation: First letter of first name + first letter of last name + ' ' + first name
    // e.g. "Adem Heyar" -> "AH Adem"
    LaunchedEffect(regFname, regLname) {
        val trimmedF = regFname.trim()
        val trimmedL = regLname.trim()
        if (trimmedF.isNotBlank() && trimmedL.isNotBlank()) {
            val fInit = trimmedF.first().uppercaseChar()
            val lInit = trimmedL.first().uppercaseChar()
            regUsername = "$fInit$lInit $trimmedF"
        } else if (trimmedF.isNotBlank()) {
            val fInit = trimmedF.first().uppercaseChar()
            regUsername = "$fInit $trimmedF"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Create New User Form",
            color = DarkBlueAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        OutlinedTextField(
            value = regFname,
            onValueChange = { regFname = it },
            label = { Text("First Name", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = regLname,
            onValueChange = { regLname = it },
            label = { Text("Last Name", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.menuAnchor().fillMaxWidth()
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
                            selectedCity = currentCities.firstOrNull() ?: ""
                            countryExpanded = false
                        },
                        colors = MenuDefaults.itemColors(textColor = TextLight)
                    )
                }
            }
        }

        // City Dropdown
        val currentCitiesForDropdown = LocationData.getCities(selectedCountry)
        ExposedDropdownMenuBox(
            expanded = cityExpanded,
            onExpandedChange = { cityExpanded = !cityExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCity,
                onValueChange = {},
                readOnly = true,
                label = { Text("City", color = TextMuted) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = cityExpanded,
                onDismissRequest = { cityExpanded = false },
                modifier = Modifier.background(CardBackground)
            ) {
                currentCitiesForDropdown.forEach { city ->
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

        // Gender Dropdown
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedGender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender", color = TextMuted) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false },
                modifier = Modifier.background(CardBackground)
            ) {
                genders.forEach { gender ->
                    DropdownMenuItem(
                        text = { Text(gender, color = TextLight) },
                        onClick = {
                            selectedGender = gender
                            genderExpanded = false
                        },
                        colors = MenuDefaults.itemColors(textColor = TextLight)
                    )
                }
            }
        }

        OutlinedTextField(
            value = regUsername,
            onValueChange = { regUsername = it },
            label = { Text("Username (Auto-generated)", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = regPassword,
            onValueChange = { regPassword = it },
            label = { Text("Password", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = regPhone,
            onValueChange = { regPhone = it },
            label = { Text("Phone Number", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (regUsername.isNotBlank() && regPassword.isNotBlank()) {
                    val newUser = User(
                        userId = (1000..9999).random(),
                        fName = regFname.ifBlank { "New" },
                        lName = regLname.ifBlank { "User" },
                        userName = regUsername,
                        gender = selectedGender,
                        country = selectedCountry,
                        phoneNum = regPhone,
                        address = selectedCity,
                        userType = "Cashier",
                        password = regPassword,
                        userShop = "",
                        userWorkShop = "[]"
                    )
                    userViewModel.saveUser(newUser)
                    onBack()
                } else {
                    regSuccessMsg = "Enter valid username & password."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register User")
        }
        regSuccessMsg?.let { Text(it, color = EnergyRed, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        TextButton(onClick = onBack) {
            Text("← Back to login", color = DarkBlueAccent)
        }
    }
}
