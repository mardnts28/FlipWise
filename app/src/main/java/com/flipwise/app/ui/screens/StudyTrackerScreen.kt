package com.flipwise.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.data.model.Achievement
import com.flipwise.app.viewmodel.DeckViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.flipwise.app.data.ai.AiInsight
import androidx.compose.animation.*
import com.flipwise.app.ui.theme.*

@Composable
fun StudyTrackerScreen(
    onBack: () -> Unit,
    viewModel: DeckViewModel = viewModel()
) {
    val achievements by viewModel.achievements.collectAsState(initial = emptyList())
    val progress by viewModel.userProgress.collectAsState()
    val decks by viewModel.decks.collectAsState(initial = emptyList())
    val sessions by viewModel.sessions.collectAsState(initial = emptyList())
    
    val totalCards = decks.sumOf { it.cardCount }
    val totalMastered = decks.sumOf { it.masteredCount }
    val unlockedAchievements = achievements.filter { it.isUnlocked }
    
    val masteryPercent = if (totalCards > 0) totalMastered.toFloat() / totalCards else 0f
    
    val totalCorrect = sessions.sumOf { it.correctCount }
    val totalStudied = sessions.sumOf { it.cardsStudied }
    val retentionRate = if (totalStudied > 0) totalCorrect.toFloat() / totalStudied else 0f
    
    val productivity = if (sessions.isNotEmpty()) {
        val firstSessionDate = sessions.minOf { it.date }
        val daysDiff = ((System.currentTimeMillis() - firstSessionDate) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
        totalStudied.toFloat() / daysDiff
    } else 0f

    val consistency = (progress.currentStreak.toFloat() / 30f).coerceAtMost(1f)
    
    var showAchievementDialog by remember { mutableStateOf(false) }
    var selectedAchievements by remember { mutableStateOf(unlockedAchievements) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    
    val aiInsight by viewModel.aiInsight.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB))
            .verticalScroll(rememberScrollState())
    ) {
        // --- Header ---
        Surface(
            color = Color(0xFF10B981),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Study Tracker",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Track your learning journey",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- AI Insights ---
            AnimatedVisibility(
                visible = aiInsight != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                aiInsight?.let { insight ->
                    AiInsightBox(insight)
                }
            }

            // --- Level Progression ---
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Learning Level", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyInk)
                            Text("Level ${progress.totalPoints / 1000 + 1}", color = Color.Gray, fontSize = 14.sp)
                        }
                        Surface(
                            color = GrapePop.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Text(
                                "XP: ${progress.totalPoints % 1000}/1000",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = GrapePop,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { (progress.totalPoints % 1000) / 1000f },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFFF0F0F5)
                    )
                }
            }

            // --- Activity Section (Monthly Calendar) ---
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Study Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyInk)
                    Spacer(Modifier.height(16.dp))
                    
                    MonthlyActivityCalendar(
                        sessions = sessions,
                        achievements = unlockedAchievements,
                        onDayClick = { date, dayAchievements -> 
                            selectedDate = date
                            selectedAchievements = dayAchievements
                            showAchievementDialog = true
                        }
                    )
                }
            }

            // --- Stats Grid ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TrackerStatCard(
                    title = "Retention",
                    value = "${(retentionRate * 100).toInt()}%",
                    icon = Icons.Default.TrackChanges,
                    iconColor = Color(0xFF7C3AED),
                    iconBg = Color(0xFFF0E6FF),
                    progress = retentionRate,
                    modifier = Modifier.weight(1f)
                )
                TrackerStatCard(
                    title = "Consistency",
                    value = "${(consistency * 100).toInt()}%",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconColor = Color(0xFFF97316),
                    iconBg = Color(0xFFFFF0E6),
                    progress = consistency,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TrackerStatCard(
                    title = "Productivity",
                    value = if (productivity > 0) String.format("%.1f", productivity) else "0",
                    subtitle = "cards/day",
                    icon = Icons.Default.Bolt,
                    iconColor = Color(0xFFFBBF24),
                    iconBg = Color(0xFFFFF9E6),
                    progress = (productivity / 50f).coerceAtMost(1f),
                    modifier = Modifier.weight(1f)
                )
                TrackerStatCard(
                    title = "Mastery",
                    value = "${(masteryPercent * 100).toInt()}%",
                    icon = Icons.Default.MilitaryTech,
                    iconColor = Color(0xFF10B981),
                    iconBg = Color(0xFFE6F9F0),
                    progress = masteryPercent,
                    modifier = Modifier.weight(1f)
                )
            }

            // --- Overall Stats Section ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF7C3AED)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Overall Stats", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Cards", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(totalCards.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mastered", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(totalMastered.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Current Streak", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("${progress.currentStreak} days", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Best Streak", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("${progress.longestStreak} days", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showAchievementDialog) {
        AchievementsUnlockedDialog(
            unlockedAchievements = selectedAchievements,
            selectedDate = selectedDate,
            onDismiss = { showAchievementDialog = false }
        )
    }
}

