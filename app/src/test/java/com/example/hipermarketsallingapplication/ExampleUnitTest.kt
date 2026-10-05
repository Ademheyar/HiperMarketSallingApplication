package com.example.hipermarketsallingapplication

import com.example.hipermarketsallingapplication.data.model.Product
import com.example.hipermarketsallingapplication.ui.screens.shouldShowProductCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun productCatalog_isHiddenWhenSearchIsBlank() {
        val products = listOf(
            Product(
                id = 1,
                name = "Milk",
                code = "P001",
                type = "Food",
                barcode = "123456"
            )
        )

        assertFalse(shouldShowProductCatalog(products, ""))
    }

    @Test
    fun productCatalog_isHiddenWhenThereAreNoResults() {
        assertFalse(shouldShowProductCatalog(emptyList(), "milk"))
    }

    @Test
    fun productCatalog_isShownWhenThereAreResults() {
        val products = listOf(
            Product(
                id = 1,
                name = "Milk",
                code = "P001",
                type = "Food",
                barcode = "123456"
            )
        )

        assertTrue(shouldShowProductCatalog(products, "milk"))
    }
}