package com.flipwise.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flipwise.app.data.database.AppDatabase
import com.flipwise.app.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val db           = AppDatabase.getDatabase(application)
    private val profileDao   = db.profileDao()
    private val friendDao    = db.friendDao()
    private val challengeDao = db.challengeDao()
    private val sessionDao   = db.studySessionDao()

    val userProfile: StateFlow<UserProfile> = profileDao.getUserProfile()
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    val friends: Flow<List<Friend>>       = friendDao.getAllFriends()
    val challenges: Flow<List<Challenge>> = challengeDao.getAllChallenges()
    val recentSessions: Flow<List<StudySession>> = sessionDao.getAllSessions().map { it.take(5) }

    init {
        viewModelScope.launch {
            val profile = profileDao.getUserProfile().firstOrNull()
            if (profile == null) {
                profileDao.insertProfile(UserProfile())
            }
        }
    }

    fun updateProfile(displayName: String, username: String, bio: String, avatar: String) {
        viewModelScope.launch {
            // Use current state to preserve fields like level, xp, etc.
            val current = userProfile.value
            profileDao.insertProfile(
                current.copy(
                    displayName = displayName, 
                    username = username, 
                    bio = bio, 
                    avatar = avatar
                )
            )
        }
    }

    fun loginOrRegister(name: String, email: String) {
        viewModelScope.launch {
            val current = userProfile.value
            val finalName = if (name.isNotBlank()) name else email.substringBefore("@")
            val finalUsername = email.substringBefore("@")
            
            profileDao.insertProfile(
                current.copy(
                    displayName = finalName,
                    username = finalUsername
                )
            )
        }
    }

    fun addFriend(username: String) {
        if (username.isBlank()) return
        viewModelScope.launch {
            friendDao.insertFriend(
                Friend(
                    id                = UUID.randomUUID().toString(),
                    userId            = UUID.randomUUID().toString(),
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
        viewModelScope.launch { friendDao.deleteFriend(friendId) }
    }
}
