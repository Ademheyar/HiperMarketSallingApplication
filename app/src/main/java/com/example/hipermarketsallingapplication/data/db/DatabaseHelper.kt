package com.example.hipermarketsallingapplication.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.hipermarketsallingapplication.data.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "my_database.db"
        private const val DATABASE_VERSION = 3

        // Table Names
        const val TABLE_PRODUCTS = "product"
        const val TABLE_DOCS = "doc_table"
        const val TABLE_USERS = "Users"
        const val TABLE_ACTIONS = "Actions"
        const val TABLE_TOOLS = "tools"
        const val TABLE_EXPENSES = "expenses"
        const val TABLE_CUSTOMERS = "customers"
        const val TABLE_SHOPS = "shops"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Product Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_PRODUCTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                code TEXT,
                type TEXT,
                barcode TEXT,
                at_shop TEXT,
                quantity INTEGER,
                cost REAL,
                tax REAL,
                price REAL,
                include_tax INTEGER,
                price_change INTEGER,
                more_info TEXT,
                images TEXT,
                description TEXT,
                service TEXT,
                default_quantity INTEGER,
                active INTEGER
            )
            """.trimIndent()
        )

        // Doc Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_DOCS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                doc_barcode TEXT,
                extension_barcode TEXT,
                At_Shop_Id TEXT,
                user_id TEXT,
                Seller_id TEXT,
                customer_id TEXT,
                pid INT,
                type TEXT,
                item TEXT,
                qty REAL,
                price REAL,
                Profite REAL,
                discount REAL,
                tax REAL,
                payments TEXT,
                doc_created_date TEXT,
                doc_expire_date TEXT,
                doc_updated_date TEXT
            )
            """.trimIndent()
        )

        // Users Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                User_id INTEGER,
                User_fname TEXT,
                User_Lname TEXT,
                User_name TEXT,
                User_gender TEXT,
                User_country TEXT,
                User_phone_num TEXT,
                User_email TEXT,
                User_address TEXT,
                User_id_pp_num TEXT,
                User_home_no TEXT,
                User_type TEXT,
                User_password TEXT,
                User_about TEXT,
                User_shop TEXT,
                User_work_shop TEXT,
                User_likes TEXT,
                User_following_shop TEXT,
                User_favoraite_items TEXT,
                User_rate TEXT,
                User_access TEXT,
                User_pimg TEXT
            )
            """.trimIndent()
        )

        // Actions Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_ACTIONS (
                Actions_Id INTEGER PRIMARY KEY AUTOINCREMENT,
                Product_Id TEXT,
                Product_Name TEXT,
                Product_Code TEXT,
                Product_Price_is REAL,
                Product_Make_price REAL,
                Product_Make_Discount INT,
                Enabel INT,
                From_Date TEXT,
                TO_Date TEXT
            )
            """.trimIndent()
        )

        // Tools Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_TOOLS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                code TEXT,
                type TEXT,
                short_key TEXT,
                enabel INTEGER,
                quick_pay INTEGER,
                customer_required INTEGER,
                open_drower INTEGER
            )
            """.trimIndent()
        )

        // Expenses Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_EXPENSES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                category TEXT,
                amount REAL,
                date TEXT,
                note TEXT,
                user_id TEXT
            )
            """.trimIndent()
        )

        // Customers Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_CUSTOMERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                phone TEXT,
                email TEXT,
                address TEXT
            )
            """.trimIndent()
        )

        // Shops Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_SHOPS (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
              Shop_Id INTEGER,
              Shop_name TEXT,
              Shop_brand_name TEXT,
              Shop_oweners_id TEXT,
              Shop_type TEXT,
              Shop_email TEXT,
              Shop_link TEXT,
              Shop_password TEXT,
              Shop_about TEXT,
              Shop_country TEXT,
              Shop_contact TEXT,
              Shop_isenabled TEXT,

              Shop_SocLinks TEXT,
              Shop_rules TEXT,
              Shop_location TEXT,
              
              Shop_profile_img TEXT,
              Shop_banner_imgs TEXT,
              
              Company_Started_Date TEXT,
              Shop_rate TEXT,
              
              Shop_Page TEXT,
              
              Shop_items TEXT,
              Shop_followers TEXT,
              Shop_workers TEXT,
              Shop_Payment_Tools TEXT,
              Shop_Security_Levels TEXT,
              Shop_likes TEXT,
              Shop_Settings TEXT,
              Shop_payment_info TEXT,
              Shop_Slip_Settings TEXT,
              Shop_Expenses TEXT,
              Shop_Actions TEXT,

              
              Shop_Items_type TEXT,
              Shop_payment_r TEXT,
              Shop_Access_levels TEXT
            )
            """.trimIndent()
        )

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DOCS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TOOLS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CUSTOMERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SHOPS")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {

        // Payment Tools
        val paymentTools = listOf(
            PaymentTool(name = "Cash", code = "CASH", shortKey = "F1", quickPay = true, openDrawer = true),
            PaymentTool(name = "Credit Card", code = "CARD", shortKey = "F2", quickPay = true, openDrawer = false),
            PaymentTool(name = "Mobile Payment", code = "MOMO", shortKey = "F3", quickPay = true, openDrawer = false),
            PaymentTool(name = "Store Credit", code = "CREDIT", shortKey = "F4", quickPay = false, customerRequired = true)
        )
        for (pt in paymentTools) {
            val cv = ContentValues().apply {
                put("name", pt.name)
                put("code", pt.code)
                put("type", pt.type)
                put("short_key", pt.shortKey)
                put("enabel", if (pt.enabled) 1 else 0)
                put("quick_pay", if (pt.quickPay) 1 else 0)
                put("customer_required", if (pt.customerRequired) 1 else 0)
                put("open_drower", if (pt.openDrawer) 1 else 0)
            }
            db.insert(TABLE_TOOLS, null, cv)
        }

        // Default Sample Customers
        val customers = listOf(
            Customer(name = "Walk-in Customer", phone = "000-000-0000"),
            Customer(name = "John Doe", phone = "+1 555-0101", email = "john@example.com", address = "123 Elm St"),
            Customer(name = "Jane Smith", phone = "+1 555-0102", email = "jane@example.com", address = "456 Oak St")
        )
        for (c in customers) {
            val cv = ContentValues().apply {
                put("name", c.name)
                put("phone", c.phone)
                put("email", c.email)
                put("address", c.address)
            }
            db.insert(TABLE_CUSTOMERS, null, cv)
        }

        // Sample Products
        val products = listOf(
            Product(name = "Organic Whole Milk 1L", code = "MILK01", type = "Dairy", barcode = "8901001", price = 3.49, cost = 2.10, quantity = 85, tax = 0.15),
            Product(name = "Fresh White Bread 500g", code = "BRD01", type = "Bakery", barcode = "8901002", price = 2.29, cost = 1.20, quantity = 120, tax = 0.10),
            Product(name = "Red Apples (1kg)", code = "APL01", type = "Produce", barcode = "8901003", price = 4.99, cost = 3.00, quantity = 200, tax = 0.00),
            Product(name = "Sparkling Water 1.5L", code = "WTR01", type = "Beverages", barcode = "8901004", price = 1.49, cost = 0.60, quantity = 300, tax = 0.20),
            Product(name = "Dark Chocolate Bar 100g", code = "CHOC01", type = "Confectionery", barcode = "8901005", price = 2.99, cost = 1.50, quantity = 60, tax = 0.30),
            Product(name = "Ground Coffee 250g", code = "COF01", type = "Beverages", barcode = "8901006", price = 7.99, cost = 4.80, quantity = 45, tax = 0.40),
            Product(name = "Olive Oil Extra Virgin 750ml", code = "OIL01", type = "Grocery", barcode = "8901007", price = 12.99, cost = 8.50, quantity = 30, tax = 0.65),
            Product(name = "Cheddar Cheese 200g", code = "CHS01", type = "Dairy", barcode = "8901008", price = 4.49, cost = 2.80, quantity = 50, tax = 0.25),
            Product(name = "Orange Juice 1L", code = "JUICE01", type = "Beverages", barcode = "8901009", price = 3.99, cost = 2.20, quantity = 75, tax = 0.20),
            Product(name = "Potato Chips 150g", code = "CHIP01", type = "Snacks", barcode = "8901010", price = 1.99, cost = 0.90, quantity = 150, tax = 0.15)
        )

        for (p in products) {
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
            db.insert(TABLE_PRODUCTS, null, cv)
        }

        // Sample initial sales doc
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = sdf.format(Date())
        val docCv = ContentValues().apply {
            put("doc_barcode", "DOC-${System.currentTimeMillis() % 100000}")
            put("extension_barcode", "EXT100")
            put("At_Shop_Id", "Shop 1")
            put("user_id", "admin")
            put("Seller_id", "1001")
            put("customer_id", "Walk-in Customer")
            put("pid", 1)
            put("type", "Sale")
            put("item", "Organic Whole Milk 1L x2, Fresh White Bread 500g x1")
            put("qty", 3.0)
            put("price", 9.27)
            put("Profite", 3.87)
            put("discount", 0.0)
            put("tax", 0.40)
            put("payments", "Cash")
            put("doc_created_date", now)
            put("doc_expire_date", "")
            put("doc_updated_date", now)
        }
        db.insert(TABLE_DOCS, null, docCv)
    }
}
