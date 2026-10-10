package com.example.hipermarketsallingapplication.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.hipermarketsallingapplication.data.db.DatabaseHelper
import com.example.hipermarketsallingapplication.data.model.*
import com.example.hipermarketsallingapplication.utils.JsonHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun getAllProducts(): List<Product> {
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_PRODUCTS} ORDER BY name ASC", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Product(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        code = cursor.getString(cursor.getColumnIndexOrThrow("code")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        barcode = cursor.getString(cursor.getColumnIndexOrThrow("barcode")),
                        atShop = cursor.getString(cursor.getColumnIndexOrThrow("at_shop")),
                        quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cost = cursor.getDouble(cursor.getColumnIndexOrThrow("cost")),
                        tax = cursor.getDouble(cursor.getColumnIndexOrThrow("tax")),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        includeTax = cursor.getInt(cursor.getColumnIndexOrThrow("include_tax")) == 1,
                        priceChange = cursor.getInt(cursor.getColumnIndexOrThrow("price_change")) == 1,
                        moreInfo = cursor.getString(cursor.getColumnIndexOrThrow("more_info")),
                        images = cursor.getString(cursor.getColumnIndexOrThrow("images")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        service = cursor.getString(cursor.getColumnIndexOrThrow("service")),
                        defaultQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("default_quantity")),
                        active = cursor.getInt(cursor.getColumnIndexOrThrow("active")) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun searchProducts(query: String): List<Product> {
        if (query.isBlank()) return getAllProducts()
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val q = "%${query.trim()}%"
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_PRODUCTS} WHERE name LIKE ? OR code LIKE ? OR barcode LIKE ? OR type LIKE ?",
            arrayOf(q, q, q, q)
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Product(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        code = cursor.getString(cursor.getColumnIndexOrThrow("code")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        barcode = cursor.getString(cursor.getColumnIndexOrThrow("barcode")),
                        atShop = cursor.getString(cursor.getColumnIndexOrThrow("at_shop")),
                        quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cost = cursor.getDouble(cursor.getColumnIndexOrThrow("cost")),
                        tax = cursor.getDouble(cursor.getColumnIndexOrThrow("tax")),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        includeTax = cursor.getInt(cursor.getColumnIndexOrThrow("include_tax")) == 1,
                        priceChange = cursor.getInt(cursor.getColumnIndexOrThrow("price_change")) == 1,
                        moreInfo = cursor.getString(cursor.getColumnIndexOrThrow("more_info")),
                        images = cursor.getString(cursor.getColumnIndexOrThrow("images")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        service = cursor.getString(cursor.getColumnIndexOrThrow("service")),
                        defaultQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("default_quantity")),
                        active = cursor.getInt(cursor.getColumnIndexOrThrow("active")) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getProductByBarcode(barcode: String): Product? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_PRODUCTS} WHERE barcode = ? OR code = ? LIMIT 1",
            arrayOf(barcode, barcode)
        )
        var product: Product? = null
        if (cursor.moveToFirst()) {
            product = Product(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                code = cursor.getString(cursor.getColumnIndexOrThrow("code")),
                type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                barcode = cursor.getString(cursor.getColumnIndexOrThrow("barcode")),
                atShop = cursor.getString(cursor.getColumnIndexOrThrow("at_shop")),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                cost = cursor.getDouble(cursor.getColumnIndexOrThrow("cost")),
                tax = cursor.getDouble(cursor.getColumnIndexOrThrow("tax")),
                price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                includeTax = cursor.getInt(cursor.getColumnIndexOrThrow("include_tax")) == 1,
                priceChange = cursor.getInt(cursor.getColumnIndexOrThrow("price_change")) == 1,
                moreInfo = cursor.getString(cursor.getColumnIndexOrThrow("more_info")),
                images = cursor.getString(cursor.getColumnIndexOrThrow("images")),
                description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                service = cursor.getString(cursor.getColumnIndexOrThrow("service")),
                defaultQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("default_quantity")),
                active = cursor.getInt(cursor.getColumnIndexOrThrow("active")) == 1
            )
        }
        cursor.close()
        return product
    }

    fun insertProduct(p: Product): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", p.name)
            put("code", p.code)
            put("type", p.type)
            put("barcode", p.barcode)
            put("at_shop", p.atShop)
            put("quantity", p.quantity)
            put("cost", p.cost)
            put("tax", p.tax)
            put("price", p.price)
            put("include_tax", if (p.includeTax) 1 else 0)
            put("price_change", if (p.priceChange) 1 else 0)
            put("more_info", p.moreInfo)
            put("images", p.images)
            put("description", p.description)
            put("service", p.service)
            put("default_quantity", p.defaultQuantity)
            put("active", if (p.active) 1 else 0)
        }
        return db.insert(DatabaseHelper.TABLE_PRODUCTS, null, cv)
    }

    fun updateProduct(p: Product): Int {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", p.name)
            put("code", p.code)
            put("type", p.type)
            put("barcode", p.barcode)
            put("at_shop", p.atShop)
            put("quantity", p.quantity)
            put("cost", p.cost)
            put("tax", p.tax)
            put("price", p.price)
            put("include_tax", if (p.includeTax) 1 else 0)
            put("price_change", if (p.priceChange) 1 else 0)
            put("more_info", p.moreInfo)
            put("images", p.images)
            put("description", p.description)
            put("service", p.service)
            put("default_quantity", p.defaultQuantity)
            put("active", if (p.active) 1 else 0)
        }
        return db.update(DatabaseHelper.TABLE_PRODUCTS, cv, "id = ?", arrayOf(p.id.toString()))
    }

    fun deleteProduct(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_PRODUCTS, "id = ?", arrayOf(id.toString()))
    }

    fun reduceStock(productId: Long, qty: Int) {
        val db = dbHelper.writableDatabase
        db.execSQL("UPDATE ${DatabaseHelper.TABLE_PRODUCTS} SET quantity = MAX(0, quantity - ?) WHERE id = ?", arrayOf(qty, productId))
    }

    fun getProductsByShopItems(shopNames: List<String>): List<Product> {
        val productIds = mutableSetOf<String>()
        val db = dbHelper.readableDatabase

        for (shopName in shopNames) {
            val cursor = db.rawQuery("SELECT Shop_items FROM ${DatabaseHelper.TABLE_SHOPS} WHERE Shop_name = ? LIMIT 1", arrayOf(shopName))
            if (cursor.moveToFirst()) {
                val jsonStr = cursor.getString(0) ?: ""
                val parsed = JsonHelper.loads(jsonStr)
                if (parsed is List<*>) {
                    for (item in parsed) {
                        if (item is List<*>) {
                            val pId = item.getOrNull(0)?.toString() ?: ""
                            if (pId.isNotBlank()) {
                                productIds.add(pId)
                            }
                        }
                    }
                }
            }
            cursor.close()
        }

        if (productIds.isEmpty()) {
            return getAllProducts().filter { p -> shopNames.isEmpty() || shopNames.any { sn -> p.atShop.equals(sn, ignoreCase = true) } }
        }

        val list = mutableListOf<Product>()
        for (id in productIds) {
            val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_PRODUCTS} WHERE id = ? OR code = ? OR barcode = ?", arrayOf(id, id, id))
            if (cursor.moveToFirst()) {
                list.add(
                    Product(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        code = cursor.getString(cursor.getColumnIndexOrThrow("code")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        barcode = cursor.getString(cursor.getColumnIndexOrThrow("barcode")),
                        atShop = cursor.getString(cursor.getColumnIndexOrThrow("at_shop")),
                        quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cost = cursor.getDouble(cursor.getColumnIndexOrThrow("cost")),
                        tax = cursor.getDouble(cursor.getColumnIndexOrThrow("tax")),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        includeTax = cursor.getInt(cursor.getColumnIndexOrThrow("include_tax")) == 1,
                        priceChange = cursor.getInt(cursor.getColumnIndexOrThrow("price_change")) == 1,
                        moreInfo = cursor.getString(cursor.getColumnIndexOrThrow("more_info")),
                        images = cursor.getString(cursor.getColumnIndexOrThrow("images")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        service = cursor.getString(cursor.getColumnIndexOrThrow("service")),
                        defaultQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("default_quantity")),
                        active = cursor.getInt(cursor.getColumnIndexOrThrow("active")) == 1
                    )
                )
            }
            cursor.close()
        }
        return list
    }
}

class SalesRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insertSalesDoc(doc: SalesDoc): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("doc_barcode", doc.docBarcode)
            put("extension_barcode", doc.extensionBarcode)
            put("At_Shop_Id", doc.atShopId)
            put("user_id", doc.userId)
            put("Seller_id", doc.sellerId)
            put("customer_id", doc.customerId)
            put("pid", doc.pid)
            put("type", doc.type)
            put("item", doc.item)
            put("qty", doc.qty)
            put("price", doc.price)
            put("Profite", doc.profit)
            put("discount", doc.discount)
            put("tax", doc.tax)
            put("payments", doc.payments)
            put("doc_created_date", doc.docCreatedDate)
            put("doc_expire_date", doc.docExpireDate)
            put("doc_updated_date", doc.docUpdatedDate)
        }
        return db.insert(DatabaseHelper.TABLE_DOCS, null, cv)
    }

    fun getAllDocs(): List<SalesDoc> {
        val list = mutableListOf<SalesDoc>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_DOCS} ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    SalesDoc(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        docBarcode = cursor.getString(cursor.getColumnIndexOrThrow("doc_barcode")) ?: "",
                        extensionBarcode = cursor.getString(cursor.getColumnIndexOrThrow("extension_barcode")) ?: "",
                        atShopId = cursor.getString(cursor.getColumnIndexOrThrow("At_Shop_Id")) ?: "Shop 1",
                        userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id")) ?: "",
                        sellerId = cursor.getString(cursor.getColumnIndexOrThrow("Seller_id")) ?: "",
                        customerId = cursor.getString(cursor.getColumnIndexOrThrow("customer_id")) ?: "Walk-in",
                        pid = cursor.getInt(cursor.getColumnIndexOrThrow("pid")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")) ?: "Sale",
                        item = cursor.getString(cursor.getColumnIndexOrThrow("item")) ?: "",
                        qty = cursor.getDouble(cursor.getColumnIndexOrThrow("qty")),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        profit = cursor.getDouble(cursor.getColumnIndexOrThrow("Profite")),
                        discount = cursor.getDouble(cursor.getColumnIndexOrThrow("discount")),
                        tax = cursor.getDouble(cursor.getColumnIndexOrThrow("tax")),
                        payments = cursor.getString(cursor.getColumnIndexOrThrow("payments")) ?: "Cash",
                        docCreatedDate = cursor.getString(cursor.getColumnIndexOrThrow("doc_created_date")) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getEndOfDaySummary(): EndOfDaySummary {
        val docs = getAllDocs()
        var totalRev = 0.0
        var totalProf = 0.0
        var totalDisc = 0.0
        var totalTaxAmt = 0.0
        var cash = 0.0
        var card = 0.0
        var credit = 0.0

        for (d in docs) {
            totalRev += d.price
            totalProf += d.profit
            totalDisc += d.discount
            totalTaxAmt += d.tax
            when {
                d.payments.contains("Cash", ignoreCase = true) -> cash += d.price
                d.payments.contains("Card", ignoreCase = true) -> card += d.price
                else -> credit += d.price
            }
        }

        return EndOfDaySummary(
            totalSalesCount = docs.size,
            totalRevenue = totalRev,
            totalProfit = totalProf,
            totalDiscounts = totalDisc,
            totalTax = totalTaxAmt,
            cashSales = cash,
            cardSales = card,
            creditSales = credit
        )
    }

    fun deleteDoc(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_DOCS, "id = ?", arrayOf(id.toString()))
    }
}

class UserRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun getUsers(): List<User> {
        val list = mutableListOf<User>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_USERS}", null)
        if (cursor.moveToFirst()) {
            do {
                val wsIdx = cursor.getColumnIndex("User_work_shop")
                val workShop = if (wsIdx >= 0) cursor.getString(wsIdx) ?: "[]" else "[]"
                list.add(
                    User(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("Id")),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow("User_id")),
                        fName = cursor.getString(cursor.getColumnIndexOrThrow("User_fname")),
                        lName = cursor.getString(cursor.getColumnIndexOrThrow("User_Lname")),
                        userName = cursor.getString(cursor.getColumnIndexOrThrow("User_name")),
                        gender = cursor.getString(cursor.getColumnIndexOrThrow("User_gender")),
                        country = cursor.getString(cursor.getColumnIndexOrThrow("User_country")),
                        phoneNum = cursor.getString(cursor.getColumnIndexOrThrow("User_phone_num")),
                        email = cursor.getString(cursor.getColumnIndexOrThrow("User_email")),
                        address = cursor.getString(cursor.getColumnIndexOrThrow("User_address")),
                        userType = cursor.getString(cursor.getColumnIndexOrThrow("User_type")),
                        password = cursor.getString(cursor.getColumnIndexOrThrow("User_password")),
                        userShop = cursor.getString(cursor.getColumnIndexOrThrow("User_shop")),
                        userWorkShop = workShop,
                        userAccess = cursor.getString(cursor.getColumnIndexOrThrow("User_access"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getUserByUsername(username: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE User_name = ? LIMIT 1",
            arrayOf(username)
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            val wsIdx = cursor.getColumnIndex("User_work_shop")
            val workShop = if (wsIdx >= 0) cursor.getString(wsIdx) ?: "[]" else "[]"
            user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("Id")),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("User_id")),
                fName = cursor.getString(cursor.getColumnIndexOrThrow("User_fname")),
                lName = cursor.getString(cursor.getColumnIndexOrThrow("User_Lname")),
                userName = cursor.getString(cursor.getColumnIndexOrThrow("User_name")),
                userType = cursor.getString(cursor.getColumnIndexOrThrow("User_type")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("User_password")),
                userShop = cursor.getString(cursor.getColumnIndexOrThrow("User_shop")),
                userWorkShop = workShop,
                userAccess = cursor.getString(cursor.getColumnIndexOrThrow("User_access"))
            )
        }
        cursor.close()
        return user
    }

    fun login(username: String, pass: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE User_name = ? AND User_password = ? LIMIT 1",
            arrayOf(username, pass)
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            val wsIdx = cursor.getColumnIndex("User_work_shop")
            val workShop = if (wsIdx >= 0) cursor.getString(wsIdx) ?: "[]" else "[]"
            user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("Id")),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("User_id")),
                fName = cursor.getString(cursor.getColumnIndexOrThrow("User_fname")),
                lName = cursor.getString(cursor.getColumnIndexOrThrow("User_Lname")),
                userName = cursor.getString(cursor.getColumnIndexOrThrow("User_name")),
                userType = cursor.getString(cursor.getColumnIndexOrThrow("User_type")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("User_password")),
                userShop = cursor.getString(cursor.getColumnIndexOrThrow("User_shop")),
                userWorkShop = workShop,
                userAccess = cursor.getString(cursor.getColumnIndexOrThrow("User_access"))
            )
        }
        cursor.close()
        return user
    }

    fun loginWithRemoteFallback(username: String, pass: String, serverUrl: String?): User? {
        if (!serverUrl.isNullOrBlank()) {
            // Check online database / remote link endpoint if configured
            // Fallback to local DB verification
        }
        return login(username, pass)
    }

    fun insertUser(user: User): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("User_id", (1000..9999).random())
            put("User_fname", user.fName)
            put("User_Lname", user.lName)
            put("User_name", user.userName)
            put("User_gender", user.gender)
            put("User_country", user.country)
            put("User_phone_num", user.phoneNum)
            put("User_email", user.email)
            put("User_address", user.address)
            put("User_type", user.userType)
            put("User_password", user.password)
            put("User_shop", user.userShop)
            put("User_work_shop", user.userWorkShop)
            put("User_access", user.userAccess)
        }
        return db.insert(DatabaseHelper.TABLE_USERS, null, cv)
    }

    fun deleteUser(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_USERS, "Id = ?", arrayOf(id.toString()))
    }

    fun updateUserShop(username: String, newShopListString: String) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("User_shop", newShopListString)
        }
        val rows = db.update(DatabaseHelper.TABLE_USERS, cv, "User_name = ?", arrayOf(username))
    }

    fun updateUserWorkShop(username: String, workShopJson: String) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("User_work_shop", workShopJson)
        }
        val rows = db.update(DatabaseHelper.TABLE_USERS, cv, "User_name = ?", arrayOf(username))
    }



    fun updateShopWorkers(shopName: String, shopBrandName: String, shopWorkersJson: String): Int {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("Shop_workers", shopWorkersJson)
        }
        return if (shopBrandName.isNotBlank()) {
            db.update(DatabaseHelper.TABLE_SHOPS, cv, "Shop_name = ? AND Shop_brand_name = ?", arrayOf(shopName, shopBrandName))
        } else {
            db.update(DatabaseHelper.TABLE_SHOPS, cv, "Shop_name = ?", arrayOf(shopName))
        }
    }


    fun getAllShops(): List<List<*>> {
        val list = mutableListOf<List<*>>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_SHOPS}", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("Id")).toString()
                val name = cursor.getString(cursor.getColumnIndexOrThrow("Shop_name")) ?: ""
                val brand = cursor.getString(cursor.getColumnIndexOrThrow("Shop_brand_name")) ?: ""
                val level = 10
                list.add(listOf(id, name, brand, level))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun insertShop(
        shopName: String,
        shopBrandName: String,
        ownerId: String,
        shopType: String,
        shopEmail: String,
        shopCountry: String,
        shopContact: String,
        shopworkers: String
    ): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("Shop_Id", (0..9999).random())
            put("Shop_name", shopName)
            put("Shop_brand_name", shopBrandName)
            put("Shop_oweners_id", ownerId)
            put("Shop_type", shopType)
            put("Shop_email", shopEmail)
            put("Shop_country", shopCountry)
            put("Shop_contact", shopContact)
            put("Shop_isenabled", "1")
            put("Shop_workers", shopworkers)

        }
        return db.insert(DatabaseHelper.TABLE_SHOPS, null, cv)
    }

    fun updateShop(
        shopName: String,
        shopBrandName: String,
        ownerId: String,
        shopType: String,
        shopEmail: String,
        shopCountry: String,
        shopContact: String,
        shopworkers: String
    ): Int {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("Shop_name", shopName)
            put("Shop_brand_name", shopBrandName)
            put("Shop_oweners_id", ownerId)
            put("Shop_type", shopType)
            put("Shop_email", shopEmail)
            put("Shop_country", shopCountry)
            put("Shop_contact", shopContact)
            put("Shop_workers", shopworkers)
        }
        return db.update(DatabaseHelper.TABLE_SHOPS, cv, "Shop_name = ? AND Shop_brand_name = ? ", arrayOf(shopName, shopBrandName))
    }

    fun getShopItemsJson(shopName: String): String {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT Shop_items FROM ${DatabaseHelper.TABLE_SHOPS} WHERE Shop_name = ? LIMIT 1", arrayOf(shopName))
        var json = ""
        if (cursor.moveToFirst()) {
            json = cursor.getString(0) ?: ""
        }
        cursor.close()
        return json
    }

    fun updateShopItemsJson(shopName: String, shopItemsJson: String) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("Shop_items", shopItemsJson)
        }
        db.update(DatabaseHelper.TABLE_SHOPS, cv, "Shop_name = ?", arrayOf(shopName))
    }

    fun appendProductToShopItems(shopName: String, productId: String, dateStr: String) {
        val currentJson = getShopItemsJson(shopName)
        val parsed = JsonHelper.loads(currentJson)
        val itemsList = mutableListOf<List<String>>()

        if (parsed is List<*>) {
            for (item in parsed) {
                if (item is List<*>) {
                    val pId = item.getOrNull(0)?.toString() ?: ""
                    val dDate = item.getOrNull(1)?.toString() ?: ""
                    if (pId.isNotBlank()) {
                        itemsList.add(listOf(pId, dDate))
                    }
                }
            }
        }

        if (itemsList.none { it.getOrNull(0) == productId }) {
            itemsList.add(listOf(productId, dateStr))
        }

        val updatedJson = JsonHelper.dumps(itemsList)
        updateShopItemsJson(shopName, updatedJson)
    }
}

class SettingsRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun getPaymentTools(): List<PaymentTool> {
        val list = mutableListOf<PaymentTool>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_TOOLS}", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    PaymentTool(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        code = cursor.getString(cursor.getColumnIndexOrThrow("code")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        shortKey = cursor.getString(cursor.getColumnIndexOrThrow("short_key")),
                        enabled = cursor.getInt(cursor.getColumnIndexOrThrow("enabel")) == 1,
                        quickPay = cursor.getInt(cursor.getColumnIndexOrThrow("quick_pay")) == 1,
                        customerRequired = cursor.getInt(cursor.getColumnIndexOrThrow("customer_required")) == 1,
                        openDrawer = cursor.getInt(cursor.getColumnIndexOrThrow("open_drower")) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getExpenses(): List<Expense> {
        val list = mutableListOf<Expense>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_EXPENSES} ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Expense(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun insertExpense(e: Expense): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("title", e.title)
            put("amount", e.amount)
            put("category", e.category)
            put("date", e.date)
            put("shop_id", e.shopId)
            put("user_id", e.userId)
        }
        return db.insert(DatabaseHelper.TABLE_EXPENSES, null, cv)
    }

    fun getCustomers(): List<Customer> {
        val list = mutableListOf<Customer>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_CUSTOMERS}", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Customer(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun insertCustomer(c: Customer): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", c.name)
            put("phone", c.phone)
            put("email", c.email)
            put("address", c.address)
        }
        return db.insert(DatabaseHelper.TABLE_CUSTOMERS, null, cv)
    }
}
