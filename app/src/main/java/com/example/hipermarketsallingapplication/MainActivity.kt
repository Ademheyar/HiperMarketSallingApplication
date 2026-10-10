package com.example.hipermarketsallingapplication

import android.os.Bundle
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
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
    MANAGE("Manage", Icons.Default.AdminPanelSettings, requiresLogin = true, showInBottomNav = false),
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
        userViewModel = userViewModel,
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
                    userViewModel = userViewModel,
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
                    userViewModel = userViewModel,
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
    userViewModel: UserViewModel? = null,
    isLoggedIn: Boolean = false,
    onOpenSecurity: () -> Unit = {},
    screenContent: @Composable (AppDestination, User?, (AppDestination) -> Unit) -> Unit
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.HOME) }
    val userWorkShops = userViewModel?.userWorkingShops?.collectAsState()?.value ?: emptyList()

    val userShop = currentUser?.userShop
    val hasWorkingShops = userWorkShops.isNotEmpty() || (!userShop.isNullOrBlank() && userShop != "[]" && userShop != "None")

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
                            } else if (dest == AppDestination.MANAGE && !hasWorkingShops) {
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
                } else if (navDest == AppDestination.MANAGE && !hasWorkingShops) {
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
    userViewModel: UserViewModel,
    onLogout: () -> Unit,
    onNavigateTo: (AppDestination) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Messages, 1: Notif, 2: History
    var activeStatView by remember { mutableStateOf<String?>(null) } // "following", "likes", "saved", or null
    val userWorkShops by userViewModel.userWorkingShops.collectAsState()
    var shopDropdownExpanded by remember { mutableStateOf(false) }

    val activeShopName = currentUser?.userShop?.ifBlank { "All Working Shops" } ?: "All Working Shops"

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instagram Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "@${currentUser?.userName ?: "user_profile"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(Icons.Default.Verified, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(16.dp))
                }
            }

            // Instagram Profile Header Section (Avatar + Stats)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar with Ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(DarkBlueAccent, SuccessGreen, EnergyRed)
                            ),
                            shape = CircleShape
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = DarkBlueAccent,
                                modifier = Modifier.size(68.dp)
                            )
                        }
                    }
                }

                // Stats: Following, Likes, Saved (Clickable to show panel below tabs)
                Row(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatColumn("Following", "3") { activeStatView = "following" }
                    ProfileStatColumn("Likes", "142") { activeStatView = "likes" }
                    ProfileStatColumn("Saved", "18") { activeStatView = "saved" }
                }
            }

            // Bio / User Details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${currentUser?.fName ?: "Hiper"} ${currentUser?.lName ?: "User"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "💼 Role: ${currentUser?.userType ?: "Admin"} | ",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    Box {
                        OutlinedButton(
                            onClick = { shopDropdownExpanded = true },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("🏬 $activeShopName", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlueAccent)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Shop", tint = DarkBlueAccent, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = shopDropdownExpanded,
                            onDismissRequest = { shopDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🌐 All Working Shops (Global)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen) },
                                onClick = {
                                    userViewModel.updateUserShop(currentUser?.userName ?: "", emptyList())
                                    shopDropdownExpanded = false
                                }
                            )

                            HorizontalDivider()

                            if (userWorkShops.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Main Shop", fontSize = 11.sp) },
                                    onClick = {
                                        userViewModel.updateUserShop(currentUser?.userName ?: "", listOf("Main Shop"))
                                        shopDropdownExpanded = false
                                    }
                                )
                            } else {
                                userWorkShops.forEach { wShop ->
                                    DropdownMenuItem(
                                        text = { Text("🏬 ${wShop.name} (${wShop.brandName})", fontSize = 11.sp) },
                                        onClick = {
                                            userViewModel.updateUserShop(currentUser?.userName ?: "", listOf(wShop.name))
                                            shopDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "📍 Location: ${currentUser?.country ?: "Global"} | ✉️ ${currentUser?.email ?: "user@hipermarket.com"}",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            // Action Buttons (POS Terminal, Manage, Edit Profile)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { onNavigateTo(AppDestination.POS) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("POS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onNavigateTo(AppDestination.MANAGE) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = { onNavigateTo(AppDestination.SETTINGS) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = DarkBlueAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Log Out Button placed ON TOP of the tabs as requested
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sign Out / Log Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Divider(color = DarkBlueDarker, thickness = 1.dp)

            // Instagram Tabs Row (Messages, Notification, History)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabButton(
                    icon = Icons.Default.Message,
                    label = "Messages",
                    isSelected = activeStatView == null && selectedTab == 0,
                    onClick = {
                        activeStatView = null
                        selectedTab = 0
                    }
                )
                TabButton(
                    icon = Icons.Default.Notifications,
                    label = "Notif",
                    isSelected = activeStatView == null && selectedTab == 1,
                    onClick = {
                        activeStatView = null
                        selectedTab = 1
                    }
                )
                TabButton(
                    icon = Icons.Default.History,
                    label = "History",
                    isSelected = activeStatView == null && selectedTab == 2,
                    onClick = {
                        activeStatView = null
                        selectedTab = 2
                    }
                )
            }

            Divider(color = DarkBlueDarker, thickness = 0.5.dp)

            // Tab Content Section
            when (activeStatView) {
                "following" -> FollowingShopsTabContent()
                "likes" -> LikedProductsTabContent()
                "saved" -> SavedProductsTabContent()
                else -> {
                    when (selectedTab) {
                        0 -> MessagesTabContent()
                        1 -> NotificationsTabContent()
                        2 -> CartHistoryTabContent()
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatColumn(title: String, count: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Text(count, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(title, fontSize = 12.sp, color = TextMuted)
    }
}

@Composable
fun TabButton(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val tintColor = if (isSelected) DarkBlueAccent else TextMuted
    TextButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(icon, contentDescription = label, tint = tintColor, modifier = Modifier.size(22.dp))
            Text(label, color = tintColor, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun LikedProductsTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("❤️ Liked Products (Favorites)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        val likedItems = listOf(
            Triple("Wireless POS Barcode Scanner", "$120.00", "Shop 1"),
            Triple("Thermal Receipt Printer 80mm", "$250.00", "Shop 1"),
            Triple("Cash Drawer Heavy Duty", "$85.00", "Shop 2"),
            Triple("Touch POS Terminal 15-inch", "$650.00", "Shop 1")
        )
        likedItems.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.first, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Location: ${item.third}", color = TextMuted, fontSize = 11.sp)
                    }
                    Text(item.second, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MessagesTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("💬 Messages & Chats", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        val messages = listOf(
            Pair("System Admin", "Your daily POS shift report for Shop 1 has been verified."),
            Pair("Warehouse Manager", "New stock of barcode scanners has arrived."),
            Pair("Customer Support", "Inquiry resolved for invoice #1042.")
        )
        messages.forEach { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(msg.first, color = DarkBlueAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(msg.second, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun NotificationsTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("🔔 System Notifications", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        val notifications = listOf(
            Pair("Shift Open", "POS Terminal shift opened successfully at Shop 1."),
            Pair("Price Update", "Promotional price update applied to 4 items."),
            Pair("Backup Complete", "Local database backup synced to secure storage.")
        )
        notifications.forEach { notif ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(notif.first, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(notif.second, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FollowingShopsTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("🏪 Following Shops", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        val shops = listOf(
            Triple("Shop 1 - Central Retail", "Main Branch", "Following"),
            Triple("Shop 2 - Downtown Electronics", "Express Branch", "Following"),
            Triple("Shop 3 - Express Mart", "Suburban Branch", "Following")
        )
        shops.forEach { shop ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(shop.first, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(shop.second, color = TextMuted, fontSize = 11.sp)
                    }
                    Text(shop.third, color = DarkBlueAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CartHistoryTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("🛒 Cart & Sales History", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        val historyItems = listOf(
            Triple("Order #1052 - POS Checkout", "$340.00", "Completed • Today"),
            Triple("Order #1051 - Express Sale", "$85.50", "Completed • Yesterday"),
            Triple("Order #1050 - Retail Counter", "$190.00", "Completed • 2 days ago")
        )
        historyItems.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.first, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(item.third, color = TextMuted, fontSize = 11.sp)
                    }
                    Text(item.second, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun SavedProductsTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("🔖 Saved Products & Bookmarks", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        val savedItems = listOf(
            Triple("Receipt Paper Rolls (Box of 50)", "$45.00", "Saved for next order"),
            Triple("Barcode Labels 40x30mm", "$18.00", "Inventory stock item"),
            Triple("POS Touch Screen Protector", "$12.50", "Accessory")
        )
        savedItems.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.first, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(item.third, color = TextMuted, fontSize = 11.sp)
                    }
                    Text(item.second, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                            onUpdateItem = { updatedItem -> posViewModel.updateCartItem(updatedItem) },
                            onSplitItem = { itemToSplit -> posViewModel.splitCartItem(itemToSplit) },
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
