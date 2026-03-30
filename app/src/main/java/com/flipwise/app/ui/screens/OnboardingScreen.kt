package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipwise.app.ui.theme.*

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val slides = listOf(
        OnboardingSlide(
            icon = Icons.Default.Book,
            title = "Create Your Decks",
            description = "Organize your flashcards into colorful decks by subject or topic",
            iconBg = Color(0xFFB991FA)
        ),
        OnboardingSlide(
            icon = Icons.Default.ElectricBolt,
            title = "Smart Study Mode",
            description = "Rate cards as Easy, Hard, or Forgot to focus on what matters",
            iconBg = Color(0xFFF97316)
        ),
        OnboardingSlide(
            icon = Icons.Default.EmojiEvents,
            title = "Track Your Progress",
            description = "Earn points, unlock achievements, and build study streaks",
            iconBg = Color(0xFFFBBF24)
        )
    )
    var currentSlide by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F7FF))) {
        Text(
            text = "Skip",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .clickable { onComplete() },
            color = NavyInk60,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val slide = slides[currentSlide]
            
            Surface(
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                color = slide.iconBg.copy(alpha = 0.9f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = slide.icon,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(56.dp))
            
            Text(
                text = slide.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NavyInk,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = slide.description,
                fontSize = 16.sp,
                color = NavyInk60,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(80.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (index == currentSlide) 32.dp else 8.dp)
                            .background(
                                color = if (index == currentSlide) GrapePop else GrapePop.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }
            }
            
            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { 
                    if (currentSlide < slides.size - 1) {
                        currentSlide++ 
                    } else {
                        onComplete() 
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GrapePop)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (currentSlide < slides.size - 1) "Next" else "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

data class OnboardingSlide(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val iconBg: Color
)
