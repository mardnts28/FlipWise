package com.flipwise.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.ui.theme.*
import com.flipwise.app.ui.components.FlipWiseTextField
import com.flipwise.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun CompleteProfileScreen(
    onComplete: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    var nickname by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val dimensions = FlipWiseDesign.dimensions
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GrapePop.copy(alpha = 0.1f),
                        GhostWhite,
                        CoralZest.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "👋", fontSize = 80.sp)
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-10).dp)
                        .background(
                            Brush.linearGradient(listOf(HoneyGold, Color(0xFFF59E0B))),
                            CircleShape
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "One Last Step!",
                color = NavyInk,
                fontSize = dimensions.titleFontSize,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Pick a nickname to start your journey",
                color = NavyInk.copy(alpha = 0.6f),
                fontSize = dimensions.bodyFontSize,
                modifier = Modifier.padding(top = dimensions.paddingSmall)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.cardCornerRadius)),
                color = Color.White.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(dimensions.paddingLarge),
                    verticalArrangement = Arrangement.spacedBy(dimensions.paddingLarge)
                ) {
                    AnimatedVisibility(visible = error != null) {
                        Text(text = error ?: "", color = Color.Red, fontSize = 14.sp)
                    }

                    Column {
                        Text(
                            text = "Your Nickname",
                            color = NavyInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        FlipWiseTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            placeholder = "e.g. flipper123",
                            leadingIcon = Icons.Rounded.Badge
                        )
                    }

                    Button(
                        onClick = {
                            if (nickname.isBlank()) {
                                error = "Please enter a nickname"
                            } else if (!"^[a-zA-Z0-9_]{3,20}$".toRegex().matches(nickname.trim())) {
                                error = "Nickname must be 3-20 characters and only contain letters, numbers, or underscores"
                            } else {
                                    isLoading = true
                                    error = null
                                    scope.launch {
                                        if (profileViewModel.isUsernameTaken(nickname.trim())) {
                                            error = "Username is already taken. Please pick another one."
                                            isLoading = false
                                            return@launch
                                        }

                                        val profile = profileViewModel.userProfile.value
                                        val result = profileViewModel.updateProfile(
                                            displayName = profile.displayName,
                                            username = nickname,
                                            bio = profile.bio,
                                            avatar = profile.avatar
                                        )
                                        isLoading = false
                                        if (result.isSuccess) {
                                            onComplete()
                                        } else {
                                            error = "Failed to save profile: ${result.exceptionOrNull()?.message}"
                                        }
                                    }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensions.buttonHeight),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(GrapePop, Color(0xFF8B5CF6), Color(0xFF9333EA))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Get Started",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
