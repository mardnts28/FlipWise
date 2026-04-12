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
    val dimensions = FlipWiseDesign.dimensions
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
                modifier = Modifier.size(dimensions.logoSize)
            )
            Spacer(Modifier.height(dimensions.paddingMedium))
            Text(
                "FlipWise", 
                fontSize = dimensions.headerFontSize, 
                fontWeight = FontWeight.Bold, 
                color = Color.White
            )
            Spacer(Modifier.height(dimensions.paddingSmall))
            Text(
                "Master your knowledge, one flip at a time", 
                fontSize = dimensions.detailFontSize, 
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
