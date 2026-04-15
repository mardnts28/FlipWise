package com.flipwise.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.data.model.Challenge
import com.flipwise.app.data.model.Flashcard
import com.flipwise.app.data.model.UserProfile
import com.flipwise.app.ui.theme.*
import com.flipwise.app.viewmodel.DeckViewModel
import com.flipwise.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChallengeGameScreen(
    challengeId: String,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    deckViewModel: DeckViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val challenges by profileViewModel.challenges.collectAsState(initial = emptyList())
    val challenge = challenges.find { it.id == challengeId }
    
    val deckIds = remember(challenge) { challenge?.deckIds?.split(",")?.filter { it.isNotBlank() } ?: emptyList() }
    val allCards by deckViewModel.getCardsForDecks(deckIds).collectAsState(initial = emptyList())
    
    var gameCards by remember { mutableStateOf<List<Flashcard>?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var userScore by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(challenge?.timeLimit ?: 300) }
    var isFlipped by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var simulatedOpponentScore by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    // Initialize Game and Sync Time
    LaunchedEffect(allCards) {
        if (gameCards == null && allCards.isNotEmpty()) {
            gameCards = allCards.shuffled()
        }
    }

    LaunchedEffect(challenge) {
        challenge?.let {
            timeLeft = it.timeLimit
        }
    }

    // Timer Logic
    LaunchedEffect(timeLeft, isFinished) {
        if (timeLeft > 0 && !isFinished) {
            delay(1000)
            timeLeft--
            // Simulate opponent making progress
            if (timeLeft % 10 == 0) {
                simulatedOpponentScore += (5..15).random()
            }
        } else if (timeLeft == 0) {
            isFinished = true
        }
    }

    if (isFinished) {
        ChallengeResultContent(
            userScore = userScore,
            opponentScore = simulatedOpponentScore,
            subType = challenge?.subType ?: "1v1",
            onPlayAgain = {
                currentIndex = 0
                userScore = 0
                timeLeft = challenge?.timeLimit ?: 300
                isFlipped = false
                isFinished = false
                simulatedOpponentScore = 0
                gameCards = allCards.shuffled()
            },
            onGoHome = onNavigateHome
        )
    } else if (gameCards != null && gameCards!!.isNotEmpty()) {
        val currentCard = gameCards!![currentIndex % gameCards!!.size] // Loop cards if needed
        val isMCQ = !currentCard.options.isNullOrBlank()
        val optionsList = remember(currentCard) { currentCard.options?.split("|")?.filter { it.isNotBlank() } ?: emptyList() }
        
        val rotation by animateFloatAsState(
            targetValue = if (isFlipped && !isMCQ) 180f else 0f,
            animationSpec = tween(500),
            label = "flip"
        )

        Column(
            modifier = Modifier.fillMaxSize().background(GrapePop)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Quit", tint = Color.White)
                }
                
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Score: $userScore",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Opponent Progress Bar (Top)
            val winProgress = if (userScore + simulatedOpponentScore > 0) userScore.toFloat() / (userScore + simulatedOpponentScore) else 0.5f
            LinearProgressIndicator(
                progress = { winProgress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = CoralZest,
                trackColor = Color(0xFF3B82F6) // Blue for opponent
            )

            Spacer(Modifier.weight(0.5f))

            // Card
            Box(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable(enabled = !isMCQ) { isFlipped = !isFlipped },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = if (rotation < 90f) currentCard.front else currentCard.back,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyInk,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer {
                                rotationY = if (rotation < 90f) 0f else 180f
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(0.5f))

            // Controls
            if (isMCQ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    optionsList.forEach { option ->
                        Button(
                            onClick = {
                                if (option == currentCard.back) {
                                    userScore += 15
                                } else {
                                    // Wrong answer, maybe don't add score or subtract
                                }
                                currentIndex++
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NavyInk),
                            border = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Text(option, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else if (isFlipped) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            userScore += 5
                            currentIndex++
                            isFlipped = false
                        },
                        modifier = Modifier.weight(1f).height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Hard", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            userScore += 15
                            currentIndex++
                            isFlipped = false
                        },
                        modifier = Modifier.weight(1f).height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Easy", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Tap to reveal answer", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp)
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(GrapePop), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text("Preparing your decks...", color = Color.White)
            }
        }
    }
}

@Composable
fun ChallengeResultContent(
    userScore: Int,
    opponentScore: Int,
    subType: String,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val isWin = userScore >= opponentScore
    val title = if (isWin) "VICTORY! \uD83C\uDFC6" else "DEFEAT \uD83D\uDE14"
    val winColor = if (isWin) Color(0xFF10B981) else Color(0xFFEF4444)

    // Apply points to profile once when finished
    var pointsApplied by remember { mutableStateOf(false) }
    val profileViewModel: ProfileViewModel = viewModel()
    
    LaunchedEffect(isWin, pointsApplied) {
        if (!pointsApplied) {
            val userProfile = profileViewModel.userProfile.value
            val pxDiff = if (isWin) 50 else -20
            val newTotalPoints = (userProfile.totalPoints + pxDiff).coerceAtLeast(0)
            val newXp = (userProfile.xp + pxDiff).coerceAtLeast(0)
            
            profileViewModel.updateProfileData(userProfile.copy(
                totalPoints = newTotalPoints,
                xp = newXp
            ))
            pointsApplied = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrapePop)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        
        Text(title, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = winColor, textAlign = TextAlign.Center)
        Text(
            if (isWin) "You out-studied them!" else "Better luck next time!",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        
        Spacer(Modifier.height(48.dp))

        // Versus Scoreboard
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(32.dp)
        ) {
            Row(modifier = Modifier.padding(32.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("\uD83E\uDD29", fontSize = 48.sp)
                    Text("You", fontWeight = FontWeight.Bold, color = NavyInk)
                    Text(userScore.toString(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = GrapePop)
                }
                
                Text("VS", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("\uD83D\uDE08", fontSize = 48.sp)
                    Text(if (subType == "1v1") "Opponent" else "Red Team", fontWeight = FontWeight.Bold, color = NavyInk)
                    Text(opponentScore.toString(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = GrapePop)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Play Again", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
        ) {
            Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(12.dp))
            Text("Go Home", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
