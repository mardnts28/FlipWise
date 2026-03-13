package com.flipwise.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
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

    LaunchedEffect(dbCards) {
        if (studyCards == null && dbCards.isNotEmpty()) {
            studyCards = dbCards.shuffled()
        }
    }

    if (isFinished) {
        StudySummaryContent(
            cardsStudied = studyCards?.size ?: 0,
            easyCount = easyCount,
            hardCount = hardCount,
            forgotCount = forgotCount,
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

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { isFlipped = !isFlipped },
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
                            color = Color(0xFF1E1B4B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer {
                                rotationY = if (rotation < 90f) 0f else 180f
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                if (!isFlipped) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tap the card to reveal the answer", color = Color.White, fontSize = 14.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StudyRatingButton("Forgot", Color(0xFFF43F5E), Modifier.weight(1f)) {
                            forgotCount++
                            totalPoints += 1
                            if (currentIndex + 1 < studyCards!!.size) {
                                currentIndex++
                                isFlipped = false
                            } else {
                                isFinished = true
                            }
                        }
                        StudyRatingButton("Hard", Color(0xFFFBBF24), Modifier.weight(1f)) {
                            hardCount++
                            totalPoints += 5
                            if (currentIndex + 1 < studyCards!!.size) {
                                currentIndex++
                                isFlipped = false
                            } else {
                                isFinished = true
                            }
                        }
                        StudyRatingButton("Easy", Color(0xFF10B981), Modifier.weight(1f)) {
                            easyCount++
                            totalPoints += 10
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
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (dbCards.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No cards in this deck", color = NavyInk)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Go Back") }
                }
            } else {
                CircularProgressIndicator(color = GrapePop)
            }
        }
    }
}

@Composable
fun StudyRatingButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun StudySummaryContent(
    cardsStudied: Int,
    easyCount: Int,
    hardCount: Int,
    forgotCount: Int,
    pointsEarned: Int,
    onStudyAgain: () -> Unit,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF7C3AED), Color(0xFFC4B5FD))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = Color(0xFFFBBF24)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
            }
        }
        
        Spacer(Modifier.height(24.dp))
        Text("Great Job!", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("You completed the study session", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
        
        Spacer(Modifier.height(48.dp))

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
                
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6))
                Spacer(Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    SummaryItem("$easyCount", "Easy", Color(0xFF10B981))
                    SummaryItem("$hardCount", "Hard", Color(0xFFFBBF24))
                    SummaryItem("$forgotCount", "Forgot", Color(0xFFF43F5E))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

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

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStudyAgain,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
        ) {
            Text("Study Again", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color.White),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Back to Home", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun SummaryItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = color) {
            Box(contentAlignment = Alignment.Center) {
                Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
