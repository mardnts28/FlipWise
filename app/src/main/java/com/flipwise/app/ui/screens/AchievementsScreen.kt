package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.data.model.Achievement
import com.flipwise.app.viewmodel.DeckViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.flipwise.app.viewmodel.ProfileViewModel
import com.flipwise.app.ui.components.CreateChallengeDialog
import androidx.compose.material.icons.filled.AddCircleOutline

@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: DeckViewModel = viewModel()
) {
    val achievements by viewModel.achievements.collectAsState(initial = emptyList())
    val progress by viewModel.userProgress.collectAsState()
    val unlockedCount = achievements.count { it.isUnlocked }
    
    val profileViewModel: ProfileViewModel = viewModel()
    var showCreateGoalDialog by remember { mutableStateOf(false) }
    
    val categories = listOf("All", "Streaks", "Cards", "Mastery", "Points")
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }

    val filteredAchievements = remember(selectedCategory, achievements) {
        if (selectedCategory == "All") achievements
        else achievements.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFBBF24), Color(0xFFF97316))
                            ),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Achievements",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(onClick = { showCreateGoalDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "Create Goal",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(32.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unlocked", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                                Text("$unlockedCount/${achievements.size}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { if (achievements.isNotEmpty()) unlockedCount.toFloat() / achievements.size else 0f },
                                    modifier = Modifier.fillMaxWidth(0.8f).height(8.dp).clip(CircleShape),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Total Points", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(progress.totalPoints.toString(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Categories
            item(span = { GridItemSpan(2) }) {
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory),
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFF97316),
                    edgePadding = 24.dp,
                    divider = {},
                    indicator = {}
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Tab(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFFF97316) else Color(0xFFF3F4F6),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
                                    Text(
                                        text = category,
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Grid items
            itemsIndexed(filteredAchievements) { index, achievement ->
                val paddingStart = if (index % 2 == 0) 24.dp else 0.dp
                val paddingEnd = if (index % 2 != 0) 24.dp else 0.dp
                Box(modifier = Modifier.padding(start = paddingStart, end = paddingEnd)) {
                    AchievementGridItem(achievement) {
                        selectedAchievement = achievement
                    }
                }
            }
        }
    }

    if (selectedAchievement != null) {
        AchievementDetailDialog(
            achievement = selectedAchievement!!,
            onDismiss = { selectedAchievement = null }
        )
    }

    val friends by profileViewModel.friends.collectAsState(initial = emptyList())

    if (showCreateGoalDialog) {
        CreateChallengeDialog(
            friends = friends,
            onDismiss = { showCreateGoalDialog = false },
            onCreate = { challenge ->
                profileViewModel.addChallenge(challenge)
                showCreateGoalDialog = false
            }
        )
    }
}

@Composable
fun AchievementGridItem(achievement: Achievement, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF0E6FF),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (achievement.isUnlocked) Color.White else Color.LightGray.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (achievement.isUnlocked) {
                        Text(achievement.icon, fontSize = 28.sp)
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = if (achievement.isUnlocked) achievement.title else "???",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B4B),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = if (achievement.isUnlocked) achievement.description else "Keep studying to unlock!",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun AchievementDetailDialog(achievement: Achievement, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = Color(0xFF7C3AED).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (achievement.isUnlocked) {
                                Text(achievement.icon, fontSize = 48.sp)
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        text = if (achievement.isUnlocked) achievement.title else "Locked Achievement",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = if (achievement.isUnlocked) achievement.description else "Keep studying to unlock this achievement!",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    if (achievement.isUnlocked && achievement.unlockedAt != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF9F9FB)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Unlocked on", fontSize = 12.sp, color = Color.Gray)
                                Spacer(Modifier.height(4.dp))
                                val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                                val stf = SimpleDateFormat("h:mm a", Locale.getDefault())
                                val date = Date(achievement.unlockedAt)
                                Text(
                                    text = sdf.format(date),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    text = stf.format(date),
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFECFDF5)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Achievement Completed",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF9F9FB)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Requirement", fontSize = 12.sp, color = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = achievement.description,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF0E6))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Keep Studying to Unlock",
                                    color = Color(0xFFF97316),
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
