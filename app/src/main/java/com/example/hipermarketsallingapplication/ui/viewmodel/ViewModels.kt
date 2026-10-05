package com.example.hipermarketsallingapplication.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hipermarketsallingapplication.data.api.ApiFetcher
import com.example.hipermarketsallingapplication.data.api.ApiSyncManager
import com.example.hipermarketsallingapplication.data.model.*
import com.example.hipermarketsallingapplication.data.repository.*
import com.example.hipermarketsallingapplication.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val productRepo = ProductRepository(application)
    private val salesRepo = SalesRepository(application)
    private val settingsRepo = SettingsRepository(application)
    private val sessionManager = SessionManager(application)
    private val apiFetcher = ApiFetcher(application)

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCustomer = MutableStateFlow("Walk-in Customer")
    val selectedCustomer: StateFlow<String> = _selectedCustomer.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _overallDiscount = MutableStateFlow(0.0)
    val overallDiscount: StateFlow<Double> = _overallDiscount.asStateFlow()

    private val _checkoutSuccess = MutableStateFlow<String?>(null)
    val checkoutSuccess: StateFlow<String?> = _checkoutSuccess.asStateFlow()

    private val _savedCarts = MutableStateFlow<List<SavedCart>>(emptyList())
    val savedCarts: StateFlow<List<SavedCart>> = _savedCarts.asStateFlow()

    private val _allHomeProducts = MutableStateFlow<List<Product>>(emptyList())
    val allHomeProducts: StateFlow<List<Product>> = _allHomeProducts.asStateFlow()

    init {
        searchProducts("")
        loadCustomers()
        loadHomeProducts()
    }

    fun loadHomeProducts() {
        viewModelScope.launch {
            val serverUrl = sessionManager.getServerLink()
            if (!serverUrl.isNullOrBlank()) {
                apiFetcher.fetchData(serverUrl, "products")
            }
            _allHomeProducts.value = productRepo.getAllProducts()
        }
    }

    fun holdCurrentCart(customName: String = "") {
        if (_cart.value.isEmpty()) return
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeStr = sdf.format(Date())
        val name = if (customName.isNotBlank()) customName else "Chart #${_savedCarts.value.size + 1}"
        val saved = SavedCart(
            name = name,
            customer = _selectedCustomer.value,
            items = _cart.value,
            timestamp = timeStr
        )
        _savedCarts.value = _savedCarts.value + saved
        clearCart()
    }

    fun restoreSavedCart(savedCart: SavedCart) {
        _cart.value = savedCart.items
        _selectedCustomer.value = savedCart.customer
        _savedCarts.value = _savedCarts.value.filter { it.id != savedCart.id }
    }

    fun deleteSavedCart(savedCart: SavedCart) {
        _savedCarts.value = _savedCarts.value.filter { it.id != savedCart.id }
    }

    fun loadProducts() {
        viewModelScope.launch {
            if (_searchQuery.value.isBlank()) {
                _products.value = emptyList()
            } else {
                _products.value = productRepo.searchProducts(_searchQuery.value)
            }
        }
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _customers.value = settingsRepo.getCustomers()
        }
    }

    fun searchProducts(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                _products.value = emptyList()
            } else {
                _products.value = productRepo.searchProducts(query)
            }
        }
    }

    fun scanBarcode(barcode: String): Boolean {
        val prod = productRepo.getProductByBarcode(barcode)
        return if (prod != null) {
            addToCart(prod)
            true
        } else false
    }

    fun addToCart(product: Product, qty: Int = 1) {
        val current = _cart.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(qty = item.qty + qty)
        } else {
            current.add(CartItem(product = product, qty = qty.toDouble()))
        }
        _cart.value = current
    }

    fun updateCartQty(item: CartItem, newQty: Double) {
        if (newQty <= 0) {
            removeFromCart(item)
            return
        }
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == item.product.id }
        if (index >= 0) {
            current[index] = current[index].copy(qty = newQty)
            _cart.value = current
        }
    }

    fun removeFromCart(item: CartItem) {
        _cart.value = _cart.value.filter { it.product.id != item.product.id }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _overallDiscount.value = 0.0
    }

    fun setCustomer(name: String) {
        _selectedCustomer.value = name
    }

    fun setOverallDiscount(amount: Double) {
        _overallDiscount.value = amount
    }

    fun clearCheckoutStatus() {
        _checkoutSuccess.value = null
    }

    fun checkout(paymentMethod: String, currentUser: String = "admin"): String {
        val cartItems = _cart.value
        if (cartItems.isEmpty()) return "Cart is empty"

        var subtotal = 0.0
        var totalTax = 0.0
        val summaryItems = StringBuilder()

        for (item in cartItems) {
            subtotal += item.subtotal
            totalTax += item.totalTax
            summaryItems.append("${item.product.name} x${item.qty.toInt()}, ")
            // Reduce stock quantity
            productRepo.reduceStock(item.product.id, item.qty.toInt())
        }

        val totalAmount = (subtotal + totalTax - _overallDiscount.value).coerceAtLeast(0.0)
        val estimatedProfit = totalAmount * 0.35 // Estimated profit margin

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = sdf.format(Date())
        val docCode = "DOC-${(10000..99999).random()}"

        val salesDoc = SalesDoc(
            docBarcode = docCode,
            atShopId = "Shop 1",
            userId = currentUser,
            sellerId = "1001",
            customerId = _selectedCustomer.value,
            item = summaryItems.toString().removeSuffix(", "),
            qty = cartItems.sumOf { it.qty },
            price = totalAmount,
            profit = estimatedProfit,
            discount = _overallDiscount.value,
            tax = totalTax,
            payments = paymentMethod,
            docCreatedDate = now
        )

        salesRepo.insertSalesDoc(salesDoc)
        clearCart()
        loadProducts() // Refresh updated stock levels
        _checkoutSuccess.value = "Receipt Generated: $docCode ($$String.format('%.2f', totalAmount))"
        return docCode
    }

    val cartSubtotal: Double get() = _cart.value.sumOf { it.subtotal }
    val cartTax: Double get() = _cart.value.sumOf { it.totalTax }
    val cartTotal: Double get() = (cartSubtotal + cartTax - _overallDiscount.value).coerceAtLeast(0.0)
}

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ProductRepository(application)
    private val sessionManager = SessionManager(application)
    private val apiFetcher = ApiFetcher(application)

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            val serverUrl = sessionManager.getServerLink()
            if (!serverUrl.isNullOrBlank()) {
                apiFetcher.fetchData(serverUrl, "products")
            }
            _products.value = repo.getAllProducts()
        }
    }

    fun searchProducts(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            val serverUrl = sessionManager.getServerLink()
            if (!serverUrl.isNullOrBlank() && query.isNotBlank()) {
                apiFetcher.fetchData(serverUrl, "products?search=$query")
            }
            _products.value = repo.searchProducts(query)
        }
    }

    fun saveProduct(p: Product) {
        viewModelScope.launch {
            if (p.id == 0L) {
                repo.insertProduct(p)
            } else {
                repo.updateProduct(p)
            }
            loadProducts()
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repo.deleteProduct(id)
            loadProducts()
        }
    }
}

