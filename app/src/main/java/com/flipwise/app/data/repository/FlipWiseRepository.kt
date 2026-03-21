package com.flipwise.app.data.repository

import android.content.Context
import com.flipwise.app.data.database.AppDatabase
import com.flipwise.app.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class FlipWiseRepository(context: Context) {
    private val db         by lazy { AppDatabase.getDatabase(context) }
    private val deckDao    by lazy { db.deckDao() }
    private val flashcardDao by lazy { db.flashcardDao() }
    private val profileDao by lazy { db.profileDao() }
    private val sessionDao by lazy { db.studySessionDao() }

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val realtimeDb by lazy { FirebaseDatabase.getInstance().reference }

    val userId: String
        get() = auth.currentUser?.uid ?: "local_user"

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // --- Authentication ---
    suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            if (result.user?.isEmailVerified == false) {
                auth.signOut()
                throw Exception("Please verify your email address before logging in.")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = try {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Decks ---
    val allDecks: Flow<List<Deck>> = deckDao.getAllDecks()

    suspend fun createDeck(deck: Deck) {
        deckDao.insertDeck(deck)
        // Sync to cloud
        realtimeDb.child("users").child(userId).child("decks").child(deck.id).setValue(deck).await()
    }

    suspend fun updateDeck(deck: Deck) {
        deckDao.updateDeck(deck)
        realtimeDb.child("users").child(userId).child("decks").child(deck.id).setValue(deck).await()
    }

    suspend fun deleteDeck(deckId: String) {
        deckDao.deleteDeckById(deckId)
        realtimeDb.child("users").child(userId).child("decks").child(deckId).removeValue().await()
    }

    // --- Cards ---
    fun getCardsForDeck(deckId: String): Flow<List<Flashcard>> = flashcardDao.getCardsByDeck(deckId)

    suspend fun addFlashcard(card: Flashcard) {
        flashcardDao.insertCard(card)
        // Sync to cloud
        realtimeDb.child("users").child(userId).child("cards").child(card.id).setValue(card).await()
    }

    suspend fun updateFlashcard(card: Flashcard) {
        flashcardDao.updateCard(card)
        realtimeDb.child("users").child(userId).child("cards").child(card.id).setValue(card).await()
    }

    suspend fun deleteFlashcard(card: Flashcard) {
        flashcardDao.deleteCard(card)
        realtimeDb.child("users").child(userId).child("cards").child(card.id).removeValue().await()
    }

    // --- Profile ---
    val userProfile: Flow<UserProfile?> = profileDao.getUserProfile()

    suspend fun updateProfile(profile: UserProfile) {
        profileDao.insertProfile(profile)
        // Sync to cloud
        realtimeDb.child("users").child(userId).child("profile").setValue(profile).await()
        // Save to public leaderboard reference too
        realtimeDb.child("leaderboard").child(userId).setValue(
            mapOf(
                "userId" to userId,
                "username" to profile.username,
                "displayName" to profile.displayName,
                "avatar" to profile.avatar,
                "totalPoints" to profile.totalPoints,
                "xp" to profile.xp,
                "level" to profile.level
            )
        ).await()
    }

    suspend fun syncProfile(): UserProfile? {
        return try {
            val snapshot = realtimeDb.child("users").child(userId).child("profile").get().await()
            val profile = snapshot.getValue(UserProfile::class.java)
            if (profile != null) {
                profileDao.insertProfile(profile)
            }
            profile
        } catch (e: Exception) {
            null
        }
    }

    // --- Sessions ---
    val sessions: Flow<List<StudySession>> = sessionDao.getAllSessions()

    suspend fun saveSession(session: StudySession) {
        sessionDao.insertSession(session)
        // Sync to cloud
        realtimeDb.child("users").child(userId).child("sessions").push().setValue(session).await()
    }

    // --- Leaderboard ---
    suspend fun getLeaderboard(): List<UserProfile> {
        return try {
            val snapshot = realtimeDb.child("leaderboard")
                .orderByChild("totalPoints")
                .limitToLast(100)
                .get()
                .await()
            
            snapshot.children.mapNotNull { child ->
                child.getValue(UserProfile::class.java)
            }.reversed() // Highest points first
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Challenges ---
    suspend fun addChallenge(challenge: Challenge) {
        db.challengeDao().insertChallenge(challenge)
        realtimeDb.child("users").child(userId).child("challenges").child(challenge.id).setValue(challenge).await()
    }

    suspend fun joinChallenge(challengeId: String, userProfile: UserProfile) {
        val participant = mapOf(
            "userId" to userId,
            "username" to userProfile.username,
            "displayName" to userProfile.displayName,
            "avatar" to userProfile.avatar,
            "score" to 0,
            "team" to if ((0..1).random() == 0) "Blue" else "Red" // For team challenges
        )
        realtimeDb.child("challenges").child(challengeId).child("participants").child(userId).setValue(participant).await()
    }

    suspend fun updateChallengeScore(challengeId: String, scoreDelta: Int) {
        val ref = realtimeDb.child("challenges").child(challengeId).child("participants").child(userId).child("score")
        val current = ref.get().await().getValue(Int::class.java) ?: 0
        ref.setValue(current + scoreDelta).await()
    }

    fun getActiveChallenges(): Flow<List<Challenge>> = db.challengeDao().getAllChallenges()
}
