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
            modelName = "gemini-1.5-flash", // Use 1.5 Flash for better compatibility with 0.9.0 SDK
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
            val responseText = response.text?.trim() ?: return Result.failure(Exception("AI returned an empty response. This might be due to safety filters blocking the content."))

            val cards = parseResponse(responseText)
            if (cards.isEmpty()) {
                // If parsing fails, maybe the response was just not in JSON format
                Result.failure(Exception("AI generated content but it couldn't be parsed into flashcards. Please try again with a different part of the text."))
            } else {
                Result.success(cards)
            }
        } catch (e: Exception) {
            Result.failure(Exception("AI connection failed: ${e.localizedMessage}. Please check your internet connection and API key."))
        }
    }

    private fun parseResponse(responseText: String): List<GeneratedCard> {
        // Step 1: Try direct parsing if it's clean JSON
        try {
            val type = object : TypeToken<List<GeneratedCard>>() {}.type
            val result: List<GeneratedCard>? = Gson().fromJson(responseText, type)
            if (result != null) return result
        } catch (e: Exception) { /* Continue to fallback */ }

        // Step 2: Try cleaning markdown code blocks
        try {
            val cleaned = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            val type = object : TypeToken<List<GeneratedCard>>() {}.type
            val result: List<GeneratedCard>? = Gson().fromJson(cleaned, type)
            if (result != null) return result
        } catch (e: Exception) { /* Continue to fallback */ }

        // Step 3: Extract anything between the first [ and last ]
        return try {
            val jsonStart = responseText.indexOf('[')
            val jsonEnd = responseText.lastIndexOf(']')
            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                val jsonStr = responseText.substring(jsonStart, jsonEnd + 1)
                val type = object : TypeToken<List<GeneratedCard>>() {}.type
                Gson().fromJson(jsonStr, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
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
