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
    val code: String = "",
    val title: String = "",
    val createdDate: String = "",
    val fromDate: String = "",
    val toDate: String = "",
    val ifTotalPrice: Double = 0.0,
    val ifTotalDiscount: Double = 0.0,
    val ifTotalQty: Double = 0.0,
    val ifTotalProfit: Double = 0.0,
    val ifItems: String = "",
    val doMakeTotalPrice: Double = 0.0,
    val doMakeTotalDiscount: Double = 0.0,
    val doItems: String = "",
    val enabled: Boolean = true
)

data class PaymentTool(
    val id: Long = 0,
    val shopId: String = "1",
    val shopName: String = "Main Shop",
    val name: String,
    val method: String = "CASH",
    val type: String = "Payment",
    val code: String = "",
    val shortKey: String = "",
    val accessKey: String = "",
    val enabled: Boolean = true,
    val quickPay: Boolean = true,
    val markPad: Boolean = false,
    val customerRequired: Boolean = false,
    val openDrawer: Boolean = true,
    val printSlip: Boolean = true,
    val changeAllowed: Boolean = true
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

data class UserWorkItem(
    val id: String,
    val name: String,
    val brandName: String = "MainBrand",
    val level: Int = 10
) {
    val userLevel: Int get() = level
    fun toList(): List<Any> = listOf(id, name, brandName, level)
}

data class MoreInfoItemDetails(
    val barcode: String = "",
    val type: String = "",
    val price: Double? = null,
    val qty: Double? = null,
    val qtyLeft: Double? = null
)

class MoreInfoParser(
    val moreInfoJson: String,
    val fallbackShop: String = "Main Shop",
    val fallbackCode: String = "P101",
    val fallbackColor: String = "Default",
    val fallbackSize: String = "M"
) {
    private val rawShops = mutableListOf<ShopNode>()

    data class ShopNode(val shopName: String, val codes: List<CodeNode>)
    data class CodeNode(val codeName: String, val colors: List<ColorNode>)
    data class ColorNode(val colorName: String, val sizes: List<SizeNode>)
    data class SizeNode(val sizeName: String, val details: List<MoreInfoItemDetails>)

    init {
        parseJson(moreInfoJson)
    }

    private fun parseJson(jsonStr: String) {
        if (jsonStr.isBlank()) return
        try {
            val rootArray: org.json.JSONArray = if (jsonStr.trim().startsWith("{")) {
                val obj = org.json.JSONObject(jsonStr)
                obj.optJSONArray("item_list") ?: org.json.JSONArray()
            } else if (jsonStr.trim().startsWith("[")) {
                org.json.JSONArray(jsonStr)
            } else {
                org.json.JSONArray()
            }

            for (s in 0 until rootArray.length()) {
                val shopArr = rootArray.optJSONArray(s) ?: continue
                if (shopArr.length() < 2) continue
                val shopName = shopArr.optString(0, fallbackShop).ifBlank { fallbackShop }
                val codesArr = shopArr.optJSONArray(1) ?: org.json.JSONArray()

                val codeNodes = mutableListOf<CodeNode>()
                for (c in 0 until codesArr.length()) {
                    val codeArr = codesArr.optJSONArray(c) ?: continue
                    if (codeArr.length() < 2) continue
                    val codeName = codeArr.optString(0, fallbackCode).ifBlank { fallbackCode }
                    val colorsArr = codeArr.optJSONArray(1) ?: org.json.JSONArray()

                    val colorNodes = mutableListOf<ColorNode>()
                    for (clr in 0 until colorsArr.length()) {
                        val colorArr = colorsArr.optJSONArray(clr) ?: continue
                        if (colorArr.length() < 2) continue
                        val colorName = colorArr.optString(0, fallbackColor).ifBlank { fallbackColor }
                        val sizesArr = colorArr.optJSONArray(1) ?: org.json.JSONArray()

                        val sizeNodes = mutableListOf<SizeNode>()
                        for (sz in 0 until sizesArr.length()) {
                            val sizeArr = sizesArr.optJSONArray(sz) ?: continue
                            if (sizeArr.length() < 2) continue
                            val sizeName = sizeArr.optString(0, fallbackSize).ifBlank { fallbackSize }
                            val detailsArr = sizeArr.optJSONArray(1) ?: org.json.JSONArray()

                            val itemDetailsList = mutableListOf<MoreInfoItemDetails>()
                            for (dt in 0 until detailsArr.length()) {
                                val dtArr = detailsArr.optJSONArray(dt)
                                if (dtArr != null && dtArr.length() > 0) {
                                    val bCode = dtArr.optString(0, "")
                                    val tType = dtArr.optString(1, "")
                                    val pr = dtArr.optDouble(2, Double.NaN)
                                    val qt = dtArr.optDouble(3, Double.NaN)
                                    val qL = dtArr.optDouble(4, Double.NaN)
                                    itemDetailsList.add(
                                        MoreInfoItemDetails(
                                            barcode = bCode,
                                            type = tType,
                                            price = if (pr.isNaN()) null else pr,
                                            qty = if (qt.isNaN()) null else qt,
                                            qtyLeft = if (qL.isNaN()) null else qL
                                        )
                                    )
                                }
                            }
                            sizeNodes.add(SizeNode(sizeName, itemDetailsList))
                        }
                        colorNodes.add(ColorNode(colorName, sizeNodes))
                    }
                    codeNodes.add(CodeNode(codeName, colorNodes))
                }
                rawShops.add(ShopNode(shopName, codeNodes))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getShops(): List<String> {
        val list = rawShops.map { it.shopName }.distinct()
        return if (list.isNotEmpty()) list else listOf(fallbackShop)
    }

    fun getCodes(shop: String): List<String> {
        val sNode = rawShops.find { it.shopName.equals(shop, ignoreCase = true) } ?: rawShops.firstOrNull()
        val list = sNode?.codes?.map { it.codeName }?.distinct() ?: emptyList()
        return if (list.isNotEmpty()) list else listOf(fallbackCode)
    }

    fun getColors(shop: String, code: String): List<String> {
        val sNode = rawShops.find { it.shopName.equals(shop, ignoreCase = true) } ?: rawShops.firstOrNull()
        val cNode = sNode?.codes?.find { it.codeName.equals(code, ignoreCase = true) } ?: sNode?.codes?.firstOrNull()
        val list = cNode?.colors?.map { it.colorName }?.distinct() ?: emptyList()
        return if (list.isNotEmpty()) list else listOf(fallbackColor)
    }

    fun getSizes(shop: String, code: String, color: String): List<String> {
        val sNode = rawShops.find { it.shopName.equals(shop, ignoreCase = true) } ?: rawShops.firstOrNull()
        val cNode = sNode?.codes?.find { it.codeName.equals(code, ignoreCase = true) } ?: sNode?.codes?.firstOrNull()
        val clrNode = cNode?.colors?.find { it.colorName.equals(color, ignoreCase = true) } ?: cNode?.colors?.firstOrNull()
        val list = clrNode?.sizes?.map { it.sizeName }?.distinct() ?: emptyList()
        return if (list.isNotEmpty()) list else listOf(fallbackSize)
    }

    fun getItemDetails(shop: String, code: String, color: String, size: String): MoreInfoItemDetails? {
        val sNode = rawShops.find { it.shopName.equals(shop, ignoreCase = true) } ?: rawShops.firstOrNull()
        val cNode = sNode?.codes?.find { it.codeName.equals(code, ignoreCase = true) } ?: sNode?.codes?.firstOrNull()
        val clrNode = cNode?.colors?.find { it.colorName.equals(color, ignoreCase = true) } ?: cNode?.colors?.firstOrNull()
        val szNode = clrNode?.sizes?.find { it.sizeName.equals(size, ignoreCase = true) } ?: clrNode?.sizes?.firstOrNull()
        return szNode?.details?.firstOrNull()
    }
}
