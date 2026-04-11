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
    private val auditLogDao = com.flipwise.app.data.database.AppDatabase.getDatabase(application).auditLogDao()

    private fun logAction(action: String, details: String = "") {
        viewModelScope.launch {
            val uid = repository.userId ?: "anonymous"

            // Save locally to Room
            auditLogDao.log(com.flipwise.app.data.model.AuditLog(
                userId = uid,
                action = action,
                details = details
            ))

            // Save to Firebase Firestore (cloud)
            try {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("audit_logs")
                    .add(mapOf(
                        "userId" to uid,
                        "action" to action,
                        "details" to details,
                        "timestamp" to System.currentTimeMillis()
                    ))
            } catch (e: Exception) {
                android.util.Log.e("AUDIT_LOG", "Failed to save to Firestore: ${e.message}")
            }

            android.util.Log.d("AUDIT_LOG", "✅ Logged: $action | $details")
        }
    }

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    val friends: Flow<List<Friend>> = repository.getFriendsFlow()

    val challenges: Flow<List<Challenge>> = repository.getActiveChallenges()
    
    val recentSessions: Flow<List<StudySession>> = repository.sessions.map { it.take(5) }

    val leaderboard: StateFlow<List<UserProfile>> = repository.getLeaderboardFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isUserLoggedIn: Boolean 
        get() = repository.isUserLoggedIn()

    suspend fun updateProfile(displayName: String, username: String, bio: String, avatar: String): Result<Unit> {
        return try {
            val current = userProfile.value
            val profileToSave = current.copy(
                id = repository.userId,
                displayName = displayName, 
                username = username, 
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

    suspend fun syncProfile(): UserProfile? {
        return repository.syncProfile()
    }

    suspend fun fullSync(): UserProfile? {
        return repository.fullSync()
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        // rate limiting (new)
        if (!RateLimiter.isAllowed("LOGIN", maxCount = 5, windowMs = 5 * 60 * 1000)) {
            return Result.failure(Exception("Too many login attempts. Please wait a few minutes."))
        }
        return repository.signIn(email, password).also {
            if (it.isSuccess) {
                logAction("LOGIN", "email=$email")  // audit log (from before)
                RateLimiter.reset("LOGIN")           // rate limiting (new)
            }
        }
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
        return repository.signInWithGoogle(idToken)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return repository.sendPasswordResetEmail(email)
    }

    suspend fun resendVerificationEmail(email: String, password: String): Result<Unit> {
        return repository.resendVerificationEmail(email, password)
    }

    suspend fun loginOrRegister(name: String, email: String): Result<Unit> {
        return try {
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

    fun addFriend(username: String) {
        val trimmed = username.trim()
        if (trimmed.isBlank()) return
        
        viewModelScope.launch {
            val targetProfile = repository.findUserByUsername(trimmed)
            if (targetProfile != null && targetProfile.id != repository.userId) {
                repository.addFriend(targetProfile.id, targetProfile)
            }
        }
        logAction("FRIEND_ADDED", "username=$trimmed")
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch { 
            com.flipwise.app.data.database.AppDatabase.getDatabase(getApplication()).friendDao().deleteFriend(friendId) 
        }
        logAction("FRIEND_REMOVED", "friendId=$friendId")
    }

    fun addChallenge(challenge: Challenge) {
        viewModelScope.launch {
            repository.addChallenge(challenge)
        }
    }
}
