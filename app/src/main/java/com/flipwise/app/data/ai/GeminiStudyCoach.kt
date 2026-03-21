package com.flipwise.app.data.ai

import com.flipwise.app.BuildConfig
import com.flipwise.app.data.model.StudySession
import com.flipwise.app.data.model.UserProgress
import com.google.ai.client.generativeai.GenerativeModel

data class AiInsight(
    val title: String,
    val description: String,
    val icon: String,
    val actionLabel: String? = null
)

class GeminiStudyCoach {

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    suspend fun getStudyInsights(progress: UserProgress, recentSessions: List<StudySession>): Result<AiInsight> {
        return try {
            val sessionsSummary = recentSessions.joinToString("\n") { 
                "Date: ${it.date}, Cards: ${it.cardsStudied}, Correct: ${it.correctCount}, Points: ${it.pointsEarned}"
            }

            val prompt = """
                You are a study coach in the FlipWise flashcard app. 
                Based on the user's progress and recent study sessions, provide ONE brief, encouraging insight.
                
                Progress:
                - Total Points: ${progress.totalPoints}
                - Total Cards Studied: ${progress.totalCardsStudied}
                - Current Streak: ${progress.currentStreak}
                - Longest Streak: ${progress.longestStreak}
                
                Recent Sessions:
                $sessionsSummary
                
                Your response must only be ONE line in JSON format with exactly:
                {
                  "title": "Short title (e.g., 'Keep it Up!', 'New Record!', 'Morning Scholar')",
                  "description": "Short, encouraging insight or observation (max 15 words)",
                  "icon": "A single emoji representing the insight",
                  "actionLabel": "Optional button text (e.g., 'Start Review', 'See Decks') or null"
                }
                
                Be concise and friendly.
            """.trimIndent()

            val response = model.generateContent(prompt)
            val responseText = response.text?.trim() ?: return Result.failure(Exception("AI returned an empty response"))
            
            val cleaned = responseText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            
            val insight = com.google.gson.Gson().fromJson(cleaned, AiInsight::class.java)
            Result.success(insight)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
