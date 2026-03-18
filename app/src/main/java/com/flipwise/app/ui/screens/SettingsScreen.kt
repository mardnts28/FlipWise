package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.ui.theme.*
import com.flipwise.app.viewmodel.DeckViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: DeckViewModel = viewModel()
) {
    var notificationsOn by remember { mutableStateOf(true) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GrapePop
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GhostWhite)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // My Profile
            SettingsCard(
                icon = Icons.Rounded.Person,
                iconContainerColor = GrapePop.copy(alpha = 0.1f),
                iconColor = GrapePop,
                title = "My Profile",
                description = "View profile, friends & challenges",
                onClick = onNavigateToProfile
            )

            // Notifications
            SettingsCard(
                icon = Icons.Rounded.Notifications,
                iconContainerColor = GrapePop.copy(alpha = 0.1f),
                iconColor = GrapePop,
                title = "Notifications",
                description = "Daily study reminders",
                trailingContent = {
                    Switch(
                        checked = notificationsOn,
                        onCheckedChange = { notificationsOn = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MintGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            )

            // About FlipWise
            SettingsCard(
                icon = Icons.Rounded.Info,
                iconContainerColor = CoralZest.copy(alpha = 0.1f),
                iconColor = CoralZest,
                title = "About FlipWise",
                description = "Version 1.0.0",
                content = {
                    Text(
                        text = "Master your knowledge, one flip at a time. FlipWise helps you learn effectively with spaced repetition and smart study techniques.",
                        fontSize = 14.sp,
                        color = NavyInk.copy(alpha = 0.6f),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            )

            // Logout
            SettingsCard(
                icon = Icons.AutoMirrored.Rounded.Logout,
                iconContainerColor = GrapePop.copy(alpha = 0.1f),
                iconColor = GrapePop,
                title = "Logout",
                description = "Sign out of your account",
                onClick = onLogout
            )

            // Danger Zone
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        text = "Danger Zone",
                        color = CherryRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "These actions cannot be undone",
                        color = NavyInk.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
                
                HorizontalDivider(color = GhostWhite, thickness = 1.dp)

                DangerActionRow(
                    icon = Icons.AutoMirrored.Rounded.ExitToApp,
                    title = "Reset Onboarding",
                    description = "See the welcome tutorial again",
                    onClick = { /* Reset onboarding logic */ }
                )

                HorizontalDivider(color = GhostWhite, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))

                DangerActionRow(
                    icon = Icons.Rounded.Delete,
                    iconColor = CherryRed,
                    title = "Clear All Data",
                    description = "Delete all decks, cards, and progress",
                    onClick = { showClearConfirm = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Made with ❤️ for learners everywhere",
                color = NavyInk.copy(alpha = 0.5f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete everything. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CherryRed)
                ) { Text("Clear Everything") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCard(
    icon: ImageVector,
    iconContainerColor: Color,
    iconColor: Color,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconContainerColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NavyInk
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = NavyInk.copy(alpha = 0.6f)
                    )
                }

                if (trailingContent != null) {
                    trailingContent()
                }
            }
            if (content != null) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DangerActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    iconColor: Color = HoneyGold,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyInk
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = NavyInk.copy(alpha = 0.6f)
                )
            }
        }
    }
}