package com.flipwise.app.data.ai

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import java.util.regex.Pattern

class FileTextExtractor(private val context: Context) {

    init {
        PDFBoxResourceLoader.init(context)
    }

    fun extractText(uri: Uri): Result<String> {
        return try {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val fileName = getFileName(uri)

            val text = when {
                mimeType == "application/pdf" || fileName.endsWith(".pdf", true) -> {
                    extractFromPdf(uri)
                }
                mimeType == "text/plain" || fileName.endsWith(".txt", true) -> {
                    extractFromTxt(uri)
                }
                mimeType.contains("wordprocessingml") || 
                fileName.endsWith(".docx", true) -> {
                    extractFromDocx(uri)
                }
                mimeType.contains("presentationml") || 
                fileName.endsWith(".pptx", true) -> {
                    extractFromPptx(uri)
                }
                mimeType.contains("msword") ||
                fileName.endsWith(".doc", true) -> {
                    return Result.failure(Exception("Older Binary Word (.doc) files are not supported. Please save as .docx or .pdf."))
                }
                mimeType.contains("powerpoint") ||
                fileName.endsWith(".ppt", true) -> {
                    return Result.failure(Exception("Older Binary PowerPoint (.ppt) files are not supported. Please save as .pptx or .pdf."))
                }
                else -> {
                    // Try plain text as fallback for actual text files
                    extractFromTxt(uri)
                }
            }

            if (text.isBlank()) {
                Result.failure(Exception("Could not extract any text from the file. Please try a different file format."))
            } else {
                // Limit text to ~15000 chars to avoid exceeding AI token limits
                val trimmedText = if (text.length > 15000) text.take(15000) + "\n[Content truncated...]" else text
                Result.success(trimmedText)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to read file: ${e.message}"))
        }
    }

    private fun extractFromPdf(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open file")
        
        return inputStream.use { stream ->
            val document = PDDocument.load(stream)
            document.use { doc ->
                val stripper = PDFTextStripper()
                stripper.getText(doc)
            }
        }
    }

    private fun extractFromTxt(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open file")
        
        return inputStream.use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
                reader.readText()
            }
        }
    }

    private fun extractFromDocx(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open file")
        val textBuilder = StringBuilder()
        
        ZipInputStream(inputStream).use { zipStream ->
            var entry = zipStream.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    val content = BufferedReader(InputStreamReader(zipStream)).readText()
                    textBuilder.append(stripXmlTags(content))
                }
                entry = zipStream.nextEntry
            }
        }
        return textBuilder.toString()
    }

    private fun extractFromPptx(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open file")
        val textBuilder = StringBuilder()
        
        ZipInputStream(inputStream).use { zipStream ->
            var entry = zipStream.nextEntry
            while (entry != null) {
                // PPTX stores text in slides/slide1.xml, slide2.xml...
                if (entry.name.startsWith("ppt/slides/slide") && entry.name.endsWith(".xml")) {
                    val content = BufferedReader(InputStreamReader(zipStream)).readText()
                    textBuilder.append(stripXmlTags(content)).append("\n---\n")
                }
                entry = zipStream.nextEntry
            }
        }
        return textBuilder.toString()
    }

    private fun stripXmlTags(xml: String): String {
        // Regex to match anything between < and >
        val htmlTagPattern = Pattern.compile("<[^>]*>")
        val matcher = htmlTagPattern.matcher(xml)
        val plainText = matcher.replaceAll(" ")
        
        // Clean up multi-whitespace and common XML entity references
        return plainText
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun getFileName(uri: Uri): String {
        var name = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex) ?: ""
                }
            }
        }
        return name
    }

    fun getFileSize(uri: Uri): Long {
        var size = 0L
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = it.getLong(sizeIndex)
                }
            }
        }
        return size
    }
}
