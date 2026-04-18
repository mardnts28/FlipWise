package com.flipwise.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipwise.app.R
import com.flipwise.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {
    val logoSize = FlipWiseDesign.dimensions.logoSize
    val headerSize = FlipWiseDesign.dimensions.headerFontSize
    val detailSize = FlipWiseDesign.dimensions.detailFontSize

    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateNext()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(GrapePop),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "FlipWise Logo",
                modifier = Modifier.size(logoSize)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "FlipWise", 
                fontSize = headerSize, 
                fontWeight = FontWeight.Bold, 
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Master your knowledge, one flip at a time", 
                fontSize = detailSize, 
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
