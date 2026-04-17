package com.flipwise.app.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToLeaderboard: (Int) -> Unit,
    viewModel: DeckViewModel = viewModel(),
    profileViewModel: com.flipwise.app.viewmodel.ProfileViewModel = viewModel()
) {
    val decks by viewModel.decks.collectAsState(initial = emptyList())
    val progress by viewModel.userProgress.collectAsState()
    val friends by profileViewModel.friends.collectAsState(initial = emptyList())
    val profile by profileViewModel.userProfile.collectAsState()
    val activeGoal by viewModel.activeGoal.collectAsState()
    val allActiveGoals by viewModel.allActiveGoals.collectAsState()
    val lastCompletedGoal by viewModel.lastCompletedGoal.collectAsState()
    val dimensions = FlipWiseDesign.dimensions

    var showCreateDialog by remember { mutableStateOf(false) }
    var showFriendNotification by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<com.flipwise.app.data.model.Challenge?>(null) }
    var showAllGoalsDialog by remember { mutableStateOf(false) }

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

    // --- System Notifications Logic ---
    val announcement by profileViewModel.publicAnnouncement.collectAsState(initial = null)
    val privateNotes by profileViewModel.privateNotifications.collectAsState(initial = emptyList())
    var dismissedAnnouncementTimestamp by remember { mutableStateOf(0L) }
    
    val activeAnnouncement = remember(announcement, dismissedAnnouncementTimestamp) {
        announcement?.let { 
            val ts = it["timestamp"] as? Long ?: 0L
            if (ts > dismissedAnnouncementTimestamp) it else null
        }
    }

    val unreadPrivateNote = remember(privateNotes) {
        privateNotes.firstOrNull { it["read"] == false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFF))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Notification Overlay ---
        Column(modifier = Modifier.widthIn(max = 1200.dp).fillMaxWidth().padding(horizontal = dimensions.paddingLarge)) {
            // Priority 1: Private Alerts
            unreadPrivateNote?.let { note ->
                SystemNotificationCard(
                    title = note["title"] as? String ?: "Notification",
                    message = note["message"] as? String ?: "",
                    isPrivate = true,
                    onDismiss = { profileViewModel.markNotificationAsRead(note["id"] as String) }
                )
            }
            // Priority 2: Public Announcements
            activeAnnouncement?.let { note ->
                SystemNotificationCard(
                    title = note["title"] as? String ?: "Announcement",
                    message = note["message"] as? String ?: "",
                    isPrivate = false,
                    onDismiss = { dismissedAnnouncementTimestamp = note["timestamp"] as Long }
                )
            }
        }

        // --- Header Section ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        bottomStart = dimensions.cardCornerRadius,
                        bottomEnd = dimensions.cardCornerRadius
                    )
                ),
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
                        val hour =
                            java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                        when (hour) {
                            in 0..11 -> "Good Morning!"
                            in 12..16 -> "Good Afternoon!"
                            else -> "Good Evening!"
                        }
                    }
                    Text(
                        text = "$greeting ${profile.username}",
                        fontSize = dimensions.headerFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
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
                                Icon(
                                    Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Current Streak",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                "${progress.currentStreak} days",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Points", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                progress.totalPoints.toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
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
                    Text(
                        progress.totalCardsStudied.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F3D56)
                    )
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
                    Text(
                        "$totalMastered",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        // --- Active Goal Widget ---
        activeGoal?.let { goal ->
            Surface(
                modifier = Modifier
                    .widthIn(max = 1200.dp)
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.paddingLarge)
                    .offset(y = (-14).dp)
                    .clickable { 
                        goalToEdit = allActiveGoals.find { it.id == goal.goalId }
                    },
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF7C3AED).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                goal.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                            Text(
                                "${goal.current} / ${goal.target} ${goal.goalType}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${(goal.percentage * 100).toInt()}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED)
                            )
                            Text(
                                "${goal.daysLeft}d left",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { goal.percentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF7C3AED),
                        trackColor = Color(0xFF7C3AED).copy(alpha = 0.1f)
                    )
                }
            }
            if (allActiveGoals.size > 1) {
                TextButton(
                    onClick = { showAllGoalsDialog = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("View ${allActiveGoals.size - 1} Other Goals", color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
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
            Text(
                "Quick Actions",
                fontSize = dimensions.headerFontSize,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B4B)
            )
            Spacer(Modifier.height(dimensions.paddingMedium))

            if (dimensions.isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
                ) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
            Text(
                "Recent Decks",
                fontSize = dimensions.headerFontSize,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B4B)
            )
            TextButton(onClick = onNavigateToDecks) {
                Text(
                    "View All",
                    color = Color(0xFF7C3AED),
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyFontSize
                )
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
                Text(
                    "Your decks will appear here.",
                    color = Color.Gray,
                    modifier = Modifier.padding(8.dp)
                )
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
            title = { Text("New Friend Request!") },
            text = {
                Text("${firstRequest.displayName} (@${firstRequest.username}) wants to be friends on FlipWise!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFriendNotification = false
                        onNavigateToLeaderboard(1) // 1 = Friends tab on Leaderboard
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("View")
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

    if (goalToEdit != null) {
        com.flipwise.app.ui.components.CreateGoalDialog(
            decks = decks,
            existingGoal = goalToEdit,
            onDismiss = { goalToEdit = null },
            onCreate = { challenge ->
                profileViewModel.addChallenge(challenge)
                viewModel.refreshGoals()
                goalToEdit = null
                showAllGoalsDialog = false
            }
        )
    }

    if (showAllGoalsDialog) {
        AlertDialog(
            onDismissRequest = { showAllGoalsDialog = false },
            title = { Text("Your Active Goals", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(allActiveGoals) { goal ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable {
                                goalToEdit = goal
                                showAllGoalsDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF9F9FB)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(goal.name, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                                Text("Target: ${goal.goal} ${goal.goalType}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllGoalsDialog = false }) { Text("Close") }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // --- Personal Goal Congratulations Pop-up ---
    lastCompletedGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { viewModel.clearCompletedGoal() },
            title = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("🎉 Congratulations!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C3AED))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = Color(0xFFF0FDF4)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Goal Accomplished!", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp, 
                        color = Color(0xFF1E1B4B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "You've successfully completed your goal:\n\"${goal.name}\"",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4)
                    ) {
                        Text(
                            "+50 Bonus XP Earned!",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearCompletedGoal() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("Keep it up!", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
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
fun SystemNotificationCard(
    title: String,
    message: String,
    isPrivate: Boolean,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        color = if (isPrivate) Color(0xFFF0F9FF) else Color(0xFFFFF7ED),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            (if (isPrivate) Color(0xFF0369A1) else Color(0xFFC2410C)).copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isPrivate) Color(0xFF0369A1).copy(alpha = 0.1f) else Color(0xFFC2410C).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPrivate) Icons.Rounded.Shield else Icons.Rounded.Campaign,
                    contentDescription = null,
                    tint = if (isPrivate) Color(0xFF0369A1) else Color(0xFFC2410C),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isPrivate) Color(0xFF1E3A8A) else Color(0xFF7C2D12)
                )
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = (if (isPrivate) Color(0xFF1E3A8A) else Color(0xFF7C2D12)).copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close, 
                    contentDescription = "Dismiss", 
                    tint = if (isPrivate) Color(0xFF0369A1) else Color(0xFFC2410C),
                    modifier = Modifier.size(18.dp)
                )
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
