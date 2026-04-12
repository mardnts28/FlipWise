package com.flipwise.app.ui.screens

import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
import com.flipwise.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val focusManager = LocalFocusManager.current

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
                .background(GrapePop.copy(alpha = 0.2f * blob1Pos), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 100.dp)
                .size(400.dp)
                .background(CoralZest.copy(alpha = 0.2f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Section
            Spacer(modifier = Modifier.height(40.dp))
            
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "FlipWise Logo",
                    modifier = Modifier.size(140.dp)
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
                        imageVector = Icons.Rounded.Star,
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
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome Back",
                color = NavyInk,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Continue your learning journey",
                color = NavyInk.copy(alpha = 0.6f),
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Main Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp)),
                color = Color.White.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Success Message
                    AnimatedVisibility(
                        visible = successMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MintGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .border(1.dp, MintGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = MintGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(successMessage ?: "", color = NavyInk, fontSize = 14.sp)
                        }
                    }

                    // Error Message
                    AnimatedVisibility(
                        visible = error != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
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
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (error?.contains("verify", ignoreCase = true) == true) {
                                TextButton(
                                    onClick = {
                                        isLoading = true
                                        error = null
                                        scope.launch {
                                            val result = profileViewModel.resendVerificationEmail(email.trim(), password.trim())
                                            isLoading = false
                                            if (result.isSuccess) {
                                                successMessage = "Verification email resent! Check your inbox."
                                            } else {
                                                error = "Failed to resend: ${result.exceptionOrNull()?.message}"
                                            }
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                                ) {
                                    Text("Resend Verification Email", color = GrapePop, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
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
                            placeholder = "Enter your password",
                            leadingIcon = Icons.Rounded.Lock,
                            isPassword = true,
                            showPassword = showPassword,
                            onPasswordToggle = { showPassword = !showPassword },
                            keyboardType = KeyboardType.Password
                        )
                    }

                    // Forgot Password Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                if (email.isBlank()) {
                                    error = "Enter your email first to reset password"
                                    successMessage = null
                                } else {
                                    isLoading = true
                                    successMessage = null
                                    error = null
                                    scope.launch {
                                        val result = profileViewModel.sendPasswordResetEmail(email.trim())
                                        isLoading = false
                                        if (result.isSuccess) {
                                            successMessage = "Password reset email sent!"
                                        } else {
                                            error = "Failed to send reset email: ${result.exceptionOrNull()?.message}"
                                        }
                                    }
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = GrapePop,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (email.isBlank() || password.isBlank()) {
                                error = "Please fill in all fields"
                                successMessage = null
                            } else if (!email.contains("@")) {
                                error = "Please enter a valid email"
                                successMessage = null
                            } else {
                                isLoading = true
                                error = null
                                successMessage = null
                                scope.launch {
                                    val result = profileViewModel.signIn(email, password)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        onLoginSuccess()
                                    } else {
                                        val ex = result.exceptionOrNull()
                                        error = when (ex) {
                                            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "This account does not exist in our system."
                                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "The email address or password provided is incorrect."
                                            else -> result.exceptionOrNull()?.message ?: "Login failed"
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
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
                                Text(
                                    text = "Sign In",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // --- Google One-Tap Sign-In ---
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val googleLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)
                            account.idToken?.let { token ->
                                scope.launch {
                                    isLoading = true
                                    val signInResult = profileViewModel.signInWithGoogle(token)
                                    if (signInResult.isSuccess) {
                                        val regResult = profileViewModel.loginOrRegister(account.displayName ?: "", account.email ?: "")
                                        isLoading = false
                                        if (regResult.isSuccess) {
                                            onLoginSuccess()
                                        } else {
                                            error = "Failed to finalize account: ${regResult.exceptionOrNull()?.message}"
                                        }
                                    } else {
                                        isLoading = false
                                        error = "Verification failed. Try again."
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            val apiEx = e as? ApiException
                            error = when(apiEx?.statusCode) {
                                12502 -> "Sign-in was interrupted. Please try again."
                                12501 -> "Sign-in canceled by user."
                                12500 -> "Configuration issue. Please use email/password for now."
                                else -> "Google Sign-In error: ${e.message}"
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken("307290224469-gf0osk7odpkmvuo3t8tmbfhh1s8b6am3.apps.googleusercontent.com")
                                .requestEmail()
                                .build()
                            val client = GoogleSignIn.getClient(context, gso)
                            googleLauncher.launch(client.signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, NavyInk.copy(alpha = 0.1f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyInk)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF4285F4))
                            Spacer(Modifier.width(12.dp))
                            Text("Sign in with Google", fontWeight = FontWeight.Bold)
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
                    text = "New to FlipWise? ",
                    color = NavyInk.copy(alpha = 0.7f),
                    fontSize = 18.sp
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Create Account",
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
