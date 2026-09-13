package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateResponse(
        prompt: String,
        personality: String,
        contextMemories: List<String> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API Key is not configured. Please add your key in AI Studio Secrets.")
                )
            }

            val systemPrompt = buildString {
                append("You are a personal dedicated AI Assistant acting with the mastery of a ")
                append(personality)
                append(". You have comprehensive expertise across Android (Jetpack Compose, Kotlin, XML, Room), ")
                append("languages (Kotlin, Java, Python, C, C++, C#, Go, Rust, Dart/Flutter, JS/TS, SQL, Bash), ")
                append("DevOps, Docker, Git, Cyber Security, and System Architecture.\n")
                append("STRICT BEHAVIOR RULES:\n")
                append("1. Never make assumptions. Only act on explicit instructions.\n")
                append("2. If the user asks to perform an action on device (make a phone call, send SMS, delete files, change system settings), ")
                append("you MUST state clearly that you need their explicit confirmation before proceeding.\n")
                append("3. Keep voice replies articulate, conversational, accurate, and direct. Support Bengali and English naturally.\n")
                if (contextMemories.isNotEmpty()) {
                    append("User Private Memories:\n")
                    contextMemories.forEach { append("- ").append(it).append("\n") }
                }
            }

            val requestJson = JSONObject().apply {
                // System instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })

                // Contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                return@withContext Result.failure(
                    Exception("Online AI service error: HTTP ${response.code}")
                )
            }

            val responseObj = JSONObject(responseBody)
            val candidates = responseObj.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty response from Online AI"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
