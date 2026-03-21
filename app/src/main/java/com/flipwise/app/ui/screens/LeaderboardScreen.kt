package com.flipwise.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.data.model.UserProfile
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Leaderboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GhostWhite,
                    titleContentColor = NavyInk
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshLeaderboard() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        containerColor = GhostWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Podium / Top 3 Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(listOf(GrapePop, GrapePop.copy(alpha = 0.8f))),
                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏅", fontSize = 48.sp)
                    Text(
                        "Top Learners",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Real-time ranking from around the world",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
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
    }
}

@Composable
fun LeaderboardItem(rank: Int, profile: UserProfile, isMe: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        color    = if (isMe) GrapePop20 else Color.White,
        border   = if (isMe) androidx.compose.foundation.BorderStroke(2.dp, GrapePop) else null,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text     = "#$rank",
                modifier = Modifier.width(40.dp),
                fontWeight = FontWeight.Bold,
                color    = if (rank <= 3) HoneyGold else NavyInk60,
                fontSize = 18.sp
            )

            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(GrapePop.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(profile.avatar, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = profile.displayName,
                    fontWeight = FontWeight.Bold,
                    color      = NavyInk,
                    fontSize   = 16.sp
                )
                Text(
                    text  = "@${profile.username}",
                    color = NavyInk60,
                    fontSize = 12.sp
                )
            }

            // Points
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "${profile.totalPoints}",
                    fontWeight = FontWeight.ExtraBold,
                    color      = GrapePop,
                    fontSize   = 18.sp
                )
                Text(
                    text  = "pts",
                    color = NavyInk60,
                    fontSize = 10.sp
                )
            }
        }
    }
}
