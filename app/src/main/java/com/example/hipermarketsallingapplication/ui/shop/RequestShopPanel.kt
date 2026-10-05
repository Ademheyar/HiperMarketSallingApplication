package com.example.hipermarketsallingapplication.ui.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.ui.theme.*

@Composable
fun RequestShopPanel(
    currentUserUsername: String,
    shopsList: List<List<*>>,
    onRequestSubmitted: (String) -> Unit,
    onBack: () -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var shopBrand by remember { mutableStateOf("") }
    var requestReason by remember { mutableStateOf("") }
    var isShopFound by remember { mutableStateOf<Boolean?>(null) }
    var msg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Request Shop Assignment (User: $currentUserUsername)",
            color = DarkBlueAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it; isShopFound = null; msg = null },
            label = { Text("Shop Name", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = shopBrand,
            onValueChange = { shopBrand = it; isShopFound = null; msg = null },
            label = { Text("Shop Brand", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = requestReason,
            onValueChange = { requestReason = it },
            label = { Text("Reason / Request Notes for Admin", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (shopName.isNotBlank()) {
                    val found = shopsList.any { shop ->
                        val name = shop.getOrNull(1)?.toString() ?: ""
                        val brand = shop.getOrNull(2)?.toString() ?: ""
                        name.equals(shopName.trim(), ignoreCase = true) &&
                                (shopBrand.isBlank() || brand.equals(shopBrand.trim(), ignoreCase = true))
                    }
                    isShopFound = found
                    msg = if (found) {
                        "Shop found in database! ✓"
                    } else {
                        "Shop not found in database."
                    }
                } else {
                    msg = "Please enter a shop name."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Check Shop")
        }

        msg?.let {
            Text(
                text = it,
                color = if (isShopFound == true) SuccessGreen else EnergyRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isShopFound == true) {
            Button(
                onClick = {
                    onRequestSubmitted("$shopName (${shopBrand.ifBlank { "MainBrand" }})")
                    msg = "Shop request sent for '$shopName'! Added 'Waiting For Response: $shopName'."
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Shop Request")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        TextButton(onClick = onBack) {
            Text("← Back to Shop Selection", color = DarkBlueAccent)
        }
    }
}