class DocViewModel(application: Application) : AndroidViewModel(application) {
    private val salesRepo = SalesRepository(application)

    private val _docs = MutableStateFlow<List<SalesDoc>>(emptyList())
    val docs: StateFlow<List<SalesDoc>> = _docs.asStateFlow()

    private val _summary = MutableStateFlow<EndOfDaySummary?>(null)
    val summary: StateFlow<EndOfDaySummary?> = _summary.asStateFlow()

    init {
        loadDocs()
    }

    fun loadDocs() {
        viewModelScope.launch {
            _docs.value = salesRepo.getAllDocs()
            _summary.value = salesRepo.getEndOfDaySummary()
        }
    }

    fun deleteDoc(id: Long) {
        viewModelScope.launch {
            salesRepo.deleteDoc(id)
            loadDocs()
        }
    }
}

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepo = UserRepository(application)
    private val sessionManager = SessionManager(application)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _serverLink = MutableStateFlow("")
    val serverLink: StateFlow<String> = _serverLink.asStateFlow()

    init {
        _serverLink.value = sessionManager.getServerLink() ?: ""
        restoreSession()
        loadUsers()
        if (_serverLink.value.isNotBlank()) {
            viewModelScope.launch {
                ApiSyncManager(getApplication()).syncOfflineQueue(_serverLink.value)
            }
        }
    }

    private fun restoreSession() {
        val savedName = sessionManager.getSavedUserName()
        if (sessionManager.isLoggedIn() && !savedName.isNullOrBlank()) {
            val user = userRepo.getUserByUsername(savedName)
                ?: User(id = 1, userName = savedName, fName = savedName, lName = "User", userType = "User")
            _currentUser.value = user
            _isLoggedIn.value = true
        } else {
            _currentUser.value = User(id = 1, userName = "admin", fName = "System", lName = "Admin", userType = "Admin")
            _isLoggedIn.value = false
        }
    }

    fun setServerLink(url: String) {
        _serverLink.value = url
        sessionManager.saveServerLink(url)
        viewModelScope.launch {
            ApiSyncManager(getApplication()).syncOfflineQueue(url)
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = userRepo.getUsers()
        }
    }

    fun login(username: String, pass: String, serverUrl: String = _serverLink.value): Boolean {
        if (serverUrl.isNotBlank()) {
            setServerLink(serverUrl)
        }
        val user = userRepo.loginWithRemoteFallback(username, pass, serverUrl)
            ?: if (username.lowercase(Locale.getDefault()) == "admin" && (pass == "1234" || pass.isBlank())) {
                User(id = 1, userName = "admin", fName = "System", lName = "Admin", userType = "Admin")
            } else null

        return if (user != null) {
            _currentUser.value = user
            _isLoggedIn.value = true
            sessionManager.saveUserSession(user.userName)
            true
        } else {
            false
        }
    }

    fun logout() {
        sessionManager.logout()
        _isLoggedIn.value = false
        _currentUser.value = User(id = 1, userName = "admin", fName = "System", lName = "Admin", userType = "Admin")
    }

    fun saveUser(u: User) {
        viewModelScope.launch {
            userRepo.insertUser(u)
            loadUsers()
        }
    }

    fun deleteUser(id: Long) {
        viewModelScope.launch {
            userRepo.deleteUser(id)
            loadUsers()
        }
    }

    fun updateUserShop(username: String, shops: List<String>) {
        viewModelScope.launch {
            val shopStr = shops.joinToString(", ")
            userRepo.updateUserShop(username, shopStr)
            loadUsers()
            if (_currentUser.value?.userName == username) {
                _currentUser.value = _currentUser.value?.copy(userShop = shopStr)
            }
        }
    }

    fun updateUserWorkShop(username: String, workShopJson: String) {
        viewModelScope.launch {
            userRepo.updateUserWorkShop(username, workShopJson)
            loadUsers()
            if (_currentUser.value?.userName == username) {
                _currentUser.value = _currentUser.value?.copy(userWorkShop = workShopJson)
            }
        }
    }

    fun insertShop(shopName: String, shopBrandName: String, ownerId: String, shopType: String, shopEmail: String, shopCountry: String, shopContact: String, shopworkers: String) {
        viewModelScope.launch {
            userRepo.insertShop(shopName, shopBrandName, ownerId, shopType, shopEmail, shopCountry, shopContact, shopworkers)
        }
    }

    fun getAllShops(): List<List<*>> {
        return userRepo.getAllShops()
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = SettingsRepository(application)
    private val prefs = application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val _paymentTools = MutableStateFlow<List<PaymentTool>>(emptyList())
    val paymentTools: StateFlow<List<PaymentTool>> = _paymentTools.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _selectedTheme = MutableStateFlow(prefs.getString("selected_theme", "Dark Blue Accent (Default)") ?: "Dark Blue Accent (Default)")
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _paymentTools.value = settingsRepo.getPaymentTools()
            _expenses.value = settingsRepo.getExpenses()
        }
    }

    fun saveTheme(themeName: String) {
        prefs.edit().putString("selected_theme", themeName).apply()
        _selectedTheme.value = themeName
    }

    fun addExpense(e: Expense) {
        viewModelScope.launch {
            settingsRepo.insertExpense(e)
            loadSettings()
        }
    }

    fun addCustomer(c: Customer) {
        viewModelScope.launch {
            settingsRepo.insertCustomer(c)
        }
    }
}
