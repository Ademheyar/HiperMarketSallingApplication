package com.example.hipermarketsallingapplication.ui.screens

import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.ui.theme.*

/**
 * GetValueDialog matching GetVALUE.py
 * Separate standalone function & file for numeric calculations and value prompts in POS Terminal.
 * Used for:
 * 1. Calculator (F1)
 * 2. None / Custom Item Price & Quantity Input (F2)
 * 3. Any numeric value or quantity prompts
 */
@Composable
fun GetValueDialog(
    titles: List<String> = listOf("Calculator / Value Input"),
    initialValue: String = "0",
    onValueConfirmed: (List<Double>) -> Unit = {},
    onDismiss: () -> Unit
) {
    var titleIndex by remember { mutableStateOf(0) }
    val currentTitle = titles.getOrElse(titleIndex) { "Enter Value" }

    var displayExpression by remember { mutableStateOf(initialValue) }
    var holdOperation by remember { mutableStateOf("") }
    val collectedValues = remember { mutableStateListOf<Double>() }

    fun appendChar(char: String) {
        if (displayExpression == "0" && char != ".") {
            displayExpression = char
        } else {
            displayExpression += char
        }
    }

    fun calculateResult(): Double {
        return try {
            val sanitized = (holdOperation + displayExpression).replace("×", "*").replace("÷", "/")
            val result = evaluateSimpleExpression(sanitized)
            displayExpression = String.format(Locale.getDefault(), "%.2f", result).removeSuffix(".00")
            holdOperation = ""
            result
        } catch (e: Exception) {
            displayExpression = "0"
            holdOperation = ""
            0.0
        }
    }

    fun handleConfirm() {
        val calculated = calculateResult()
        collectedValues.add(calculated)

        if (titleIndex + 1 < titles.size) {
            titleIndex++
            displayExpression = "1"
            holdOperation = ""
        } else {
            onValueConfirmed(collectedValues.toList())
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = DarkBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(currentTitle, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Calculator / Value Display Box (GetVALUE.py)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        if (holdOperation.isNotBlank()) {
                            Text(
                                text = holdOperation,
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            )
                        }
                        Text(
                            text = displayExpression.ifBlank { "0" },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }
                }

                // Calculator Keypad (GetVALUE.py)
                val keys = listOf(
                    listOf("7", "8", "9", "Clean"),
                    listOf("4", "5", "6", "÷"),
                    listOf("1", "2", "3", "×"),
                    listOf("0", ".", "-", "+"),
                    listOf("⌫", "=", "Enter", "Close")
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    keys.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { key ->
                                Button(
                                    onClick = {
                                        when (key) {
                                            "Clean" -> {
                                                displayExpression = "0"
                                                holdOperation = ""
                                            }
                                            "⌫" -> {
                                                displayExpression = if (displayExpression.length > 1) displayExpression.dropLast(1) else "0"
                                            }
                                            "=" -> {
                                                calculateResult()
                                            }
                                            "+", "-", "×", "÷" -> {
                                                holdOperation = displayExpression + " " + key + " "
                                                displayExpression = "0"
                                            }
                                            "Enter" -> {
                                                handleConfirm()
                                            }
                                            "Close" -> {
                                                onDismiss()
                                            }
                                            else -> appendChar(key)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (key) {
                                            "Enter" -> SuccessGreen
                                            "Clean", "Close" -> EnergyRed
                                            "=", "+", "-", "×", "÷" -> DarkBlueAccent
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = key,
                                        color = if (key in listOf("Enter", "Clean", "Close", "=", "+", "-", "×", "÷")) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (key.length > 3) 11.sp else 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { handleConfirm() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (titleIndex + 1 < titles.size) "Next: ${titles[titleIndex + 1]}" else "Confirm Value",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    )
}

fun evaluateSimpleExpression(expression: String): Double {
    val tokens = expression.split(Regex("(?<=[+*-/])|(?=[+*-/])")).map { it.trim() }.filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return 0.0
    var current = tokens[0].toDoubleOrNull() ?: 0.0
    var i = 1
    while (i < tokens.size - 1) {
        val op = tokens[i]
        val nextVal = tokens[i + 1].toDoubleOrNull() ?: 0.0
        when (op) {
            "+" -> current += nextVal
            "-" -> current -= nextVal
            "*" -> current *= nextVal
            "/" -> if (nextVal != 0.0) current /= nextVal
        }
        i += 2
    }
    return current
}
