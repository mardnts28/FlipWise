package com.flipwise.app.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
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
    decks: List<com.flipwise.app.data.model.Deck> = emptyList(),
    onDismiss: () -> Unit,
    onCreate: (Challenge) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var subType by remember { mutableStateOf("1v1") } // "1v1" or "team_vs_team"
    var timerMinutes by remember { mutableStateOf("5") }
    var selectedFriendIds by remember { mutableStateOf(setOf<String>()) }
    var selectedDeckIds by remember { mutableStateOf(setOf<String>()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(Color(0xFF7C3AED).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Groups, contentDescription = null, tint = Color(0xFF7C3AED))
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("New Challenge", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyInk)
                }
                
                Spacer(Modifier.height(24.dp))

                Text("Battle Name", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Flashcard Duel", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9FB),
                        unfocusedContainerColor = Color(0xFFF9F9FB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text("Format", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModeButton(
                        label = "1 vs 1",
                        selected = subType == "1v1",
                        icon = "⚔\uFE0F",
                        onClick = { subType = "1v1" },
                        modifier = Modifier.weight(1f)
                    )
                    ModeButton(
                        label = "Teams",
                        selected = subType == "team_vs_team",
                        icon = "\uD83D\uDCAA",
                        onClick = { subType = "team_vs_team" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Time Limit (Minutes)", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = timerMinutes,
                    onValueChange = { if(it.all { char -> char.isDigit() }) timerMinutes = it },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9FB),
                        unfocusedContainerColor = Color(0xFFF9F9FB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text("Select Decks (Multiple)", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                if (decks.isEmpty()) {
                    Text("No decks found.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        decks.forEach { deck ->
                            DeckOptionItem(
                                deck = deck,
                                selected = selectedDeckIds.contains(deck.id),
                                onClick = {
                                    selectedDeckIds = if (selectedDeckIds.contains(deck.id)) {
                                        selectedDeckIds - deck.id
                                    } else {
                                        selectedDeckIds + deck.id
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text("Invite Friends", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                
                val acceptedFriends = friends.filter { it.status == "accepted" }
                if (acceptedFriends.isEmpty()) {
                    Text("No online friends to challenge.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        acceptedFriends.forEach { friend ->
                            FriendOptionItem(
                                friend = friend,
                                selected = selectedFriendIds.contains(friend.id),
                                onClick = {
                                    selectedFriendIds = if (selectedFriendIds.contains(friend.id)) {
                                        selectedFriendIds - friend.id
                                    } else {
                                        selectedFriendIds + friend.id
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel", color = NavyInk, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val timeLimit = (timerMinutes.toIntOrNull() ?: 5) * 60
                            onCreate(Challenge(
                                id = UUID.randomUUID().toString(),
                                name = name.ifBlank { "Battle vs Friends" },
                                description = "Competitive study session",
                                type = "versus",
                                subType = subType,
                                timeLimit = timeLimit,
                                participants = ("local_user" + (if (selectedFriendIds.isNotEmpty()) "," + selectedFriendIds.joinToString(",") else "")),
                                deckIds = selectedDeckIds.joinToString(","),
                                status = "active"
                            ))
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        enabled = selectedDeckIds.isNotEmpty() && (subType == "team_vs_team" || selectedFriendIds.isNotEmpty())
                    ) {
                        Text("Start Battle", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
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
        color = if (selected) Color(0xFF7C3AED) else Color(0xFFF9F9FB),
        border = BorderStroke(1.5.dp, if (selected) Color(0xFF7C3AED) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.Bold, color = if (selected) Color.White else NavyInk)
        }
    }
}

@Composable
fun DeckOptionItem(deck: com.flipwise.app.data.model.Deck, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0xFF7C3AED).copy(alpha = 0.1f) else Color(0xFFF9F9FB),
        border = if (selected) BorderStroke(1.dp, Color(0xFF7C3AED)) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(try { Color(android.graphics.Color.parseColor(deck.color)) } catch(e: Exception) { Color.Gray }, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(deck.icon, fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(deck.name, fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            if (selected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
            }
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
