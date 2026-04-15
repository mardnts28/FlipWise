package com.flipwise.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Groups
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipwise.app.data.model.StudySession
import com.flipwise.app.data.model.Friend
import com.flipwise.app.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChallengeGame: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.userProfile.collectAsState()
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    val decks by viewModel.decks.collectAsState(initial = emptyList())
    val challenges by viewModel.challenges.collectAsState(initial = emptyList())
    val recentSessions by viewModel.recentSessions.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddFriend by remember { mutableStateOf(false) }
    var showAddChallenge by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var friendMessage by remember { mutableStateOf<String?>(null) }

    // Show a snackbar for friend add feedback
    LaunchedEffect(friendMessage) {
        friendMessage?.let {
            kotlinx.coroutines.delay(3000)
            friendMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFF))
            .verticalScroll(rememberScrollState())
    ) {
        // --- Header ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF7C3AED),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    IconButton(onClick = { showEditProfile = true }) {
                        Icon(Icons.Default.EditNote, contentDescription = "Edit", tint = Color.White)
                    }
                }
                
                // Avatar
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(profile.avatar, fontSize = 64.sp)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Text(profile.displayName, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("@${profile.username}", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                Text(profile.bio, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.padding(top = 8.dp))
                
                Spacer(Modifier.height(24.dp))
                
                // XP Bar
                val xpInCurrentLevel = profile.xp % 500
                val progress = xpInCurrentLevel / 500f
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Level ${profile.level}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("$xpInCurrentLevel / 500 XP", color = Color.White)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ProfileStatItem(profile.totalPoints.toString(), "Points")
                    ProfileStatItem(friends.size.toString(), "Friends")
                    ProfileStatItem(if(profile.badges.isEmpty()) "0" else profile.badges.split(",").size.toString(), "Badges")
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // --- Tabs ---
        Surface(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                TabButton(
                    text = "Activity",
                    icon = Icons.Default.History,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Friends",
                    icon = Icons.Default.People,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Challenges",
                    icon = Icons.Rounded.Groups,
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- Tab Content ---
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            when (selectedTab) {
                0 -> {
                    Text("Recent Activity", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    Spacer(Modifier.height(16.dp))
                    if (recentSessions.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.History,
                            title = "No activity yet",
                            description = "Start studying to earn points and XP!"
                        )
                    } else {
                        recentSessions.forEach { session ->
                            RecentActivityItem(session)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
                1 -> {
                    val pendingRequests = friends.filter { it.status == "pending" }
                    val activeFriends = friends.filter { it.status == "accepted" }
                    val sentRequests = friends.filter { it.status == "sent" }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Friends", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        Button(
                            onClick = { showAddFriend = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Friend")
                        }
                    }
                    
                    if (pendingRequests.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        Text("Pending Requests", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        Spacer(Modifier.height(12.dp))
                        pendingRequests.forEach { request ->
                            FriendRequestItem(
                                request = request,
                                onAccept = { viewModel.acceptFriendRequest(request) },
                                onDecline = { viewModel.declineFriendRequest(request.id) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("My Friends (${activeFriends.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    Spacer(Modifier.height(12.dp))
                    
                    if (activeFriends.isEmpty() && sentRequests.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.PeopleOutline,
                            title = "No friends yet",
                            description = "Add friends to compete together!"
                        )
                    } else {
                        activeFriends.forEach { friend ->
                            FriendItem(friend = friend, onDelete = { viewModel.removeFriend(friend.id) })
                            Spacer(Modifier.height(12.dp))
                        }
                        
                        if (sentRequests.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text("Sent Requests", fontSize = 14.sp, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            sentRequests.forEach { friend ->
                                FriendItem(friend = friend, onDelete = { viewModel.removeFriend(friend.id) })
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
                2 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("My Challenges", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        Button(
                            onClick = { showAddChallenge = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                        ) {
                            Icon(Icons.Rounded.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("New Challenge")
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    if (challenges.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.Groups,
                            title = "No active challenges",
                            description = "Start a group battle or versus with friends!"
                        )
                    } else {
                        challenges.forEach { challenge ->
                            val decksInChallenge = decks.filter { challenge.deckIds.contains(it.id) }
                            ProfileChallengeItem(
                                challenge = challenge, 
                                decks = decksInChallenge,
                                onClick = { onNavigateToChallengeGame(challenge.id) }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                    }
                }
            }
            Spacer(Modifier.height(100.dp))
        }

        if (showEditProfile) {
            EditProfileDialog(
                profile = profile,
                onDismiss = { showEditProfile = false },
                onSave = { updatedProfile ->
                    scope.launch {
                        viewModel.updateProfile(
                            updatedProfile.displayName,
                            updatedProfile.username,
                            updatedProfile.bio,
                            updatedProfile.avatar
                        )
                    }
                    showEditProfile = false
                }
            )
        }

        if (showAddChallenge) {
            com.flipwise.app.ui.components.CreateChallengeDialog(
                friends = friends,
                decks = decks,
                onDismiss = { showAddChallenge = false },
                onCreate = { challenge ->
                    viewModel.addChallenge(challenge)
                    showAddChallenge = false
                    onNavigateToChallengeGame(challenge.id)
                }
            )
        }

        if (showAddFriend) {
            AddFriendStyledDialog(
                onDismiss = { showAddFriend = false },
                onAdd = { username ->
                    scope.launch {
                        val result = viewModel.addFriend(username)
                        if (result.isSuccess) {
                            friendMessage = "Friend request sent!"
                        } else {
                            friendMessage = result.exceptionOrNull()?.message ?: "Failed to add friend"
                        }
                    }
                    showAddFriend = false
                }
            )
        }

        // Friend feedback message banner
        friendMessage?.let { msg ->
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (msg.contains("sent")) Color(0xFF10B981) else Color(0xFFEF4444),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }


@Composable
fun RecentActivityItem(session: StudySession) {
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF7C3AED).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF7C3AED))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Study Session", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Text(sdf.format(Date(session.date)), fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("+${session.pointsEarned} pts", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Text("${session.correctCount}/${session.cardsStudied} correct", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    profile: com.flipwise.app.data.model.UserProfile,
    onDismiss: () -> Unit,
    onSave: (com.flipwise.app.data.model.UserProfile) -> Unit
) {
    var displayName by remember { mutableStateOf(profile.displayName) }
    var username by remember { mutableStateOf(profile.username) }
    var bio by remember { mutableStateOf(profile.bio) }
    var selectedAvatar by remember { mutableStateOf(profile.avatar) }

    val avatars = listOf("👤", "😎", "😜", "🎓", "📚", "🧠", "💡", "🚀", "⭐", "✨", "🔥", "💪")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Avatar", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B), fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                
                // Avatar Selection Grid
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        avatars.take(6).forEach { avatar ->
                            AvatarOptionItem(avatar, selectedAvatar == avatar) { selectedAvatar = avatar }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        avatars.drop(6).forEach { avatar ->
                            AvatarOptionItem(avatar, selectedAvatar == avatar) { selectedAvatar = avatar }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                ProfileEditField(label = "Display Name", value = displayName, onValueChange = { displayName = it })
                Spacer(Modifier.height(16.dp))
                ProfileEditField(label = "Username", value = username, onValueChange = { username = it })
                Spacer(Modifier.height(16.dp))
                
                Text("Bio", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B), fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text("Cancel", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { 
                            onSave(profile.copy(
                                displayName = displayName, 
                                username = username, 
                                bio = bio, 
                                avatar = selectedAvatar
                            )) 
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarOptionItem(avatar: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFF7C3AED), CircleShape)
                else Modifier
            ),
        shape = CircleShape,
        color = if (isSelected) Color(0xFF7C3AED).copy(alpha = 0.1f) else Color(0xFFF3F4F6)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(avatar, fontSize = 24.sp)
        }
    }
}

@Composable
fun ProfileEditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B), fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7C3AED),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )
    }
}

@Composable
fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
fun TabButton(text: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFF7C3AED) else Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyStateView(icon: ImageVector, title: String, description: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(description, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}

@Composable
fun AddFriendStyledDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var username by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Add Friend",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Enter your friend's username to add them",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(24.dp))
                
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Username", color = Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text("Cancel", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onAdd(username) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        enabled = username.isNotBlank()
                    ) {
                        Text("Add Friend", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRequestItem(request: Friend, onAccept: () -> Unit, onDecline: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF5F3FF),
        border = BorderStroke(1.dp, Color(0xFFDDD6FE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(request.avatar, fontSize = 24.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.displayName, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Text("@${request.username}", fontSize = 12.sp, color = Color.Gray)
            }
            Row {
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier.background(Color(0xFF10B981), CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier.background(Color(0xFFEF4444), CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun FriendItem(friend: Friend, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (friend.status == "sent") Color.LightGray.copy(alpha = 0.1f) else Color(0xFF7C3AED).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(friend.avatar, fontSize = 24.sp, modifier = Modifier.then(if (friend.status == "sent") Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.5f)) else Modifier))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(friend.displayName, fontWeight = FontWeight.Bold, color = if (friend.status == "sent") Color.Gray else Color(0xFF1E1B4B))
                    if (friend.status == "sent") {
                        Spacer(Modifier.width(8.dp))
                        Surface(color = Color.LightGray.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                            Text("Pending", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
                Text("@${friend.username}", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    if (friend.status == "sent") Icons.Default.Close else Icons.Default.PersonRemove, 
                    contentDescription = "Remove Friend", 
                    tint = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun ProfileChallengeItem(
    challenge: com.flipwise.app.data.model.Challenge, 
    decks: List<com.flipwise.app.data.model.Deck>,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = try { Color(android.graphics.Color.parseColor(decks.firstOrNull()?.color ?: "#7C3AED")) } catch (e: Exception) { Color(0xFF7C3AED) },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(decks.firstOrNull()?.icon ?: "🎮", fontSize = 24.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(challenge.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E1B4B))
                Text(
                    text = if (challenge.subType == "1v1") "1 vs 1 Battle ⚔\uFE0F" else "Teams Clash \uD83D\uDCAA",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${challenge.timeLimit / 60}m", fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED), fontSize = 18.sp)
                Text("Limit", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
