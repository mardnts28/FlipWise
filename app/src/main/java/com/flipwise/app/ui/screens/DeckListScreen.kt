package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.data.model.Deck
import com.flipwise.app.ui.components.CreateDeckDialog
import com.flipwise.app.ui.theme.*
import com.flipwise.app.viewmodel.DeckViewModel
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    onNavigateToDeck: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: DeckViewModel = viewModel()
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<String?>(null) }
    val decks by viewModel.decks.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Decks", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF7C3AED)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF7C3AED),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Deck", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFBFBFF))
        ) {
            if (decks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No decks yet. Tap + to create one!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(decks, key = { it.id }) { deck ->
                        DeckDetailItem(
                            deck = deck,
                            onClick = { onNavigateToDeck(deck.id) },
                            onDelete = { deckToDelete = deck.id }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateDeckDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, subject, color, icon ->
                viewModel.createDeck(name, subject, color, icon)
                showCreateDialog = false
            }
        )
    }

    if (deckToDelete != null) {
        AlertDialog(
            onDismissRequest = { deckToDelete = null },
            title = { Text("Delete Deck?") },
            text = { Text("Are you sure you want to delete this deck? This action cannot be undone and all cards within will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deckToDelete?.let { viewModel.deleteDeck(it) }
                        deckToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deckToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DeckDetailItem(deck: Deck, onClick: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = try { Color(deck.color.toColorInt()) } catch (e: Exception) { Color(0xFF7C3AED) },
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(deck.icon, fontSize = 32.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(deck.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    if (deck.subject.isNotBlank()) {
                        Text(deck.subject, fontSize = 14.sp, color = Color.Gray)
                    }
                    Text("${deck.cardCount} cards", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.7f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A8A))
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            val progressPercent = if (deck.cardCount > 0) (deck.masteredCount * 100 / deck.cardCount) else 0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Progress", fontSize = 12.sp, color = Color.Gray)
                Text("$progressPercent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (deck.cardCount > 0) deck.masteredCount.toFloat() / deck.cardCount else 0f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF10B981),
                trackColor = Color(0xFFF0F0F5)
            )
        }
    }
}
