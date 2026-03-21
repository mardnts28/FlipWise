package com.flipwise.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flipwise.app.data.ai.FileTextExtractor
import com.flipwise.app.data.ai.GeminiFlashcardGenerator
import com.flipwise.app.data.database.AppDatabase
import com.flipwise.app.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

import com.flipwise.app.data.repository.FlipWiseRepository
import com.flipwise.app.data.ai.GeminiStudyCoach
import com.flipwise.app.data.ai.AiInsight

class DeckViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FlipWiseRepository(application)
    private val db = AppDatabase.getDatabase(application)
    private val deckDao = db.deckDao()
    private val cardDao = db.flashcardDao()
    private val achievementDao = db.achievementDao()
    private val sessionDao = db.studySessionDao()

    val decks: Flow<List<Deck>>               = repository.allDecks
    val achievements: Flow<List<Achievement>> = AppDatabase.getDatabase(application).achievementDao().getAllAchievements()
    val sessions: Flow<List<StudySession>>    = repository.sessions

    private val _userProgress = MutableStateFlow(UserProgress())
    val userProgress: StateFlow<UserProgress> = _userProgress.asStateFlow()

    // AI Generation State
    private val _aiGenerationState = MutableStateFlow<AiGenerationState>(AiGenerationState.Idle)
    val aiGenerationState: StateFlow<AiGenerationState> = _aiGenerationState.asStateFlow()

    private val flashcardGenerator = GeminiFlashcardGenerator()
    private val fileTextExtractor = FileTextExtractor(application)
    private val studyCoach = GeminiStudyCoach()

    private val _aiInsight = MutableStateFlow<AiInsight?>(null)
    val aiInsight: StateFlow<AiInsight?> = _aiInsight.asStateFlow()

    init {
        initAchievements()
        loadUserProgress()
        
        // Refresh AI insight when sessions or progress change
        viewModelScope.launch {
            combine(userProgress, sessions) { p, s -> p to s }
                .distinctUntilChanged()
                .debounce(2000) // Avoid too many API calls
                .collect { (p, s) ->
                    if (s.isNotEmpty()) {
                        refreshAiInsight(p, s)
                    }
                }
        }
    }

    // ─── Achievements ────────────────────────────────────────────

    private fun initAchievements() {
        viewModelScope.launch {
            val existing = achievementDao.getAllAchievementsOnce()
            
            // Check if we need to update (if count is different or we want to force refresh)
            // To ensure the user gets the new list immediately, we'll refresh if the count isn't what we expect
            if (existing.size < 25) { 
                achievementDao.deleteAllAchievements()
                val list = mutableListOf<Achievement>()
                
                // Streaks
                list.add(Achievement("streak_3", "3-Day Streak", "Maintain a 3-day study streak", "🔥", "Streaks"))
                list.add(Achievement("streak_7", "Week Warrior", "Maintain a 7-day study streak", "🔥", "Streaks"))
                list.add(Achievement("streak_14", "Fortnight Fighter", "Maintain a 14-day study streak", "🔥", "Streaks"))
                list.add(Achievement("streak_21", "21-Day Habit", "Maintain a 21-day study streak", "🔥", "Streaks"))
                list.add(Achievement("streak_30", "Month Master", "Maintain a 30-day study streak", "🏆", "Streaks"))
                list.add(Achievement("streak_90", "Season Scholar", "Maintain a 90-day study streak", "🏆", "Streaks"))
                list.add(Achievement("streak_210", "Grit Greatness", "Maintain a 210-day study streak", "🏆", "Streaks"))
                list.add(Achievement("streak_365", "Year Legend", "Maintain a 365-day study streak", "👑", "Streaks"))

                // Cards
                list.add(Achievement("cards_10", "First Steps", "Study 10 flashcards", "🌱", "Cards"))
                list.add(Achievement("cards_30", "Getting Started", "Study 30 flashcards", "🌱", "Cards"))
                list.add(Achievement("cards_50", "Steady Learner", "Study 50 flashcards", "🌿", "Cards"))
                list.add(Achievement("cards_150", "Centurion", "Study 150 flashcards", "💯", "Cards"))
                list.add(Achievement("cards_250", "Knowledge Seeker", "Study 250 flashcards", "🎓", "Cards"))
                list.add(Achievement("cards_500", "Scholar", "Study 500 flashcards", "🎓", "Cards"))

                // Mastery
                list.add(Achievement("mastery_10", "Novice Master", "Master 10 cards", "⭐", "Mastery"))
                list.add(Achievement("mastery_30", "Adept Master", "Master 30 cards", "⭐", "Mastery"))
                list.add(Achievement("mastery_50", "Skillful Master", "Master 50 cards", "🌟", "Mastery"))
                list.add(Achievement("mastery_100", "Elite Master", "Master 100 cards", "🌟", "Mastery"))
                list.add(Achievement("mastery_150", "Grand Master", "Master 150 cards", "✨", "Mastery"))
                list.add(Achievement("mastery_250", "Zen Master", "Master 250 cards", "✨", "Mastery"))
                list.add(Achievement("mastery_500", "Ultimate Master", "Master 500 cards", "👑", "Mastery"))

                // Points (renamed to complete as requested)
                list.add(Achievement("points_10", "Beginner Complete", "Earn points from 10 cards", "💎", "Points"))
                list.add(Achievement("points_30", "Novice Complete", "Earn points from 30 cards", "💎", "Points"))
                list.add(Achievement("points_50", "Intermediate Complete", "Earn points from 50 cards", "💎", "Points"))
                list.add(Achievement("points_100", "Advanced Complete", "Earn points from 100 cards", "💎", "Points"))
                list.add(Achievement("points_150", "Expert Complete", "Earn points from 150 cards", "💎", "Points"))
                list.add(Achievement("points_250", "Master Complete", "Earn points from 250 cards", "💎", "Points"))
                list.add(Achievement("points_500", "Legendary Complete", "Earn points from 500 cards", "💎", "Points"))

                achievementDao.insertAchievements(list)
            }
        }
    }

    private suspend fun checkAchievements(updatedProgress: UserProgress) {
        val all      = achievementDao.getAllAchievementsOnce()
        val progress = updatedProgress
        val totalMastered = decks.first().sumOf { it.masteredCount }

        suspend fun unlock(id: String) {
            val a = all.find { it.id == id }
            if (a != null && !a.isUnlocked) {
                achievementDao.updateAchievement(
                    a.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis())
                )
            }
        }

        // Streaks
        if (progress.currentStreak >= 3) unlock("streak_3")
        if (progress.currentStreak >= 7) unlock("streak_7")
        if (progress.currentStreak >= 14) unlock("streak_14")
        if (progress.currentStreak >= 21) unlock("streak_21")
        if (progress.currentStreak >= 30) unlock("streak_30")
        if (progress.currentStreak >= 90) unlock("streak_90")
        if (progress.currentStreak >= 210) unlock("streak_210")
        if (progress.currentStreak >= 365) unlock("streak_365")

        // Cards
        if (progress.totalCardsStudied >= 10) unlock("cards_10")
        if (progress.totalCardsStudied >= 30) unlock("cards_30")
        if (progress.totalCardsStudied >= 50) unlock("cards_50")
        if (progress.totalCardsStudied >= 150) unlock("cards_150")
        if (progress.totalCardsStudied >= 250) unlock("cards_250")
        if (progress.totalCardsStudied >= 500) unlock("cards_500")

        // Mastery
        if (totalMastered >= 10) unlock("mastery_10")
        if (totalMastered >= 30) unlock("mastery_30")
        if (totalMastered >= 50) unlock("mastery_50")
        if (totalMastered >= 100) unlock("mastery_100")
        if (totalMastered >= 150) unlock("mastery_150")
        if (totalMastered >= 250) unlock("mastery_250")
        if (totalMastered >= 500) unlock("mastery_500")

        // Points/Complete
        if (progress.totalCardsStudied >= 10) unlock("points_10")
        if (progress.totalCardsStudied >= 30) unlock("points_30")
        if (progress.totalCardsStudied >= 50) unlock("points_50")
        if (progress.totalCardsStudied >= 100) unlock("points_100")
        if (progress.totalCardsStudied >= 150) unlock("points_150")
        if (progress.totalCardsStudied >= 250) unlock("points_250")
        if (progress.totalCardsStudied >= 500) unlock("points_500")
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
    fun getCardsForDeck(deckId: String) = repository.getCardsForDeck(deckId)

    fun createDeck(name: String, subject: String, color: String, icon: String) {
        viewModelScope.launch {
            repository.createDeck(
                Deck(id = UUID.randomUUID().toString(), name = name, subject = subject, color = color, icon = icon)
            )
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
        }
    }

    // ─── Cards ───────────────────────────────────────────────────

    fun addFlashcard(deckId: String, front: String, back: String) {
        viewModelScope.launch {
            repository.addFlashcard(
                Flashcard(id = UUID.randomUUID().toString(), deckId = deckId, front = front, back = back)
            )
            val count = cardDao.getCardCountForDeck(deckId)
            deckDao.getDeckById(deckId)?.let { deck ->
                repository.updateDeck(deck.copy(cardCount = count))
            }
        }
    }

    fun deleteCard(card: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcard(card)
            val count = cardDao.getCardCountForDeck(card.deckId)
            deckDao.getDeckById(card.deckId)?.let { deck ->
                repository.updateDeck(deck.copy(cardCount = count))
            }
        }
    }

    // ─── Study Session ───────────────────────────────────────────

    fun saveStudySession(deckId: String, cardsStudied: Int, correctCount: Int) {
        val points = correctCount * 10 + (cardsStudied - correctCount) * 5
        viewModelScope.launch {
            repository.saveSession(
                StudySession(
                    deckId       = deckId,
                    cardsStudied = cardsStudied,
                    correctCount = correctCount,
                    pointsEarned = points
                )
            )
            
            // Update User Profile with new XP and Points
            val currentProfile = repository.userProfile.firstOrNull() ?: UserProfile()
            val newTotalPoints = currentProfile.totalPoints + points
            val newXp = currentProfile.xp + points
            val newLevel = (newXp / 500) + 1
            
            repository.updateProfile(
                currentProfile.copy(
                    totalPoints = newTotalPoints,
                    xp = newXp,
                    level = newLevel
                )
            )

            // Update local and cloud deck stats
            val deck = AppDatabase.getDatabase(getApplication()).deckDao().getDeckById(deckId)
            deck?.let {
                val newMastered = (it.masteredCount + correctCount).coerceAtMost(it.cardCount)
                repository.updateDeck(it.copy(
                    lastStudied = System.currentTimeMillis(),
                    masteredCount = newMastered
                ))
            }
            // checkAchievements(cardsStudied) // Removed, called below with local state
            
            // Update active challenge scores
            repository.getActiveChallenges().first().forEach { challenge ->
                if (challenge.status == "active") {
                    repository.updateChallengeScore(challenge.id, points)
                }
            }
            // Trigger check with updated local values
            val updatedProgress = _userProgress.value.copy(
                totalPoints = _userProgress.value.totalPoints + points,
                totalCardsStudied = _userProgress.value.totalCardsStudied + cardsStudied
            )
            checkAchievements(updatedProgress)
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

    // ─── AI Flashcard Generation ─────────────────────────────────

    fun generateFlashcardsFromFile(deckId: String, fileUri: Uri) {
        viewModelScope.launch {
            _aiGenerationState.value = AiGenerationState.Loading("Reading file...")

            // Check file size (max 10MB)
            val fileSize = fileTextExtractor.getFileSize(fileUri)
            if (fileSize > 10 * 1024 * 1024) {
                _aiGenerationState.value = AiGenerationState.Error("File is too large. Maximum size is 10MB.")
                return@launch
            }

            // Extract text from file
            val textResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                fileTextExtractor.extractText(fileUri)
            }

            textResult.fold(
                onSuccess = { text ->
                    _aiGenerationState.value = AiGenerationState.Loading("AI is generating flashcards...")

                    val cardsResult = flashcardGenerator.generateFlashcards(text)

                    cardsResult.fold(
                        onSuccess = { generatedCards ->
                            // Save all generated cards to the deck
                            var savedCount = 0
                            for (card in generatedCards) {
                                try {
                                    repository.addFlashcard(
                                        Flashcard(
                                            id = UUID.randomUUID().toString(),
                                            deckId = deckId,
                                            front = card.front,
                                            back = card.back
                                        )
                                    )
                                    savedCount++
                                } catch (e: Exception) {
                                    // Continue saving other cards even if one fails
                                }
                            }

                            // Update deck card count
                            val count = cardDao.getCardCountForDeck(deckId)
                            deckDao.getDeckById(deckId)?.let { deck ->
                                repository.updateDeck(deck.copy(cardCount = count))
                            }

                            _aiGenerationState.value = AiGenerationState.Success(savedCount)
                        },
                        onFailure = { error ->
                            _aiGenerationState.value = AiGenerationState.Error(error.message ?: "Failed to generate flashcards")
                        }
                    )
                },
                onFailure = { error ->
                    _aiGenerationState.value = AiGenerationState.Error(error.message ?: "Failed to read file")
                }
            )
        }
    }

    fun resetAiGenerationState() {
        _aiGenerationState.value = AiGenerationState.Idle
    }

    private fun refreshAiInsight(progress: UserProgress, recentSessions: List<StudySession>) {
        viewModelScope.launch {
            val result = studyCoach.getStudyInsights(progress, recentSessions.take(10))
            result.onSuccess { insight ->
                _aiInsight.value = insight
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private fun calculateStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        val today = System.currentTimeMillis() / 1000 / 60 / 60 / 24
        val days  = sessions.map { it.date / 1000 / 60 / 60 / 24 }.sortedDescending().distinct()
        if (days.isEmpty() || days.first() < today - 1) return 0
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
