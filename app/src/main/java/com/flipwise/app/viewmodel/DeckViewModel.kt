package com.flipwise.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flipwise.app.data.ai.FileTextExtractor
import com.flipwise.app.data.ai.GeminiFlashcardGenerator
import com.flipwise.app.data.ai.GeneratedCard
import com.flipwise.app.data.database.AppDatabase
import com.flipwise.app.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import com.flipwise.app.data.security.RateLimiter

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
    private val auditLogDao = db.auditLogDao()

    val decks: Flow<List<Deck>>               = repository.allDecks
    val achievements: Flow<List<Achievement>> = AppDatabase.getDatabase(application).achievementDao().getAllAchievements()
    val sessions: Flow<List<StudySession>>    = repository.sessions

    private val _userProgress = MutableStateFlow(UserProgress())
    val userProgress: StateFlow<UserProgress> = _userProgress.asStateFlow()

    // Active Goal for HomeScreen widget
    private val _activeGoal = MutableStateFlow<GoalProgressInfo?>(null)
    val activeGoal: StateFlow<GoalProgressInfo?> = _activeGoal.asStateFlow()

    private val _allActiveGoals = MutableStateFlow<List<Challenge>>(emptyList())
    val allActiveGoals: StateFlow<List<Challenge>> = _allActiveGoals.asStateFlow()

    private val _lastCompletedGoal = MutableStateFlow<Challenge?>(null)
    val lastCompletedGoal: StateFlow<Challenge?> = _lastCompletedGoal.asStateFlow()

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
        refreshGoals()
        
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
                list.add(Achievement("streak_3", "3-Day Streak", "Maintain a 3-day study streak", "🔥", "Streaks", tier = "bronze"))
                list.add(Achievement("streak_7", "Week Warrior", "Maintain a 7-day study streak", "🔥", "Streaks", tier = "bronze"))
                list.add(Achievement("streak_14", "Fortnight Fighter", "Maintain a 14-day study streak", "🥈", "Streaks", tier = "silver"))
                list.add(Achievement("streak_21", "21-Day Habit", "Maintain a 21-day study streak", "🥈", "Streaks", tier = "silver"))
                list.add(Achievement("streak_30", "Month Master", "Maintain a 30-day study streak", "🥇", "Streaks", tier = "gold"))
                list.add(Achievement("streak_90", "Season Scholar", "Maintain a 90-day study streak", "🥇", "Streaks", tier = "gold"))
                list.add(Achievement("streak_210", "Grit Greatness", "Maintain a 210-day study streak", "🏆", "Streaks", tier = "gold"))
                list.add(Achievement("streak_365", "Year Legend", "Maintain a 365-day study streak", "👑", "Streaks", tier = "gold"))

                // Cards
                list.add(Achievement("cards_10", "First Steps", "Study 10 flashcards", "🌱", "Cards", tier = "bronze"))
                list.add(Achievement("cards_30", "Getting Started", "Study 30 flashcards", "🌱", "Cards", tier = "bronze"))
                list.add(Achievement("cards_50", "Steady Learner", "Study 50 flashcards", "🥈", "Cards", tier = "silver"))
                list.add(Achievement("cards_150", "Centurion", "Study 150 flashcards", "🥈", "Cards", tier = "silver"))
                list.add(Achievement("cards_250", "Knowledge Seeker", "Study 250 flashcards", "🥇", "Cards", tier = "gold"))
                list.add(Achievement("cards_500", "Scholar", "Study 500 flashcards", "🥇", "Cards", tier = "gold"))

                // Mastery
                list.add(Achievement("mastery_10", "Novice Master", "Master 10 cards", "🥉", "Mastery", tier = "bronze"))
                list.add(Achievement("mastery_30", "Adept Master", "Master 30 cards", "🥉", "Mastery", tier = "bronze"))
                list.add(Achievement("mastery_50", "Skillful Master", "Master 50 cards", "🥈", "Mastery", tier = "silver"))
                list.add(Achievement("mastery_100", "Elite Master", "Master 100 cards", "🥈", "Mastery", tier = "silver"))
                list.add(Achievement("mastery_150", "Grand Master", "Master 150 cards", "🥇", "Mastery", tier = "gold"))
                list.add(Achievement("mastery_250", "Zen Master", "Master 250 cards", "🥇", "Mastery", tier = "gold"))
                list.add(Achievement("mastery_500", "Ultimate Master", "Master 500 cards", "👑", "Mastery", tier = "gold"))

                // Points
                list.add(Achievement("points_10", "Beginner Points", "Earn points from 10 cards", "🥉", "Points", tier = "bronze"))
                list.add(Achievement("points_100", "Advanced Points", "Earn points from 100 cards", "🥈", "Points", tier = "silver"))
                list.add(Achievement("points_500", "Legendary Points", "Earn points from 500 cards", "🥇", "Points", tier = "gold"))

                // Sessions
                list.add(Achievement("study_1", "First Study Session", "Complete your first study session", "🔰", "Milestones", tier = "bronze"))
                list.add(Achievement("study_10", "Dedicated Student", "Complete 10 study sessions", "🌟", "Milestones", tier = "silver"))
                list.add(Achievement("study_50", "Study Machine", "Complete 50 study sessions", "🔥", "Milestones", tier = "gold"))

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

        // Sessions
        val totalSessions = sessionDao.getAllSessionsOnce(repository.userId).size
        if (totalSessions >= 1) unlock("study_1")
        if (totalSessions >= 10) unlock("study_10")
        if (totalSessions >= 50) unlock("study_50")
    }

    // ─── Progress ────────────────────────────────────────────────

    private fun loadUserProgress() {
        viewModelScope.launch {
            repository.sessions.collect { sessions ->
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
    
    fun getCardsForDecks(deckIds: List<String>): Flow<List<Flashcard>> {
        if (deckIds.isEmpty()) return flowOf(emptyList())
        val flows = deckIds.map { repository.getCardsForDeck(it) }
        return combine(flows) { arrays -> arrays.flatMap { it } }
    }

    fun createDeck(name: String, subject: String, color: String, icon: String) {
        viewModelScope.launch {
            repository.createDeck(
                Deck(id = UUID.randomUUID().toString(), name = name, subject = subject, color = color, icon = icon)
            )
            logAction("DECK_CREATED", "name=$name")
        }
    }

    fun updateDeck(deck: Deck) {
        viewModelScope.launch {
            repository.updateDeck(deck)
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
            logAction("DECK_DELETED", "deckId=$deckId")
        }
    }

    // ─── Cards ───────────────────────────────────────────────────

    fun addFlashcard(deckId: String, front: String, back: String, options: String? = null) {
        viewModelScope.launch {
            repository.addFlashcard(
                Flashcard(id = UUID.randomUUID().toString(), deckId = deckId, front = front, back = back, options = options)
            )
            val count = cardDao.getCardCountForDeck(deckId)
            deckDao.getDeckById(deckId)?.let { deck ->
                repository.updateDeck(deck.copy(cardCount = count))
            }
        }
    }

    fun updateFlashcard(card: Flashcard) {
        viewModelScope.launch {
            repository.updateFlashcard(card)
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
            logAction("STUDY_SESSION", "deckId=$deckId cards=$cardsStudied correct=$correctCount points=$points")
            
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
            
            // Update active challenge scores
            repository.getActiveChallenges().first().forEach { challenge ->
                if (challenge.status == "active" && challenge.type != "personal") {
                    repository.updateChallengeScore(challenge.id, points)
                }
            }

            // Update personal goal progress after study session
            updateGoalProgress()

            // Trigger check with updated local values
            val updatedProgress = _userProgress.value.copy(
                totalPoints = _userProgress.value.totalPoints + points,
                totalCardsStudied = _userProgress.value.totalCardsStudied + cardsStudied
            )
            checkAchievements(updatedProgress)
        }
    }

    /**
     * Updates the SRS (Spaced Repetition System) stats for a specific card based on user rating.
     * rating: "easy", "hard", "forgot"
     */
    fun updateCardSrs(card: Flashcard, rating: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            
            // Simple SRS algorithm (SM-2 variant basics)
            val interval = when (rating) {
                "forgot" -> 10 * 60 * 1000L // 10 minutes
                "hard" -> 1 * 24 * 60 * 60 * 1000L // 1 day
                "easy" -> 4 * 24 * 60 * 60 * 1000L // 4 days
                else -> 1 * 24 * 60 * 60 * 1000L
            }
            
            // Multiplier based on previous reviews
            val multiplier = if (card.reviews > 0) (card.reviews + 1).coerceAtMost(10) else 1
            val nextReview = now + (interval * multiplier)
            
            repository.updateFlashcard(
                card.copy(
                    nextReview = nextReview,
                    reviews = card.reviews + 1,
                    difficulty = rating
                )
            )
        }
    }

    // ─── Settings ────────────────────────────────────────────────

    fun clearAllData() {
        viewModelScope.launch {
            logAction("CLEAR_ALL_DATA", "all local data wiped")
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                db.clearAllTables()
            }
            initAchievements()
        }
    }

    // ─── AI Flashcard Generation ─────────────────────────────────

    fun generateFlashcardsFromFile(deckId: String, fileUri: Uri) {
        // Max 3 AI generations per minute
        if (!RateLimiter.isAllowed("AI_GENERATE", maxCount = 3, windowMs = 60 * 1000)) {
            _aiGenerationState.value = AiGenerationState.Error("Too many requests. Please wait a moment.")
            return
        }
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
                            saveGeneratedCards(deckId, generatedCards)
                        },
                        onFailure = { error ->
                            _aiGenerationState.value = AiGenerationState.Loading("AI failed. Using fallback extraction...")
                            // Fallback logic
                            val fallbackCards = generateFallbackCards(text)
                            saveGeneratedCards(deckId, fallbackCards)
                        }
                    )
                },
                onFailure = { error ->
                    _aiGenerationState.value = AiGenerationState.Error(error.message ?: "Failed to read file")
                }
            )
        }
    }

    private fun generateFallbackCards(text: String): List<GeneratedCard> {
        val cards = mutableListOf<GeneratedCard>()
        val lines = text.split("\n").filter { it.isNotBlank() }
        
        // Simple fallback: Try to find "Question: Answer" patterns or just use adjacent lines
        var i = 0
        while (i < lines.size - 1 && cards.size < 20) {
            val line = lines[i].trim()
            if (line.contains("?") || line.length > 20) {
                // If it looks like a question or a long sentence
                val question = line
                val answer = lines[i+1].trim()
                
                // Occasionally create multiple choice if we have enough lines
                if (i + 4 < lines.size && i % 3 == 0) {
                    val wrongOptions = listOf(lines[i+2].trim(), lines[i+3].trim(), lines[i+4].trim())
                    cards.add(GeneratedCard(
                        front = question,
                        back = answer,
                        options = (wrongOptions + answer).shuffled()
                    ))
                    i += 5
                } else {
                    cards.add(GeneratedCard(
                        front = question,
                        back = answer,
                        options = null
                    ))
                    i += 2
                }
            } else {
                i++
            }
        }
        
        // If still empty, just take any lines
        if (cards.isEmpty() && lines.size >= 2) {
            cards.add(GeneratedCard(front = lines[0], back = lines[1], options = null))
        }
        
        return cards
    }

    private suspend fun saveGeneratedCards(deckId: String, generatedCards: List<GeneratedCard>) {
        var savedCount = 0
        for (card in generatedCards) {
            try {
                repository.addFlashcard(
                    Flashcard(
                        id = UUID.randomUUID().toString(),
                        deckId = deckId,
                        front = card.front,
                        back = card.back,
                        options = card.options?.joinToString("|")
                    )
                )
                savedCount++
            } catch (e: Exception) {}
        }

        // Update deck card count
        val count = cardDao.getCardCountForDeck(deckId)
        deckDao.getDeckById(deckId)?.let { deck ->
            repository.updateDeck(deck.copy(cardCount = count))
        }

        _aiGenerationState.value = AiGenerationState.Success(savedCount)
    }

    fun resetAiGenerationState() {
        _aiGenerationState.value = AiGenerationState.Idle
    }

    // ─── Goal Management ─────────────────────────────────────────

    /**
     * Refreshes all active personal goals:
     * - Auto-expires goals past their endDate
     * - Auto-completes goals that met their target
     * - Updates the activeGoal StateFlow for the HomeScreen widget
     */
    fun refreshGoals() {
        viewModelScope.launch {
            try {
                val goals = repository.getActivePersonalGoals()
                val now = System.currentTimeMillis()

                var bestActiveGoal: GoalProgressInfo? = null

                for (goal in goals) {
                    val progress = calculateGoalProgress(goal)
                    val percentage = if (goal.goal > 0) {
                        (progress.toFloat() / goal.goal.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    when {
                        // Auto-complete: target reached
                        percentage >= 1f -> {
                            repository.updateChallenge(goal.copy(status = "completed"))
                            // Award bonus XP for completing a goal
                            val currentProfile = repository.userProfile.firstOrNull()
                            if (currentProfile != null) {
                                val bonusXp = 50
                                repository.updateProfile(
                                    currentProfile.copy(
                                        totalPoints = currentProfile.totalPoints + bonusXp,
                                        xp = currentProfile.xp + bonusXp
                                    )
                                )
                            }
                            logAction("GOAL_COMPLETED", "goalId=${goal.id} name=${goal.name}")
                            _lastCompletedGoal.value = goal
                        }
                        // Auto-expire: deadline passed
                        now > goal.endDate -> {
                            repository.updateChallenge(goal.copy(status = "expired"))
                            logAction("GOAL_EXPIRED", "goalId=${goal.id} name=${goal.name}")
                        }
                        // Still active — track for the HomeScreen widget
                        else -> {
                            val info = GoalProgressInfo(
                                goalId = goal.id,
                                name = goal.name,
                                goalType = goal.goalType,
                                target = goal.goal,
                                current = progress,
                                percentage = percentage,
                                daysLeft = ((goal.endDate - now) / 86400000).coerceAtLeast(0)
                            )
                            // Pick the goal closest to completion for the widget
                            if (bestActiveGoal == null || info.percentage > bestActiveGoal!!.percentage) {
                                bestActiveGoal = info
                            }
                        }
                    }
                }
                _activeGoal.value = bestActiveGoal
                _allActiveGoals.value = goals.filter { it.status == "active" }
            } catch (e: Exception) {
                android.util.Log.e("GOAL", "refreshGoals failed: ${e.message}")
            }
        }
    }

    /**
     * Called after every study session to update personal goal progress.
     */
    private suspend fun updateGoalProgress() {
        try {
            val goals = repository.getActivePersonalGoals()
            val now = System.currentTimeMillis()

            for (goal in goals) {
                val progress = calculateGoalProgress(goal)
                val percentage = if (goal.goal > 0) {
                    (progress.toFloat() / goal.goal.toFloat()).coerceIn(0f, 1f)
                } else 0f

                when {
                    percentage >= 1f -> {
                        repository.updateChallenge(goal.copy(status = "completed"))
                        val currentProfile = repository.userProfile.firstOrNull()
                        if (currentProfile != null) {
                            val bonusXp = 50
                            repository.updateProfile(
                                currentProfile.copy(
                                    totalPoints = currentProfile.totalPoints + bonusXp,
                                    xp = currentProfile.xp + bonusXp
                                )
                            )
                        }
                        logAction("GOAL_COMPLETED", "goalId=${goal.id} name=${goal.name}")
                        _lastCompletedGoal.value = goal
                    }
                    now > goal.endDate -> {
                        repository.updateChallenge(goal.copy(status = "expired"))
                        logAction("GOAL_EXPIRED", "goalId=${goal.id} name=${goal.name}")
                    }
                }
            }
            // Refresh the widget after updates
            refreshGoals()
        } catch (e: Exception) {
            android.util.Log.e("GOAL", "updateGoalProgress failed: ${e.message}")
        }
    }

    fun clearCompletedGoal() {
        _lastCompletedGoal.value = null
    }

    /**
     * Calculates the current progress value for a goal, correctly filtering
     * by deck and date range.
     */
    private suspend fun calculateGoalProgress(goal: Challenge): Int {
        return when (goal.goalType) {
            "Cards Studied" -> {
                val sessions = repository.getSessionsForGoal(goal)
                sessions.sumOf { it.cardsStudied }
            }
            "Points Earned" -> {
                val sessions = repository.getSessionsForGoal(goal)
                sessions.sumOf { it.pointsEarned }
            }
            "Streak Days" -> _userProgress.value.currentStreak
            else -> 0
        }
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
    private fun logAction(action: String, details: String = "") {
        viewModelScope.launch {
            val uid = try {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
            } catch (e: Exception) { "anonymous" }

            // Save locally to Room
            auditLogDao.log(AuditLog(userId = uid, action = action, details = details))

            // Save to Realtime Database instead of Firestore
            try {
                val url = "https://flipwise-dc052-default-rtdb.asia-southeast1.firebasedatabase.app"
                com.google.firebase.database.FirebaseDatabase.getInstance(url)
                    .getReference("audit_logs")
                    .push()
                    .setValue(mapOf(
                        "userId" to uid,
                        "action" to action,
                        "details" to details,
                        "timestamp" to System.currentTimeMillis()
                    ))
            } catch (e: Exception) {
                android.util.Log.e("AUDIT_LOG", "Failed to save to Firebase: ${e.message}")
            }

            android.util.Log.d("AUDIT_LOG", "✅ Logged: $action | $details")
        }
    }
}
