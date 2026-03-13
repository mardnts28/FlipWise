package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StudyTrackerScreen(onBack: () -> Unit) {
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
            // --- Activity Section ---
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E1B4B))
                    Spacer(Modifier.height(16.dp))
                    
                    ActivityHeatmap()
                    
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Less", fontSize = 12.sp, color = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Box(Modifier.size(12.dp).background(Color(0xFFF0E6FF), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Box(Modifier.size(12.dp).background(Color(0xFF7C3AED), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text("More", fontSize = 12.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(Color(0xFFFBBF24), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text("Achievement", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // --- Stats Grid ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TrackerStatCard(
                    title = "Retention",
                    value = "0%",
                    icon = Icons.Default.TrackChanges,
                    iconColor = Color(0xFF7C3AED),
                    iconBg = Color(0xFFF0E6FF),
                    modifier = Modifier.weight(1f)
                )
                TrackerStatCard(
                    title = "Consistency",
                    value = "0%",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconColor = Color(0xFFF97316),
                    iconBg = Color(0xFFFFF0E6),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TrackerStatCard(
                    title = "Productivity",
                    value = "0",
                    subtitle = "cards/day",
                    icon = Icons.Default.Bolt,
                    iconColor = Color(0xFFFBBF24),
                    iconBg = Color(0xFFFFF9E6),
                    modifier = Modifier.weight(1f)
                )
                TrackerStatCard(
                    title = "Mastery",
                    value = "0%",
                    icon = Icons.Default.MilitaryTech,
                    iconColor = Color(0xFF10B981),
                    iconBg = Color(0xFFE6F9F0),
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
                            Text("0", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mastered", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("0", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Current Streak", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("0 days", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Best Streak", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("0 days", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ActivityHeatmap() {
    val days = listOf("Sun", "", "Tue", "", "Thu", "", "Sat")
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            days.forEach { day ->
                Text(day, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(14) { // Columns
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { // Cells
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFFF0F0F5), CircleShape)
                        )
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
                progress = { 0f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = iconColor,
                trackColor = Color(0xFFF0F0F5)
            )
        }
    }
}
