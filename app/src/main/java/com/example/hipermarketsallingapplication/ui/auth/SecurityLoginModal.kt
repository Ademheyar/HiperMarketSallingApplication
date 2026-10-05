package com.example.hipermarketsallingapplication.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.api.ApiClient
import com.example.hipermarketsallingapplication.data.model.User
import com.example.hipermarketsallingapplication.data.repository.UserRepository
import com.example.hipermarketsallingapplication.ui.shop.CreateShopPanel
import com.example.hipermarketsallingapplication.ui.shop.RequestShopPanel
import com.example.hipermarketsallingapplication.ui.shop.ShopManagementPanel
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.UserViewModel
import com.example.hipermarketsallingapplication.utils.JsonHelper
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityLoginModal(
    currentUser: User?,
    isLoggedIn: Boolean = false,
    initialServerLink: String = "",
    userViewModel: UserViewModel,
    onLogin: (String, String, String) -> Boolean,
    onLogout: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var step by remember { mutableStateOf("SELECT_USER") } // "SELECT_USER", "NEW_LOGIN", "SHOP_MANAGEMENT", "CREATE_SHOP", "REQUEST_SHOP", "SUCCESS_PANEL", "FORGET_PASSWORD", "CREATE_USER"
    var selectedUsername by remember { mutableStateOf("admin") }
    var passwordInput by remember { mutableStateOf("") }
    var serverLinkInput by remember { mutableStateOf(initialServerLink) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var urlCheckStatus by remember { mutableStateOf<String?>(null) }

    var previousUsers by remember { mutableStateOf(listOf("admin", "cashier_1", "manager")) }

    LaunchedEffect(Unit) {
        try {
            val file = File(context.filesDir, "prevlog.text")
            if (file.exists()) {
                val lines = file.readLines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .mapNotNull { line ->
                        val parts = line.split("|").map { it.trim() }
                        if (parts.size >= 2) parts[1] else null
                    }
                if (lines.isNotEmpty()) {
                    previousUsers = lines.distinct()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val initialShopItems = remember(currentUser) {
        val list = mutableListOf<ShopWorkItem>()
        try {
            val dbShops = UserRepository(context).getAllShops()
            for (shop in dbShops) {
                val id = shop.getOrNull(0)?.toString() ?: "1"
                val name = shop.getOrNull(1)?.toString() ?: ""
                val brand = shop.getOrNull(2)?.toString() ?: "MainBrand"
                val level = shop.getOrNull(3)?.toString()?.toIntOrNull() ?: 10
                if (name.isNotBlank() && list.none { it.name == name }) {
                    list.add(ShopWorkItem(id, name, brand, level))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val parsed = JsonHelper.loads(currentUser?.userWorkShop ?: currentUser?.userShop)
        if (parsed is List<*>) {
            for (element in parsed) {
                if (element is List<*>) {
                    if (element.size >= 4) {
                        val id = element[0]?.toString() ?: "1"
                        val name = element[1]?.toString() ?: ""
                        val brand = element[2]?.toString() ?: "MainBrand"
                        val level = element[3]?.toString()?.toIntOrNull() ?: -1
                        if (name.isNotBlank() && list.none { it.name == name }) {
                            list.add(ShopWorkItem(id, name, brand, level))
                        }
                    } else if (element.size >= 2) {
                        val id = element[0]?.toString() ?: "1"
                        val name = element[1]?.toString() ?: ""
                        if (name.isNotBlank() && list.none { it.name == name }) {
                            list.add(ShopWorkItem(id, name, "MainBrand", -1))
                        }
                    }
                } else if (element != null) {
                    val name = element.toString().trim()
                    if (name.isNotBlank() && list.none { it.name == name }) {
                        list.add(ShopWorkItem("1", name, "MainBrand", -1))
                    }
                }
            }
        }
        if (list.isEmpty()) {
            emptyList()
        } else {
            list
        }
    }
    val shopItemsState = remember { mutableStateListOf(*initialShopItems.toTypedArray()) }
    val shopsList = remember(shopItemsState.toList()) { shopItemsState.map { listOf(it.id, it.name, it.brand, it.level) }.toMutableStateList() }
    var selectedShop by remember { mutableStateOf(initialShopItems.firstOrNull()?.name ?: "") }

    val handleClose = {
        val username = currentUser?.userName ?: selectedUsername
        if (username.isNotBlank() && selectedShop.isNotBlank()) {
            userViewModel.updateUserShop(username, listOf(selectedShop))
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = handleClose,
        containerColor = CardBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = DarkBlueAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Security & Account Panel", color = TextLight, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLoggedIn && step == "SELECT_USER") {
                    SuccessPanel(
                        currentUser = currentUser,
                        selectedUsername = selectedUsername,
                        selectedShop = selectedShop,
                        onLogout = {
                            onLogout()
                            onDismiss()
                        }
                    )
                } else {
                    when (step) {
                        "SELECT_USER" -> {
                            Text(
                                text = "Step 1: Choose New Login or select a previous user",
                                color = TextMuted,
                                fontSize = 12.sp
                            )

                            Button(
                                onClick = {
                                    selectedUsername = ""
                                    passwordInput = ""
                                    step = "NEW_LOGIN"
                                    errorMessage = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Login / Other Account", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Previously Logged-In Users:",
                                color = TextLight,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 90.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                previousUsers.forEach { user ->
                                    Button(
                                        onClick = {
                                            selectedUsername = user
                                            step = "NEW_LOGIN"
                                            errorMessage = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(user, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Text("Select →", color = DarkBlueAccent, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        "NEW_LOGIN" -> {
                            NewLoginPanel(
                                username = selectedUsername,
                                onUsernameChange = { selectedUsername = it },
                                password = passwordInput,
                                onPasswordChange = {
                                    passwordInput = it
                                    errorMessage = null
                                },
                                errorMessage = errorMessage,
                                onForgot = { step = "FORGET_PASSWORD" },
                                onCreateNewUser = { step = "CREATE_USER" },
                                onBack = { step = "SELECT_USER" }
                            )
                        }

                        "SHOP_MANAGEMENT" -> {
                            ShopManagementPanel(
                                selectedShop = selectedShop,
                                onShopSelected = { selectedShop = it },
                                userShopData = currentUser?.userWorkShop,
                                onCreateShop = { step = "CREATE_SHOP" },
                                onRequestShop = { step = "REQUEST_SHOP" },
                                errorMessage = errorMessage
                            )
                        }

                        "CREATE_SHOP" -> {
                            CreateShopPanel(
                                currentUser = currentUser,
                                onShopCreated = { Shop ->
                                    val newId = (100..999).random().toString()
                                    val shopname = Shop.getOrNull(0)?.toString() ?: ""
                                    val shopbrand = Shop.getOrNull(1)?.toString() ?: "MainBrand"
                                    val username = currentUser?.userName ?: selectedUsername

                                    userViewModel.insertShop(
                                        shopName = shopname,
                                        shopBrandName = shopbrand,
                                        ownerId = username,
                                        shopType = "General Retail",
                                        shopEmail = currentUser?.email ?: "@hipermarket.com",
                                        shopCountry = currentUser?.country ?: "",
                                        shopContact = currentUser?.phoneNum ?: "",
                                        shopworkers = "[[\'"+ currentUser?.userId +"\', \'" +currentUser?.userName + "\' , \'" +currentUser?.userName + "\', 'OWNER', \'"+shopname+ "\', \'"+shopbrand+ "\', 10]]"
                                    )
                                    if (shopItemsState.none { it.name == shopname }) {

                                        // Updating User Working place

                                        val newItem = ShopWorkItem(
                                            id = newId,
                                            name = shopname,
                                            brand = shopbrand,
                                            level = 10
                                        )
                                        if (shopItemsState.none { it.name == shopname }) {
                                            shopItemsState.add(newItem)
                                            val packedJson = JsonHelper.dumps(shopItemsState)
                                            userViewModel.updateUserWorkShop(username, packedJson)
                                            userViewModel.updateUserShop(username, listOf(shopname))
                                        }
                                        selectedShop = shopname
                                        step = "SHOP_MANAGEMENT"
                                    }
                                },
                                onBack = { step = "SHOP_MANAGEMENT" }
                            )
                        }

                        "REQUEST_SHOP" -> {
                            RequestShopPanel(
                                currentUserUsername = currentUser?.userName ?: selectedUsername,
                                shopsList = shopsList,
                                onRequestSubmitted = { requestedShop ->
                                    val pendingName = "Waiting For Response: $requestedShop"
                                    val newId = (100..999).random().toString()
                                    val newItem = ShopWorkItem(id = newId, name = pendingName, brand = "PendingBrand", level = 1)
                                    if (shopItemsState.none { it.name == pendingName }) {
                                        shopItemsState.add(newItem)
                                        val packedJson = JsonHelper.dumps(shopItemsState)
                                        userViewModel.updateUserWorkShop(currentUser?.userName ?: selectedUsername, packedJson)
                                    }
                                    step = "SHOP_MANAGEMENT"
                                },
                                onBack = { step = "SHOP_MANAGEMENT" }
                            )
                        }

                        "SUCCESS_PANEL" -> {
                            SuccessPanel(
                                currentUser = currentUser,
                                selectedUsername = selectedUsername,
                                selectedShop = selectedShop
                            )
                        }

                        "FORGET_PASSWORD" -> {
                            ForgotPasswordPanel(
                                onBack = { step = "NEW_LOGIN" },
                                onContinue = { emailOrUser ->
                                    step = "NEW_LOGIN"
                                }
                            )
                        }

                        "CREATE_USER" -> {
                            CreateUserPanel(
                                userViewModel = userViewModel,
                                onBack = { step = "SELECT_USER" }
                            )
                        }
                    }

                    if (step == "SELECT_USER" || step == "NEW_LOGIN") {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = DarkBlueDarker)
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = serverLinkInput,
                            onValueChange = { serverLinkInput = it; urlCheckStatus = null },
                            label = { Text("Server / DB Link Combobox", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            urlCheckStatus?.let { status ->
                                Text(status, color = if (status.contains("Available")) SuccessGreen else EnergyRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } ?: Spacer(modifier = Modifier.width(1.dp))

                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val isAvailable = ApiClient(context).checkUrlConnection(serverLinkInput)
                                        urlCheckStatus = if (isAvailable) "URL is Available & Online ✓" else "URL is Unreachable ✗"
                                    }
                                }
                            ) {
                                Text("Check URL", color = DarkBlueAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        TextButton(
                            onClick = { step = "CREATE_USER" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create new user if not exist →", color = DarkBlueAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
                Unit
            }
        },
        confirmButton = {
            when (step) {
                "NEW_LOGIN" -> {
                    Button(
                        onClick = {
                            val ok = onLogin(selectedUsername, passwordInput, serverLinkInput)
                            if (ok) {
                                try {
                                    val file = File(context.filesDir, "prevlog.text")
                                    val userId = currentUser?.id ?: (1000..9999).random()
                                    val entry = "$userId | $selectedUsername | $serverLinkInput"
                                    val existing = if (file.exists()) file.readText() else ""
                                    if (!existing.contains("$userId | $selectedUsername") && !existing.contains(selectedUsername)) {
                                        file.appendText("$entry\n")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                step = "SHOP_MANAGEMENT"
                            } else {
                                errorMessage = "Invalid credentials. Try username: admin, pass: 1234"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)
                    ) {
                        Text("Continue")
                    }
                }
                "SHOP_MANAGEMENT" -> {
                    Button(
                        onClick = {
                            val selectedItem = shopItemsState.find { it.name == selectedShop }
                            val level = selectedItem?.level ?: -1
                            if (level > 0) {
                                val username = currentUser?.userName ?: selectedUsername
                                if (username.isNotBlank() && selectedShop.isNotBlank()) {
                                    userViewModel.updateUserShop(username, listOf(selectedShop))
                                }
                                errorMessage = null
                                handleClose()
                            } else {
                                errorMessage = "Select Actived Shop"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("Continue")
                    }
                }
                "SUCCESS_PANEL" -> {
                    Button(
                        onClick = {
                            handleClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("Enter Shop Panel")
                    }
                }
                else -> {
                    if (isLoggedIn && step == "SELECT_USER") {
                        Button(onClick = handleClose, colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent)) {
                            Text("Done")
                        }
                    }
                }
            }
            Unit
        },
        dismissButton = {
            TextButton(onClick = handleClose) {
                Text("Close", color = TextMuted)
            }
        }
    )
}

data class ShopWorkItem(
    val id: String,
    val name: String,
    val brand: String,
    val level: Int
)
