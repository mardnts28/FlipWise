package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.flipwise.app.data.model.Challenge
import com.flipwise.app.ui.theme.*
import com.flipwise.app.viewmodel.DeckViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    challengeId: String,
    onBack: () -> Unit,
    profileViewModel: com.flipwise.app.viewmodel.ProfileViewModel = viewModel(),
    deckViewModel: DeckViewModel = viewModel()
) {
    val decks by deckViewModel.decks.collectAsState(initial = emptyList())
    val userProfile by profileViewModel.userProfile.collectAsState()
    var challenge by remember { mutableStateOf<Challenge?>(null) }
    var participants by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    
    val ref = FirebaseDatabase.getInstance().reference.child("challenges").child(challengeId)
    val isParticipant = participants.any { it["userId"] == userProfile.id }

    DisposableEffect(challengeId) {
        val listener = ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                challenge = snapshot.getValue(Challenge::class.java)
                val parts = mutableListOf<Map<String, Any>>()
                snapshot.child("participants").children.forEach { child ->
                    val map = child.value as? Map<String, Any>
                    if (map != null) parts.add(map)
                }
                participants = parts.sortedByDescending { it["score"] as? Long ?: 0L }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
        onDispose { ref.removeEventListener(listener) }
    }

    Scaffold(
        containerColor = GhostWhite,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(challenge?.name ?: "Challenge Detail", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = NavyInk
                )
            )
        }
    ) { padding ->
        if (challenge == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GrapePop) }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                
                // Progress Card for the Event
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GrapePop.copy(alpha = 0.1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = GrapePop,
                                    modifier = Modifier.padding(8.dp).size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(challenge?.goalType?.uppercase() ?: "CHALLENGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrapePop)
                                Text(challenge?.name ?: "", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = NavyInk)
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Text(challenge?.description ?: "", color = Color.Gray, fontSize = 14.sp)
                        
                        Spacer(Modifier.height(24.dp))
                        
                        // Tracking real-time total (Global Progress)
                        val totalCards = participants.sumOf { it["score"] as? Long ?: 0L }
                        val goal = challenge?.goal?.toLong()?.coerceAtLeast(1L) ?: 1L
                        val progress = (totalCards.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Event Progress", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${totalCards.toLocaleString()} / ${goal.toLocaleString()}", color = GrapePop, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                            color = GrapePop,
                            trackColor = Color(0xFFF1F5F9) // ghost white / gray
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Join Button Section
                if (!isParticipant) {
                    Button(
                        onClick = { profileViewModel.joinGlobalChallenge(challengeId) },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GrapePop)
                    ) {
                        Text("Join Global Event", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(androidx.compose.material.icons.Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(Modifier.width(8.dp))
                            Text("You are participating in this event!", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                
                Text("Top Participants", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = NavyInk)
                Spacer(Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(participants) { part ->
                        ParticipantItem(part)
                    }
                }
            }
        }
    }
}

// Extension to format Long
fun Long.toLocaleString(): String = java.text.NumberFormat.getInstance().format(this)

@Composable
fun TeamScoreBoard(participants: List<Map<String, Any>>) {
    val blueScore = participants.filter { it["team"] == "Blue" }.sumOf { it["score"] as? Long ?: 0L }
    val redScore = participants.filter { it["team"] == "Red" }.sumOf { it["score"] as? Long ?: 0L }
    
    Row(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        TeamCard("Blue Team", blueScore, Color(0xFF3B82F6), Modifier.weight(1f))
        Spacer(Modifier.width(16.dp))
        TeamCard("Red Team", redScore, Color(0xFFEF4444), Modifier.weight(1f))
    }
}

@Composable
fun TeamCard(name: String, score: Long, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, color = color, shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, color = Color.White.copy(alpha = 0.8f))
            Text(score.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("points", color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun VersusBoard(participants: List<Map<String, Any>>) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        color = Color(0xFF1E1B4B),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            participants.take(2).forEachIndexed { i, p ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(p["avatar"] as? String ?: "👤", fontSize = 40.sp)
                    Text(p["displayName"] as? String ?: "Player", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(p["score"].toString(), color = Color.White, fontSize = 24.sp)
                }
                if (i == 0 && participants.size > 1) {
                    Text("VS", color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Black, fontSize = 32.sp)
                }
            }
        }
    }
}

@Composable
fun ParticipantItem(part: Map<String, Any>) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(part["avatar"] as? String ?: "👤", fontSize = 24.sp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(part["displayName"] as? String ?: "Player", fontWeight = FontWeight.Bold)
                if (part.containsKey("team")) Text("Team: ${part["team"]}", fontSize = 11.sp, color = Color.Gray)
            }
            Text(part["score"].toString(), fontWeight = FontWeight.Bold, color = GrapePop, fontSize = 18.sp)
        }
    }
}

@Composable
fun MatchDetailsBoard(challenge: Challenge, decks: List<com.flipwise.app.data.model.Deck>, participants: List<Map<String, Any>>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF9F9FB),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Match Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyInk)
            Spacer(Modifier.height(12.dp))
            val timeMins = challenge.timeLimit / 60
            Text("⏱ Time Limit: $timeMins minutes", fontSize = 14.sp, color = Color.Gray)
            
            val mappedDecks = challenge.deckIds.split(",").mapNotNull { id -> decks.find { it.id.trim() == id.trim() }?.name }
            val deckStr = if (mappedDecks.isEmpty()) "Unknown Deck" else mappedDecks.joinToString(", ")
            Text("📚 Deck Used: $deckStr", fontSize = 14.sp, color = Color.Gray)
            
            if (participants.isNotEmpty() && challenge.status != "active") {
                val winner = participants.first() // assuming sorted by score descending
                val winnerName = winner["displayName"] as? String ?: "Player"
                Text("🏆 Winner: $winnerName", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    }
}
