package com.flipwise.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flipwise.app.data.model.*
import com.flipwise.app.data.repository.FlipWiseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FlipWiseRepository(application)

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
        return repository.signIn(email, password)
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return repository.signUp(email, password)
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
            
            // If the user is new or hasn't set a username (default is "flipper"), 
            // we use their name/email prefix as a default to avoid forcing them to the nickname screen
            val finalUsername = if (current.username == "flipper" || current.username.isBlank()) {
                finalDisplayName.lowercase().replace(" ", "_") + (100..999).random()
            } else {
                current.username
            }
            
            repository.updateProfile(
                current.copy(
                    id = repository.userId,
                    displayName = finalDisplayName,
                    username = finalUsername
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
        repository.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> {
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
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch { 
            com.flipwise.app.data.database.AppDatabase.getDatabase(getApplication()).friendDao().deleteFriend(friendId) 
        }
    }

    fun addChallenge(challenge: Challenge) {
        viewModelScope.launch {
            repository.addChallenge(challenge)
        }
    }
}
