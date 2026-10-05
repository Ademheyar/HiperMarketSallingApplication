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
fun NewLoginPanel(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String?,
    onForgot: () -> Unit,
    onCreateNewUser: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Step 2: Enter credentials for '$username'",
            color = TextMuted,
            fontSize = 12.sp
        )

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        errorMessage?.let { err ->
            Text(err, color = EnergyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onForgot) {
                Text("Forgot Password?", color = EnergyRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            TextButton(onClick = onCreateNewUser) {
                Text("Create New User →", color = DarkBlueAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        TextButton(onClick = onBack) {
            Text("← Back to user list", color = DarkBlueAccent)
        }
    }
}
