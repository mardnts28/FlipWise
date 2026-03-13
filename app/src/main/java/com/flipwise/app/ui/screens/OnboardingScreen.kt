package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val slide = slides[currentSlide]
            
            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color = slide.iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = slide.icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(48.dp))
            
            Text(
                text = slide.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F3D56),
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = slide.description,
                fontSize = 16.sp,
                color = Color(0xFF7D7D91),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                slides.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (index == currentSlide) 32.dp else 8.dp)
                            .background(
                                color = if (index == currentSlide) Color(0xFF7C3AED) else Color(0xFFD1C4E9),
                                shape = CircleShape
                            )
                    )
                }
            }
            
            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { if (currentSlide < slides.size - 1) currentSlide++ else onComplete() },
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (currentSlide < slides.size - 1) "Next" else "Get Started",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
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
