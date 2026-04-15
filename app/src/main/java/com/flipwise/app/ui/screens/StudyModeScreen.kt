package com.flipwise.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.data.model.Flashcard
import com.flipwise.app.ui.theme.*
import com.flipwise.app.viewmodel.DeckViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun StudyModeScreen(
    deckId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    viewModel: DeckViewModel = viewModel()
) {
    val dbCards by viewModel.getCardsForDeck(deckId).collectAsState(initial = emptyList())
    val decks by viewModel.decks.collectAsState(initial = emptyList())
    val deck = decks.find { it.id == deckId }

    var studyCards by remember { mutableStateOf<List<Flashcard>?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    
    var easyCount by remember { mutableIntStateOf(0) }
    var hardCount by remember { mutableIntStateOf(0) }
    var forgotCount by remember { mutableIntStateOf(0) }
    var totalPoints by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(dbCards) {
        if (studyCards == null && dbCards.isNotEmpty()) {
            studyCards = dbCards.shuffled()
        }
    }

    if (isFinished) {
        StudySummaryContent(
            cardsStudied = studyCards?.size ?: 0,
            easyCount = easyCount,
            pointsEarned = totalPoints,
            onStudyAgain = {
                studyCards = dbCards.shuffled()
                currentIndex = 0
                isFlipped = false
                isFinished = false
                easyCount = 0
                hardCount = 0
                forgotCount = 0
                totalPoints = 0
            },
            onBackToHome = {
                viewModel.saveStudySession(deckId, studyCards?.size ?: 0, easyCount)
                onComplete()
            }
        )
    } else if (studyCards != null && studyCards!!.isNotEmpty()) {
        val currentCard = studyCards!![currentIndex]
        val rotation by animateFloatAsState(
            targetValue = if (isFlipped) 180f else 0f,
            animationSpec = tween(500),
            label = "flip"
        )

        val studyBackgroundColor = Color(0xFFF97316) 

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(studyBackgroundColor)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${currentIndex + 1} / ${studyCards!!.size}", color = Color.White, fontSize = 14.sp)
                        Text("$totalPoints points", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    IconButton(onClick = { 
                        studyCards = dbCards.shuffled()
                        currentIndex = 0
                        isFlipped = false
                        totalPoints = 0
                        easyCount = 0
                        hardCount = 0
                        forgotCount = 0
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / studyCards!!.size },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }

            Spacer(Modifier.height(32.dp))
            
            val isMultipleChoice = !currentCard.options.isNullOrEmpty()
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .height(if (isMultipleChoice) 200.dp else 400.dp)
                    .graphicsLayer {
                        rotationY = if (isMultipleChoice) 0f else rotation
                        cameraDistance = 12f * density
                    }
                    .clickable(enabled = !isMultipleChoice) { isFlipped = !isFlipped },
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
                            text = if (isMultipleChoice || rotation < 90f) currentCard.front else currentCard.back,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B4B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer {
                                rotationY = if (isMultipleChoice || rotation < 90f) 0f else 180f
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(bottom = 24.dp),
                shape = RoundedCornerShape(40.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                val options = currentCard.options?.split("|")
                if (!options.isNullOrEmpty()) {
                    // Multiple Choice Mode
                    var selectedChoice by remember(currentCard.id) { mutableStateOf<String?>(null) }
                    val scope = rememberCoroutineScope()
                    
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedChoice == null) "Choose the correct option" 
                                   else if (selectedChoice == currentCard.back) "✨ BRILLIANT! CORRECT ✨" 
                                   else "❌ NOT QUITE. THE ANSWER IS:",
                            color = if (selectedChoice == null) NavyInk.copy(alpha = 0.5f) 
                                    else if (selectedChoice == currentCard.back) Color(0xFF10B981) 
                                    else Color(0xFFF43F5E),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        if (selectedChoice != null && selectedChoice != currentCard.back) {
                             Text(
                                text = currentCard.back,
                                color = NavyInk,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        options.forEach { option ->
                            val isCorrect = option == currentCard.back
                            val isSelected = selectedChoice == option
                            
                            Surface(
                                onClick = { 
                                    if (selectedChoice == null) {
                                        selectedChoice = option
                                        // Auto-advance logic
                                        scope.launch {
                                            delay(1500)
                                            if (option == currentCard.back) {
                                                easyCount++
                                                totalPoints += 10
                                                viewModel.updateCardSrs(currentCard, "easy")
                                            } else {
                                                forgotCount++
                                                totalPoints += 1
                                                viewModel.updateCardSrs(currentCard, "forgot")
                                            }
                                            
                                            if (currentIndex + 1 < studyCards!!.size) {
                                                currentIndex++
                                                isFlipped = false
                                                selectedChoice = null
                                            } else {
                                                isFinished = true
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .height(64.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = when {
                                    selectedChoice == null -> Color(0xFFF8FAFC)
                                    isSelected && isCorrect -> Color(0xFFD1FAE5)
                                    isSelected && !isCorrect -> Color(0xFFFEE2E2)
                                    selectedChoice != null && isCorrect -> Color(0xFFD1FAE5).copy(alpha = 0.6f)
                                    else -> Color(0xFFF8FAFC).copy(alpha = 0.5f)
                                },
                                border = BorderStroke(
                                    width = if (isSelected || (selectedChoice != null && isCorrect)) 2.dp else 1.dp,
                                    color = when {
                                        selectedChoice == null -> Color(0xFFE2E8F0)
                                        isSelected && isCorrect -> Color(0xFF10B981)
                                        isSelected && !isCorrect -> Color(0xFFF43F5E)
                                        selectedChoice != null && isCorrect -> Color(0xFF10B981).copy(alpha = 0.6f)
                                        else -> Color(0xFFE2E8F0)
                                    }
                                )
                            ) {
                                Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 24.dp)) {
                                    Text(
                                        text = option,
                                        fontSize = 17.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedChoice != null && (isSelected || isCorrect)) NavyInk else NavyInk.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (selectedChoice != null) "Next card in a moment..." else "Take your time",
                            color = NavyInk.copy(alpha = 0.3f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Standard Flip Mode
                    if (!isFlipped) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tap the card to reveal the answer", color = Color.Gray, fontSize = 16.sp)
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("How well did you know this?", color = Color.Gray, fontSize = 15.sp)
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StudyRatingButton("😟", "Forgot", Color(0xFFF43F5E), Modifier.weight(1f)) {
                                    forgotCount++
                                    totalPoints += 1
                                    viewModel.updateCardSrs(currentCard, "forgot")
                                    if (currentIndex + 1 < studyCards!!.size) {
                                        currentIndex++
                                        isFlipped = false
                                    } else {
                                        isFinished = true
                                    }
                                }
                                StudyRatingButton("🧐", "Hard", Color(0xFFF97316), Modifier.weight(1f)) {
                                    hardCount++
                                    totalPoints += 5
                                    viewModel.updateCardSrs(currentCard, "hard")
                                    if (currentIndex + 1 < studyCards!!.size) {
                                        currentIndex++
                                        isFlipped = false
                                    } else {
                                        isFinished = true
                                    }
                                }
                                StudyRatingButton("😊", "Easy", Color(0xFF10B981), Modifier.weight(1f)) {
                                    easyCount++
                                    totalPoints += 10
                                    viewModel.updateCardSrs(currentCard, "easy")
                                    if (currentIndex + 1 < studyCards!!.size) {
                                        currentIndex++
                                        isFlipped = false
                                    } else {
                                        isFinished = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (dbCards.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No cards in this deck", color = Color(0xFF1E1B4B))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Go Back") }
                }
            } else {
                CircularProgressIndicator(color = Color(0xFF7C3AED))
            }
        }
    }
}

@Composable
fun StudyRatingButton(emoji: String, label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        }
    }
}

@Composable
fun StudySummaryContent(
    cardsStudied: Int,
    easyCount: Int,
    pointsEarned: Int,
    onStudyAgain: () -> Unit,
    onBackToHome: () -> Unit
) {
    val profileViewModel: com.flipwise.app.viewmodel.ProfileViewModel = viewModel()
    val profile by profileViewModel.userProfile.collectAsState()
    
    // Check if user reached a new level (assuming 500 XP per level)
    val totalXpAfter = profile.xp + pointsEarned
    val currentLevel = profile.level
    val nextLevel = (totalXpAfter / 500) + 1
    val isLevelUp = nextLevel > currentLevel
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B5CF6))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = Color(0xFFFBBF24)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            }
        }
        
        Spacer(Modifier.height(24.dp))
        Text("CONGRATULATIONS!", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
        Text("You've mastered this session! 🎊", fontSize = 18.sp, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
        
        Spacer(Modifier.height(40.dp))

        // Cards Studied Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color(0xFFFFF0E6)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.TrackChanges, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Cards Studied", fontSize = 14.sp, color = Color.Gray)
                        Text("$cardsStudied", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    }
                }
            }
        }

        if (isLevelUp) {
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF10B981)
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("LEVEL UP!", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                        Text("Reached Level $nextLevel", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Points Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color(0xFFFFF9E6)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Points Earned", fontSize = 14.sp, color = Color.Gray)
                    Text("+$pointsEarned", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Accuracy Card
        val accuracy = if (cardsStudied > 0) (easyCount * 100 / cardsStudied) else 0
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color(0xFFE6F9F0)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Accuracy", fontSize = 14.sp, color = Color.Gray)
                    LinearProgressIndicator(
                        progress = { accuracy / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFFF3F4F6)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text("$accuracy%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }
        }

        Spacer(Modifier.height(24.dp))

        // Buttons
        Button(
            onClick = onStudyAgain,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
        ) {
            Text("Study Again", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(12.dp))
        
        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF7C3AED)
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Back to Home", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun SummaryItem(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = color) {
            Box(contentAlignment = Alignment.Center) {
                Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
