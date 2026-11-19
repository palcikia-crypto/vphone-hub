package com.vphone.hub.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AiClient(
    private val baseUrl: String,
    private val apiKey: String,
    private val model: String
) {
    fun chat(messages: List<Pair<String, String>>): String {
        val payloads = JSONArray()
        messages.forEach { (role, content) ->
            payloads.put(JSONObject().put("role", role).put("content", content))
        }
        val body = JSONObject().put("model", model).put("messages", payloads)
        val conn = URL(baseUrl.trimEnd('/') + "/chat/completions").openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 30_000
            conn.readTimeout = 60_000
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                .bufferedReader().readText()
            if (code !in 200..299) throw IllegalStateException("HTTP $code: $text")
            JSONObject(text).getJSONArray("choices")
                .getJSONObject(0).getJSONObject("message").getString("content")
        } finally {
            conn.disconnect()
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1"
        const val DEFAULT_MODEL = "mistralai/mistral-small-latest"
        private const val PREFS = "ai_config"

        fun loadConfig(ctx: Context): Triple<String, String, String> {
            val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            return Triple(
                p.getString("baseUrl", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL,
                p.getString("apiKey", "") ?: "",
                p.getString("model", DEFAULT_MODEL) ?: DEFAULT_MODEL
            )
        }

        fun saveConfig(ctx: Context, baseUrl: String, apiKey: String, model: String) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("baseUrl", baseUrl)
                .putString("apiKey", apiKey)
                .putString("model", model)
                .apply()
        }
    }
}
