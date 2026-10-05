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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.User
import com.example.hipermarketsallingapplication.ui.auth.LocationData
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(viewModel: UserViewModel) {
    val users by viewModel.users.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddUserDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        // Active User Header & Add User Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Active Cashier: ${currentUser?.userName ?: "Guest"}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Role: ${currentUser?.userType ?: "Cashier"}", color = TextMuted, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = { showAddUserDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add User", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Users & Cashiers List
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Cashier & Staff Accounts (${users.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Divider(color = DarkBlueDarker, modifier = Modifier.padding(bottom = 8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(users) { user ->
                        UserRow(
                            user = user,
                            onSwitch = { viewModel.login(user.userName, user.password) },
                            onDelete = { viewModel.deleteUser(user.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        AddUserModal(
            onDismiss = { showAddUserDialog = false },
            onSave = { newUser ->
                viewModel.saveUser(newUser)
                showAddUserDialog = false
            }
        )
    }
}

@Composable
fun UserRow(
    user: User,
    onSwitch: () -> Unit,
    onDelete: () -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = null,
                    tint = if (user.userType == "Admin") EnergyRed else DarkBlueAccent
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("${user.fName} ${user.lName} (@${user.userName})", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Role: ${user.userType} | Shop: ${user.userShop} | ID: ${user.userId}", color = TextMuted, fontSize = 11.sp)
                }
            }

            Row {
                Button(
                    onClick = onSwitch,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSecondary),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Switch", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EnergyRed)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserModal(
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var fName by remember { mutableStateOf("") }
    var lName by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("Cashier") }

    val countries = LocationData.countries.map { it.name }
    var selectedCountry by remember { mutableStateOf(countries.first()) }
    var currentCities by remember { mutableStateOf(LocationData.getCities(selectedCountry)) }
    var selectedCity by remember { mutableStateOf(currentCities.firstOrNull() ?: "") }
    var countryExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text("Add Staff / Cashier User", color = TextLight, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fName,
                    onValueChange = { fName = it },
                    label = { Text("First Name", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                )
                OutlinedTextField(
                    value = lName,
                    onValueChange = { lName = it },
                    label = { Text("Last Name", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
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
                                    selectedCity = currentCities.firstOrNull() ?: ""
                                    countryExpanded = false
                                },
                                colors = MenuDefaults.itemColors(textColor = TextLight)
                            )
                        }
                    }
                }

                // City Dropdown
                val citiesForDropdown = LocationData.getCities(selectedCountry)
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
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false },
                        modifier = Modifier.background(CardBackground)
                    ) {
                        citiesForDropdown.forEach { city ->
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
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Username", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password / PIN", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { userType = "Cashier" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userType == "Cashier") DarkBlueAccent else SurfaceDark
                        )
                    ) {
                        Text("Cashier")
                    }
                    Button(
                        onClick = { userType = "Admin" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userType == "Admin") EnergyRed else SurfaceDark
                        )
                    ) {
                        Text("Admin")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userName.isNotBlank() && password.isNotBlank()) {
                        val u = User(
                            fName = fName,
                            lName = lName,
                            userName = userName,
                            country = selectedCountry,
                            address = selectedCity,
                            password = password,
                            userType = userType
                        )
                        onSave(u)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
            ) {
                Text("Create User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
