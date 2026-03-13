package com.flipwise.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.ui.theme.*
import com.flipwise.app.viewmodel.DeckViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: DeckViewModel = viewModel()) {
    var notificationsOn      by remember { mutableStateOf(true) }
    var showClearConfirm     by remember { mutableStateOf(false) }
    var showClearedMessage   by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Preferences", fontSize = 13.sp, color = NavyInk60, fontWeight = FontWeight.SemiBold)

            // Notifications
            Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Study Reminders", fontWeight = FontWeight.Medium)
                        Text("Daily notification to keep your streak", fontSize = 12.sp, color = NavyInk60)
                    }
                    Switch(
                        checked         = notificationsOn,
                        onCheckedChange = { notificationsOn = it },
                        colors          = SwitchDefaults.colors(checkedThumbColor = GrapePop, checkedTrackColor = GrapePop20)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Data", fontSize = 13.sp, color = NavyInk60, fontWeight = FontWeight.SemiBold)

            // Clear data
            Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Clear All Data", fontWeight = FontWeight.Medium, color = CherryRed)
                    Text("Deletes all decks, cards, sessions and progress", fontSize = 12.sp, color = NavyInk60)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showClearConfirm = true },
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = CherryRed),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Clear All Data") }
                }
            }

            if (showClearedMessage) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MintGreen.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "✅ All data cleared successfully.",
                        modifier = Modifier.padding(16.dp),
                        color    = MintGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Text("FlipWise v1.0.0", fontSize = 12.sp, color = NavyInk60)
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title            = { Text("Clear All Data?") },
            text             = { Text("This will permanently delete all your decks, flashcards, study sessions, and progress. This cannot be undone.") },
            confirmButton    = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm   = false
                        showClearedMessage = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CherryRed)
                ) { Text("Yes, Delete Everything") }
            },
            dismissButton    = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}