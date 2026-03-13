package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipwise.app.ui.theme.GrapePop
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateNext()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(GrapePop),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🃏", fontSize = 72.sp)
            Spacer(Modifier.height(16.dp))
            Text("FlipWise", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Master your knowledge, one flip at a time", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}