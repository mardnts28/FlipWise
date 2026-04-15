package com.flipwise.app.data.ai

import com.flipwise.app.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class GeneratedCard(
    val front: String,
    val back: String,
    val options: List<String>? = null
)

class GeminiFlashcardGenerator {

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.3f   // Lower = more factual and consistent
                topK = 40
                maxOutputTokens = 4096
            }
        )
    }

    // Fallback model in case gemini-2.0-flash fails
    private val fallbackModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.3f
                topK = 40
                maxOutputTokens = 4096
            }
        )
    }

    suspend fun generateFlashcards(text: String, maxCards: Int = 15): Result<List<GeneratedCard>> {
        val safeText = sanitizeInput(text)
        return try {
            val prompt = buildPolishedPrompt(safeText, maxCards)

            // Try primary model first, then fallback
            val response = try {
                model.generateContent(prompt)
            } catch (e: Exception) {
                fallbackModel.generateContent(prompt)
            }

            val responseText = response.text?.trim()
                ?: return Result.failure(Exception("AI returned an empty response. This might be due to safety filters blocking the content."))

            val cards = parseResponse(responseText)
            if (cards.isEmpty()) {
                Result.failure(Exception("AI generated content but it couldn't be parsed into flashcards. Please try again with a different part of the text."))
            } else {
                // Post-process: clean up any remaining issues
                val polishedCards = cards.map { card ->
                    card.copy(
                        front = polishText(card.front),
                        back = polishText(card.back),
                        options = card.options?.map { polishText(it) }
                    )
                }.filter { it.front.isNotBlank() && it.back.isNotBlank() }

                Result.success(polishedCards)
            }
        } catch (e: Exception) {
            Result.failure(Exception("AI connection failed: ${e.localizedMessage}. Please check your internet connection and API key."))
        }
    }

    private fun buildPolishedPrompt(text: String, maxCards: Int): String {
        return """
You are an expert academic reviewer and flashcard creator for a study app called FlipWise.
Your job is to read the provided study material and produce a comprehensive set of high-quality review flashcards.

━━━ STRICT RULES ━━━

CONTENT QUALITY:
• Step 1: Categorize and summarize the provided text in a "definition = answer" format internally.
• Step 2: Use that organized information to generate MANY distinct question cards.
• Do NOT cram all the information into a single question. 
• Scatter the facts, concepts, and definitions across MULTIPLE unique cards (generate up to $maxCards cards to cover everything).
• Every question must be factually accurate and directly based on the source text.
• Answers must be precise, complete, and grammatically correct.
• Use proper academic language — no slang, no filler, no vague phrasing.

CARD TYPES — create a balanced mix of BOTH types:
1. FLIP CARDS (approximately 50%):
   - "front": A clear, specific question.
   - "back": A concise, complete answer (1–2 sentences max).
   - "options": null (do NOT include options for flip cards).

2. MULTIPLE CHOICE CARDS (approximately 50%):
   - "front": A clear, specific question.
   - "back": The single correct answer (exact text must appear in options).
   - "options": Exactly 4 choices — 1 correct + 3 plausible but wrong distractors.
   - Shuffle the position of the correct answer randomly among the 4 options.

FORMATTING:
• Do NOT use markdown (no **, no ##, no bullet points) in any field.
• Do NOT use numbering like "1." or "a)" inside questions or answers.
• Keep questions under 120 characters when possible.
• Keep answers under 200 characters when possible.

━━━ OUTPUT FORMAT ━━━

Return ONLY a valid JSON array. No explanations, no commentary, no code fences.
Example:
[{"front":"What is photosynthesis?","back":"The process by which plants convert light energy into chemical energy using carbon dioxide and water.","options":null},{"front":"Which organelle performs photosynthesis?","back":"Chloroplast","options":["Mitochondria","Chloroplast","Ribosome","Nucleus"]}]

━━━ SOURCE MATERIAL ━━━
$text
        """.trimIndent()
    }

    /**
     * Cleans up minor text issues from AI output.
     */
    private fun polishText(text: String): String {
        return text
            .replace(Regex("^[\\s*#•\\-]+"), "")   // Strip leading markdown/bullet artifacts
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1") // Remove bold markdown
            .replace(Regex("__(.+?)__"), "$1")         // Remove underline markdown
            .replace(Regex("`(.+?)`"), "$1")           // Remove code markdown
            .replace(Regex("\\s+"), " ")               // Collapse whitespace
            .trim()
    }

    private fun parseResponse(responseText: String): List<GeneratedCard> {
        // Step 1: Try direct parsing if it's clean JSON
        try {
            val type = object : TypeToken<List<GeneratedCard>>() {}.type
            val result: List<GeneratedCard>? = Gson().fromJson(responseText, type)
            if (!result.isNullOrEmpty()) return result
        } catch (e: Exception) { /* Continue to fallback */ }

        // Step 2: Try cleaning markdown code blocks
        try {
            val cleaned = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            val type = object : TypeToken<List<GeneratedCard>>() {}.type
            val result: List<GeneratedCard>? = Gson().fromJson(cleaned, type)
            if (!result.isNullOrEmpty()) return result
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
