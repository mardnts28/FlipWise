const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * 1. SECURE LEADERBOARD UPDATE
 * Triggered when a user saves a new study session.
 * Calculates points on the server to prevent tampering.
 */
exports.updateLeaderboardOnSession = functions.database
    .ref("/users/{uid}/sessions/{sessionId}")
    .onCreate(async (snapshot, context) => {
      const session = snapshot.val();
      const uid = context.params.uid;

      // Calculate points securely on the server
      const pointsEarned = (session.cardsStudied || 0) * 10 +
          (session.correctCount || 0) * 5;

      // Update user's private profile totals
      const profileRef = admin.database().ref(`/users/${uid}/profile`);
      const profileSnap = await profileRef.get();
      const profile = profileSnap.val() || {};

      const newTotalPoints = (profile.totalPoints || 0) + pointsEarned;
      const newXP = (profile.xp || 0) + pointsEarned;
      const newLevel = Math.floor(Math.sqrt(newXP / 100)) + 1;

      await profileRef.update({
        totalPoints: newTotalPoints,
        xp: newXP,
        level: newLevel,
      });

      // Update public leaderboard
      await admin.database().ref(`/leaderboard/${uid}`).update({
        id: uid,
        username: profile.username || "Anonymous",
        displayName: profile.displayName || "User",
        avatar: profile.avatar || "🎓",
        totalPoints: newTotalPoints,
        xp: newXP,
        level: newLevel,
      });

      console.log(`Securely updated points for ${uid}: +${pointsEarned}`);
      return null;
    });

/**
 * 2. SYNC PROFILE TO LEADERBOARD
 * Syncs display names and avatars with profile edits.
 */
exports.updateLeaderboardOnProfile = functions.database
    .ref("/users/{uid}/profile")
    .onUpdate(async (change, context) => {
      const profile = change.after.val();
      const uid = context.params.uid;

      await admin.database().ref(`/leaderboard/${uid}`).update({
        username: profile.username || "Anonymous",
        displayName: profile.displayName || "User",
        avatar: profile.avatar || "🎓",
        totalPoints: profile.totalPoints || 0,
        xp: profile.xp || 0,
        level: profile.level || 1,
      });

      return console.log(`Synced profile for ${uid} to leaderboard`);
    });
