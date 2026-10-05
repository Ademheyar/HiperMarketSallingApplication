package com.example.hipermarketsallingapplication.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.ui.theme.*

@Composable
fun ForgotPasswordPanel(
    onBack: () -> Unit,
    onContinue: (String) -> Unit
) {
    var forgetEmailOrUser by remember { mutableStateOf("") }
    var forgetMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "User Recovery / Forgot Password",
                color = EnergyRed,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Button(
                onClick = {
                    onContinue(forgetEmailOrUser)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Continue", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = forgetEmailOrUser,
            onValueChange = { forgetEmailOrUser = it },
            label = { Text("Username, Email or Phone", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                forgetMsg = if (forgetEmailOrUser.isNotBlank()) "Recovery info sent. Default password is '1234'." else "Enter valid user."
            },
            colors = ButtonDefaults.buttonColors(containerColor = EnergyRed),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retrieve Account Info")
        }
        forgetMsg?.let { Text(it, color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        TextButton(onClick = onBack) {
            Text("← Back to Login", color = DarkBlueAccent)
        }
    }
}
