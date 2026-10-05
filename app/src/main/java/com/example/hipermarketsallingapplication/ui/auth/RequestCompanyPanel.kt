package com.example.hipermarketsallingapplication.ui.auth

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
fun RequestCompanyPanel(
    onBack: () -> Unit
) {
    var companyNameQuery by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var requestMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Request Company Assignment",
            color = DarkBlueAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        OutlinedTextField(
            value = companyNameQuery,
            onValueChange = { companyNameQuery = it },
            label = { Text("Company Name / Code", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason / Notes for Admin", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                requestMsg = if (companyNameQuery.isNotBlank()) "Assignment request sent to admin for '$companyNameQuery'!" else "Enter company name."
            },
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Request")
        }
        requestMsg?.let { Text(it, color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        TextButton(onClick = onBack) {
            Text("← Back to Company Selection", color = DarkBlueAccent)
        }
    }
}
