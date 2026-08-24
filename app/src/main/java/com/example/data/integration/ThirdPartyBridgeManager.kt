package com.example.data.integration

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
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ThirdPartyApiKey(
    val id: String,
    val name: String,
    val token: String,
    val scopes: List<String>,
    val webhookUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    val totalRequests: Int = 0
)

data class ThirdPartyApiLog(
    val id: String,
    val toolName: String,
    val endpoint: String,
    val method: String,
    val status: Int,
    val payloadSnippet: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)

class ThirdPartyBridgeManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("neoncrypt_thirdparty_prefs", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val _apiKeys = MutableStateFlow<List<ThirdPartyApiKey>>(loadApiKeys())
    val apiKeys: StateFlow<List<ThirdPartyApiKey>> = _apiKeys.asStateFlow()

    private val _apiLogs = MutableStateFlow<List<ThirdPartyApiLog>>(loadLogs())
    val apiLogs: StateFlow<List<ThirdPartyApiLog>> = _apiLogs.asStateFlow()

    private fun serializeKeysToJson(list: List<ThirdPartyApiKey>): String {
        val arr = JSONArray()
        for (k in list) {
            val obj = JSONObject().apply {
                put("id", k.id)
                put("name", k.name)
                put("token", k.token)
                put("scopes", JSONArray(k.scopes))
                put("webhookUrl", k.webhookUrl)
                put("isActive", k.isActive)
                put("createdAt", k.createdAt)
                put("lastUsedAt", k.lastUsedAt)
                put("totalRequests", k.totalRequests)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun loadApiKeys(): List<ThirdPartyApiKey> {
        val rawJson = prefs.getString("keys_json", null)
        if (rawJson.isNullOrBlank()) {
            // Seed 2 default sample integrations for quick testing
            val defaultKey1 = ThirdPartyApiKey(
                id = "key_zapier_bot",
                name = "Zapier Automation Hook",
                token = "nc_live_zapier_" + generateRandomHex(12),
                scopes = listOf("messages:write", "webhook:trigger"),
                webhookUrl = "https://hooks.zapier.com/hooks/catch/sample",
                isActive = true,
                createdAt = System.currentTimeMillis() - 86400000 * 2,
                totalRequests = 14
            )
            val defaultKey2 = ThirdPartyApiKey(
                id = "key_crm_bridge",
                name = "CRM & Support External Bridge",
                token = "nc_live_crm_" + generateRandomHex(12),
                scopes = listOf("messages:read", "messages:write", "users:read"),
                webhookUrl = "https://api.crm-external.io/neoncrypt-inbound",
                isActive = true,
                createdAt = System.currentTimeMillis() - 86400000 * 5,
                totalRequests = 42
            )
            val initialList = listOf(defaultKey1, defaultKey2)
            prefs.edit().putString("keys_json", serializeKeysToJson(initialList)).apply()
            return initialList
        }

        return try {
            val arr = JSONArray(rawJson)
            val list = mutableListOf<ThirdPartyApiKey>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val scopesArr = obj.optJSONArray("scopes") ?: JSONArray()
                val scopesList = mutableListOf<String>()
                for (j in 0 until scopesArr.length()) {
                    scopesList.add(scopesArr.getString(j))
                }
                list.add(
                    ThirdPartyApiKey(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        token = obj.getString("token"),
                        scopes = scopesList,
                        webhookUrl = if (obj.has("webhookUrl") && !obj.isNull("webhookUrl")) obj.getString("webhookUrl") else null,
                        isActive = obj.optBoolean("isActive", true),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        lastUsedAt = if (obj.has("lastUsedAt") && !obj.isNull("lastUsedAt")) obj.getLong("lastUsedAt") else null,
                        totalRequests = obj.optInt("totalRequests", 0)
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("ThirdPartyBridge", "Error loading keys", e)
            emptyList()
        }
    }

    private fun saveApiKeys(list: List<ThirdPartyApiKey>) {
        prefs.edit().putString("keys_json", serializeKeysToJson(list)).apply()
        _apiKeys.value = list
    }

    private fun loadLogs(): List<ThirdPartyApiLog> {
        val raw = prefs.getString("logs_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<ThirdPartyApiLog>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ThirdPartyApiLog(
                        id = obj.getString("id"),
                        toolName = obj.getString("toolName"),
                        endpoint = obj.getString("endpoint"),
                        method = obj.getString("method"),
                        status = obj.getInt("status"),
                        payloadSnippet = obj.getString("payloadSnippet"),
                        timestamp = obj.getLong("timestamp"),
                        isSuccess = obj.getBoolean("isSuccess")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun addLog(log: ThirdPartyApiLog) {
        val current = _apiLogs.value.toMutableList()
        current.add(0, log)
        val trimmed = current.take(50) // keep last 50
        _apiLogs.value = trimmed

        val arr = JSONArray()
        for (l in trimmed) {
            val obj = JSONObject().apply {
                put("id", l.id)
                put("toolName", l.toolName)
                put("endpoint", l.endpoint)
                put("method", l.method)
                put("status", l.status)
                put("payloadSnippet", l.payloadSnippet)
                put("timestamp", l.timestamp)
                put("isSuccess", l.isSuccess)
            }
            arr.put(obj)
        }
        prefs.edit().putString("logs_json", arr.toString()).apply()
    }

    fun createApiKey(
        name: String,
        scopes: List<String>,
        webhookUrl: String? = null
    ): ThirdPartyApiKey {
        val newKey = ThirdPartyApiKey(
            id = "key_" + UUID.randomUUID().toString().substring(0, 8),
            name = name.trim().ifEmpty { "Outil Tiers #" + (1000..9999).random() },
            token = "nc_live_" + generateRandomHex(24),
            scopes = scopes.ifEmpty { listOf("messages:write", "webhook:trigger") },
            webhookUrl = webhookUrl?.trim()?.ifEmpty { null },
            isActive = true,
            createdAt = System.currentTimeMillis(),
            totalRequests = 0
        )
        val updated = _apiKeys.value + newKey
        saveApiKeys(updated)

        addLog(
            ThirdPartyApiLog(
                id = UUID.randomUUID().toString(),
                toolName = newKey.name,
                endpoint = "/api/v1/auth/tokens/create",
                method = "POST",
                status = 201,
                payloadSnippet = "Clé d'intégration créée avec scopes: ${newKey.scopes.joinToString()}",
                isSuccess = true
            )
        )

        return newKey
    }

    fun toggleApiKeyStatus(keyId: String, active: Boolean) {
        val updated = _apiKeys.value.map {
            if (it.id == keyId) it.copy(isActive = active) else it
        }
        saveApiKeys(updated)
    }

    fun deleteApiKey(keyId: String) {
        val updated = _apiKeys.value.filter { it.id != keyId }
        saveApiKeys(updated)
    }

    fun validateToken(token: String): ThirdPartyApiKey? {
        val key = _apiKeys.value.find { it.token == token.trim() && it.isActive } ?: return null
        // Increment usage
        val updated = _apiKeys.value.map {
            if (it.id == key.id) it.copy(lastUsedAt = System.currentTimeMillis(), totalRequests = it.totalRequests + 1) else it
        }
        saveApiKeys(updated)
        return key
    }

    suspend fun testExternalWebhook(
        targetUrl: String,
        eventName: String = "message.received",
        mockPayload: String = """{"event": "message.received", "sender": "Alice", "content": "Test d'intégration outil tiers", "timestamp": ${System.currentTimeMillis()}}"""
    ): Result<String> = withContext(Dispatchers.IO) {
        if (targetUrl.isBlank()) {
            return@withContext Result.failure(Exception("Veuillez renseigner une URL de Webhook cible."))
        }

        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = mockPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("X-NeonCrypt-Event", eventName)
                .addHeader("X-NeonCrypt-Signature", "sha256=" + generateRandomHex(16))
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code
            val respStr = response.body?.string() ?: ""

            addLog(
                ThirdPartyApiLog(
                    id = UUID.randomUUID().toString(),
                    toolName = "Webhook Dispatcher",
                    endpoint = targetUrl,
                    method = "POST",
                    status = code,
                    payloadSnippet = "Event: $eventName -> Code $code",
                    isSuccess = response.isSuccessful
                )
            )

            if (response.isSuccessful) {
                Result.success("Webhook livré avec succès ! (HTTP $code)\nRéponse: ${respStr.take(150)}")
            } else {
                Result.failure(Exception("Échec de livraison Webhook (HTTP $code) : ${respStr.take(150)}"))
            }
        } catch (e: Exception) {
            addLog(
                ThirdPartyApiLog(
                    id = UUID.randomUUID().toString(),
                    toolName = "Webhook Dispatcher",
                    endpoint = targetUrl,
                    method = "POST",
                    status = 500,
                    payloadSnippet = "Exception: ${e.localizedMessage}",
                    isSuccess = false
                )
            )
            Result.failure(Exception("Erreur lors de l'appel Webhook : ${e.localizedMessage}"))
        }
    }

    fun recordThirdPartyEvent(
        toolName: String,
        endpoint: String,
        method: String,
        status: Int,
        snippet: String,
        isSuccess: Boolean = true
    ) {
        addLog(
            ThirdPartyApiLog(
                id = UUID.randomUUID().toString(),
                toolName = toolName,
                endpoint = endpoint,
                method = method,
                status = status,
                payloadSnippet = snippet,
                isSuccess = isSuccess
            )
        )
    }

    private fun generateRandomHex(bytesLength: Int): String {
        val random = SecureRandom()
        val bytes = ByteArray(bytesLength)
        random.nextBytes(bytes)
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}
