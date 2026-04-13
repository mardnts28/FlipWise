package com.flipwise.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
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
import com.flipwise.app.ui.theme.GrapePop
import com.flipwise.app.ui.theme.NavyInk
import java.util.*

@Composable
fun CreateGoalDialog(
    decks: List<com.flipwise.app.data.model.Deck> = emptyList(),
    onDismiss: () -> Unit, 
    onCreate: (Challenge) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("100") }
    var type by remember { mutableStateOf("Cards Studied") }
    var duration by remember { mutableStateOf("7") }
    var selectedDeckId by remember { mutableStateOf<String?>(null) }
    var expandedDecks by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(GrapePop.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = GrapePop)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("Set Personal Goal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyInk)
                }

                Spacer(Modifier.height(24.dp))

                Text("What's your target?", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Master Biology", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9FB),
                        unfocusedContainerColor = Color(0xFFF9F9FB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text("Select Deck (Optional)", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedButton(
                        onClick = { expandedDecks = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        val selectedDeck = decks.find { it.id == selectedDeckId }
                        Text(selectedDeck?.name ?: "All Decks", color = NavyInk)
                    }
                    DropdownMenu(expanded = expandedDecks, onDismissRequest = { expandedDecks = false }) {
                        DropdownMenuItem(
                            text = { Text("All Decks") },
                            onClick = { selectedDeckId = null; expandedDecks = false }
                        )
                        decks.forEach { deck ->
                            DropdownMenuItem(
                                text = { Text(deck.name) },
                                onClick = { selectedDeckId = deck.id; expandedDecks = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(3f)) {
                        Text("Goal Type", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        
                        val types = listOf("Cards Studied", "Points Earned", "Streak Days")
                        var expanded by remember { mutableStateOf(false) }
                        
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(type, color = NavyInk)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                types.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t) },
                                        onClick = { type = t; expanded = false }
                                    )
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(2f)) {
                        Text("Target", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = target,
                            onValueChange = { if(it.all { c -> c.isDigit() }) target = it },
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

                Spacer(Modifier.height(20.dp))

                Text("Timeframe (days)", fontWeight = FontWeight.Bold, color = NavyInk, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = duration,
                    onValueChange = { if(it.all { c -> c.isDigit() }) duration = it },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9FB),
                        unfocusedContainerColor = Color(0xFFF9F9FB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        onCreate(Challenge(
                            id = UUID.randomUUID().toString(),
                            name = title.ifBlank { "Study Goal" },
                            description = "Personal Achievement",
                            type = "personal",
                            goal = target.toIntOrNull() ?: 100,
                            goalType = type,
                            startDate = System.currentTimeMillis(),
                            endDate = System.currentTimeMillis() + (duration.toLongOrNull() ?: 7) * 86400000,
                            status = "active",
                            createdBy = "local_user",
                            participants = "local_user",
                            deckIds = selectedDeckId ?: ""
                        ))
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrapePop)
                ) {
                    Text("Start Journey", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}
