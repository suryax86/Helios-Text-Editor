package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan

@Composable
fun UnsavedChangesDialog(
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2C2C2E),
        title = {
            Text(
                text = "Open file...",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "You have unsaved changes. Do you want to save your changes?",
                color = Color.LightGray,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDiscard) {
                    Text("DISCARD", color = AccentCyan)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onSave) {
                    Text("SAVE", color = AccentCyan)
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(8.dp)
    )
}
