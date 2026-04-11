package com.flipwise.app.data.repository

import android.content.Context
import com.flipwise.app.data.database.AppDatabase
import com.flipwise.app.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class FlipWiseRepository(context: Context) {
    private val appDatabase by lazy { AppDatabase.getDatabase(context) }
    private val deckDao    by lazy { appDatabase.deckDao() }
    private val flashcardDao by lazy { appDatabase.flashcardDao() }
    private val profileDao by lazy { appDatabase.profileDao() }
    private val sessionDao by lazy { appDatabase.studySessionDao() }

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val remoteDatabase by lazy { 
        // Persistence must be enabled BEFORE any other usage of the database
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Persistence might already be enabled or failed in this process
        }
        FirebaseDatabase.getInstance().reference 
    }

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
        // Clear local database to align user profiles and data correctly
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch {
            appDatabase.clearAllTables()
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        val currentUserId = userId
        return try {
            // 1. Remove from leaderboard (Public)
            remoteDatabase.child("leaderboard").child(currentUserId).removeValue().await()
            
            // 2. Remove all user data (Private)
            remoteDatabase.child("users").child(currentUserId).removeValue().await()
            
            // 3. Clear local DB
            appDatabase.clearAllTables()
            
            // 4. Delete Auth account
            user.delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // If it fails because of sensitive dynamic (requires recent login), 
            // the UI should ideally prompt for re-auth.
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.sendEmailVerification()?.await()
            auth.signOut()
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
        remoteDatabase.child("users").child(userId).child("decks").child(deck.id).setValue(deck).await()
    }

    suspend fun updateDeck(deck: Deck) {
        deckDao.updateDeck(deck)
        remoteDatabase.child("users").child(userId).child("decks").child(deck.id).setValue(deck).await()
    }

    suspend fun deleteDeck(deckId: String) {
        deckDao.deleteDeckById(deckId)
        remoteDatabase.child("users").child(userId).child("decks").child(deckId).removeValue().await()
    }

    // --- Cards ---
    fun getCardsForDeck(deckId: String): Flow<List<Flashcard>> = flashcardDao.getCardsByDeck(deckId)

    suspend fun addFlashcard(card: Flashcard) {
        flashcardDao.insertCard(card)
        // Sync to cloud
        remoteDatabase.child("users").child(userId).child("cards").child(card.id).setValue(card).await()
    }

    suspend fun updateFlashcard(card: Flashcard) {
        flashcardDao.updateCard(card)
        remoteDatabase.child("users").child(userId).child("cards").child(card.id).setValue(card).await()
    }

    suspend fun deleteFlashcard(card: Flashcard) {
        flashcardDao.deleteCard(card)
        remoteDatabase.child("users").child(userId).child("cards").child(card.id).removeValue().await()
    }

    // --- Profile ---
    val userProfile: Flow<UserProfile?> = profileDao.getUserProfile()

    suspend fun updateProfile(profile: UserProfile) {
        profileDao.insertProfile(profile)
        // Sync to cloud
        remoteDatabase.child("users").child(userId).child("profile").setValue(profile).await()
        // Save to public leaderboard reference too
        remoteDatabase.child("leaderboard").child(userId).setValue(
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
        if (!isUserLoggedIn()) return null
        return try {
            val snapshot = remoteDatabase.child("users").child(userId).child("profile").get().await()
            val profile = snapshot.getValue(UserProfile::class.java)
            if (profile != null) {
                profileDao.insertProfile(profile)
            }
            profile
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fullSync(): UserProfile? {
        if (!isUserLoggedIn()) return null
        return try {
            // 1. Sync Profile
            val profile = syncProfile()
            
            // 2. Sync Decks
            val decksSnapshot = remoteDatabase.child("users").child(userId).child("decks").get().await()
            decksSnapshot.children.mapNotNull { it.getValue(Deck::class.java) }.forEach {
                deckDao.insertDeck(it)
            }
            
            // 3. Sync Cards
            val cardsSnapshot = remoteDatabase.child("users").child(userId).child("cards").get().await()
            cardsSnapshot.children.mapNotNull { it.getValue(Flashcard::class.java) }.forEach {
                flashcardDao.insertCard(it)
            }
            
            // 4. Sync Sessions
            val sessionsSnapshot = remoteDatabase.child("users").child(userId).child("sessions").get().await()
            sessionsSnapshot.children.mapNotNull { it.getValue(StudySession::class.java) }.forEach {
                sessionDao.insertSession(it)
            }
            
            // 5. Sync Achievements (if any)
            // ...
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
        remoteDatabase.child("users").child(userId).child("sessions").push().setValue(session).await()
    }

    // --- Leaderboard ---
    suspend fun getLeaderboard(): List<UserProfile> {
        return try {
            val snapshot = remoteDatabase.child("leaderboard")
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

    fun getLeaderboardFlow(): Flow<List<UserProfile>> = kotlinx.coroutines.flow.callbackFlow {
        val ref = remoteDatabase.child("leaderboard").orderByChild("totalPoints").limitToLast(100)
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(UserProfile::class.java)
                }.reversed()
                trySend(list)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // --- Challenges ---
    suspend fun addChallenge(challenge: Challenge) {
        appDatabase.challengeDao().insertChallenge(challenge)
        remoteDatabase.child("users").child(userId).child("challenges").child(challenge.id).setValue(challenge).await()
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
        remoteDatabase.child("challenges").child(challengeId).child("participants").child(userId).setValue(participant).await()
    }

    suspend fun updateChallengeScore(challengeId: String, scoreDelta: Int) {
        val ref = remoteDatabase.child("challenges").child(challengeId).child("participants").child(userId).child("score")
        val current = ref.get().await().getValue(Int::class.java) ?: 0
        ref.setValue(current + scoreDelta).await()
    }

    fun getActiveChallenges(): Flow<List<Challenge>> = appDatabase.challengeDao().getAllChallenges()

    // --- Friends Social System ---
    suspend fun findUserByUsername(username: String): UserProfile? {
        return try {
            val snapshot = remoteDatabase.child("leaderboard")
                .orderByChild("username")
                .equalTo(username.trim())
                .limitToFirst(1)
                .get()
                .await()
            
            snapshot.children.firstOrNull()?.getValue(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addFriend(targetId: String, targetProfile: UserProfile) {
        // 1. Add to my friends list in cloud
        val friendEntry = Friend(
            id = targetId,
            userId = userId,
            username = targetProfile.username,
            displayName = targetProfile.displayName,
            avatar = targetProfile.avatar,
            status = "accepted",
            addedAt = System.currentTimeMillis(),
            totalPoints = targetProfile.totalPoints,
            currentStreak = 0, // Simplified for now
            totalCardsStudied = 0
        )
        remoteDatabase.child("users").child(userId).child("friends").child(targetId).setValue(friendEntry).await()
        
        // 2. Add to my local DB
        appDatabase.friendDao().insertFriend(friendEntry)
    }

    fun getFriendsFlow(): Flow<List<Friend>> = callbackFlow {
        val ref = remoteDatabase.child("users").child(userId).child("friends")
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val friends = snapshot.children.mapNotNull { it.getValue(Friend::class.java) }
                trySend(friends)
                
                // Keep local DB in sync
                @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                kotlinx.coroutines.GlobalScope.launch {
                    friends.forEach { appDatabase.friendDao().insertFriend(it) }
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
