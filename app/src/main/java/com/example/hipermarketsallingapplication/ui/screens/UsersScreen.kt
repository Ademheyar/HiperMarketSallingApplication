package com.example.hipermarketsallingapplication.ui.screens

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.SalesDoc
import com.example.hipermarketsallingapplication.data.model.User
import com.example.hipermarketsallingapplication.ui.auth.LocationData
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.DocViewModel
import com.example.hipermarketsallingapplication.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: UserViewModel,
    docViewModel: DocViewModel? = null
) {
    val users by viewModel.users.collectAsState()
    val docs = docViewModel?.docs?.collectAsState()?.value ?: emptyList()

    var searchQuery by remember { mutableStateOf("") }
    var selectedUserInTable by remember { mutableStateOf<User?>(null) }
    var showUserEditorSubpanel by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var showDocDetailModal by remember { mutableStateOf<SalesDoc?>(null) }

    // Filtered users list matching Users.py search_users
    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) {
            users
        } else {
            users.filter { u ->
                u.userName.contains(searchQuery, ignoreCase = true) ||
                        u.fName.contains(searchQuery, ignoreCase = true) ||
                        u.lName.contains(searchQuery, ignoreCase = true) ||
                        u.phoneNum.contains(searchQuery, ignoreCase = true) ||
                        u.email.contains(searchQuery, ignoreCase = true) ||
                        u.userType.contains(searchQuery, ignoreCase = true) ||
                        u.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // User Form Editor State variables matching Users.py
    var editId by remember { mutableStateOf(0L) }
    var editFName by remember { mutableStateOf("") }
    var editLName by remember { mutableStateOf("") }
    var editUserName by remember { mutableStateOf("") }
    var editGender by remember { mutableStateOf("MALE") }
    var editCountry by remember { mutableStateOf("Ethiopia") }
    var editCity by remember { mutableStateOf("Addis Ababa") }
    var editHomeNo by remember { mutableStateOf("") }
    var editIdNo by remember { mutableStateOf("") }
    var editPhoneNo by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editType by remember { mutableStateOf("Cashier") }
    var editPassword by remember { mutableStateOf("0000") }
    var editShowPassword by remember { mutableStateOf(false) }
    var editAbout by remember { mutableStateOf("") }
    var editShop by remember { mutableStateOf("Main Shop") }
    var editWorkAt by remember { mutableStateOf("Cashier Of Main Shop") }
    var editAccess by remember { mutableStateOf("") }

    fun populateUserEditor(u: User) {
        editId = u.id
        editFName = u.fName
        editLName = u.lName
        editUserName = u.userName
        editGender = if (u.gender.isNotBlank()) u.gender else "MALE"
        editCountry = if (u.country.isNotBlank()) u.country else "Ethiopia"
        editCity = if (u.address.isNotBlank()) u.address else "Addis Ababa"
        editPhoneNo = u.phoneNum
        editEmail = u.email
        editType = if (u.userType.isNotBlank()) u.userType else "Cashier"
        editPassword = u.password
        editShop = if (u.userShop.isNotBlank()) u.userShop else "Main Shop"
        editWorkAt = "${u.userType} Of ${u.userShop}"
        editAccess = u.userAccess
    }

    fun clearUserEditor() {
        editId = 0L
        editFName = ""
        editLName = ""
        editUserName = ""
        editGender = "MALE"
        editCountry = "Ethiopia"
        editCity = "Addis Ababa"
        editHomeNo = ""
        editIdNo = ""
        editPhoneNo = ""
        editEmail = ""
        editType = "Cashier"
        editPassword = "0000"
        editShowPassword = false
        editAbout = ""
        editShop = "Main Shop"
        editWorkAt = "Cashier Of Main Shop"
        editAccess = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Search & Action Buttons Bar matching Users.py (search_frame)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Users by Name, Phone, Email...", fontSize = 9.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkBlueAccent) },
                    modifier = Modifier.width(220.dp).defaultMinSize(minHeight = 52.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )

                // Add New user button matching Users.py
                Button(
                    onClick = {
                        clearUserEditor()
                        isEditMode = false
                        showUserEditorSubpanel = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New user", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Change / Edit button matching Users.py
                Button(
                    onClick = {
                        selectedUserInTable?.let { u ->
                            populateUserEditor(u)
                            isEditMode = true
                            showUserEditorSubpanel = true
                        }
                    },
                    enabled = selectedUserInTable != null,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Delete button matching Users.py
                Button(
                    onClick = {
                        selectedUserInTable?.let { u ->
                            viewModel.deleteUser(u.id)
                            selectedUserInTable = null
                            showUserEditorSubpanel = false
                        }
                    },
                    enabled = selectedUserInTable != null,
                    colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Main User Data Table Grid matching Users.py (list_box)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                Text(
                    text = "Staff & Cashier Users List (${filteredUsers.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkBlueAccent,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Table Header Row matching Users.py
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
                    TableCellHeader("Name", 140.dp)
                    TableCellHeader("Type", 90.dp)
                    TableCellHeader("Phone_Number", 110.dp)
                    TableCellHeader("Id_Number", 100.dp)
                    TableCellHeader("Email", 140.dp)
                    TableCellHeader("Adress / City", 110.dp)
                    TableCellHeader("Shop", 100.dp)
                }

                HorizontalDivider(color = DarkBlueDarker)

                if (filteredUsers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No users found matching search criteria.", color = TextMuted, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filteredUsers) { user ->
                            val isSelected = selectedUserInTable?.id == user.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) DarkBlueAccent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { selectedUserInTable = user }
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCellValue(user.userId.toString(), 50.dp, isBold = true)
                                TableCellValue("${user.fName} ${user.lName}", 140.dp, color = TextLight, isBold = true)
                                TableCellValue(user.userType, 90.dp, color = DarkBlueAccent)
                                TableCellValue(user.phoneNum.ifBlank { "-" }, 110.dp)
                                TableCellValue(user.userName, 100.dp)
                                TableCellValue(user.email.ifBlank { "-" }, 140.dp)
                                TableCellValue(user.address.ifBlank { "-" }, 110.dp)
                                TableCellValue(user.userShop.ifBlank { "Main Shop" }, 100.dp)
                            }
                        }
                    }
                }
            }
        }

        // User Details & Editing Notebook Subpanel matching Users.py (userinfo_notebook)
        if (showUserEditorSubpanel) {
            var subNotebookTab by remember { mutableStateOf(0) } // 0: info, 1: Doc info

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditMode) "Edit User Profile (Users.py)" else "Add New User Profile (Users.py)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DarkBlueAccent
                        )
                        IconButton(onClick = { showUserEditorSubpanel = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    TabRow(
                        selectedTabIndex = subNotebookTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = TextLight
                    ) {
                        Tab(selected = subNotebookTab == 0, onClick = { subNotebookTab = 0 }) {
                            Text("info (User Editor)", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
                        }
                        Tab(selected = subNotebookTab == 1, onClick = { subNotebookTab = 1 }) {
                            Text("Doc info (User Receipts)", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
                        }
                    }

                    if (subNotebookTab == 0) {
                        // Tab 1: User Profile & Details Editor (Users.py)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // First & Last Name
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = editFName,
                                    onValueChange = { editFName = it },
                                    label = { Text("First Name:", fontSize = 9.sp, color = TextMuted) },
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = editLName,
                                    onValueChange = { editLName = it },
                                    label = { Text("Last Name:", fontSize = 9.sp, color = TextMuted) },
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            // User Name & Gender
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = editUserName,
                                    onValueChange = { editUserName = it },
                                    label = { Text("User Name:", fontSize = 9.sp, color = TextMuted) },
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                MoreInfoDropdown(
                                    label = "Gender:",
                                    value = editGender,
                                    options = listOf("MALE", "FEMALE", "Prefer Not to Say"),
                                    onOptionSelected = { editGender = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Country & City Dropdowns
                            val countries = remember { LocationData.countries.map { it.name } }
                            val cities = remember(editCountry) { LocationData.getCities(editCountry) }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MoreInfoDropdown(
                                    label = "Country:",
                                    value = editCountry,
                                    options = countries,
                                    onOptionSelected = {
                                        editCountry = it
                                        val newCities = LocationData.getCities(it)
                                        editCity = newCities.firstOrNull() ?: ""
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                MoreInfoDropdown(
                                    label = "City:",
                                    value = editCity,
                                    options = if (cities.contains(editCity)) cities else listOf(editCity) + cities,
                                    onOptionSelected = { editCity = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Phone No & Email
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = editPhoneNo,
                                    onValueChange = { editPhoneNo = it },
                                    label = { Text("Phone No:", fontSize = 9.sp, color = TextMuted) },
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = editEmail,
                                    onValueChange = { editEmail = it },
                                    label = { Text("Email:", fontSize = 9.sp, color = TextMuted) },
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            // User Type & Password with Show Password Checkbox
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                MoreInfoDropdown(
                                    label = "Type:",
                                    value = editType,
                                    options = listOf("Cashier", "Admin", "Manager", "Seller", "Supervisor", "Owner"),
                                    onOptionSelected = {
                                        editType = it
                                        editWorkAt = "$it Of $editShop"
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = editPassword,
                                    onValueChange = { editPassword = it },
                                    label = { Text("Password:", fontSize = 9.sp, color = TextMuted) },
                                    visualTransformation = if (editShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = editShowPassword,
                                        onCheckedChange = { editShowPassword = it },
                                        colors = CheckboxDefaults.colors(checkedColor = DarkBlueAccent)
                                    )
                                    Text("Show Password", fontSize = 9.sp, color = TextLight)
                                }
                            }

                            // Work At & Shop
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MoreInfoDropdown(
                                    label = "Shop:",
                                    value = editShop,
                                    options = listOf("Main Shop", "Shop 1", "Shop 2"),
                                    onOptionSelected = {
                                        editShop = it
                                        editWorkAt = "$editType Of $it"
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = editWorkAt,
                                    onValueChange = { editWorkAt = it },
                                    label = { Text("Work At:", fontSize = 9.sp, color = TextMuted) },
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 54.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            // Action Buttons: Add / Update and Cancle matching Users.py
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        if (editUserName.isNotBlank()) {
                                            val u = User(
                                                id = if (isEditMode) editId else 0L,
                                                fName = editFName,
                                                lName = editLName,
                                                userName = editUserName,
                                                gender = editGender,
                                                country = editCountry,
                                                address = editCity,
                                                phoneNum = editPhoneNo,
                                                email = editEmail,
                                                userType = editType,
                                                password = editPassword,
                                                userShop = editShop,
                                                userAccess = editAccess
                                            )
                                            viewModel.saveUser(u)
                                            showUserEditorSubpanel = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(if (isEditMode) "Update" else "Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { showUserEditorSubpanel = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Cancle", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        // Tab 2: Doc info (Sales Documents created by selected user matching Users.py)
                        val userDocs = remember(docs, editUserName) {
                            docs.filter { it.userId.equals(editUserName, ignoreCase = true) || it.sellerId.equals(editUserName, ignoreCase = true) }
                        }

                        Column(modifier = Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Sales Receipts for User @$editUserName (${userDocs.size} docs)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)

                            if (userDocs.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No receipt documents found for user @$editUserName", color = TextMuted, fontSize = 11.sp)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(userDocs) { doc ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable { showDocDetailModal = doc },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("#${doc.docBarcode} (${doc.type})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextLight)
                                                    Text("Date: ${doc.docCreatedDate} • Items: ${doc.item}", fontSize = 10.sp, color = TextMuted)
                                                }
                                                Text("$${String.format(Locale.getDefault(), "%.2f", doc.price)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SuccessGreen)
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

    // Document View Detail Modal (Users.py perform_veiw)
    showDocDetailModal?.let { doc ->
        AlertDialog(
            onDismissRequest = { showDocDetailModal = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("User Sales Document #${doc.docBarcode}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Type: ${doc.type}", fontWeight = FontWeight.Bold, color = DarkBlueAccent, fontSize = 13.sp)
                    Text("Date Created: ${doc.docCreatedDate}", color = TextMuted, fontSize = 12.sp)
                    Text("Customer ID: ${doc.customerId}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Text("User/Seller ID: ${doc.userId} / ${doc.sellerId}", color = TextMuted, fontSize = 12.sp)
                    Text("Items Summary: ${doc.item}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    HorizontalDivider(color = DarkBlueDarker)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Price:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("$${String.format(Locale.getDefault(), "%.2f", doc.price)}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDocDetailModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
