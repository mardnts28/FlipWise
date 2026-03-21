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
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.flipwise.app.data.model.Challenge
import com.flipwise.app.data.model.Friend
import com.flipwise.app.ui.theme.CoralZest
import com.flipwise.app.ui.theme.NavyInk
import java.util.*

@Composable
fun CreateChallengeDialog(
    friends: List<Friend> = emptyList(),
    onDismiss: () -> Unit,
    onCreate: (Challenge) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("team") }
    var goalType by remember { mutableStateOf("Cards") }
    var goalVal by remember { mutableStateOf("100") }
    var duration by remember { mutableStateOf("7") }
    var selectedFriendId by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(CoralZest.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Groups, contentDescription = null, tint = CoralZest)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("Community Challenge", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyInk)
                }
                
                Spacer(Modifier.height(24.dp))

                Text("Challenge Name", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Finals Week Battle", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9FB),
                        unfocusedContainerColor = Color(0xFFF9F9FB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text("Mode", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModeButton(
                        label = "Team",
                        selected = type == "team",
                        icon = "🤝",
                        onClick = { type = "team" },
                        modifier = Modifier.weight(1f)
                    )
                    ModeButton(
                        label = "Versus",
                        selected = type == "versus",
                        icon = "⚔️",
                        onClick = { type = "versus" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Metric", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
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
                            Text(goalType, color = NavyInk)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Goal", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = goalVal,
                            onValueChange = { goalVal = it },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9F9FB),
                                unfocusedContainerColor = Color(0xFFF9F9FB),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                if (type == "versus") {
                    Spacer(Modifier.height(20.dp))
                    Text("Select Opponent", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    
                    if (friends.isEmpty()) {
                        Text("No friends available. Add some friends first!", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            friends.forEach { friend ->
                                FriendOptionItem(
                                    friend = friend,
                                    selected = selectedFriendId == friend.id,
                                    onClick = { selectedFriendId = friend.id }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        onCreate(Challenge(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            description = description,
                            type = type,
                            goal = goalVal.toIntOrNull() ?: 100,
                            goalType = goalType,
                            startDate = System.currentTimeMillis(),
                            endDate = System.currentTimeMillis() + (duration.toLongOrNull() ?: 7) * 86400000,
                            status = "active",
                            createdBy = "local_user",
                            participants = if (type == "versus" && selectedFriendId != null) "local_user,$selectedFriendId" else "local_user"
                        ))
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralZest),
                    enabled = name.isNotBlank() && (type != "versus" || selectedFriendId != null)
                ) {
                    Text("Launch Challenge", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun ModeButton(label: String, selected: Boolean, icon: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) CoralZest.copy(alpha = 0.1f) else Color(0xFFF9F9FB),
        border = BorderStroke(1.5.dp, if (selected) CoralZest else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.Bold, color = if (selected) CoralZest else NavyInk)
        }
    }
}

@Composable
fun FriendOptionItem(friend: Friend, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) CoralZest.copy(alpha = 0.1f) else Color(0xFFF9F9FB),
        border = if (selected) BorderStroke(1.dp, CoralZest) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(friend.avatar, fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(friend.displayName, fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                Text("@${friend.username}", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            if (selected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CoralZest, modifier = Modifier.size(20.dp))
            }
        }
    }
}
