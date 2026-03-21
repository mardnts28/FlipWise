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

    val friends: Flow<List<Friend>> = repository.userId.let { _ ->
        // Assuming friends are local or fetched via repository. In the provided repository, 
        // it doesn't have friends flow, but ProfileViewModel used to have it.
        // Let's keep it consistent with what's needed.
        // For now, I'll use the DB directly if repository doesn't have it, 
        // but it's better to add to repository.
        com.flipwise.app.data.database.AppDatabase.getDatabase(application).friendDao().getAllFriends()
    }

    val challenges: Flow<List<Challenge>> = repository.getActiveChallenges()
    
    val recentSessions: Flow<List<StudySession>> = repository.sessions.map { it.take(5) }

    private val _leaderboard = MutableStateFlow<List<UserProfile>>(emptyList())
    val leaderboard: StateFlow<List<UserProfile>> = _leaderboard.asStateFlow()

    val isUserLoggedIn: Boolean 
        get() = repository.isUserLoggedIn()

    init {
        refreshLeaderboard()
    }

    fun refreshLeaderboard() {
        viewModelScope.launch {
            _leaderboard.value = repository.getLeaderboard()
        }
    }

    suspend fun updateProfile(displayName: String, username: String, bio: String, avatar: String) {
        val current = userProfile.value
        repository.updateProfile(
            current.copy(
                displayName = displayName, 
                username = username, 
                bio = bio, 
                avatar = avatar
            )
        )
    }

    suspend fun syncProfile(): UserProfile? {
        return repository.syncProfile()
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

    suspend fun loginOrRegister(name: String, email: String) {
        val current = repository.syncProfile() ?: userProfile.value
        val finalName = if (name.isNotBlank()) name else email.substringBefore("@")
        
        repository.updateProfile(
            current.copy(
                displayName = finalName,
                // We keep the old/default username so the navigation catches it and asks for a new one
            )
        )
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

    fun addFriend(username: String) {
        if (username.isBlank()) return
        viewModelScope.launch {
            com.flipwise.app.data.database.AppDatabase.getDatabase(getApplication()).friendDao().insertFriend(
                Friend(
                    id                = UUID.randomUUID().toString(),
                    userId            = repository.userId,
                    username          = username.trim(),
                    displayName       = username.trim(),
                    avatar            = "👤",
                    status            = "accepted",
                    addedAt           = System.currentTimeMillis(),
                    totalPoints       = (100..5000).random(),
                    currentStreak     = (0..30).random(),
                    totalCardsStudied = (10..500).random()
                )
            )
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
