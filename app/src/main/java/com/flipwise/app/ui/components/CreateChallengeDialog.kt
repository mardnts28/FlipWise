package com.flipwise.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.flipwise.app.data.model.Challenge
import java.util.*

@Composable
fun CreateChallengeDialog(onDismiss: () -> Unit, onCreate: (Challenge) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Team") }
    var goalType by remember { mutableStateOf("Cards") }
    var goal by remember { mutableStateOf("50") }
    var duration by remember { mutableStateOf("7") }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text("Create New Goal", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Spacer(Modifier.height(8.dp))
                Text(
                    "Set a target for yourself or compete with friends.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(24.dp))

                Text("Challenge Name", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g., 7-Day Study Sprint", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9FB),
                        unfocusedContainerColor = Color(0xFFF9F9FB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text("Challenge Type", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (type == "Versus") 
                        "Best for quick daily sprints with one or two friends." 
                    else 
                        "Best for larger groups or longer weekly goals to keep everyone motivated.",
                    fontSize = 12.sp,
                    color = Color(0xFF7C3AED),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { type = "Team" },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "Team") Color(0xFF7C3AED) else Color(0xFFF9F9FB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🤝", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("Team", color = if (type == "Team") Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { type = "Versus" },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "Versus") Color(0xFF7C3AED) else Color(0xFFF9F9FB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚔️", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("Versus", color = if (type == "Versus") Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Goal Type", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0xFFF9F9FB), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(goalType, color = Color(0xFF1E1B4B))
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Goal", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = goal,
                            onValueChange = { goal = it },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text("Duration (days)", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = duration,
                    onValueChange = { duration = it },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text("Cancel", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onCreate(Challenge(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                description = description,
                                type = type.lowercase(),
                                goal = goal.toIntOrNull() ?: 50,
                                goalType = goalType,
                                startDate = System.currentTimeMillis(),
                                endDate = System.currentTimeMillis() + (duration.toLongOrNull() ?: 7) * 86400000,
                                status = "active",
                                createdBy = "local_user",
                                participants = "local_user"
                            ))
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) {
                        Text("Create Goal", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
