package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan

@Composable
fun GoToLineDialog(
    currentLine: Int,
    maxLines: Int,
    onDismiss: () -> Unit,
    onGoToLine: (Int) -> Unit
) {
    var lineInput by remember { mutableStateOf(currentLine.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2C2C2E),
        title = {
            Text(
                text = "Go to...",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter line number (1 - $maxLines):",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lineInput,
                    onValueChange = { lineInput = it.filter { char -> char.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = AccentCyan)
                }
                TextButton(onClick = {
                    val target = lineInput.toIntOrNull() ?: 1
                    onGoToLine(target)
                }) {
                    Text("GO", color = AccentCyan)
                }
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2C2C2E),
        title = {
            Text(
                text = "About Text Editor",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Lightweight Text Editor v1.0",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "A high-performance Android text editor supporting multi-encoding, custom themes, syntax highlighting, cloud synchronization, and real-time multi-peer collaborative editing.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = AccentCyan)
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}
