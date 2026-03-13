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

    val userProfile: StateFlow<UserProfile> = profileDao.getUserProfile()
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.Lazily, UserProfile())

    val friends: Flow<List<Friend>>       = friendDao.getAllFriends()
    val challenges: Flow<List<Challenge>> = challengeDao.getAllChallenges()

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
            val current = profileDao.getUserProfile().firstOrNull() ?: UserProfile()
            profileDao.updateProfile(
                current.copy(displayName = displayName, username = username, bio = bio, avatar = avatar)
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