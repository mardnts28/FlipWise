package com.flipwise.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.data.model.Challenge
import com.flipwise.app.data.model.Friend
import com.flipwise.app.data.model.UserProfile
import com.flipwise.app.ui.components.CreateGoalDialog
import com.flipwise.app.ui.theme.*
import com.flipwise.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChallengeDetail: (String) -> Unit,
    initialTab: Int = 0,
    viewModel: ProfileViewModel = viewModel(),
    deckViewModel: com.flipwise.app.viewmodel.DeckViewModel = viewModel()
) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val decks by viewModel.decks.collectAsState(initial = emptyList())
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    val challenges by viewModel.challenges.collectAsState(initial = emptyList())
    val allSessions by viewModel.allSessions.collectAsState(initial = emptyList())
    val progress by deckViewModel.userProgress.collectAsState()
    val globalChallenges by viewModel.globalChallenges.collectAsState(initial = emptyList())
    
    var showGoalDialog by remember { mutableStateOf(false) }
    var showAddFriend by remember { mutableStateOf(false) }
    var friendMessage by remember { mutableStateOf<String?>(null) }
    val lastCompletedGoal by deckViewModel.lastCompletedGoal.collectAsState()
    var selectedTab by remember { mutableIntStateOf(initialTab) } // 0: Global, 1: Friends, 2: Goals
    val scope = rememberCoroutineScope()

    if (showGoalDialog) {
        CreateGoalDialog(
            decks = decks,
            onDismiss = { showGoalDialog = false },
            onCreate = { goal ->
                scope.launch {
                    viewModel.addChallenge(goal)
                    showGoalDialog = false
                }
            }
        )
    }

    Scaffold(
        containerColor = GhostWhite,
        floatingActionButton = {
            if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showAddFriend = true },
                    containerColor = GrapePop,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    icon = { Icon(Icons.Rounded.PersonAdd, "Add Friend") },
                    text = { Text("Add Friend") }
                )
            } else if (selectedTab == 2 || selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showGoalDialog = true },
                    containerColor = GrapePop,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    icon = { Icon(Icons.Rounded.Flag, "Add Goal") },
                    text = { Text("Set Goal") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Premium Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GrapePop, Color(0xFF8B5CF6), Color(0xFF9333EA))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            "GLOBAL",
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        IconButton(
                            onClick = { showGoalDialog = true },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Goal", tint = Color.White)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = when(selectedTab) {
                            1 -> "My\nFriends"
                            2 -> "Study\nAmbitions"
                            else -> "Real-time\nLeaderboard"
                        },
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 36.sp
                    )
                }
            }

            // Tabs
            Surface(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    com.flipwise.app.ui.screens.TabButton(
                        text = "Global",
                        icon = Icons.Rounded.Public,
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    com.flipwise.app.ui.screens.TabButton(
                        text = "Friends",
                        icon = Icons.Rounded.People,
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    com.flipwise.app.ui.screens.TabButton(
                        text = "Goals",
                        icon = Icons.Rounded.Flag,
                        isSelected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> {
                        Column {
                            if (globalChallenges.isNotEmpty()) {
                                Text(
                                    "Organization Events",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyInk,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                )
                                androidx.compose.foundation.lazy.LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    items(globalChallenges.size) { index ->
                                        val challenge = globalChallenges[index]
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color(0xFFF5F3FF),
                                            border = BorderStroke(1.dp, GrapePop.copy(alpha = 0.2f)),
                                            modifier = Modifier
                                                .width(280.dp)
                                                .clickable { onNavigateToChallengeDetail(challenge.id) }
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Rounded.Bolt, "Event", tint = GrapePop, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(challenge.goalType.uppercase(), color = GrapePop, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Text(challenge.name, fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 16.sp)
                                                Spacer(Modifier.height(4.dp))
                                                Text(challenge.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                                                Spacer(Modifier.height(8.dp))
                                                LinearProgressIndicator(
                                                    progress = { 0.3f }, // Dummy progress for global goal (would need aggregation)
                                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                                    color = GrapePop,
                                                    trackColor = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            RankingList(leaderboard, userProfile, onNavigateToProfile)
                        }
                    }
                    1 -> {
                        val pendingRequests = friends.filter { it.status == "pending" }
                        Column {
                            if (pendingRequests.isNotEmpty()) {
                                Text(
                                    "Pending Friend Requests",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyInk,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                                )
                                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    pendingRequests.forEach { request ->
                                        FriendRequestItem(
                                            request = request,
                                            onAccept = { viewModel.acceptFriendRequest(request) },
                                            onDecline = { viewModel.declineFriendRequest(request.id) },
                                            onClick = { onNavigateToProfile(request.id) }
                                        )
                                        Spacer(Modifier.height(12.dp))
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                
                                Text(
                                    "Friends Ranking",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyInk,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                )
                            }
                            FriendsRankingList(friends, userProfile, onNavigateToProfile)
                        }
                    }
                    2 -> {
                        // Refresh goals on tab view to auto-expire/complete
                        LaunchedEffect(Unit) { deckViewModel.refreshGoals() }

                        val personalGoals = challenges.filter { it.type == "personal" }
                        if (personalGoals.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Rounded.Flag,
                                title = "No goals yet",
                                description = "Set a personal goal to track your study progress!"
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp, start = 20.dp, end = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(personalGoals.size) { index ->
                                    val goal = personalGoals[index]
                                    val isCompleted = goal.status == "completed"
                                    val isExpired = goal.status == "expired"
                                    val isActive = goal.status == "active"

                                    // Calculate progress correctly by filtering sessions by deck
                                    val currentProgress = if (isActive) {
                                        val relevantSessions = if (goal.deckIds.isNotBlank()) {
                                            allSessions.filter { it.date >= goal.startDate && it.deckId == goal.deckIds }
                                        } else {
                                            allSessions.filter { it.date >= goal.startDate }
                                        }
                                        when(goal.goalType) {
                                            "Cards Studied" -> relevantSessions.sumOf { it.cardsStudied }
                                            "Points Earned" -> relevantSessions.sumOf { it.pointsEarned }
                                            "Streak Days" -> progress.currentStreak
                                            else -> 0
                                        }
                                    } else if (isCompleted) {
                                        goal.goal // Show as 100% for completed
                                    } else {
                                        0 // Expired
                                    }

                                    val percentage = (currentProgress.toFloat() / goal.goal.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

                                    // Status-based styling
                                    val cardColor = when {
                                        isCompleted -> Color(0xFFF0FDF4)
                                        isExpired -> Color(0xFFFEF2F2)
                                        else -> Color.White
                                    }
                                    val accentColor = when {
                                        isCompleted -> Color(0xFF10B981)
                                        isExpired -> Color(0xFFF43F5E)
                                        else -> GrapePop
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        color = cardColor
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Status icon
                                                val statusIcon = when {
                                                    isCompleted -> Icons.Rounded.CheckCircle
                                                    isExpired -> Icons.Rounded.Cancel
                                                    else -> Icons.Rounded.Flag
                                                }
                                                Icon(statusIcon, contentDescription = null, tint = accentColor)
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    goal.name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = com.flipwise.app.ui.theme.NavyInk,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                // Status badge
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = accentColor.copy(alpha = 0.1f)
                                                ) {
                                                    Text(
                                                        text = when {
                                                            isCompleted -> "✅ Completed"
                                                            isExpired -> "⏰ Expired"
                                                            else -> "${((goal.endDate - System.currentTimeMillis()) / 86400000).coerceAtLeast(0)}d left"
                                                        },
                                                        color = accentColor,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.height(12.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    "Target: ${goal.goal} ${goal.goalType}",
                                                    color = Color.Gray, fontSize = 14.sp
                                                )
                                                Text(
                                                    "$currentProgress / ${goal.goal}",
                                                    color = accentColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { percentage },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                                color = accentColor,
                                                trackColor = Color.LightGray.copy(alpha = 0.3f)
                                            )

                                            // Completed bonus
                                            if (isCompleted) {
                                                Spacer(Modifier.height(8.dp))
                                                Text(
                                                    "🎉 +50 Bonus XP Earned!",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddFriend) {
            AddFriendStyledDialog(
                onDismiss = { showAddFriend = false },
                onAdd = { username ->
                    scope.launch {
                        val result = viewModel.addFriend(username)
                        if (result.isSuccess) {
                            friendMessage = "Friend request sent!"
                        } else {
                            friendMessage = result.exceptionOrNull()?.message ?: "Failed to add friend"
                        }
                    }
                    showAddFriend = false
                }
            )
        }

        // Friend feedback message banner
        friendMessage?.let { msg ->
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp), contentAlignment = Alignment.BottomCenter) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (msg.contains("sent")) Color(0xFF10B981) else Color(0xFFEF4444),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(16.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // Auto hide
            LaunchedEffect(msg) {
                delay(3000)
                friendMessage = null
            }
        }

        // --- Personal Goal Congratulations Pop-up ---
        lastCompletedGoal?.let { goal ->
            AlertDialog(
                onDismissRequest = { deckViewModel.clearCompletedGoal() },
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("🎉 Congratulations!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = GrapePop)
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
                                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Goal Accomplished!", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 18.sp, 
                            color = NavyInk,
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
                        onClick = { deckViewModel.clearCompletedGoal() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GrapePop)
                    ) {
                        Text("Keep it up!", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun AddFriendStyledDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var username by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Add Friend",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Enter your friend's username to add them",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(24.dp))
                
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Username", color = Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text("Cancel", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onAdd(username) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        enabled = username.isNotBlank()
                    ) {
                        Text("Add Friend", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RankingList(leaderboard: List<UserProfile>, userProfile: UserProfile, onNavigateToProfile: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(leaderboard) { index, profile ->
            LeaderboardItem(
                rank    = index + 1,
                profile = profile,
                isMe    = profile.username == userProfile.username,
                onClick = { onNavigateToProfile(profile.id) }
            )
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, profile: UserProfile, isMe: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape    = RoundedCornerShape(24.dp),
        color    = if (isMe) GrapePop.copy(alpha = 0.05f) else Color.White,
        border   = if (isMe) androidx.compose.foundation.BorderStroke(1.5.dp, GrapePop.copy(alpha = 0.3f)) else null,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        when (rank) {
                            1 -> HoneyGold
                            2 -> Color(0xFF94A3B8)
                            3 -> Color(0xFFB45309)
                            else -> Color.Transparent
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (rank <= 3) "★" else "#$rank",
                    color = if (rank <= 3) Color.White else NavyInk60,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (rank <= 3) 18.sp else 14.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.linearGradient(listOf(GrapePop.copy(alpha = 0.1f), Color.White)),
                        CircleShape
                    )
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(profile.avatar, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = profile.displayName,
                    fontWeight = FontWeight.Bold,
                    color      = NavyInk,
                    fontSize   = 17.sp
                )
                Text(
                    text  = if (isMe) "You" else "@${profile.username}",
                    color = if (isMe) GrapePop else NavyInk60,
                    fontSize = 13.sp,
                    fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = profile.totalPoints.toString(),
                    fontWeight = FontWeight.Black,
                    color      = NavyInk,
                    fontSize   = 20.sp
                )
                Text("POINTS", color = NavyInk.copy(alpha = 0.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FriendsRankingList(
    friends: List<Friend>,
    userProfile: UserProfile,
    onNavigateToProfile: (String) -> Unit
) {
    val sortedFriends = friends.filter { it.status == "accepted" }
        .sortedByDescending { it.totalPoints }

    // Combine with current user
    val combinedList = (sortedFriends.map {
        UserProfile(
            id = it.id,
            displayName = it.displayName,
            username = it.username,
            avatar = it.avatar,
            totalPoints = it.totalPoints
        )
    } + userProfile).sortedByDescending { it.totalPoints }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(combinedList) { index, profile ->
            LeaderboardItem(
                rank = index + 1,
                profile = profile,
                isMe = profile.username == userProfile.username,
                onClick = { onNavigateToProfile(profile.id) }
            )
        }
    }
}
