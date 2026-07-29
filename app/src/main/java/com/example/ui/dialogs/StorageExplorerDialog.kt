package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StorageNode
import com.example.ui.theme.AccentCyan

@Composable
fun StorageExplorerDialog(
    currentDirectoryPath: String,
    onDismiss: () -> Unit,
    onFileSelected: (String) -> Unit
) {
    var breadcrumbs by remember { mutableStateOf(listOf("EMULATED", "99", "DOCUMENTS")) }
    
    val sampleStorageNodes = remember {
        listOf(
            StorageNode("99", "/storage/emulated/99", isDirectory = false, itemCount = 99),
            StorageNode("0", "/storage/emulated/0", isDirectory = true, itemCount = 0),
            StorageNode("customisation.txt", "/sdcard/customisation.txt", isDirectory = false, itemCount = 1, sizeText = "2.4 KB"),
            StorageNode("Editor.txt", "/storage/emulated/99/Documents/Editor.txt", isDirectory = false, itemCount = 1, sizeText = "1.2 KB"),
            StorageNode("release.b64", "api/release.b64", isDirectory = false, itemCount = 1, sizeText = "4.8 KB"),
            StorageNode("git-token.key", "api/git-token.key", isDirectory = false, itemCount = 1, sizeText = "256 B"),
            StorageNode("VirusTotal-api.key", "api/VirusTotal-api.key", isDirectory = false, itemCount = 1, sizeText = "128 B")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2C2C2E),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                breadcrumbs.forEach { crumb ->
                    Surface(
                        color = Color(0xFF3A3A3C),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = crumb,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(sampleStorageNodes) { node ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!node.isDirectory) {
                                    onFileSelected(node.path)
                                    onDismiss()
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (node.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = node.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${node.itemCount}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { /* Switch Storage */ }) {
                    Text("STORAGE", color = AccentCyan)
                }
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = AccentCyan)
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(8.dp)
    )
}
