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
import com.example.hipermarketsallingapplication.data.model.UserWorkItem
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
    var step by remember {
        mutableStateOf(if (isLoggedIn && currentUser != null) "SHOP_MANAGEMENT" else "SELECT_USER")
    }
    var selectedUsername by remember { mutableStateOf("admin") }
    var passwordInput by remember { mutableStateOf("") }
    var serverLinkInput by remember { mutableStateOf(initialServerLink) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var urlCheckStatus by remember { mutableStateOf<String?>(null) }

    var previousUsers by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(Unit) {
        if (isLoggedIn && currentUser != null) {
            step = "SHOP_MANAGEMENT"
        } else {
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
            if (previousUsers.isEmpty()) {
                selectedUsername = ""
                passwordInput = ""
                step = "NEW_LOGIN"
            }
        }
    }

    val initialShopItems = remember(currentUser) {
        val list = mutableListOf<UserWorkItem>()
        val shopJsonStr = if (!currentUser?.userWorkShop.isNullOrBlank() && currentUser?.userWorkShop != "[]") {
            currentUser?.userWorkShop
        } else {
            currentUser?.userShop
        }
        val parsed = JsonHelper.loads(shopJsonStr)
        if (parsed is List<*>) {
            for (element in parsed) {
                if (element is List<*>) {
                    if (element.size >= 4) {
                        val id = element[0]?.toString() ?: "1"
                        val name = element[1]?.toString() ?: ""
                        val brand = element[2]?.toString() ?: "MainBrand"
                        val level = element[3]?.toString()?.toIntOrNull() ?: 10
                        if (name.isNotBlank() && list.none { it.name == name }) {
                            list.add(UserWorkItem(id, name, brand, level))
                        }
                    } else if (element.size >= 2) {
                        val id = element[0]?.toString() ?: "1"
                        val name = element[1]?.toString() ?: ""
                        if (name.isNotBlank() && list.none { it.name == name }) {
                            list.add(UserWorkItem(id, name, "MainBrand", -1))
                        }
                    }
                } else if (element != null) {
                    val name = element.toString().trim()
                    if (name.isNotBlank() && list.none { it.name == name }) {
                        list.add(UserWorkItem("1", name, "MainBrand", -1))
                    }
                }
            }
        }
        list
    }
    val shopItemsState = remember(currentUser) { mutableStateListOf(*initialShopItems.toTypedArray()) }
    val allDbShops = remember(step) {
        try {
            UserRepository(context).getAllShops()
        } catch (e: Exception) {
            emptyList<List<*>>()
        }
    }
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
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = DarkBlueAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Security & Account Panel", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                                color = MaterialTheme.colorScheme.onSurface,
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
                                onBack = { step = "SELECT_USER" },
                                onContinue = {
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
                                        errorMessage = "Invalid. User name or Password Incorrect"
                                    }
                                }
                            )
                        }

                        "SHOP_MANAGEMENT" -> {
                            ShopManagementPanel(
                                selectedShop = selectedShop,
                                onShopSelected = { shopName ->
                                    selectedShop = shopName
                                    val username = currentUser?.userName ?: selectedUsername
                                    userViewModel.updateUserShop(username, listOf(shopName))
                                    onDismiss()
                                },
                                userShopData = JsonHelper.dumps(shopItemsState.map { it.toList() }),
                                onCreateShop = { step = "CREATE_SHOP" },
                                onRequestShop = { step = "REQUEST_SHOP" },
                                onBack = { step = "SELECT_USER" },
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
                                    val ownerUserId = currentUser?.userId ?: 1

                                    userViewModel.insertShop(
                                        shopName = shopname,
                                        shopBrandName = shopbrand,
                                        ownerId = username,
                                        shopType = "General Retail",
                                        shopEmail = currentUser?.email ?: "@hipermarket.com",
                                        shopCountry = currentUser?.country ?: "",
                                        shopContact = currentUser?.phoneNum ?: "",
                                        shopworkers = "[['$ownerUserId', '$username', '$username', 'OWNER', '$shopname', '$shopbrand', 10]]"
                                    )
                                    if (shopItemsState.none { it.name == shopname }) {

                                        // Updating User Working place

                                        val newItem = UserWorkItem(newId, shopname, shopbrand, 10)

                                        if (shopItemsState.none { it.id == newId }) {
                                            shopItemsState.add(newItem)
                                            val packedJson = JsonHelper.dumps(shopItemsState.map { it.toList() })
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
                                shopsList = allDbShops,
                                onRequestSubmitted = { requestedShop ->
                                    val ownerUserId = currentUser?.userId ?: 1
                                    val shopId = requestedShop.getOrNull(0)?.toString() ?: (100..999).random().toString()
                                    val shopname = requestedShop.getOrNull(1)?.toString() ?: ""
                                    val shopbrand = requestedShop.getOrNull(2)?.toString() ?: "MainBrand"
                                    val username = currentUser?.userName ?: selectedUsername

                                    val newItem = UserWorkItem(shopId, shopname, shopbrand, -1)
                                    if (shopItemsState.none { it.name == shopname }) {
                                        shopItemsState.add(newItem)
                                        val packedUserWorkShopJson = JsonHelper.dumps(shopItemsState.map { it.toList() })
                                        userViewModel.updateUserWorkShop(username, packedUserWorkShopJson)
                                        userViewModel.updateUserShop(username, listOf(shopname))
                                    }

                                    val newShopWorker = ShopWorkItem(
                                        ownerId = ownerUserId.toString(),
                                        ownerName = username,
                                        ownerType = "Worker",
                                        name = shopname,
                                        brand = shopbrand,
                                        level = -2
                                    )
                                    val shopWorkersJson = JsonHelper.dumps(listOf(newShopWorker.toList()))
                                    userViewModel.updateShopWorkers(shopname, shopbrand, shopWorkersJson)

                                    selectedShop = shopname
                                    step = "SHOP_MANAGEMENT"
                                },
                                onBack = { step = "SHOP_MANAGEMENT" }
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
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
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
                    }
                Unit
            }
        },
        confirmButton = {
            when (step) {
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
    val ownerId: String,
    val ownerName: String,
    val ownerType: String,
    val name: String,
    val brand: String,
    val level: Int
) {
    fun toList(): List<Any> = listOf(ownerId, ownerName, ownerType, name, brand, level)
}
