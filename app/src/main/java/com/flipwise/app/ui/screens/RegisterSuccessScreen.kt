package com.flipwise.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipwise.app.ui.theme.*

@Composable
fun RegisterSuccessScreen(
    email: String,
    onContinueToLogin: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "success")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GrapePop.copy(alpha = 0.05f), Color.White)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Success Icon with Pulse
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(140.dp * scale),
                    shape = CircleShape,
                    color = MintGreen.copy(alpha = 0.1f)
                ) {}
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MintGreen,
                    shadowElevation = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MarkEmailRead,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(24.dp).fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            Text(
                text = "Account Created!",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyInk,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "We've sent a verification link to:",
                fontSize = 16.sp,
                color = NavyInk.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = GrapePop.copy(alpha = 0.05f)
            ) {
                Text(
                    text = email,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrapePop
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Please check your inbox (and spam folder) to verify your account before logging in.",
                fontSize = 14.sp,
                color = NavyInk.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(64.dp))

            Button(
                onClick = onContinueToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GrapePop,
                    contentColor = Color.White
                )
            ) {
                Text("Continue to Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
