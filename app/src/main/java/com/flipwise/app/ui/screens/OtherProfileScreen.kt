package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.viewmodel.ProfileViewModel

import kotlinx.coroutines.launch

@Composable
fun OtherProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val otherUser by viewModel.otherUser.collectAsState()
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        viewModel.fetchOtherUser(userId)
    }

    val friendship = friends.find { it.id == userId }
    val isFriend = friendship?.status == "accepted"
    val isPending = friendship?.status == "pending"
    val isSent = friendship?.status == "sent"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFF))
            .verticalScroll(rememberScrollState())
    ) {
        // --- Header ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF7C3AED),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
                
                otherUser?.let { profile ->
                    // Avatar
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(profile.avatar, fontSize = 64.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text(profile.displayName, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("@${profile.username}", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Stats
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ProfileStatItem(profile.totalPoints.toString(), "Points")
                        ProfileStatItem(profile.level.toString(), "Level")
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Action Button
                    Button(
                        onClick = {
                            if (isFriend || isSent || isPending) {
                                viewModel.removeFriend(userId)
                            } else {
                                scope.launch {
                                    viewModel.addFriend(profile.username)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isFriend -> Color(0xFFEF4444)
                                isPending || isSent -> Color.Gray
                                else -> Color.White
                            },
                            contentColor = if (isFriend || isPending || isSent) Color.White else Color(0xFF7C3AED)
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
                    ) {
                        val (icon, text) = when {
                            isFriend -> Icons.Default.PersonRemove to "Remove Friend"
                            isPending -> Icons.Default.PersonAdd to "Accept Request"
                            isSent -> Icons.Default.PersonAdd to "Request Sent"
                            else -> Icons.Default.PersonAdd to "Add Friend"
                        }
                        Icon(icon, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(text, fontWeight = FontWeight.Bold)
                    }
                } ?: run {
                    Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        // Activity Placeholder
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Public Achievements", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("No public achievements visible", color = Color.Gray)
                }
            }
        }
    }
}
