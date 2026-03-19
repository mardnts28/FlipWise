package com.flipwise.app.data.database

import android.content.Context
import androidx.room.*
import com.flipwise.app.data.model.*

@Database(
    entities = [
        Deck::class,
        Flashcard::class,
        StudySession::class,
        Achievement::class,
        UserProfile::class,
        Friend::class,
        Challenge::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun profileDao(): ProfileDao
    abstract fun friendDao(): FriendDao
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flipwise_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
