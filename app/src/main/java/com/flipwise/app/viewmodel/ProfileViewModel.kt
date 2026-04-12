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

    val decks: Flow<List<Deck>> = repository.allDecks

    val challenges: Flow<List<Challenge>> = repository.getActiveChallenges()
    
    val recentSessions: Flow<List<StudySession>> = repository.sessions.map { it.take(5) }

    val leaderboard: StateFlow<List<UserProfile>> = repository.getLeaderboardFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isUserLoggedIn: Boolean 
        get() = repository.isUserLoggedIn()

    fun isGoogleUser(): Boolean = repository.isGoogleUser()

    suspend fun updateProfile(displayName: String, username: String, bio: String, avatar: String): Result<Unit> {
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
            repository.declineFriendRequest(friendId)
        }
    }

    fun acceptFriendRequest(friend: Friend) {
        viewModelScope.launch {
            repository.acceptFriendRequest(friend)
        }
    }

    fun declineFriendRequest(friendId: String) {
        viewModelScope.launch {
            repository.declineFriendRequest(friendId)
        }
    }

    fun addChallenge(challenge: Challenge) {
        viewModelScope.launch {
            repository.addChallenge(challenge)
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
}