@Composable
fun AchievementsUnlockedDialog(
    unlockedAchievements: List<Achievement>,
    selectedDate: Long? = null,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (unlockedAchievements.isEmpty()) "Activity on this day" else "Achievements Unlocked",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B4B)
                        )
                        val dateToShow = selectedDate ?: unlockedAchievements.maxByOrNull { it.unlockedAt ?: 0L }?.unlockedAt ?: System.currentTimeMillis()
                        val dateText = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(dateToShow))
                        
                        Text(
                            dateText,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Achievement List
                if (unlockedAchievements.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f, fill = false)
                    ) {
                        unlockedAchievements.forEach { achievement ->
                            AchievementUnlockedItem(
                                title = achievement.title,
                                description = achievement.description,
                                time = achievement.unlockedAt?.let { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it)) } ?: "--:--",
                                icon = achievement.icon,
                                tier = achievement.tier
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No achievements earned on this day.", color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Bottom Badge/Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF7C3AED)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (unlockedAchievements.isNotEmpty()) 
                                "${unlockedAchievements.size} Achievement${if(unlockedAchievements.size > 1) "s" else ""} on this day!"
                            else "Keep studying to earn badges!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementUnlockedItem(title: String, description: String, time: String, icon: String, tier: String = "bronze") {
    val tierColor = when (tier.lowercase()) {
        "gold" -> Color(0xFFFBBF24)
        "silver" -> Color(0xFF94A3B8)
        else -> Color(0xFFB45309)
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.2f)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = tierColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 28.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = tierColor)
                Text(description, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text("Unlocked at $time", fontSize = 11.sp, color = Color.Gray.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun MonthlyActivityCalendar(
    sessions: List<com.flipwise.app.data.model.StudySession>,
    achievements: List<com.flipwise.app.data.model.Achievement>,
    onDayClick: (Long, List<com.flipwise.app.data.model.Achievement>) -> Unit
) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    // Header
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    
    fun getDayKey(timestamp: Long): String {
        val c = Calendar.getInstance()
        c.timeInMillis = timestamp
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
    }
    
    val sessionMap = sessions.groupBy { getDayKey(it.date) }
    val achievementMap = achievements.filter { it.unlockedAt != null }.groupBy { getDayKey(it.unlockedAt!!) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(monthName, fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                Spacer(Modifier.width(4.dp))
                Text("Studied", fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.width(12.dp))
                Box(Modifier.size(8.dp).background(Color(0xFFFBBF24), CircleShape))
                Spacer(Modifier.width(4.dp))
                Text("Badge", fontSize = 11.sp, color = Color.Gray)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Month Grid
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val rows = (daysInMonth + firstDayOfWeek + 6) / 7
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Day Labels
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(day, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            repeat(rows) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(7) { colIndex ->
                        val dayNumber = rowIndex * 7 + colIndex - firstDayOfWeek + 1
                        
                        if (dayNumber in 1..daysInMonth) {
                            val tempCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, currentYear)
                                set(Calendar.MONTH, currentMonth)
                                set(Calendar.DAY_OF_MONTH, dayNumber)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            
                            val dayKey = getDayKey(tempCal.timeInMillis)
                            val daySessions = sessionMap[dayKey] ?: emptyList()
                            val dayAchievements = achievementMap[dayKey] ?: emptyList()
                            val studyCount = daySessions.sumOf { it.cardsStudied }
                            val isToday = getDayKey(System.currentTimeMillis()) == dayKey
                            
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            studyCount > 0 -> Color(0xFF10B981).copy(alpha = (studyCount.toFloat() / 50f).coerceIn(0.1f, 1f))
                                            else -> Color(0xFFF8F9FB)
                                        }
                                    )
                                    .border(
                                        if (dayAchievements.isNotEmpty()) 1.5.dp else if (isToday) 1.dp else 0.dp,
                                        if (dayAchievements.isNotEmpty()) Color(0xFFFBBF24) else if (isToday) Color(0xFF10B981) else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable(enabled = studyCount > 0 || dayAchievements.isNotEmpty()) {
                                        onDayClick(tempCal.timeInMillis, dayAchievements)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    dayNumber.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (studyCount > 0) Color.White else NavyInk
                                )
                            }
                        } else {
                            Box(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackerStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        modifier = modifier.height(160.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(title, fontSize = 14.sp, color = Color.Gray)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                if (subtitle != null) {
                    Spacer(Modifier.width(4.dp))
                    Text(subtitle, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = iconColor,
                trackColor = Color(0xFFF0F0F5)
            )
        }
    }
}

@Composable
fun AiInsightBox(insight: AiInsight) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E1B4B),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(insight.icon, fontSize = 28.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Study Coach",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = insight.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = insight.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
