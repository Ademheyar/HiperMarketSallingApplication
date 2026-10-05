package com.example.hipermarketsallingapplication.ui.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.utils.JsonHelper

@Composable
fun ShopManagementPanel(
    selectedShop: String,
    onShopSelected: (String) -> Unit,
    userShopData: String?,
    onCreateShop: () -> Unit,
    onRequestShop: () -> Unit,
    errorMessage: String? = null
) {
    val loadedShopNames = remember(userShopData) {
        val parsed = JsonHelper.loads(userShopData)
        val elements = mutableListOf<List<*>>()
        if (parsed is List<*>) {
            for (element in parsed) {
                if (element is List<*>)
                    elements.add(element)
            }
        }
        elements
    }

    var localError by remember { mutableStateOf<String?>(null) }
    val displayError = errorMessage ?: localError

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Step 3: Select User Shop (User Workplace)" + userShopData,
            color = DarkBlueAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        if (displayError != null) {
            Text(
                text = displayError,
                color = EnergyRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                loadedShopNames.forEach { shop ->
                    val shopName = shop.getOrNull(1)?.toString() ?: shop.getOrNull(0)?.toString() ?: ""
                    val shopBrand = shop.getOrNull(2)?.toString() ?: ""
                    val shopLevel = shop.getOrNull(3)?.toString() ?: "-1"

                    val isSelected = selectedShop == shopName
                    val shopLevelInt = shopLevel.toIntOrNull() ?: -1

                    val containerColor = when (shopLevel) {
                        "-2" -> Color.Yellow
                        "-1" -> Color.Blue
                        "0" -> CardBackground
                        else -> SuccessGreen
                    }

                    Button(
                        onClick = {
                            if (shopLevelInt > 0) {
                                localError = null
                                onShopSelected(shopName)
                            } else {
                                localError = "Select Actived Shop"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = containerColor,
                            disabledContainerColor = containerColor.copy(alpha = 0.6f),
                            disabledContentColor = TextLight.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val displayName = if (isSelected) "$shopName ✓" else shopName
                            Text(displayName, fontSize = 12.sp, color = TextLight, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            Text(shopBrand, fontSize = 12.sp, color = TextLight)
                            if (shopLevel == "-2") Text("Accept", color = TextLight, fontSize = 11.sp)
                            else if (shopLevel == "-1") Text("Requested", color = TextLight, fontSize = 11.sp)
                            else if (shopLevel == "0") Text("Coustermers", color = TextLight, fontSize = 11.sp)
                            else Text("Active", color = TextLight, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCreateShop,
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                modifier = Modifier.weight(1f)
            ) {
                Text("Create Shop", fontSize = 12.sp)
            }
            Button(
                onClick = onRequestShop,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                modifier = Modifier.weight(1f)
            ) {
                Text("Request Shop", fontSize = 12.sp, color = DarkBlueAccent)
            }
        }
    }
}
