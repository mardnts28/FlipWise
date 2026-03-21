package com.flipwise.app.data.ai

import com.flipwise.app.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class GeneratedCard(
    val front: String,
    val back: String
)

class GeminiFlashcardGenerator {

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    suspend fun generateFlashcards(text: String, maxCards: Int = 15): Result<List<GeneratedCard>> {
        val safeText = sanitizeInput(text)
        return try {
            val prompt = """
You are an expert flashcard creator. Analyze the following text and create effective study flashcards.

Rules:
1. Create between 5 and $maxCards flashcards based on the content length and complexity.
2. Each flashcard should have a "front" (question) and "back" (answer).
3. Mix question types: definitions, concepts, key facts, and fill-in-the-blank.
4. Keep questions clear and concise.
5. Keep answers brief but complete.
6. Focus on the most important information.
7. Do NOT include any markdown formatting in the questions or answers.

IMPORTANT: Return ONLY a valid JSON array with no extra text, no markdown code blocks, no explanation. 
The format must be exactly:
[{"front":"question here","back":"answer here"},{"front":"question here","back":"answer here"}]

Text to create flashcards from:
---
$safeText
---
            """.trimIndent()

            val response = model.generateContent(prompt)
            val responseText = response.text?.trim() ?: return Result.failure(Exception("AI returned an empty response"))

            val cards = parseResponse(responseText)
            if (cards.isEmpty()) {
                Result.failure(Exception("Could not generate flashcards from the provided content"))
            } else {
                Result.success(cards)
            }
        } catch (e: Exception) {
            Result.failure(Exception("AI generation failed: ${e.message}"))
        }
    }

    private fun parseResponse(responseText: String): List<GeneratedCard> {
        return try {
            // Clean the response - remove markdown code block markers if present
            val cleaned = responseText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val type = object : TypeToken<List<GeneratedCard>>() {}.type
            Gson().fromJson<List<GeneratedCard>>(cleaned, type) ?: emptyList()
        } catch (e: Exception) {
            // Try to extract JSON array from the response as a fallback
            try {
                val jsonStart = responseText.indexOf('[')
                val jsonEnd = responseText.lastIndexOf(']')
                if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                    val jsonStr = responseText.substring(jsonStart, jsonEnd + 1)
                    val type = object : TypeToken<List<GeneratedCard>>() {}.type
                    Gson().fromJson<List<GeneratedCard>>(jsonStr, type) ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    private fun sanitizeInput(input: String): String {
        // Remove common prompt injection keywords to prevent instruction override
        val restrictedKeywords = listOf("ignore", "previous", "instructions", "system", "prompt", "override")
        var sanitized = input
        restrictedKeywords.forEach { keyword ->
            sanitized = sanitized.replace(Regex("(?i)\\b$keyword\\b"), "[REDACTED]")
        }
        // Limit input size to prevent token exhaustion and buffer attacks
        return sanitized.take(10000).trim()
    }
}
