package com.example.data

import com.example.crypto.CryptoEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val appScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val allContacts: Flow<List<ContactEntity>> = chatDao.getAllContacts()
    val allStories: Flow<List<StatusStoryEntity>> = chatDao.getAllStories()
    val allCalls: Flow<List<CallLogEntity>> = chatDao.getAllCalls()
    val starredMessages: Flow<List<MessageEntity>> = chatDao.getStarredMessages()

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> =
        chatDao.getMessagesForChat(chatId)

    fun getContactById(contactId: String): Flow<ContactEntity?> =
        chatDao.getContactById(contactId)

    fun getLastMessageForChat(chatId: String): Flow<MessageEntity?> =
        chatDao.getLastMessageForChat(chatId)

    fun searchMessages(query: String): Flow<List<MessageEntity>> =
        chatDao.searchMessages(query)

    suspend fun checkAndSeedDatabase() {
        val existing = chatDao.getAllContacts().first()
        if (existing.isEmpty()) {
            seedInitialData()
        }
    }

    private suspend fun seedInitialData() {
        val contactAlice = ContactEntity(
            id = "alice_sec",
            name = "Alice • Cryptographe",
            phoneNumber = "+33 6 42 10 98 76",
            avatarColorHex = "#00F59B", // Neon Emerald
            statusMessage = "🔑 Clés rotatives actives | Chiffré E2EE",
            isOnline = true,
            lastSeen = "En ligne",
            isPinned = true,
            isVerified = true,
            safetyNumber = CryptoEngine.generateSafetyNumber("me_user", "alice_sec"),
            publicKeyFingerprint = CryptoEngine.generateShortFingerprint("alice_sec"),
            unreadCount = 1,
            ephemeralTimerMinutes = 1440 // 24h
        )

        val contactDevTeam = ContactEntity(
            id = "group_secops",
            name = "🛡️ SecOps Core & Dev",
            phoneNumber = "Canal chiffré (4 membres)",
            avatarColorHex = "#00D4FF", // Neon Cyan
            statusMessage = "Zero-Knowledge Architecture v3.2",
            isOnline = true,
            lastSeen = "4 participants actifs",
            isPinned = true,
            isVerified = true,
            safetyNumber = CryptoEngine.generateSafetyNumber("me_user", "group_secops"),
            publicKeyFingerprint = CryptoEngine.generateShortFingerprint("group_secops"),
            unreadCount = 2,
            ephemeralTimerMinutes = 0
        )

        val contactSophie = ContactEntity(
            id = "sophie_ux",
            name = "Sophie Martin",
            phoneNumber = "+33 7 89 54 12 30",
            avatarColorHex = "#B388FF", // Neon Violet
            statusMessage = "Mode nuit activé 🌙 Ne pas déranger",
            isOnline = false,
            lastSeen = "Aujourd'hui à 23:14",
            isPinned = false,
            isVerified = true,
            safetyNumber = CryptoEngine.generateSafetyNumber("me_user", "sophie_ux"),
            publicKeyFingerprint = CryptoEngine.generateShortFingerprint("sophie_ux"),
            unreadCount = 0,
            ephemeralTimerMinutes = 0
        )

        val contactMarc = ContactEntity(
            id = "marc_cto",
            name = "Marc Dubois • CTO",
            phoneNumber = "+33 6 12 34 56 78",
            avatarColorHex = "#FF9100", // Neon Amber
            statusMessage = "Audit de sécurité AES-256 terminé ✅",
            isOnline = false,
            lastSeen = "Hier à 19:45",
            isPinned = false,
            isVerified = false,
            safetyNumber = CryptoEngine.generateSafetyNumber("me_user", "marc_cto"),
            publicKeyFingerprint = CryptoEngine.generateShortFingerprint("marc_cto"),
            unreadCount = 0,
            ephemeralTimerMinutes = 60
        )

        val contactLucas = ContactEntity(
            id = "lucas_ai",
            name = "Lucas • SysAdmin",
            phoneNumber = "+33 7 45 67 89 01",
            avatarColorHex = "#00E5FF",
            statusMessage = "Serveurs protégés contre les écoutes tierces",
            isOnline = true,
            lastSeen = "En ligne",
            isPinned = false,
            isVerified = true,
            safetyNumber = CryptoEngine.generateSafetyNumber("me_user", "lucas_ai"),
            publicKeyFingerprint = CryptoEngine.generateShortFingerprint("lucas_ai"),
            unreadCount = 0,
            ephemeralTimerMinutes = 0
        )

        chatDao.insertContacts(listOf(contactAlice, contactDevTeam, contactSophie, contactMarc, contactLucas))

        // Pre-seed encrypted messages
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600000
        val twoHoursAgo = now - 7200000
        val yesterday = now - 86400000

        // Alice messages
        val keyAlice = CryptoEngine.deriveSessionKey("alice_sec")
        val m1Enc = CryptoEngine.encrypt("Bonjour ! As-tu vérifié l'empreinte de sécurité de notre canal ?", keyAlice)
        val m2Enc = CryptoEngine.encrypt("Oui, le protocole AES-256-GCM est bien synchronisé. Tout est sécurisé.", keyAlice)
        val m3Enc = CryptoEngine.encrypt("Parfait ! Personne d'autre ne peut intercepter nos échanges.", keyAlice)

        val aliceMessages = listOf(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "alice_sec",
                senderId = "alice_sec",
                plainText = "Bonjour ! As-tu vérifié l'empreinte de sécurité de notre canal ?",
                cipherText = m1Enc.cipherTextBase64,
                ivHex = m1Enc.ivHex,
                authTagHex = m1Enc.authTagHex,
                timestamp = twoHoursAgo,
                status = MessageStatus.READ,
                messageType = MessageType.TEXT
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "alice_sec",
                senderId = "me",
                plainText = "Oui, le protocole AES-256-GCM est bien synchronisé. Tout est sécurisé.",
                cipherText = m2Enc.cipherTextBase64,
                ivHex = m2Enc.ivHex,
                authTagHex = m2Enc.authTagHex,
                timestamp = oneHourAgo,
                status = MessageStatus.READ,
                messageType = MessageType.TEXT
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "alice_sec",
                senderId = "alice_sec",
                plainText = "Parfait ! Personne d'autre ne peut intercepter nos échanges.",
                cipherText = m3Enc.cipherTextBase64,
                ivHex = m3Enc.ivHex,
                authTagHex = m3Enc.authTagHex,
                timestamp = now - 900000,
                status = MessageStatus.DELIVERED,
                messageType = MessageType.TEXT
            )
        )

        // DevTeam messages
        val keyGroup = CryptoEngine.deriveSessionKey("group_secops")
        val g1Enc = CryptoEngine.encrypt("Rappel : Les sauvegardes sont chiffrées avec votre clé maîtresse locale.", keyGroup)
        val g2Enc = CryptoEngine.encrypt("Le mode nuit néon à contraste optimisé a été validé pour l'équipe de garde.", keyGroup)

        val groupMessages = listOf(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "group_secops",
                senderId = "group_secops",
                plainText = "Rappel : Les sauvegardes sont chiffrées avec votre clé maîtresse locale.",
                cipherText = g1Enc.cipherTextBase64,
                ivHex = g1Enc.ivHex,
                authTagHex = g1Enc.authTagHex,
                timestamp = yesterday,
                status = MessageStatus.READ,
                messageType = MessageType.TEXT
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "group_secops",
                senderId = "group_secops",
                plainText = "Le mode nuit néon à contraste optimisé a été validé pour l'équipe de garde.",
                cipherText = g2Enc.cipherTextBase64,
                ivHex = g2Enc.ivHex,
                authTagHex = g2Enc.authTagHex,
                timestamp = now - 1800000,
                status = MessageStatus.DELIVERED,
                messageType = MessageType.TEXT
            )
        )

        // Sophie messages (Voice Note simulation)
        val keySophie = CryptoEngine.deriveSessionKey("sophie_ux")
        val s1Enc = CryptoEngine.encrypt("Message vocal sécurisé (14s)", keySophie)
        val s2Enc = CryptoEngine.encrypt("J'ai adoré le rendu sombre OLED avec l'accent vert néon !", keySophie)

        val sophieMessages = listOf(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "sophie_ux",
                senderId = "sophie_ux",
                plainText = "Message vocal sécurisé (14s)",
                cipherText = s1Enc.cipherTextBase64,
                ivHex = s1Enc.ivHex,
                authTagHex = s1Enc.authTagHex,
                timestamp = yesterday - 10000,
                status = MessageStatus.READ,
                messageType = MessageType.VOICE_NOTE,
                voiceDurationSeconds = 14
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "sophie_ux",
                senderId = "sophie_ux",
                plainText = "J'ai adoré le rendu sombre OLED avec l'accent vert néon !",
                cipherText = s2Enc.cipherTextBase64,
                ivHex = s2Enc.ivHex,
                authTagHex = s2Enc.authTagHex,
                timestamp = yesterday,
                status = MessageStatus.READ,
                messageType = MessageType.TEXT
            )
        )

        chatDao.insertMessages(aliceMessages + groupMessages + sophieMessages)

        // Stories / Statuts
        val stories = listOf(
            StatusStoryEntity(
                id = "story_1",
                contactId = "alice_sec",
                contactName = "Alice",
                avatarColorHex = "#00F59B",
                textCaption = "🔒 Clé de session réinitialisée. Confidentialité totale garantie !",
                backgroundGradientIndex = 0,
                timestamp = now - 7200000,
                isViewed = false
            ),
            StatusStoryEntity(
                id = "story_2",
                contactId = "sophie_ux",
                contactName = "Sophie Martin",
                avatarColorHex = "#B388FF",
                textCaption = "🌙 Session de lecture nocturne sans fatigue oculaire.",
                backgroundGradientIndex = 1,
                timestamp = now - 14400000,
                isViewed = false
            ),
            StatusStoryEntity(
                id = "story_3",
                contactId = "marc_cto",
                contactName = "Marc Dubois",
                avatarColorHex = "#FF9100",
                textCaption = "⚡ Zero-Knowledge validé : vos clés restent sur votre appareil.",
                backgroundGradientIndex = 2,
                timestamp = now - 28800000,
                isViewed = true
            )
        )
        chatDao.insertStories(stories)

        // Call logs
        val calls = listOf(
            CallLogEntity(
                id = "call_1",
                contactId = "alice_sec",
                contactName = "Alice • Cryptographe",
                avatarColorHex = "#00F59B",
                callType = CallType.AUDIO,
                direction = CallDirection.INCOMING,
                timestamp = now - 3600000 * 5,
                durationSeconds = 184,
                isEncrypted = true
            ),
            CallLogEntity(
                id = "call_2",
                contactId = "sophie_ux",
                contactName = "Sophie Martin",
                avatarColorHex = "#B388FF",
                callType = CallType.VIDEO,
                direction = CallDirection.OUTGOING,
                timestamp = yesterday,
                durationSeconds = 420,
                isEncrypted = true
            ),
            CallLogEntity(
                id = "call_3",
                contactId = "marc_cto",
                contactName = "Marc Dubois • CTO",
                avatarColorHex = "#FF9100",
                callType = CallType.AUDIO,
                direction = CallDirection.MISSED,
                timestamp = yesterday - 14400000,
                durationSeconds = 0,
                isEncrypted = true
            )
        )
        chatDao.insertCalls(calls)
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        messageType: MessageType = MessageType.TEXT,
        voiceDurationSeconds: Int = 0,
        mediaUri: String? = null
    ) {
        val sessionKey = CryptoEngine.deriveSessionKey(chatId)
        val encrypted = CryptoEngine.encrypt(text, sessionKey)

        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "me",
            plainText = text,
            cipherText = encrypted.cipherTextBase64,
            ivHex = encrypted.ivHex,
            authTagHex = encrypted.authTagHex,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            messageType = messageType,
            voiceDurationSeconds = voiceDurationSeconds,
            mediaUri = mediaUri,
            isStarred = false,
            isDecryptedLocally = true
        )

        chatDao.insertMessage(message)

        // Transition status from SENT -> DELIVERED -> READ
        appScope.launch {
            delay(600)
            chatDao.updateMessage(message.copy(status = MessageStatus.DELIVERED))
            delay(700)
            chatDao.updateMessage(message.copy(status = MessageStatus.READ))

            // Trigger realistic automated encrypted reply from the contact
            simulateContactResponse(chatId, text)
        }
    }

    private suspend fun simulateContactResponse(chatId: String, userText: String) {
        val contact = chatDao.getContactByIdSync(chatId) ?: return
        delay(1400) // Realistic typing time

        val replyText = generateContextualReply(contact.name, userText)
        val sessionKey = CryptoEngine.deriveSessionKey(chatId)
        val encrypted = CryptoEngine.encrypt(replyText, sessionKey)

        val replyMessage = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = chatId,
            plainText = replyText,
            cipherText = encrypted.cipherTextBase64,
            ivHex = encrypted.ivHex,
            authTagHex = encrypted.authTagHex,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ,
            messageType = MessageType.TEXT
        )

        chatDao.insertMessage(replyMessage)
    }

    private fun generateContextualReply(contactName: String, prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("bonjour") || lower.contains("salut") || lower.contains("hello") || lower.contains("coucou") ->
                "Salut ! J'ai bien reçu ton message chiffré via notre clé partagée. Tout fonctionne parfaitement."
            lower.contains("chiffr") || lower.contains("e2ee") || lower.contains("cle") || lower.contains("sécurit") || lower.contains("securit") ->
                "Les paquets sont vérifiés avec AES-GCM et le code de sécurité 60 chiffres correspond exactement. Aucune interception possible !"
            lower.contains("nuit") || lower.contains("sombre") || lower.contains("neon") || lower.contains("yeux") ->
                "Le thème néon sombre est vraiment reposant pour les yeux la nuit. Le contraste OLED et la lueur ambrée évitent toute fatigue."
            lower.contains("vocal") || lower.contains("audio") || lower.contains("appel") ->
                "Bien reçu ! Le canal audio temps réel est également chiffré avec notre secret éphémère."
            lower.contains("photo") || lower.contains("image") ->
                "Superbe image, la charge binaire a été transmise de manière complètement scellée."
            else ->
                "Message déchiffré avec succès 🔒 (AES-256-GCM). Tout est fluide et confidentiel entre nous."
        }
    }

    suspend fun clearUnread(chatId: String) {
        chatDao.clearUnreadCount(chatId)
    }

    suspend fun toggleStarMessage(messageId: String) {
        chatDao.toggleStar(messageId)
    }

    suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessage(messageId)
    }

    suspend fun clearChat(chatId: String) {
        chatDao.clearChat(chatId)
    }

    suspend fun updateContactVerification(contactId: String, isVerified: Boolean) {
        chatDao.updateVerification(contactId, isVerified)
    }

    suspend fun updateEphemeralTimer(contactId: String, minutes: Int) {
        chatDao.updateEphemeralTimer(contactId, minutes)
        // Add a security notice in chat
        val timerLabel = when (minutes) {
            0 -> "désactivé"
            5 -> "5 minutes"
            60 -> "1 heure"
            1440 -> "24 heures"
            else -> "$minutes minutes"
        }
        val notice = "⌛ Vous avez défini le délai d'expiration des messages éphémères sur $timerLabel."
        val key = CryptoEngine.deriveSessionKey(contactId)
        val enc = CryptoEngine.encrypt(notice, key)
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = contactId,
            senderId = "system",
            plainText = notice,
            cipherText = enc.cipherTextBase64,
            ivHex = enc.ivHex,
            authTagHex = enc.authTagHex,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ,
            messageType = MessageType.SECURITY_ALERT
        )
        chatDao.insertMessage(msg)
    }

    suspend fun addNewContact(name: String, phone: String, statusMsg: String = "Disponible"): ContactEntity {
        val id = "contact_" + UUID.randomUUID().toString().substring(0, 8)
        val colors = listOf("#00F59B", "#00D4FF", "#B388FF", "#FF9100", "#00E5FF", "#FF5252")
        val avatarColor = colors.random()
        val contact = ContactEntity(
            id = id,
            name = name,
            phoneNumber = phone,
            avatarColorHex = avatarColor,
            statusMessage = statusMsg,
            isOnline = true,
            lastSeen = "En ligne",
            isPinned = false,
            isVerified = false,
            safetyNumber = CryptoEngine.generateSafetyNumber("me_user", id),
            publicKeyFingerprint = CryptoEngine.generateShortFingerprint(id),
            unreadCount = 0,
            ephemeralTimerMinutes = 0
        )
        chatDao.insertContact(contact)

        // Add security handshake message
        val handshakeText = "🔒 Les messages envoyés dans cette discussion sont chiffrés de bout en bout avec AES-256. Touchez pour vérifier les clés."
        val key = CryptoEngine.deriveSessionKey(id)
        val enc = CryptoEngine.encrypt(handshakeText, key)
        val initialMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = id,
            senderId = "system",
            plainText = handshakeText,
            cipherText = enc.cipherTextBase64,
            ivHex = enc.ivHex,
            authTagHex = enc.authTagHex,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ,
            messageType = MessageType.SECURITY_ALERT
        )
        chatDao.insertMessage(initialMsg)

        return contact
    }

    suspend fun recordCall(
        contactId: String,
        contactName: String,
        avatarColorHex: String,
        callType: CallType,
        direction: CallDirection,
        durationSeconds: Int
    ) {
        val call = CallLogEntity(
            id = UUID.randomUUID().toString(),
            contactId = contactId,
            contactName = contactName,
            avatarColorHex = avatarColorHex,
            callType = callType,
            direction = direction,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            isEncrypted = true
        )
        chatDao.insertCall(call)
    }

    suspend fun markStoryViewed(storyId: String) {
        chatDao.markStoryViewed(storyId)
    }

    suspend fun addStatusStory(caption: String, gradientIndex: Int = 0) {
        val story = StatusStoryEntity(
            id = UUID.randomUUID().toString(),
            contactId = "me_user",
            contactName = "Moi",
            avatarColorHex = "#00F59B",
            textCaption = caption,
            backgroundGradientIndex = gradientIndex,
            timestamp = System.currentTimeMillis(),
            isViewed = false
        )
        chatDao.insertStory(story)
    }
}
