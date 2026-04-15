package com.flipwise.app.data.database

import android.content.Context
import androidx.room.*
import com.flipwise.app.data.model.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory


@Database(
    entities = [
        Deck::class,
        Flashcard::class,
        StudySession::class,
        Achievement::class,
        UserProfile::class,
        Friend::class,
        Challenge::class,
        AuditLog::class
    ],
    version = 11,
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
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null



        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = SQLiteDatabase.getBytes("flipwise-secure-v1-key".toCharArray())
                val factory = SupportFactory(passphrase)

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flipwise_database"
                )
                    .openHelperFactory(factory)

                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
