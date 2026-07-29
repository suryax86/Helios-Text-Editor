package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel

class MainActivity : ComponentActivity() {
    private val editorViewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EditorScreen(
                viewModel = editorViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
