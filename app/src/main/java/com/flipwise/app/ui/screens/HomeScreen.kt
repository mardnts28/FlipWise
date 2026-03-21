package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.data.model.Deck
import com.flipwise.app.ui.theme.*
import com.flipwise.app.viewmodel.DeckViewModel

@Composable
fun HomeScreen(
    onNavigateToDeck: (String) -> Unit,
    onNavigateToStudy: (String?) -> Unit,
    onNavigateToDecks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DeckViewModel = viewModel()
) {
    val decks by viewModel.decks.collectAsState(initial = emptyList())
    val progress by viewModel.userProgress.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    val recentDecks = remember(decks) {
        decks.sortedByDescending { it.lastStudied ?: 0L }.take(4)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFF))
            .verticalScroll(rememberScrollState())
    ) {
        // --- Header Section ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)),
            color = Color(0xFF7C3AED)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val greeting = remember {
                        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                        when (hour) {
                            in 0..11 -> "Good Morning!"
                            in 12..16 -> "Good Afternoon!"
                            else -> "Good Evening!"
                        }
                    }
                    Text(
                        text = greeting,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
                Text(
                    text = "Ready to learn something new?",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(Modifier.height(32.dp))
                
                // Stats Card inside Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Color(0xFFFBBF24)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Current Streak", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("${progress.currentStreak} days", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Points", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(progress.totalPoints.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }

        // --- Cards Studied/Mastered Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-30).dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cards Studied", fontSize = 14.sp, color = Color.Gray)
                    Text(progress.totalCardsStudied.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F3D56))
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp,
                color = Color.White
            ) {
                val totalMastered = decks.sumOf { it.masteredCount }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cards Mastered", fontSize = 14.sp, color = Color.Gray)
                    Text("$totalMastered", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }

        // --- Quick Actions Section ---
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Quick Actions", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Continue Studying
                val lastDeckId = recentDecks.firstOrNull()?.id
                Surface(
                    modifier = Modifier.weight(1f).height(160.dp).clickable { onNavigateToStudy(lastDeckId) },
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF7C3AED)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.weight(1f))
                        Text("Continue Studying", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(recentDecks.firstOrNull()?.name ?: "No decks yet", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
                // Create New Deck
                Surface(
                    modifier = Modifier.weight(1f).height(160.dp).clickable { showCreateDialog = true },
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF97316)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.weight(1f))
                        Text("Create New Deck", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Add content", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Practice Cards
            Surface(
                modifier = Modifier.fillMaxWidth().height(140.dp).clickable { onNavigateToStudy(null) },
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFBBF24)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.weight(1f))
                    Text("Practice Cards", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Flashcard mix", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        // --- Recent Decks Section ---
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Decks", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
            TextButton(onClick = onNavigateToDecks) {
                Text("View All", color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
            }
        }
        
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (recentDecks.isEmpty()) {
                Text("Your decks will appear here.", color = Color.Gray, modifier = Modifier.padding(8.dp))
            } else {
                recentDecks.forEach { deck ->
                    RecentDeckItem(deck = deck, onClick = { onNavigateToDeck(deck.id) })
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }

    if (showCreateDialog) {
        com.flipwise.app.ui.components.CreateDeckDialog(
            onDismiss = { showCreateDialog = false },
            onDeckCreate = { name: String, subject: String, color: String, icon: String ->
                viewModel.createDeck(name, subject, color, icon)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun RecentDeckItem(deck: Deck, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = try { Color(android.graphics.Color.parseColor(deck.color)) } catch (e: Exception) { Color(0xFFF97316) },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(deck.icon, fontSize = 24.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(deck.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                if (deck.subject.isNotBlank()) {
                    Text(deck.subject, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${deck.masteredCount}/${deck.cardCount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Text("mastered", fontSize = 11.sp, color = Color(0xFF10B981))
            }
        }
    }
}
