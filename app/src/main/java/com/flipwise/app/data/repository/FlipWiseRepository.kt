package com.flipwise.app.data.repository

import android.content.Context
import com.flipwise.app.data.database.AppDatabase
import com.flipwise.app.data.model.*
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

class FlipWiseRepository(context: Context) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val appDatabase by lazy { AppDatabase.getDatabase(context) }
    private val deckDao    by lazy { appDatabase.deckDao() }
    private val flashcardDao by lazy { appDatabase.flashcardDao() }
    private val profileDao by lazy { appDatabase.profileDao() }
    private val sessionDao by lazy { appDatabase.studySessionDao() }

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val integrityManager by lazy { com.flipwise.app.data.security.IntegrityManager(context) }
    private val remoteDatabase by lazy { 
        val url = "https://flipwise-dc052-default-rtdb.asia-southeast1.firebasedatabase.app"
        // Persistence must be enabled BEFORE any other usage of the database
        try {
            FirebaseDatabase.getInstance(url).setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Persistence might already be enabled or failed in this process
        }
        FirebaseDatabase.getInstance(url).reference 
    }

    val userId: String
        get() = auth.currentUser?.uid ?: "local_user"

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun isGoogleUser(): Boolean {
        return auth.currentUser?.providerData?.any { it.providerId == "google.com" } ?: false
    }

    suspend fun isAdmin(): Boolean {
        val profile = userProfile.firstOrNull() ?: syncProfile()
        return profile?.role == "admin"
    }

    suspend fun getIntegrityToken(nonce: String): String? {
        return integrityManager.fetchIntegrityToken(nonce)
    }

    // --- Authentication ---
    val currentUserEmail: String get() = auth.currentUser?.email ?: ""

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
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
        repositoryScope.launch {
            withContext(Dispatchers.IO) {
                appDatabase.clearAllTables()
            }
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
            withContext(Dispatchers.IO) {
                appDatabase.clearAllTables()
            }
            
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
        
        // Atomic multi-path update to keep private profile and public leaderboard in sync
        val updates = hashMapOf<String, Any>(
            "users/$userId/profile" to profile,
            "leaderboard/$userId" to mapOf(
                "id" to userId,
                "username" to profile.username,
                "displayName" to profile.displayName,
                "avatar" to profile.avatar,
                "totalPoints" to profile.totalPoints,
                "xp" to profile.xp,
                "level" to profile.level
            )
        )
        remoteDatabase.updateChildren(updates).await()
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
            
            // 5. Sync Friends
            try {
                val friendsSnapshot = remoteDatabase.child("users").child(userId).child("friends").get().await()
                friendsSnapshot.children.mapNotNull { child ->
                    parseFriendFromSnapshot(child)
                }.forEach {
                    appDatabase.friendDao().insertFriend(it)
                }
            } catch (e: Exception) {
                android.util.Log.e("SYNC", "Friends sync failed: ${e.message}")
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

    suspend fun updateChallenge(challenge: Challenge) {
        appDatabase.challengeDao().updateChallenge(challenge)
        remoteDatabase.child("users").child(userId).child("challenges").child(challenge.id).setValue(challenge).await()
    }

    suspend fun getActivePersonalGoals(): List<Challenge> {
        return appDatabase.challengeDao().getActivePersonalGoals()
    }

    /**
     * Get sessions relevant to a specific goal, filtering by deckId if the goal
     * is scoped to a specific deck, and only sessions after the goal's start date.
     */
    suspend fun getSessionsForGoal(goal: Challenge): List<StudySession> {
        val sessions = sessionDao.getSessionsSince(goal.startDate)
        return if (goal.deckIds.isNotBlank()) {
            sessions.filter { it.deckId == goal.deckIds }
        } else {
            sessions
        }
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
    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val snapshot = remoteDatabase.child("leaderboard")
                .orderByChild("username")
                .equalTo(username.trim().lowercase())
                .get()
                .await()

            snapshot.children.any { child ->
                val childUsername = child.child("username").getValue(String::class.java)
                childUsername?.lowercase() == username.trim().lowercase() && child.key != userId
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun findUserByUsername(username: String): UserProfile? {
        return try {
            val snapshot = remoteDatabase.child("leaderboard")
                .orderByChild("username")
                .equalTo(username.trim().lowercase())
                .limitToFirst(1)
                .get()
                .await()
            
            val child = snapshot.children.firstOrNull() ?: return null
            val data = child.value as? Map<*, *> ?: return null
            val resolvedId = child.key ?: return null
            
            UserProfile(
                id = resolvedId,
                username = data["username"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "User",
                avatar = data["avatar"] as? String ?: "🎓",
                totalPoints = (data["totalPoints"] as? Number)?.toInt() ?: 0,
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = (data["level"] as? Number)?.toInt() ?: 1
            )
        } catch (e: Exception) {
            android.util.Log.e("FRIEND_SEARCH", "findUserByUsername failed: ${e.message}")
            null
        }
    }

    suspend fun fetchPublicProfile(uid: String): UserProfile? {
        return try {
            val snapshot = remoteDatabase.child("leaderboard").child(uid).get().await()
            val data = snapshot.value as? Map<*, *> ?: return null
            
            UserProfile(
                id = uid,
                username = data["username"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "User",
                avatar = data["avatar"] as? String ?: "🎓",
                totalPoints = (data["totalPoints"] as? Number)?.toInt() ?: 0,
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = (data["level"] as? Number)?.toInt() ?: 1
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addFriend(targetId: String, targetProfile: UserProfile) {
        // Check for duplicate friend request
        val existingSnapshot = remoteDatabase.child("users").child(userId)
            .child("friends").child(targetId).get().await()
        if (existingSnapshot.exists()) {
            val existingStatus = existingSnapshot.child("status").getValue(String::class.java)
            when (existingStatus) {
                "accepted" -> throw Exception("You are already friends with this user")
                "sent" -> throw Exception("Friend request already sent")
                "pending" -> throw Exception("This user already sent you a request — check your Pending Requests")
            }
        }
        
        val currentProfile = userProfile.firstOrNull() ?: UserProfile(id = userId)
        val now = System.currentTimeMillis()
        
        // Use a multi-path update so both writes succeed or fail atomically
        val incomingData = mapOf(
            "id" to userId,
            "userId" to targetId,
            "username" to currentProfile.username,
            "displayName" to currentProfile.displayName,
            "avatar" to currentProfile.avatar,
            "status" to "pending",
            "addedAt" to now,
            "totalPoints" to currentProfile.totalPoints,
            "currentStreak" to 0,
            "totalCardsStudied" to 0
        )
        
        val outgoingData = mapOf(
            "id" to targetId,
            "userId" to userId,
            "username" to targetProfile.username,
            "displayName" to targetProfile.displayName,
            "avatar" to targetProfile.avatar,
            "status" to "sent",
            "addedAt" to now,
            "totalPoints" to targetProfile.totalPoints,
            "currentStreak" to 0,
            "totalCardsStudied" to 0
        )
        
        // Atomic multi-path update: writes to both users at once
        val updates = hashMapOf<String, Any>(
            "users/$targetId/friends/$userId" to incomingData,
            "users/$userId/friends/$targetId" to outgoingData
        )
        remoteDatabase.updateChildren(updates).await()
        
        // Keep local DB in sync
        val outgoingRequest = Friend(
            id = targetId,
            userId = userId,
            username = targetProfile.username,
            displayName = targetProfile.displayName,
            avatar = targetProfile.avatar,
            status = "sent",
            addedAt = now,
            totalPoints = targetProfile.totalPoints,
            currentStreak = 0,
            totalCardsStudied = 0
        )
        appDatabase.friendDao().insertFriend(outgoingRequest)
    }

    suspend fun acceptFriendRequest(friend: Friend) {
        val currentProfile = syncProfile() ?: userProfile.firstOrNull() ?: UserProfile(id = userId)
        val now = System.currentTimeMillis()
        
        // My entry: update their request in my list to "accepted"
        val myEntryData = mapOf(
            "id" to friend.id,
            "userId" to userId,
            "username" to friend.username,
            "displayName" to friend.displayName,
            "avatar" to friend.avatar,
            "status" to "accepted",
            "addedAt" to now,
            "totalPoints" to friend.totalPoints,
            "currentStreak" to friend.currentStreak,
            "totalCardsStudied" to friend.totalCardsStudied
        )
        
        // Their entry: update my entry in their list to "accepted"
        val theirEntryData = mapOf(
            "id" to userId,
            "userId" to friend.id,
            "username" to currentProfile.username,
            "displayName" to currentProfile.displayName,
            "avatar" to currentProfile.avatar,
            "status" to "accepted",
            "addedAt" to now,
            "totalPoints" to currentProfile.totalPoints,
            "currentStreak" to 0, // We could sync this too if desired
            "totalCardsStudied" to 0
        )

        // Notification for them
        val notificationData = mapOf(
            "id" to UUID.randomUUID().toString(),
            "type" to "friend_accepted",
            "title" to "Friend Request Accepted",
            "message" to "${currentProfile.displayName} accepted your friend request!",
            "timestamp" to now,
            "read" to false
        )
        
        try {
            // Atomic multi-path update: both sides become "accepted" and notification is sent
            val updates = hashMapOf<String, Any>(
                "users/$userId/friends/${friend.id}" to myEntryData,
                "users/${friend.id}/friends/$userId" to theirEntryData,
                "users/${friend.id}/notifications/${notificationData["id"]}" to notificationData
            )
            remoteDatabase.updateChildren(updates).await()
            
            // Keep local DB in sync
            appDatabase.friendDao().insertFriend(friend.copy(status = "accepted"))
        } catch (e: Exception) {
            android.util.Log.e("FRIEND_ACCEPT", "Failed to accept friend request: ${e.message}")
            throw e
        }
    }

    suspend fun declineFriendRequest(friendId: String) {
        try {
            // Atomic multi-path update: remove from both users' friends lists at once
            val updates = hashMapOf<String, Any?>(
                "users/$userId/friends/$friendId" to null,
                "users/$friendId/friends/$userId" to null
            )
            @Suppress("UNCHECKED_CAST")
            remoteDatabase.updateChildren(updates as Map<String, Any>).await()

            // Remove from local DB so the UI updates immediately
            appDatabase.friendDao().deleteFriend(friendId)
        } catch (e: Exception) {
            android.util.Log.e("FRIEND_ERROR", "Failed to unfriend: ${e.message}")
            // Fallback: at least remove from local DB and my own list
            try {
                remoteDatabase.child("users").child(userId).child("friends").child(friendId).removeValue().await()
                appDatabase.friendDao().deleteFriend(friendId)
            } catch (fallbackErr: Exception) {
                android.util.Log.e("FRIEND_ERROR", "Fallback also failed: ${fallbackErr.message}")
            }
        }
    }

    /**
     * Safely parse a Friend from a Firebase snapshot, using the snapshot key
     * as the authoritative friend ID to avoid Kotlin val-deserialization issues.
     */
    private fun parseFriendFromSnapshot(child: com.google.firebase.database.DataSnapshot): Friend? {
        val data = child.value as? Map<*, *> ?: return null
        val friendId = child.key ?: return null
        return Friend(
            id = friendId,
            userId = data["userId"] as? String ?: "",
            username = data["username"] as? String ?: "",
            displayName = data["displayName"] as? String ?: "",
            avatar = data["avatar"] as? String ?: "",
            status = data["status"] as? String ?: "",
            addedAt = (data["addedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            totalPoints = (data["totalPoints"] as? Number)?.toInt() ?: 0,
            currentStreak = (data["currentStreak"] as? Number)?.toInt() ?: 0,
            totalCardsStudied = (data["totalCardsStudied"] as? Number)?.toInt() ?: 0
        )
    }

    fun getFriendsFlow(): Flow<List<Friend>> = callbackFlow {
        if (!isUserLoggedIn()) {
            trySend(emptyList())
            return@callbackFlow
        }
        val ref = remoteDatabase.child("users").child(userId).child("friends")
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val friends = snapshot.children.mapNotNull { child ->
                    parseFriendFromSnapshot(child)
                }
                trySend(friends)
                
                // Keep local DB in sync
                repositoryScope.launch {
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
