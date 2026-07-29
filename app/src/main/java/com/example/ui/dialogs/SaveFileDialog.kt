package com.example.ui.dialogs

import androidx.compose.foundation.background
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
fun SaveFileDialog(
    initialPath: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onStorageClick: () -> Unit
) {
    var filePathInput by remember { mutableStateOf(initialPath) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2C2C2E),
        title = {
            Column {
                Text(
                    text = "Save",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose a file name",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedTextField(
                    value = filePathInput,
                    onValueChange = { filePathInput = it },
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onStorageClick) {
                    Text("STORAGE", color = AccentCyan)
                }
                Row {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = AccentCyan)
                    }
                    TextButton(onClick = { onSave(filePathInput) }) {
                        Text("SAVE", color = AccentCyan)
                    }
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(8.dp)
    )
}
