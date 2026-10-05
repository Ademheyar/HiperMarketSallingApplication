package com.example.hipermarketsallingapplication

import android.os.Bundle
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.User
import com.example.hipermarketsallingapplication.ui.auth.*
import com.example.hipermarketsallingapplication.ui.screens.*
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    private val posViewModel: PosViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private val docViewModel: DocViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val selectedTheme by settingsViewModel.selectedTheme.collectAsState()

            val customColorScheme = when (selectedTheme) {
                "Cyberpunk Energy" -> darkColorScheme(
                    primary = EnergyRed,
                    secondary = androidx.compose.ui.graphics.Color(0xFFFFD700),
                    tertiary = EnergyRed,
                    background = androidx.compose.ui.graphics.Color(0xFF121212),
                    surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
                    onPrimary = androidx.compose.ui.graphics.Color.White,
                    onSecondary = androidx.compose.ui.graphics.Color.White,
                    onBackground = androidx.compose.ui.graphics.Color.White,
                    onSurface = androidx.compose.ui.graphics.Color.White
                )
                "Emerald Wealth" -> darkColorScheme(
                    primary = SuccessGreen,
                    secondary = DarkBlueAccent,
                    tertiary = SuccessGreen,
                    background = androidx.compose.ui.graphics.Color(0xFF0D1B1E),
                    surface = androidx.compose.ui.graphics.Color(0xFF1B2A32),
                    onPrimary = androidx.compose.ui.graphics.Color(0xFFE0F2FE),
                    onSecondary = androidx.compose.ui.graphics.Color(0xFFE0F2FE),
                    onBackground = androidx.compose.ui.graphics.Color(0xFFE0F2FE),
                    onSurface = androidx.compose.ui.graphics.Color(0xFFE0F2FE)
                )
                "Modern Light Surface" -> lightColorScheme(
                    primary = DarkBlueAccent,
                    secondary = DarkBlueSecondary,
                    tertiary = EnergyRed,
                    background = androidx.compose.ui.graphics.Color(0xFFF1F5F9),
                    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                    onPrimary = androidx.compose.ui.graphics.Color(0xFF0F172A),
                    onSecondary = androidx.compose.ui.graphics.Color(0xFF0F172A),
                    onBackground = androidx.compose.ui.graphics.Color(0xFF0F172A),
                    onSurface = androidx.compose.ui.graphics.Color(0xFF0F172A)
                )
                else -> darkColorScheme(
                    primary = DarkBlueAccent,
                    secondary = DarkBlueSecondary,
                    tertiary = EnergyRed,
                    background = SurfaceDark,
                    surface = CardBackground,
                    onPrimary = TextLight,
                    onSecondary = TextLight,
                    onBackground = TextLight,
                    onSurface = TextLight
                )
            }

            HiperMarketSallingApplicationTheme(colorScheme = customColorScheme) {
                MainAppScreen(
                    posViewModel = posViewModel,
                    productViewModel = productViewModel,
                    docViewModel = docViewModel,
                    userViewModel = userViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

enum class AppDestination(val label: String, val icon: ImageVector, val requiresLogin: Boolean = false, val showInBottomNav: Boolean = true) {
    HOME("Home", Icons.Default.Home, requiresLogin = false, showInBottomNav = true),
    CHART("Active Cart", Icons.Default.ShoppingCart, requiresLogin = false, showInBottomNav = false),
    SETTINGS("Settings", Icons.Default.Settings, requiresLogin = false, showInBottomNav = true),
    POS("POS Terminal", Icons.Default.PointOfSale, requiresLogin = true, showInBottomNav = true),
    MANAGE("Manage", Icons.Default.AdminPanelSettings, requiresLogin = true, showInBottomNav = true),
    USER_PANEL("User Profile", Icons.Default.AccountCircle, requiresLogin = true, showInBottomNav = true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    posViewModel: PosViewModel,
    productViewModel: ProductViewModel,
    docViewModel: DocViewModel,
    userViewModel: UserViewModel,
    settingsViewModel: SettingsViewModel
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val serverLink by userViewModel.serverLink.collectAsState()
    var showSecurityModal by remember { mutableStateOf(false) }
    var showActiveCartDialog by remember { mutableStateOf(false) }

    MainAppScreenContent(
        currentUser = currentUser,
        isLoggedIn = isLoggedIn,
        onOpenSecurity = { showSecurityModal = true },
        screenContent = { destination, user, onNavigate ->
            when (destination) {
                AppDestination.HOME -> HomeScreen(
                    posViewModel = posViewModel,
                    onOpenActiveCart = { showActiveCartDialog = true },
                    onNavigateTo = onNavigate
                )
                AppDestination.CHART -> {
                    SideEffect {
                        showActiveCartDialog = true
                    }
                }
                AppDestination.SETTINGS -> SettingsScreen(
                    viewModel = settingsViewModel
                )
                AppDestination.POS -> PosScreen(
                    viewModel = posViewModel,
                    docViewModel = docViewModel,
                    activeUser = user?.userName ?: "admin"
                )
                AppDestination.MANAGE -> ManageScreen(
                    productViewModel = productViewModel,
                    docViewModel = docViewModel,
                    userViewModel = userViewModel,
                    settingsViewModel = settingsViewModel
                )
                AppDestination.USER_PANEL -> UserPanelScreen(
                    currentUser = user,
                    onLogout = {
                        userViewModel.logout()
                    },
                    onNavigateTo = onNavigate
                )
            }
        }
    )

    if (showActiveCartDialog) {
        ActiveCartDialog(
            posViewModel = posViewModel,
            docViewModel = docViewModel,
            activeUser = currentUser?.userName ?: "admin",
            onDismiss = { showActiveCartDialog = false }
        )
    }

    if (showSecurityModal) {
        SecurityLoginModal(
            currentUser = currentUser,
            isLoggedIn = isLoggedIn,
            initialServerLink = serverLink,
            userViewModel = userViewModel,
            onLogin = { un, pw, link ->
                userViewModel.login(un, pw, link)
            },
            onLogout = {
                userViewModel.logout()
            },
            onDismiss = { showSecurityModal = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreenContent(
    currentUser: User? = null,
    isLoggedIn: Boolean = false,
    onOpenSecurity: () -> Unit = {},
    screenContent: @Composable (AppDestination, User?, (AppDestination) -> Unit) -> Unit
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.HOME) }

    // When logged in, select user panel; when logged out, go to home panel
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            currentDestination = AppDestination.USER_PANEL
        } else {
            currentDestination = AppDestination.HOME
        }
    }

    // Filter bottom navigation bar: POS, Manage and User Profile are visible when logged in
    val visibleDestinations = AppDestination.entries.filter { dest ->
        dest.showInBottomNav && (!dest.requiresLogin || isLoggedIn) && dest != AppDestination.SETTINGS
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TextLight
            ) {
                visibleDestinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentDestination == dest,
                        onClick = {
                            if (dest.requiresLogin && !isLoggedIn) {
                                onOpenSecurity()
                            } else {
                                currentDestination = dest
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextLight,
                            selectedTextColor = TextLight,
                            indicatorColor = DarkBlueAccent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }

                // Security / User Login Button (when logged out)
                if (!isLoggedIn) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onOpenSecurity,
                        icon = { Icon(Icons.Default.Security, contentDescription = "Security Login", tint = EnergyRed) },
                        label = { Text("Log In", color = EnergyRed, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = EnergyRed,
                            unselectedTextColor = EnergyRed
                        )
                    )
                }

                // Settings destination always rendered last (rightmost side)
                val settingsDest = AppDestination.SETTINGS
                NavigationBarItem(
                    selected = currentDestination == settingsDest,
                    onClick = {
                        currentDestination = settingsDest
                    },
                    icon = { Icon(settingsDest.icon, contentDescription = settingsDest.label) },
                    label = { Text(settingsDest.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextLight,
                        selectedTextColor = TextLight,
                        indicatorColor = DarkBlueAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            screenContent(currentDestination, currentUser) { navDest ->
                if (navDest.requiresLogin && !isLoggedIn) {
                    onOpenSecurity()
                } else {
                    currentDestination = navDest
                }
            }
        }
    }
}

@Composable
fun UserPanelScreen(
    currentUser: User?,
    onLogout: () -> Unit,
    onNavigateTo: (AppDestination) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(72.dp))
            Text("User Profile & Active Session", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextLight)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Username: ${currentUser?.userName ?: "Guest"}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Full Name: ${currentUser?.fName ?: ""} ${currentUser?.lName ?: ""}", color = TextMuted, fontSize = 14.sp)
                    Text("Role / Type: ${currentUser?.userType ?: "User"}", color = TextMuted, fontSize = 14.sp)
                    Text("Assigned Shop: ${currentUser?.userShop ?: "None"}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Country / Location: ${currentUser?.country ?: "N/A"}", color = TextMuted, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onNavigateTo(AppDestination.POS) },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PointOfSale, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open POS Terminal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Button(
                onClick = { onNavigateTo(AppDestination.MANAGE) },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = DarkBlueAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage System & Shops", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextLight)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out / Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ActiveCartDialog(
    posViewModel: PosViewModel,
    docViewModel: DocViewModel,
    activeUser: String,
    onDismiss: () -> Unit
) {
    var showPaymentModal by remember { mutableStateOf(false) }
    var lastReceiptCode by remember { mutableStateOf("") }
    var showReceiptModal by remember { mutableStateOf(false) }

    val cart by posViewModel.cart.collectAsState()
    val checkoutStatus by posViewModel.checkoutSuccess.collectAsState()

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
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = DarkBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Active Cart (${cart.size} items)", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                if (cart.isNotEmpty()) {
                    TextButton(onClick = { posViewModel.clearCart() }) {
                        Text("Clear", color = EnergyRed, fontSize = 12.sp)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (cart.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your active cart is empty", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                } else {
                    cart.forEach { item ->
                        CartItemRow(
                            item = item,
                            onQtyChange = { newQty -> posViewModel.updateCartQty(item, newQty) },
                            onRemove = { posViewModel.removeFromCart(item) }
                        )
                    }

                    HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(vertical = 4.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBlueDarker, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", color = TextMuted, fontSize = 12.sp)
                            Text("$${String.format(Locale.getDefault(), "%.2f", posViewModel.cartSubtotal)}", color = TextLight, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax:", color = TextMuted, fontSize = 12.sp)
                            Text("$${String.format(Locale.getDefault(), "%.2f", posViewModel.cartTax)}", color = TextLight, fontSize = 12.sp)
                        }
                        HorizontalDivider(color = CardBackground, modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total:", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("$${String.format(Locale.getDefault(), "%.2f", posViewModel.cartTotal)}", color = EnergyRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (cart.isNotEmpty()) {
                Button(
                    onClick = { showPaymentModal = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Proceed to Payment", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close", color = TextMuted)
            }
        }
    )

    if (showPaymentModal) {
        SplitPaymentModal(
            totalAmount = posViewModel.cartTotal,
            onDismiss = { showPaymentModal = false },
            onPayConfirmed = { payMethod ->
                showPaymentModal = false
                lastReceiptCode = posViewModel.checkout(payMethod, activeUser)
                docViewModel.loadDocs()
                showReceiptModal = true
            }
        )
    }

    if (showReceiptModal) {
        ReceiptPreviewModal(
            receiptCode = lastReceiptCode,
            statusText = checkoutStatus ?: "Success",
            onDismiss = {
                showReceiptModal = false
                posViewModel.clearCheckoutStatus()
                onDismiss()
            }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    val sampleUser = User(
        id = 1,
        userName = "admin",
        fName = "System",
        lName = "Admin",
        userType = "Admin"
    )
    HiperMarketSallingApplicationTheme {
        MainAppScreenContent(
            currentUser = sampleUser,
            isLoggedIn = true,
            screenContent = { destination, user, _ ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    content = {
                        Text(
                            text = "${destination.label} (${user?.userName ?: "admin"})",
                            color = TextLight,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                )
            }
        )
    }
}
