package com.flipwise.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flipwise.app.data.database.AppDatabase
import com.flipwise.app.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class DeckViewModel(application: Application) : AndroidViewModel(application) {
    private val db             = AppDatabase.getDatabase(application)
    private val deckDao        = db.deckDao()
    private val flashcardDao   = db.flashcardDao()
    private val sessionDao     = db.studySessionDao()
    private val achievementDao = db.achievementDao()

    val decks: Flow<List<Deck>>               = deckDao.getAllDecks()
    val achievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()
    val sessions: Flow<List<StudySession>>    = sessionDao.getAllSessions()

    private val _userProgress = MutableStateFlow(UserProgress())
    val userProgress: StateFlow<UserProgress> = _userProgress.asStateFlow()

    init {
        initAchievements()
        loadUserProgress()
    }

    // ─── Achievements ────────────────────────────────────────────

    private fun initAchievements() {
        viewModelScope.launch {
            val existing = achievementDao.getAllAchievementsOnce()
            if (existing.isEmpty()) {
                listOf(
                    Achievement("first_steps",  "First Steps",  "Study 5 flashcards",         "🌱", category = "Cards"),
                    Achievement("centurion",    "Centurion",    "Study 100 flashcards",        "💯", category = "Cards"),
                    Achievement("scholar",      "Scholar",      "Study 500 flashcards",        "🎓", category = "Cards"),
                    Achievement("week_warrior", "Week Warrior", "Maintain a 7-day streak",     "🔥", category = "Streaks"),
                    Achievement("month_master", "Month Master", "Maintain a 30-day streak",    "🏆", category = "Streaks"),
                    Achievement("speed_demon",  "Speed Demon",  "Study 20+ cards in a session","⚡", category = "Points")
                ).forEach { achievementDao.insertAchievement(it) }
            }
        }
    }

    private suspend fun checkAchievements(cardsStudiedInSession: Int) {
        val all      = achievementDao.getAllAchievementsOnce()
        val progress = _userProgress.value

        suspend fun unlock(id: String) {
            val a = all.find { it.id == id }
            if (a != null && !a.isUnlocked) {
                achievementDao.updateAchievement(
                    a.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis())
                )
            }
        }

        if (progress.totalCardsStudied >= 5)   unlock("first_steps")
        if (progress.totalCardsStudied >= 100)  unlock("centurion")
        if (progress.totalCardsStudied >= 500)  unlock("scholar")
        if (progress.currentStreak >= 7)        unlock("week_warrior")
        if (progress.currentStreak >= 30)       unlock("month_master")
        if (cardsStudiedInSession >= 20)        unlock("speed_demon")
    }

    // ─── Progress ────────────────────────────────────────────────

    private fun loadUserProgress() {
        viewModelScope.launch {
            sessionDao.getAllSessions().collect { sessions ->
                _userProgress.value = UserProgress(
                    totalPoints       = sessions.sumOf { it.pointsEarned },
                    totalCardsStudied = sessions.sumOf { it.cardsStudied },
                    currentStreak     = calculateStreak(sessions),
                    longestStreak     = calculateLongestStreak(sessions)
                )
            }
        }
    }

    // ─── Decks ───────────────────────────────────────────────────

    fun getCardsForDeck(deckId: String) = flashcardDao.getCardsByDeck(deckId)

    fun createDeck(name: String, color: String, icon: String) {
        viewModelScope.launch {
            deckDao.insertDeck(
                Deck(id = UUID.randomUUID().toString(), name = name, color = color, icon = icon)
            )
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            flashcardDao.deleteCardsByDeck(deckId)
            deckDao.deleteDeckById(deckId)
        }
    }

    // ─── Cards ───────────────────────────────────────────────────

    fun addFlashcard(deckId: String, front: String, back: String) {
        viewModelScope.launch {
            flashcardDao.insertCard(
                Flashcard(id = UUID.randomUUID().toString(), deckId = deckId, front = front, back = back)
            )
            val count = flashcardDao.getCardCountForDeck(deckId)
            deckDao.getDeckById(deckId)?.let { deckDao.updateDeck(it.copy(cardCount = count)) }
        }
    }

    fun deleteCard(card: Flashcard) {
        viewModelScope.launch {
            flashcardDao.deleteCard(card)
            val count = flashcardDao.getCardCountForDeck(card.deckId)
            deckDao.getDeckById(card.deckId)?.let { deckDao.updateDeck(it.copy(cardCount = count)) }
        }
    }

    // ─── Study Session ───────────────────────────────────────────

    fun saveStudySession(deckId: String, cardsStudied: Int, correctCount: Int) {
        val points = correctCount * 10 + (cardsStudied - correctCount) * 5
        viewModelScope.launch {
            sessionDao.insertSession(
                StudySession(
                    deckId       = deckId,
                    cardsStudied = cardsStudied,
                    correctCount = correctCount,
                    pointsEarned = points
                )
            )
            deckDao.getDeckById(deckId)?.let {
                deckDao.updateDeck(it.copy(lastStudied = System.currentTimeMillis()))
            }
            checkAchievements(cardsStudied)
        }
    }

    // ─── Settings ────────────────────────────────────────────────

    fun clearAllData() {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                db.clearAllTables()
            }
            initAchievements()
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private fun calculateStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        val today = System.currentTimeMillis() / 86400000
        val days  = sessions.map { it.date / 86400000 }.sortedDescending().distinct()
        if (days.first() < today - 1) return 0
        var streak = 1
        for (i in 1 until days.size) {
            if (days[i - 1] - days[i] == 1L) streak++ else break
        }
        return streak
    }

    private fun calculateLongestStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        val days    = sessions.map { it.date / 86400000 }.sorted().distinct()
        var longest = 1
        var current = 1
        for (i in 1 until days.size) {
            if (days[i] - days[i - 1] == 1L) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }
}