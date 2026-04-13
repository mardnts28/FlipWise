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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
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
    
    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Global, 1: Friends, 2: Goals
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
            ExtendedFloatingActionButton(
                onClick = { showGoalDialog = true },
                containerColor = GrapePop,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Rounded.Flag, "Add Goal") },
                text = { Text("Set Goal") }
            )
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
                        text = "Real-time\nLeaderboard",
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
                    0 -> RankingList(leaderboard, userProfile)
                    1 -> {
                        val friendUids = friends.filter { it.status == "accepted" }.map { it.id } + userProfile.id
                        val friendLeaderboard = leaderboard.filter { friendUids.contains(it.id) }.sortedByDescending { it.totalPoints }
                        if (friendLeaderboard.size <= 1) {
                            com.flipwise.app.ui.screens.EmptyStateView(
                                icon = Icons.Rounded.GroupAdd,
                                title = "No friends yet",
                                description = "Add friends to compete against them in the leaderboard!"
                            )
                        } else {
                            RankingList(friendLeaderboard, userProfile)
                        }
                    }
                    2 -> {
                        val personalGoals = challenges.filter { it.type == "personal" }
                        if (personalGoals.isEmpty()) {
                            com.flipwise.app.ui.screens.EmptyStateView(
                                icon = Icons.Rounded.Flag,
                                title = "No active goals",
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
                                    
                                    val currentProgress = when(goal.goalType) {
                                        "Cards Studied" -> allSessions.filter { it.date >= goal.startDate }.sumOf { it.cardsStudied }
                                        "Points Earned" -> allSessions.filter { it.date >= goal.startDate }.sumOf { it.pointsEarned }
                                        "Streak Days" -> progress.currentStreak // Bounded by current streak
                                        else -> 0
                                    }
                                    
                                    val percentage = (currentProgress.toFloat() / goal.goal.toFloat()).coerceIn(0f, 1f)

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        color = Color.White
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Rounded.Flag, contentDescription = null, tint = GrapePop)
                                                Spacer(Modifier.width(8.dp))
                                                Text(goal.name, fontWeight = FontWeight.Bold, color = com.flipwise.app.ui.theme.NavyInk, fontSize = 16.sp)
                                                Spacer(Modifier.weight(1f))
                                                Text(
                                                    "${((goal.endDate - System.currentTimeMillis()) / 86400000).coerceAtLeast(0)}d left",
                                                    color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Spacer(Modifier.height(12.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    "Target: ${goal.goal} ${goal.goalType}",
                                                    color = Color.Gray, fontSize = 14.sp
                                                )
                                                Text("$currentProgress / ${goal.goal}", color = GrapePop, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { percentage },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                                color = if (percentage >= 1f) Color(0xFF10B981) else GrapePop,
                                                trackColor = Color.LightGray.copy(alpha = 0.3f)
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
}

@Composable
fun RankingList(leaderboard: List<UserProfile>, userProfile: UserProfile) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(leaderboard) { index, profile ->
            LeaderboardItem(
                rank    = index + 1,
                profile = profile,
                isMe    = profile.id == userProfile.id
            )
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, profile: UserProfile, isMe: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
