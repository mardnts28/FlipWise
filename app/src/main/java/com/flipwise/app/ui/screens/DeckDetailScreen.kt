package com.flipwise.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.data.model.AiGenerationState
import com.flipwise.app.data.model.Flashcard
import com.flipwise.app.ui.theme.*
import com.flipwise.app.viewmodel.DeckViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: String,
    onBack: () -> Unit,
    onStartStudy: () -> Unit,
    viewModel: DeckViewModel = viewModel()
) {
    val cards by viewModel.getCardsForDeck(deckId).collectAsState(initial = emptyList())
    val decks by viewModel.decks.collectAsState(initial = emptyList())
    val deck = decks.find { it.id == deckId }

    var showAddCardChoiceDialog by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }

    val aiGenerationState by viewModel.aiGenerationState.collectAsState()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.generateFlashcardsFromFile(deckId, it)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = try { Color(android.graphics.Color.parseColor(deck?.color ?: "#F97316")) } catch (e: Exception) { Color(0xFFF97316) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                    }
                    Text(
                        text = deck?.name ?: "Deck",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Study Deck",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "${cards.size} card${if (cards.size != 1) "s" else ""}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCardChoiceDialog = true },
                containerColor = Color(0xFF6D28D9),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFBFBFF))
        ) {
            if (cards.isEmpty()) {
                EmptyCardsView(onAddClick = { showAddCardChoiceDialog = true })
            } else {
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = onStartStudy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Start Study Session", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cards, key = { it.id }) { card ->
                        CardListItem(card = card, onDelete = { viewModel.deleteCard(card) })
                    }
                }
            }
        }
    }

    if (showAddCardChoiceDialog) {
        AddFlashcardChoiceDialog(
            onDismiss = { showAddCardChoiceDialog = false },
            onManualCreateSelected = {
                showAddCardChoiceDialog = false
                showAddCardDialog = true
            },
            onFileUploadSelected = {
                filePickerLauncher.launch("*/*")
            },
            aiGenerationState = aiGenerationState,
            onResetAiState = { viewModel.resetAiGenerationState() },
            onAiSuccessDismiss = {
                viewModel.resetAiGenerationState()
                showAddCardChoiceDialog = false
            }
        )
    }

    if (showAddCardDialog) {
        AddFlashcardDialog(
            onDismiss = { showAddCardDialog = false },
            onAdd = { front, back ->
                viewModel.addFlashcard(deckId, front, back)
                showAddCardDialog = false
            }
        )
    }
}

@Composable
fun CardListItem(card: Flashcard, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Front", fontSize = 12.sp, color = Color.Gray)
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A8A), modifier = Modifier.size(18.dp))
                }
            }
            Text(card.front, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
            
            Spacer(Modifier.height(16.dp))
            
            Text("Back", fontSize = 12.sp, color = Color.Gray)
            Text(card.back, fontSize = 16.sp, color = Color(0xFF1E1B4B))
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Color.LightGray, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("Not studied", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun EmptyCardsView(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFFF0F5)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("📋", fontSize = 48.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("No flashcards yet", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
        Spacer(Modifier.height(8.dp))
        Text(
            "Add your first flashcard to start studying",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddFlashcardDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(color = Color(0xFF7C3AED), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("New Flashcard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier.padding(24.dp).weight(1f)
                ) {
                    Text("Front (Question)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = front,
                        onValueChange = { if (it.length <= 500) front = it },
                        placeholder = { Text("What do you want to remember?", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9FB),
                            unfocusedContainerColor = Color(0xFFF9F9FB),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Text(
                        text = "${front.length}/500 characters",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(24.dp))

                    Text("Back (Answer)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = back,
                        onValueChange = { if (it.length <= 500) back = it },
                        placeholder = { Text("The answer or explanation", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9FB),
                            unfocusedContainerColor = Color(0xFFF9F9FB),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Text(
                        text = "${back.length}/500 characters",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(32.dp))

                    Text("Preview", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    Spacer(Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF0E6FF)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (front.isBlank()) "Front side preview" else front,
                                textAlign = TextAlign.Center,
                                color = if (front.isBlank()) Color(0xFF1E1B4B).copy(alpha = 0.6f) else Color(0xFF1E1B4B)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F1F4)),
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (back.isBlank()) "Back side preview" else back,
                                textAlign = TextAlign.Center,
                                color = if (back.isBlank()) Color(0xFF1E1B4B).copy(alpha = 0.6f) else Color(0xFF1E1B4B)
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFD1D1D6))
                        ) {
                            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        }
                        Button(
                            onClick = { onAdd(front, back) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                            enabled = front.isNotBlank() && back.isNotBlank()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Add Card", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddFlashcardChoiceDialog(
    onDismiss: () -> Unit,
    onManualCreateSelected: () -> Unit,
    onFileUploadSelected: () -> Unit,
    aiGenerationState: AiGenerationState = AiGenerationState.Idle,
    onResetAiState: () -> Unit = {},
    onAiSuccessDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = {
            if (aiGenerationState !is AiGenerationState.Loading) {
                onResetAiState()
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFBFBFF)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(color = Color(0xFF7C3AED), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (aiGenerationState !is AiGenerationState.Loading) {
                                    onResetAiState()
                                    onDismiss()
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("New Flashcard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Show different content base on AI generation state
                when (aiGenerationState) {
                    is AiGenerationState.Loading -> {
                        // Loading State
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = Color(0xFF7C3AED),
                                strokeWidth = 4.dp
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                aiGenerationState.message,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "This may take a moment...",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    is AiGenerationState.Success -> {
                        // Success State
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Flashcards Generated!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${aiGenerationState.cardsGenerated} flashcards have been added to your deck",
                                fontSize = 16.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = onAiSuccessDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                            ) {
                                Text("View Cards", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is AiGenerationState.Error -> {
                        // Error State
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                color = Color(0xFFEF4444).copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Something went wrong",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                aiGenerationState.message,
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = onResetAiState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                            ) {
                                Text("Try Again", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is AiGenerationState.Idle -> {
                        // Default Choice UI
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "How would you like to create\nflashcards?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B),
                                textAlign = TextAlign.Center,
                                lineHeight = 30.sp
                            )

                            Spacer(Modifier.height(32.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFileUploadSelected() },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Color(0xFFE5D5F5)),
                                color = Color(0xFFF8F4FF)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("\u2728", fontSize = 24.sp)
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "AI File Converter",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E1B4B)
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "Upload a PDF, PPT, or DOC file and let AI automatically generate flashcards with both standard and multiple-choice questions",
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B7280),
                                        lineHeight = 20.sp
                                    )

                                    Spacer(Modifier.height(24.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .drawBehind {
                                                drawRoundRect(
                                                    color = Color(0xFFC4B5FD),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = 2.dp.toPx(),
                                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                                    ),
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.Upload,
                                                contentDescription = "Upload",
                                                tint = Color(0xFF7C3AED),
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                "Click to upload file",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1E1B4B)
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "PDF, PPT, PPTX, DOC, DOCX, TXT\n(Max 10MB)",
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(32.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE5E7EB)))
                                Text(
                                    "OR",
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 14.sp
                                )
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE5E7EB)))
                            }

                            Spacer(Modifier.height(32.dp))

                            OutlinedButton(
                                onClick = onManualCreateSelected,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFF7C3AED)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF7C3AED)
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Create Manually", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
