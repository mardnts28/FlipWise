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
    onNavigateToProfile: () -> Unit,
    viewModel: DeckViewModel = viewModel(),
    profileViewModel: com.flipwise.app.viewmodel.ProfileViewModel = viewModel()
) {
    val decks by viewModel.decks.collectAsState(initial = emptyList())
    val progress by viewModel.userProgress.collectAsState()
    val friends by profileViewModel.friends.collectAsState(initial = emptyList())
    val dimensions = FlipWiseDesign.dimensions
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showFriendNotification by remember { mutableStateOf(false) }
    
    val pendingRequests = remember(friends) {
        friends.filter { it.status == "pending" }
    }

    LaunchedEffect(pendingRequests) {
        if (pendingRequests.isNotEmpty()) {
            showFriendNotification = true
        }
    }

    val recentDecks = remember(decks) {
        decks.sortedByDescending { it.lastStudied ?: 0L }.take(4)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFF))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Section ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = dimensions.cardCornerRadius, bottomEnd = dimensions.cardCornerRadius)),
            color = Color(0xFF7C3AED)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1200.dp)
                    .padding(dimensions.paddingLarge)
            ) {
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
                        fontSize = dimensions.headerFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
                Text(
                    text = "Ready to learn something new?",
                    fontSize = dimensions.bodyFontSize,
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
                .widthIn(max = 1200.dp)
                .fillMaxWidth()
                .padding(horizontal = dimensions.paddingLarge)
                .offset(y = (-30).dp),
            horizontalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
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
        Column(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxWidth()
                .padding(horizontal = dimensions.paddingLarge)
        ) {
            Text("Quick Actions", fontSize = dimensions.headerFontSize, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
            Spacer(Modifier.height(dimensions.paddingMedium))
            
            if (dimensions.isTablet) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Book,
                        title = "Continue Studying",
                        subtitle = recentDecks.firstOrNull()?.name ?: "No decks yet",
                        color = Color(0xFF7C3AED),
                        onClick = { onNavigateToStudy(recentDecks.firstOrNull()?.id) }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Add,
                        title = "Create New Deck",
                        subtitle = "Add content",
                        color = Color(0xFFF97316),
                        onClick = { showCreateDialog = true }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Psychology,
                        title = "Practice Cards",
                        subtitle = "Flashcard mix",
                        color = Color(0xFFFBBF24),
                        onClick = { onNavigateToStudy(null) }
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Book,
                        title = "Continue Study",
                        subtitle = recentDecks.firstOrNull()?.name ?: "Start here",
                        color = Color(0xFF7C3AED),
                        onClick = { onNavigateToStudy(recentDecks.firstOrNull()?.id) }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Add,
                        title = "New Deck",
                        subtitle = "Add content",
                        color = Color(0xFFF97316),
                        onClick = { showCreateDialog = true }
                    )
                }
                Spacer(Modifier.height(16.dp))
                QuickActionCard(
                    modifier = Modifier.fillMaxWidth(),
                    height = 120.dp,
                    icon = Icons.Default.Psychology,
                    title = "Practice Cards",
                    subtitle = "Flashcard mix",
                    color = Color(0xFFFBBF24),
                    onClick = { onNavigateToStudy(null) }
                )
            }
        }

        // --- Recent Decks Section ---
        Spacer(Modifier.height(dimensions.paddingLarge))
        Row(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxWidth()
                .padding(horizontal = dimensions.paddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Decks", fontSize = dimensions.headerFontSize, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
            TextButton(onClick = onNavigateToDecks) {
                Text("View All", color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold, fontSize = dimensions.bodyFontSize)
            }
        }
        
        Column(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxWidth()
                .padding(horizontal = dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
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

    if (showFriendNotification && pendingRequests.isNotEmpty()) {
        val firstRequest = pendingRequests.first()
        AlertDialog(
            onDismissRequest = { showFriendNotification = false },
            title = { Text("New Friend Request! \uD83D\uDC4B") },
            text = { 
                Text("${firstRequest.displayName} (@${firstRequest.username}) wants to be friends on FlipWise!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFriendNotification = false
                        onNavigateToProfile()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("View Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFriendNotification = false }) {
                    Text("Later", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }

    if (showCreateDialog) {
        com.flipwise.app.ui.components.CreateDeckDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, subject, color, icon ->
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

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 160.dp,
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(height)
            .clickable { onClick() },
        shape = RoundedCornerShape(FlipWiseDesign.dimensions.cardCornerRadius / 1.5f),
        color = color
    ) {
        Column(modifier = Modifier.padding(FlipWiseDesign.dimensions.paddingMedium)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (FlipWiseDesign.dimensions.isTablet) 48.dp else 32.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = title,
                fontSize = FlipWiseDesign.dimensions.bodyFontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = FlipWiseDesign.dimensions.detailFontSize,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
