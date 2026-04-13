package com.flipwise.app.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey val id: String,
    val name: String,
    val subject: String = "",
    val color: String,
    val icon: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastStudied: Long? = null,
    val cardCount: Int = 0,
    val masteredCount: Int = 0
) : Parcelable

@Parcelize
@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    val options: String? = null, // Pipe-separated choices
    val difficulty: String = "medium",
    val nextReview: Long = System.currentTimeMillis(),
    val reviews: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: String,
    val date: Long = System.currentTimeMillis(),
    val cardsStudied: Int,
    val correctCount: Int,
    val pointsEarned: Int
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String = "All",
    val unlockedAt: Long? = null,
    val isUnlocked: Boolean = false,
    val tier: String = "bronze" // "bronze", "silver", "gold"
)

data class UserProgress(
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCardsStudied: Int = 0,
    val lastStudyDate: Long? = null
)
@Parcelize
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "local_user",
    val username: String = "flipper",
    val displayName: String = "FlipWise User",
    val avatar: String = "🎓",
    val bio: String = "Learning every day!",
    val joinedAt: Long = System.currentTimeMillis(),
    val level: Int = 1,
    val xp: Int = 0,
    val totalPoints: Int = 0,
    val badges: String = "",
    val role: String = "standard",
    val totpSecret: String? = null
) : Parcelable

@Parcelize
@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val displayName: String,
    val avatar: String,
    val status: String,
    val addedAt: Long,
    val totalPoints: Int,
    val currentStreak: Int,
    val totalCardsStudied: Int
) : Parcelable

@Parcelize
@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val type: String, // "versus" or "team"
    val subType: String = "1v1", // "1v1" or "team_vs_team"
    val goal: Int = 0,
    val goalType: String = "Score",
    val timeLimit: Int = 300, // Seconds, default 5 mins
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + 86400000,
    val status: String = "active",
    val createdBy: String = "local_user",
    val participants: String = "local_user", // Comma-separated user IDs
    val deckIds: String = "" // Comma-separated deck IDs
) : Parcelable

sealed class AiGenerationState {
    object Idle : AiGenerationState()
    data class Loading(val message: String) : AiGenerationState()
    data class Success(val cardsGenerated: Int) : AiGenerationState()
    data class Error(val message: String) : AiGenerationState()
}
