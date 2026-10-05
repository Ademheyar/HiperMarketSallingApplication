package com.example.hipermarketsallingapplication.data.model

data class Product(
    val id: Long = 0,
    val name: String,
    val code: String,
    val type: String,
    val barcode: String,
    val atShop: String = "Shop 1",
    val quantity: Int = 0,
    val cost: Double = 0.0,
    val tax: Double = 0.0,
    val price: Double = 0.0,
    val includeTax: Boolean = true,
    val priceChange: Boolean = false,
    val moreInfo: String = "",
    val images: String = "",
    val description: String = "",
    val service: String = "",
    val defaultQuantity: Int = 1,
    val active: Boolean = true
)

data class CartItem(
    val product: Product,
    var qty: Double = 1.0,
    var price: Double = product.price,
    var discount: Double = 0.0,
    var tax: Double = product.tax
) {
    val subtotal: Double get() = (price * qty) - discount
    val totalTax: Double get() = tax * qty
    val grandTotal: Double get() = subtotal + totalTax
}

data class SavedCart(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val customer: String,
    val items: List<CartItem>,
    val timestamp: String
)

data class SalesDoc(
    val id: Long = 0,
    val docBarcode: String,
    val extensionBarcode: String = "",
    val atShopId: String = "1",
    val userId: String = "1",
    val sellerId: String = "1",
    val customerId: String = "1",
    val pid: Int = 0,
    val type: String = "Sale",
    val item: String, // JSON or summary of items
    val qty: Double = 0.0,
    val price: Double = 0.0,
    val profit: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val payments: String = "Cash", // Cash, Card, Split, Credit
    val docCreatedDate: String,
    val docExpireDate: String = "",
    val docUpdatedDate: String = ""
)

data class User(
    val id: Long = 0,
    val userId: Int = 1,
    val fName: String,
    val lName: String,
    val userName: String,
    val gender: String = "Male",
    val country: String = "N/A",
    val phoneNum: String = "",
    val email: String = "",
    val address: String = "",
    val userType: String = "Admin", // Admin, Cashier, Manager
    val password: String = "0000",
    val userShop: String = "Shop 1",
    val userWorkShop: String = "[]",
    val userAccess: String = ""
)

data class PromotionAction(
    val id: Long = 0,
    val productId: String,
    val productName: String,
    val productCode: String,
    val oldPrice: Double,
    val newPrice: Double,
    val discountPercent: Int,
    val enabled: Boolean = true,
    val fromDate: String,
    val toDate: String
)

data class PaymentTool(
    val id: Long = 0,
    val name: String,
    val code: String,
    val type: String = "Payment",
    val shortKey: String = "",
    val enabled: Boolean = true,
    val quickPay: Boolean = true,
    val customerRequired: Boolean = false,
    val openDrawer: Boolean = true
)

data class Expense(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val shopId: String = "1",
    val userId: String = "1"
)

data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = ""
)

data class EndOfDaySummary(
    val totalSalesCount: Int,
    val totalRevenue: Double,
    val totalProfit: Double,
    val totalDiscounts: Double,
    val totalTax: Double,
    val cashSales: Double,
    val cardSales: Double,
    val creditSales: Double
)
