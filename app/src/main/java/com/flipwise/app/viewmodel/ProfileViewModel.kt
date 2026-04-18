package com.flipwise.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flipwise.app.data.model.*
import com.flipwise.app.data.repository.FlipWiseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import com.flipwise.app.data.security.RateLimiter

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FlipWiseRepository(application)
    private val auditLogDao =
        com.flipwise.app.data.database.AppDatabase.getDatabase(application).auditLogDao()

    private val achievementDao =
        com.flipwise.app.data.database.AppDatabase.getDatabase(application).achievementDao()

    val achievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()

    private val _lastAcceptedFriend = MutableStateFlow<Friend?>(null)
    val lastAcceptedFriend: StateFlow<Friend?> = _lastAcceptedFriend.asStateFlow()

    private val _otherUser = MutableStateFlow<UserProfile?>(null)
    val otherUser: StateFlow<UserProfile?> = _otherUser.asStateFlow()

    fun clearAcceptedFriend() {
        _lastAcceptedFriend.value = null
    }

    fun fetchOtherUser(uid: String) {
        viewModelScope.launch {
            _otherUser.value = null
            try {
                _otherUser.value = repository.fetchPublicProfile(uid)
            } catch (e: Exception) {
                android.util.Log.e("VM_ERROR", "Failed to fetch other user: ${e.message}")
            }
        }
    }

    private fun logAction(action: String, details: String = "") {
        viewModelScope.launch {
            val uid = repository.userId ?: "anonymous"

            // Save locally to Room
            auditLogDao.log(
                com.flipwise.app.data.model.AuditLog(
                    userId = uid,
                    action = action,
                    details = details
                )
            )

            // Save to Realtime Database instead of Firestore
            try {
                val url = "https://flipwise-dc052-default-rtdb.asia-southeast1.firebasedatabase.app"
                com.google.firebase.database.FirebaseDatabase.getInstance(url)
                    .getReference("audit_logs")
                    .push()
                    .setValue(mapOf(
                        "userId" to uid,
                        "action" to action,
                        "details" to details,
                        "timestamp" to System.currentTimeMillis()
                    ))
            } catch (e: Exception) {
                android.util.Log.e("AUDIT_LOG", "Failed to save to Firebase: ${e.message}")
            }

            android.util.Log.d("AUDIT_LOG", "✅ Logged: $action | $details")
        }
    }

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    val friends: Flow<List<Friend>> = repository.getFriendsFlow()

    val decks: Flow<List<Deck>> = repository.allDecks

    val challenges: Flow<List<Challenge>> = repository.getActiveChallenges()
    
    val globalChallenges: Flow<List<Challenge>> = repository.getGlobalChallengesFlow()

    val recentSessions: Flow<List<StudySession>> = repository.sessions.map { it.take(5) }

    val allSessions: Flow<List<StudySession>> = repository.sessions

    val leaderboard: StateFlow<List<UserProfile>> = repository.getLeaderboardFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isUserLoggedIn: Boolean
        get() = repository.isUserLoggedIn()

    fun isGoogleUser(): Boolean = repository.isGoogleUser()

    suspend fun updateProfile(
        displayName: String,
        username: String,
        bio: String,
        avatar: String
    ): Result<Unit> {
        return try {
            val trimmedUsername = username.trim().lowercase()
            if (trimmedUsername != userProfile.value.username) {
                if (repository.isUsernameTaken(trimmedUsername)) {
                    return Result.failure(Exception("Username already taken"))
                }
            }

            val current = userProfile.value
            val profileToSave = current.copy(
                id = repository.userId,
                displayName = displayName,
                username = trimmedUsername,
                bio = bio,
                avatar = avatar
            )
            repository.updateProfile(profileToSave)
            logAction("PROFILE_UPDATED", "displayName=$displayName username=$username")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileData(profile: UserProfile) {
        repository.updateProfile(profile)
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        return repository.isUsernameTaken(username)
    }

    suspend fun syncProfile(): UserProfile? {
        return repository.syncProfile()
    }

    suspend fun fullSync(): UserProfile? {
        return repository.fullSync()
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        if (!RateLimiter.isAllowed("LOGIN", maxCount = 5, windowMs = 5 * 60 * 1000)) {
            return Result.failure(Exception("Too many login attempts. Please wait a few minutes."))
        }
        val result = repository.signIn(email, password)
        if (result.isSuccess) {
            logAction("LOGIN", "email=$email")
            RateLimiter.reset("LOGIN")
            repository.fullSync()
        }
        return result
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        // Max 3 sign up attempts per 10 minutes
        if (!RateLimiter.isAllowed("SIGN_UP", maxCount = 3, windowMs = 10 * 60 * 1000)) {
            return Result.failure(Exception("Too many sign up attempts. Please wait a few minutes."))
        }
        return repository.signUp(email, password).also {
            if (it.isSuccess) {
                logAction("SIGN_UP", "email=$email")
                RateLimiter.reset("SIGN_UP")
            }
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return repository.signInWithGoogle(idToken).also {
            if (it.isSuccess) repository.fullSync()
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return repository.sendPasswordResetEmail(email)
    }

    suspend fun resendVerificationEmail(email: String, password: String): Result<Unit> {
        return repository.resendVerificationEmail(email, password)
    }

    suspend fun loginOrRegister(name: String, email: String): Result<Unit> {
        return try {
            repository.fullSync()
            val current = repository.syncProfile() ?: userProfile.value
            val finalDisplayName = if (name.isNotBlank()) name else email.substringBefore("@")
            
            // We only update the display name if it's currently default.
            // We leave the username as is (usually "flipper" for new accounts)
            // so Navigation.kt can redirect Google users to the nickname entry screen.
            repository.updateProfile(
                current.copy(
                    id = repository.userId,
                    displayName = if (current.displayName == "FlipWise User") finalDisplayName else current.displayName
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, displayName: String) {
        val current = userProfile.value
        repository.updateProfile(
            current.copy(
                username = username,
                displayName = displayName
            )
        )
    }

    fun signOut() {
        logAction("LOGOUT")
        repository.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> {
        logAction("DELETE_ACCOUNT", "user requested account deletion")
        return repository.deleteAccount()
    }

    suspend fun addFriend(username: String): Result<Unit> {
        val trimmed = username.trim()
        if (trimmed.isBlank()) return Result.failure(Exception("Username cannot be empty"))
        
        return try {
            val targetProfile = repository.findUserByUsername(trimmed)
            if (targetProfile == null) {
                Result.failure(Exception("User \"$trimmed\" not found"))
            } else if (targetProfile.id == repository.userId) {
                Result.failure(Exception("You cannot add yourself as a friend"))
            } else {
                repository.addFriend(targetProfile.id, targetProfile)
                
                // Also send a notification to the target user
                try {
                    val url = "https://flipwise-dc052-default-rtdb.asia-southeast1.firebasedatabase.app"
                    val ref = com.google.firebase.database.FirebaseDatabase.getInstance(url)
                        .getReference("users/${targetProfile.id}/notifications")
                    ref.push().setValue(mapOf(
                        "id" to UUID.randomUUID().toString(),
                        "type" to "friend_request",
                        "title" to "New Friend Request",
                        "message" to "${userProfile.value.displayName} wants to be friends!",
                        "senderId" to repository.userId,
                        "timestamp" to System.currentTimeMillis(),
                        "read" to false
                    ))
                } catch (e: Exception) {
                    android.util.Log.e("FRIEND_NOTIF", "Failed to send notification: ${e.message}")
                }

                logAction("FRIEND_ADDED", "username=$trimmed")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add friend: ${e.message}"))
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            try {
                repository.declineFriendRequest(friendId)
            } catch (e: Exception) {
                // Log error instead of crashing
                android.util.Log.e("VM_ERROR", "Remove friend failed: ${e.message}")
            }
        }
    }

    fun acceptFriendRequest(friend: Friend) {
        viewModelScope.launch {
            try {
                repository.acceptFriendRequest(friend)
                _lastAcceptedFriend.value = friend
                logAction("FRIEND_ACCEPTED", "friendId=${friend.id} username=${friend.username}")
            } catch (e: Exception) {
                android.util.Log.e("VM_ERROR", "Accept friend request failed: ${e.message}")
            }
        }
    }

    fun declineFriendRequest(friendId: String) {
        viewModelScope.launch {
            try {
                repository.declineFriendRequest(friendId)
                logAction("FRIEND_REMOVED", "friendId=$friendId")
            } catch (e: Exception) {
                android.util.Log.e("VM_ERROR", "Decline friend failed: ${e.message}")
            }
        }
    }

    fun addChallenge(challenge: Challenge) {
        viewModelScope.launch {
            repository.addChallenge(challenge)
        }
    }

    fun joinGlobalChallenge(challengeId: String) {
        viewModelScope.launch {
            try {
                repository.joinChallenge(challengeId, userProfile.value)
                logAction("CHALLENGE_JOINED", "challengeId=$challengeId")
            } catch (e: Exception) {
                android.util.Log.e("VM_ERROR", "Join challenge failed: ${e.message}")
            }
        }
    }

    suspend fun updateTotpSecret(secret: String) {
        val current = userProfile.value
        repository.updateProfile(current.copy(totpSecret = secret))
    }

    val isTotpEnabled: Boolean
        get() = userProfile.value.totpSecret != null

    val currentUserEmail: String
        get() = repository.currentUserEmail

    val publicAnnouncement: Flow<Map<String, Any>?> = repository.getPublicAnnouncementFlow()
    
    val privateNotifications: Flow<List<Map<String, Any>>> = repository.getTargetedNotificationsFlow()

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }
}
