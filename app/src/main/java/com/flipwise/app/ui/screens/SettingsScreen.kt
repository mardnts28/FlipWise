package com.flipwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.flipwise.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    viewModel: DeckViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    var notificationsOn by remember { mutableStateOf(true) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }
    val profile by profileViewModel.userProfile.collectAsState()
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

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
            if (errorMessage != null) {
                Surface(
                    color = CherryRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().clickable { errorMessage = null }
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Error, contentDescription = null, tint = CherryRed)
                        Spacer(Modifier.width(12.dp))
                        Text(errorMessage!!, color = CherryRed, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.Close, contentDescription = null, tint = CherryRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

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
                        onCheckedChange = {
                            notificationsOn = it
                            if (it) {
                                com.flipwise.app.data.worker.StudyReminderWorker.schedule(context)
                            } else {
                                com.flipwise.app.data.worker.StudyReminderWorker.cancel(context)
                            }
                        },
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
                description = "Version 1.1.0 (Advanced)",
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
                    onClick = { 
                        viewModel.clearAllData()
                        onBack()
                    }
                )

                HorizontalDivider(color = GhostWhite, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))

                DangerActionRow(
                    icon = Icons.Rounded.DeleteSweep,
                    iconColor = CherryRed,
                    title = "Clear All Data",
                    description = "Delete all decks, cards, and progress",
                    onClick = { showClearConfirm = true }
                )

                HorizontalDivider(color = GhostWhite, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))

                DangerActionRow(
                    icon = Icons.Rounded.NoAccounts,
                    iconColor = CherryRed,
                    title = "Delete Account",
                    description = "Permanently remove your account and data",
                    onClick = { 
                        deleteConfirmText = ""
                        showDeleteConfirm = true 
                    }
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
            text = { Text("This will permanently delete everything from your account. This cannot be undone.") },
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

    if (showDeleteConfirm) {
        val requiredText = "delete ${profile.username}"
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account permanently?") },
            text = { 
                Column {
                    Text("This will permanently remove your account, profile, decks, and all study progress across all devices.")
                    Spacer(Modifier.height(16.dp))
                    Text("To confirm, type the following:", fontWeight = FontWeight.Bold)
                    Text(requiredText, modifier = Modifier.padding(vertical = 4.dp), color = CherryRed)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deleteConfirmText,
                        onValueChange = { deleteConfirmText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type here") },
                        isError = deleteConfirmText.isNotBlank() && deleteConfirmText != requiredText
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val result = profileViewModel.deleteAccount()
                            if (result.isSuccess) {
                                showDeleteConfirm = false
                                onAccountDeleted()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete account. You may need to log in again for verification."
                                showDeleteConfirm = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CherryRed),
                    enabled = deleteConfirmText == requiredText
                ) { Text("Delete Account") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
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