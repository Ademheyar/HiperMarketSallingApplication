package com.example.hipermarketsallingapplication.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.data.model.User
import com.example.hipermarketsallingapplication.ui.theme.*

@Composable
fun SuccessPanel(
    currentUser: User?,
    selectedUsername: String,
    selectedShop: String,
    onLogout: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
        Text("User Logged In Successfully!", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text("Active User: ${currentUser?.userName ?: selectedUsername}", color = TextLight, fontSize = 12.sp)
        Text("Assigned Shop: ${currentUser?.userShop ?: selectedShop}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        if (onLogout != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sign Out / Log Out")
            }
        }
    }
}
