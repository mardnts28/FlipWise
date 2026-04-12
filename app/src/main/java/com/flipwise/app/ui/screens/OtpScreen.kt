package com.flipwise.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.ui.theme.*
import com.flipwise.app.util.TotpManager
import com.flipwise.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    email: String,
    onVerifySuccess: () -> Unit,
    onBack: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val dimensions = FlipWiseDesign.dimensions
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isRegistration = userProfile.totpSecret == null
    
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Registration states
    var generatedSecret by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(isRegistration) {
        if (isRegistration && generatedSecret.isEmpty()) {
            val secret = TotpManager.generateSecretKey()
            generatedSecret = secret
            val uri = TotpManager.generateTotpUri(email, secret)
            qrBitmap = TotpManager.getQrCodeBitmap(uri)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostWhite)
    ) {
        // Decorative background elements
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isRegistration) CoralZest.copy(alpha = 0.15f) else GrapePop.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.paddingLarge)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.White, CircleShape).shadow(4.dp, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = NavyInk)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.size(dimensions.logoSize / 1.5f),
                shape = RoundedCornerShape(dimensions.cardCornerRadius / 1.5f),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isRegistration) Icons.Rounded.Security else Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint = if (isRegistration) CoralZest else GrapePop,
                        modifier = Modifier.size(dimensions.logoSize / 3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.paddingLarge))

            Text(
                text = if (isRegistration) "Setup Double Security" else "Identity Verification",
                fontSize = dimensions.headerFontSize,
                fontWeight = FontWeight.ExtraBold,
                color = NavyInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensions.paddingSmall))

            Text(
                text = if (isRegistration) 
                    "Scan the QR code below using Google or Microsoft Authenticator." 
                    else "Protecting your knowledge. Please enter your authentication code.",
                textAlign = TextAlign.Center,
                color = NavyInk.copy(alpha = 0.6f),
                fontSize = dimensions.bodyFontSize,
                lineHeight = 26.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (isRegistration && qrBitmap != null) {
                Spacer(modifier = Modifier.height(dimensions.paddingLarge))
                
                Surface(
                    modifier = Modifier
                        .size(if (dimensions.isTablet) 320.dp else 260.dp)
                        .shadow(12.dp, RoundedCornerShape(dimensions.cardCornerRadius))
                        .background(Color.White)
                        .padding(12.dp),
                    shape = RoundedCornerShape(dimensions.cardCornerRadius),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Registration QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = NavyInk.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "SECRET: $generatedSecret",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = NavyInk.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.paddingLarge))

            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                modifier = Modifier.width(if (dimensions.isTablet) 400.dp else 300.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 10.sp,
                    color = if (isRegistration) CoralZest else GrapePop
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(dimensions.cardCornerRadius / 2f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isRegistration) CoralZest else GrapePop,
                    unfocusedBorderColor = NavyInk.copy(alpha = 0.1f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                placeholder = {
                    Text(
                        "000000",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = NavyInk.copy(alpha = 0.1f),
                        letterSpacing = 10.sp
                    )
                }
            )

            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = error ?: "",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(dimensions.paddingLarge))

            Button(
                onClick = {
                    if (otpCode.length == 6) {
                        isLoading = true
                        error = null
                        scope.launch {
                            val secretToUse = if (isRegistration) generatedSecret else userProfile.totpSecret
                            
                            if (secretToUse != null && TotpManager.verifyOtp(secretToUse, otpCode)) {
                                if (isRegistration) {
                                    profileViewModel.updateTotpSecret(generatedSecret)
                                }
                                delay(800)
                                onVerifySuccess()
                            } else {
                                error = "Verification failed. Check your app."
                                isLoading = false
                                otpCode = "" // Clear on error
                            }
                        }
                    } else {
                        error = "Enter 6 digits"
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight)
                    .shadow(8.dp, RoundedCornerShape(dimensions.cardCornerRadius)),
                shape = RoundedCornerShape(dimensions.cardCornerRadius),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                if (isRegistration) listOf(CoralZest, Color(0xFFF43F5E), Color(0xFFE11D48))
                                else listOf(GrapePop, Color(0xFF8B5CF6), Color(0xFF7C3AED))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isRegistration) "Activate Security" else "Unlock FlipWise", 
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 18.sp
                            )
                            Spacer(Modifier.width(12.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
