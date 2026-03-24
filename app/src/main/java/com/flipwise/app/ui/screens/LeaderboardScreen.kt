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
import com.flipwise.app.ui.components.CreateChallengeDialog
import com.flipwise.app.ui.components.CreateGoalDialog
import com.flipwise.app.ui.theme.*
import com.flipwise.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val challenges by viewModel.challenges.collectAsState(initial = emptyList())
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showChallengeDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = GhostWhite,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // Goal Creation FAB (as requested: circle button at the bottom for goal creation)
                FloatingActionButton(
                    onClick = { showGoalDialog = true },
                    containerColor = GrapePop,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Rounded.Flag, contentDescription = "Create Goal")
                }
                
                // Optional: Challenge Creation FAB
                FloatingActionButton(
                    onClick = { showChallengeDialog = true },
                    containerColor = CoralZest,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Create Challenge")
                }
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
                    .height(240.dp)
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
                            onClick = { /* Refresh logic integrated in flow */ },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Rounded.Public, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Real-time\nLeaderboard",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 36.sp
                    )
                    
                    Spacer(Modifier.weight(1f))
                    
                    // Simple Tab Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(4.dp)
                    ) {
                        TabItem(
                            title = "Ranking",
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        TabItem(
                            title = "Challenges",
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Content Area
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "tabContent"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> RankingList(leaderboard, userProfile)
                        1 -> ChallengesList(challenges)
                    }
                }
            }
        }
    }

    if (showGoalDialog) {
        CreateGoalDialog(
            onDismiss = { showGoalDialog = false },
            onCreate = { goal ->
                viewModel.addChallenge(goal)
                showGoalDialog = false
            }
        )
    }

    if (showChallengeDialog) {
        CreateChallengeDialog(
            friends = friends.map { Friend(it.id, it.userId, it.username, it.displayName, it.avatar, it.status, it.addedAt, it.totalPoints, it.currentStreak, it.totalCardsStudied) }, // Map Friend to model if needed or assuming same
            onDismiss = { showChallengeDialog = false },
            onCreate = { challenge ->
                viewModel.addChallenge(challenge)
                showChallengeDialog = false
            }
        )
    }
}

@Composable
fun TabItem(title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) GrapePop else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
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
                isMe    = profile.username == userProfile.username
            )
        }
    }
}

@Composable
fun ChallengesList(challenges: List<Challenge>) {
    if (challenges.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏔️", fontSize = 64.sp)
                Spacer(Modifier.height(16.dp))
                Text("No active challenges", color = NavyInk60, fontWeight = FontWeight.Bold)
                Text("Create one to compete with others!", color = NavyInk.copy(alpha = 0.4f), fontSize = 14.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(challenges) { _, challenge ->
                ChallengeItem(challenge = challenge)
            }
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

@Composable
fun ChallengeItem(challenge: Challenge) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(CoralZest.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (challenge.type == "personal") "🎯" else "🏆", fontSize = 22.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(challenge.name, fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 18.sp)
                    Text(challenge.type.uppercase(), color = CoralZest, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = NavyInk60)
            }
            
            Spacer(Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { 0.4f }, // Placeholder
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = CoralZest,
                trackColor = CoralZest.copy(alpha = 0.1f)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${challenge.goal} ${challenge.goalType}", color = NavyInk60, fontSize = 12.sp)
                Text("active", color = MintGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
