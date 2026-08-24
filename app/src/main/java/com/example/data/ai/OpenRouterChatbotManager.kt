package com.example.data.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class OpenRouterModelOption(
    val id: String,
    val name: String,
    val provider: String,
    val isFree: Boolean,
    val description: String
)

data class OpenRouterConfig(
    val apiKey: String = "",
    val selectedModel: String = "google/gemini-2.0-flash-exp:free",
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val isChatbotEnabled: Boolean = true,
    val connectedUserIds: Set<String> = emptySet(), // Users whose messages are answered by AI
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1000,
    val enableAutoReplyForConnectedUsers: Boolean = true
)

private const val DEFAULT_SYSTEM_PROMPT = """Tu es Neon AI, l'assistant intelligent et sécurisé intégré dans NeonCrypt.
Tu réponds de manière concise, précise, chaleureuse et professionnelle.
Tu peux répondre aux questions générales, aider à la rédaction, expliquer la cryptographie et la sécurité, écrire du code, et discuter amicalement avec l'utilisateur.
Utilise des émojis pertinents et adapte-toi à la langue de l'utilisateur."""

class OpenRouterChatbotManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("neoncrypt_openrouter_prefs", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    val availableFreeModels = listOf(
        OpenRouterModelOption(
            id = "google/gemini-2.0-flash-exp:free",
            name = "Gemini 2.0 Flash (Gratuit)",
            provider = "Google",
            isFree = true,
            description = "Ultra rapide, excellente compréhension du français et multimodalité."
        ),
        OpenRouterModelOption(
            id = "meta-llama/llama-3.3-70b-instruct:free",
            name = "Llama 3.3 70B (Gratuit)",
            provider = "Meta AI",
            isFree = true,
            description = "Puissant modèle 70B open-source avec capacités de raisonnement élevées."
        ),
        OpenRouterModelOption(
            id = "deepseek/deepseek-r1:free",
            name = "DeepSeek R1 (Gratuit)",
            provider = "DeepSeek",
            isFree = true,
            description = "Modèle spécialisé dans le raisonnement complexe, les mathématiques et la logique."
        ),
        OpenRouterModelOption(
            id = "qwen/qwen-2.5-coder-32b-instruct:free",
            name = "Qwen 2.5 Coder 32B (Gratuit)",
            provider = "Alibaba / Qwen",
            isFree = true,
            description = "Idéal pour l'assistance technique, la programmation et les scripts."
        ),
        OpenRouterModelOption(
            id = "mistralai/mistral-7b-instruct:free",
            name = "Mistral 7B Instruct (Gratuit)",
            provider = "Mistral AI",
            isFree = true,
            description = "Modèle européen agile, rapide et concis."
        )
    )

    private val _configState = MutableStateFlow(loadConfig())
    val configState: StateFlow<OpenRouterConfig> = _configState.asStateFlow()

    private fun loadConfig(): OpenRouterConfig {
        val apiKey = prefs.getString("api_key", "") ?: ""
        val model = prefs.getString("selected_model", "google/gemini-2.0-flash-exp:free") ?: "google/gemini-2.0-flash-exp:free"
        val prompt = prefs.getString("system_prompt", DEFAULT_SYSTEM_PROMPT) ?: DEFAULT_SYSTEM_PROMPT
        val enabled = prefs.getBoolean("is_enabled", true)
        val connectedUsers = prefs.getStringSet("connected_user_ids", emptySet()) ?: emptySet()
        val autoReply = prefs.getBoolean("auto_reply_enabled", true)
        val temp = prefs.getFloat("temperature", 0.7f)
        val maxTokens = prefs.getInt("max_tokens", 1000)

        return OpenRouterConfig(
            apiKey = apiKey,
            selectedModel = model,
            systemPrompt = prompt,
            isChatbotEnabled = enabled,
            connectedUserIds = connectedUsers,
            temperature = temp,
            maxTokens = maxTokens,
            enableAutoReplyForConnectedUsers = autoReply
        )
    }

    fun updateConfig(
        apiKey: String? = null,
        selectedModel: String? = null,
        systemPrompt: String? = null,
        isChatbotEnabled: Boolean? = null,
        temperature: Float? = null,
        maxTokens: Int? = null,
        enableAutoReply: Boolean? = null
    ) {
        val current = _configState.value
        val newConfig = current.copy(
            apiKey = apiKey ?: current.apiKey,
            selectedModel = selectedModel ?: current.selectedModel,
            systemPrompt = systemPrompt ?: current.systemPrompt,
            isChatbotEnabled = isChatbotEnabled ?: current.isChatbotEnabled,
            temperature = temperature ?: current.temperature,
            maxTokens = maxTokens ?: current.maxTokens,
            enableAutoReplyForConnectedUsers = enableAutoReply ?: current.enableAutoReplyForConnectedUsers
        )

        prefs.edit().apply {
            apiKey?.let { putString("api_key", it.trim()) }
            selectedModel?.let { putString("selected_model", it.trim()) }
            systemPrompt?.let { putString("system_prompt", it) }
            isChatbotEnabled?.let { putBoolean("is_enabled", it) }
            temperature?.let { putFloat("temperature", it) }
            maxTokens?.let { putInt("max_tokens", it) }
            enableAutoReply?.let { putBoolean("auto_reply_enabled", it) }
            apply()
        }

        _configState.value = newConfig
    }

    fun toggleUserConnection(userId: String, isConnected: Boolean) {
        val currentSet = _configState.value.connectedUserIds.toMutableSet()
        if (isConnected) {
            currentSet.add(userId)
        } else {
            currentSet.remove(userId)
        }
        prefs.edit().putStringSet("connected_user_ids", currentSet).apply()
        _configState.value = _configState.value.copy(connectedUserIds = currentSet)
    }

    fun isUserConnectedToBot(userId: String): Boolean {
        return _configState.value.connectedUserIds.contains(userId)
    }

    suspend fun generateChatbotReply(
        conversationHistory: List<Pair<String, String>>, // List of Pair(senderName/role, messageText)
        incomingUserMessage: String,
        customPromptOverride: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val config = _configState.value
        if (!config.isChatbotEnabled) {
            return@withContext Result.failure(Exception("Le chatbot OpenRouter est actuellement désactivé par l'administrateur."))
        }

        // If no API key is provided by admin, provide an informative response with smart fallback
        val effectiveKey = config.apiKey.trim()
        if (effectiveKey.isEmpty()) {
            return@withContext Result.success(
                generateLocalFallbackReply(incomingUserMessage, config.selectedModel)
            )
        }

        try {
            val jsonBody = JSONObject().apply {
                put("model", config.selectedModel)
                put("temperature", config.temperature.toDouble())
                put("max_tokens", config.maxTokens)

                val messagesArray = JSONArray()

                // System prompt
                val sysPrompt = customPromptOverride ?: config.systemPrompt
                messagesArray.put(JSONObject().apply {
                    put("role", "system")
                    put("content", sysPrompt)
                })

                // Recent conversation context (up to last 6 messages)
                val recentHistory = conversationHistory.takeLast(6)
                for ((role, text) in recentHistory) {
                    val msgRole = if (role == "me" || role == "user") "user" else "assistant"
                    messagesArray.put(JSONObject().apply {
                        put("role", msgRole)
                        put("content", text)
                    })
                }

                // Current message
                messagesArray.put(JSONObject().apply {
                    put("role", "user")
                    put("content", incomingUserMessage)
                })

                put("messages", messagesArray)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .addHeader("Authorization", "Bearer $effectiveKey")
                .addHeader("HTTP-Referer", "https://neoncrypt.app")
                .addHeader("X-Title", "NeonCrypt Chatbot")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("OpenRouter", "HTTP ${response.code}: $responseBodyString")
                val errorMsg = try {
                    val errJson = JSONObject(responseBodyString)
                    val errorObj = errJson.optJSONObject("error")
                    errorObj?.optString("message") ?: "Erreur HTTP ${response.code}"
                } catch (e: Exception) {
                    "Erreur OpenRouter API (Code: ${response.code})"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonResponse = JSONObject(responseBodyString)
            val choices = jsonResponse.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val messageObj = firstChoice.optJSONObject("message")
                val content = messageObj?.optString("content")?.trim() ?: ""
                if (content.isNotEmpty()) {
                    return@withContext Result.success(content)
                }
            }

            Result.failure(Exception("Réponse vide reçue d'OpenRouter."))
        } catch (e: Exception) {
            Log.e("OpenRouter", "Exception while calling OpenRouter", e)
            Result.failure(Exception("Connexion à OpenRouter impossible : ${e.localizedMessage ?: "Vérifiez la clé API et le réseau"}"))
        }
    }

    suspend fun testOpenRouterConnection(apiKey: String, model: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(Exception("Veuillez renseigner une clé API OpenRouter pour effectuer le test."))
        }

        try {
            val jsonBody = JSONObject().apply {
                put("model", model)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "Tu es un assistant IA.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Dis bonjour et confirme que la connexion OpenRouter fonctionne en 1 phrase.")
                    })
                })
                put("max_tokens", 80)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .addHeader("Authorization", "Bearer $trimmedKey")
                .addHeader("HTTP-Referer", "https://neoncrypt.app")
                .addHeader("X-Title", "NeonCrypt Test")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val err = try {
                    JSONObject(responseString).optJSONObject("error")?.optString("message") ?: "Code ${response.code}"
                } catch (e: Exception) {
                    "Erreur HTTP ${response.code}"
                }
                return@withContext Result.failure(Exception("Échec du test OpenRouter : $err"))
            }

            val json = JSONObject(responseString)
            val choices = json.optJSONArray("choices")
            val reply = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content") ?: "Connexion réussie !"
            Result.success(reply.trim())
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau : ${e.localizedMessage}"))
        }
    }

    private fun generateLocalFallbackReply(prompt: String, modelName: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("bonjour") || p.contains("salut") || p.contains("hello") || p.contains("coucou") ->
                "👋 Bonjour ! Je suis **Neon AI** propulsé par OpenRouter (*$modelName*). Comment puis-je vous aider aujourd'hui ?"
            p.contains("qui es-tu") || p.contains("qui est tu") || p.contains("présente") || p.contains("presente") ->
                "🤖 Je suis le Chatbot IA de NeonCrypt configuré via l'API gratuite OpenRouter. L'administrateur peut personnaliser mon modèle (Gemini, Llama, DeepSeek) et ma clé dans la console d'administration."
            p.contains("code") || p.contains("programme") || p.contains("kotlin") || p.contains("python") ->
                "💻 Je peux vous aider à concevoir, débugger et optimiser votre code. Posez-moi votre question technique !"
            p.contains("sécurité") || p.contains("securite") || p.contains("chiffrement") || p.contains("crypto") ->
                "🔐 NeonCrypt utilise un chiffrement militaire AES-256-GCM avec zéro connaissance côté serveur. Tous vos échanges sont scellés cryptographiquement."
            else ->
                "✨ Message bien reçu par Neon AI (*$modelName*) ! [Pour activer l'intelligence complète en direct, l'administrateur peut entrer une clé OpenRouter dans la Console Admin]."
        }
    }
}
