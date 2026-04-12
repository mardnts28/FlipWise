package com.flipwise.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.R
import com.flipwise.app.ui.theme.*
import com.flipwise.app.ui.components.FlipWiseTextField
import kotlinx.coroutines.launch
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dimensions = FlipWiseDesign.dimensions
    val profileViewModel: com.flipwise.app.viewmodel.ProfileViewModel = viewModel()

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    val blob1Pos by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1"
    )

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
        // Background Decorations
        Box(
            modifier = Modifier
                .offset(x = 200.dp, y = (-100).dp)
                .size(400.dp)
                .blur(80.dp)
                .background(GrapePop.copy(alpha = 0.2f * blob1Pos), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 100.dp)
                .size(400.dp)
                .blur(80.dp)
                .background(CoralZest.copy(alpha = 0.2f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Section
            Spacer(modifier = Modifier.height(40.dp))
            
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "FlipWise Logo",
                    modifier = Modifier.size(dimensions.logoSize)
                )
                
                // Floating Sparkle Badge
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

                // Floating Zap Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-10).dp, y = 10.dp)
                        .background(
                            Brush.linearGradient(listOf(GrapePop, Color(0xFF9333EA))),
                            CircleShape
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ElectricBolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Join FlipWise",
                color = NavyInk,
                fontSize = dimensions.titleFontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Start your learning journey today",
                color = NavyInk.copy(alpha = 0.6f),
                fontSize = dimensions.bodyFontSize,
                modifier = Modifier.padding(top = dimensions.paddingSmall)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Main Card
            Surface(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.cardCornerRadius)),
                color = Color.White.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(dimensions.paddingLarge),
                    verticalArrangement = Arrangement.spacedBy(dimensions.paddingLarge)
                ) {
                    // Error Message
                    AnimatedVisibility(
                        visible = error != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFFFEF2F2), Color(0xFFFEF2F2).copy(alpha = 0.5f))),
                                    RoundedCornerShape(16.dp)
                                )
                                .border(2.dp, Color(0xFFFECACA), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error ?: "",
                                color = Color(0xFFB91C1C),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Name Field
                    Column {
                        Text(
                            text = "Full Name",
                            color = NavyInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        FlipWiseTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "John Doe",
                            leadingIcon = Icons.Rounded.Person
                        )
                    }

                    // Nickname Field
                    Column {
                        Text(
                            text = "Nickname (Username)",
                            color = NavyInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        FlipWiseTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            placeholder = "flipper123",
                            leadingIcon = Icons.Rounded.AlternateEmail
                        )
                    }

                    // Email Field
                    Column {
                        Text(
                            text = "Email Address",
                            color = NavyInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        FlipWiseTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "your@email.com",
                            leadingIcon = Icons.Rounded.Mail,
                            keyboardType = KeyboardType.Email
                        )
                    }

                    // Password Field
                    Column {
                        Text(
                            text = "Password",
                            color = NavyInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        FlipWiseTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "At least 6 characters",
                            leadingIcon = Icons.Rounded.Lock,
                            isPassword = true,
                            showPassword = showPassword,
                            onPasswordToggle = { showPassword = !showPassword }
                        )
                    }

                    // Confirm Password Field
                    Column {
                        Text(
                            text = "Confirm Password",
                            color = NavyInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        FlipWiseTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Re-enter your password",
                            leadingIcon = Icons.Rounded.Lock,
                            isPassword = true,
                            showPassword = showConfirmPassword,
                            onPasswordToggle = { showConfirmPassword = !showConfirmPassword }
                        )
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            if (name.isBlank() || nickname.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                error = "Please fill in all fields"
                            } else if (!email.lowercase().endsWith("@gmail.com")) {
                                error = "Please enter a valid Gmail address (@gmail.com)"
                            } else if (!"^[a-zA-Z0-9_]{3,20}$".toRegex().matches(nickname.trim())) {
                                error = "Nickname must be 3-20 characters and only contain letters, numbers, or underscores"
                            } else if (password.length < 6) {
                                error = "Password must be at least 6 characters"
                            } else if (password != confirmPassword) {
                                error = "Passwords do not match"
                            } else {
                                isLoading = true
                                error = null
                                scope.launch {
                                    if (profileViewModel.isUsernameTaken(nickname.trim())) {
                                        error = "Username is already taken. Please pick another one."
                                        isLoading = false
                                        return@launch
                                    }

                                    val result = profileViewModel.signUp(email.trim(), password.trim())
                                    if (result.isSuccess) {
                                        // Initialize profile
                                        profileViewModel.register(nickname.trim(), name.trim())
                                        onRegisterSuccess(email)
                                    } else {
                                        val e = result.exceptionOrNull()
                                        error = when (e) {
                                            is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                                                "This email is already associated with an account."
                                            else -> e?.message ?: "Registration failed"
                                        }
                                    }
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensions.buttonHeight),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = "Create Account",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    color = NavyInk.copy(alpha = 0.7f),
                    fontSize = 18.sp
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Sign In",
                        color = GrapePop,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
