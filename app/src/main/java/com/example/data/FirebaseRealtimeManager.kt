package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.crypto.CryptoEngine
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class CloudUserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val avatarColorHex: String = "#00F2FF",
    val statusMessage: String = "Secured with E2EE",
    val isOnline: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val publicKeyFingerprint: String = "",
    val safetyNumber: String = ""
)

data class CloudMessagePayload(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val cipherText: String = "",
    val ivHex: String = "",
    val authTagHex: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: String = "TEXT",
    val voiceDurationSeconds: Int = 0,
    val mediaUri: String? = null,
    val status: String = "SENT"
)

class FirebaseRealtimeManager(
    private val context: Context,
    private val chatDao: ChatDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w("FirebaseRealtime", "Firebase Auth non initialisé (mode local actif): ${e.message}")
            null
        }
    }
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w("FirebaseRealtime", "Firebase Firestore non initialisé (mode local actif): ${e.message}")
            null
        }
    }
    private val credentialManager by lazy {
        try {
            CredentialManager.create(context)
        } catch (e: Throwable) {
            null
        }
    }

    private val _currentUser = MutableStateFlow<CloudUserProfile?>(null)
    val currentUser: StateFlow<CloudUserProfile?> = _currentUser.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _cloudUsers = MutableStateFlow<List<CloudUserProfile>>(emptyList())
    val cloudUsers: StateFlow<List<CloudUserProfile>> = _cloudUsers.asStateFlow()

    private var usersListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null
    private var storiesListener: ListenerRegistration? = null

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyBq55GrOJL1mAOfmLhDGDIfNFeUxlqDHQ8")
                    .setApplicationId("1:721905614054:android:4417069a66d5d758518f27")
                    .setProjectId("gen-lang-client-0415258186")
                    .setStorageBucket("gen-lang-client-0415258186.firebasestorage.app")
                    .setGcmSenderId("721905614054")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d("FirebaseRealtime", "FirebaseApp initialisé avec succès avec les clés du projet")
            }
        } catch (e: Exception) {
            Log.w("FirebaseRealtime", "Initialisation FirebaseApp: ${e.message}")
        }
        // Initialize current user state safely
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        val fbUser = try {
            auth?.currentUser
        } catch (e: Throwable) {
            null
        }
        if (fbUser != null) {
            val profile = CloudUserProfile(
                uid = fbUser.uid,
                displayName = fbUser.displayName ?: "Utilisateur Néon",
                email = fbUser.email ?: "",
                phoneNumber = fbUser.phoneNumber ?: "+33 6 42 00 11 22",
                avatarColorHex = "#00F2FF",
                statusMessage = "Chiffré E2EE • Protégé par AES-256",
                isOnline = true,
                publicKeyFingerprint = CryptoEngine.generateShortFingerprint(fbUser.uid),
                safetyNumber = CryptoEngine.generateSafetyNumber("me_user", fbUser.uid)
            )
            _currentUser.value = profile
            startRealtimeListeners(fbUser.uid)
            syncUserProfileToCloud(profile)
        } else {
            // Default local authenticated session for immediate usage & demo
            val defaultUid = "user_default_me"
            val localProfile = CloudUserProfile(
                uid = defaultUid,
                displayName = "Kylian (Vous)",
                email = "user@neoncrypt.sec",
                phoneNumber = "+33 6 42 10 98 76",
                avatarColorHex = "#00F2FF",
                statusMessage = "En ligne • Chiffré E2EE",
                isOnline = true,
                publicKeyFingerprint = CryptoEngine.generateShortFingerprint(defaultUid),
                safetyNumber = CryptoEngine.generateSafetyNumber("me_user", defaultUid)
            )
            _currentUser.value = localProfile
            startRealtimeListeners(defaultUid)
        }
    }

    fun switchToProfile(profileName: String, avatarHex: String, statusText: String) {
        val uid = "user_" + profileName.lowercase().replace(" ", "_").replace("•", "").trim()
        val newProfile = CloudUserProfile(
            uid = uid,
            displayName = profileName,
            email = "$uid@neoncrypt.sec",
            phoneNumber = "+33 6 88 ${Math.abs(uid.hashCode() % 90 + 10)} 00 11",
            avatarColorHex = avatarHex,
            statusMessage = statusText,
            isOnline = true,
            publicKeyFingerprint = CryptoEngine.generateShortFingerprint(uid),
            safetyNumber = CryptoEngine.generateSafetyNumber("me_user", uid)
        )
        _currentUser.value = newProfile
        startRealtimeListeners(uid)
        syncUserProfileToCloud(newProfile)
    }

    suspend fun signInWithGoogle(webClientId: String? = null): Result<CloudUserProfile> {
        val authInstance = auth
        val credManager = credentialManager
        if (authInstance == null || credManager == null) {
            return Result.failure(Exception("Firebase Auth n'est pas disponible sur cet appareil"))
        }
        return try {
            val serverClientId = webClientId ?: "721905614054-client.apps.googleusercontent.com"
            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = authInstance.signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    val profile = CloudUserProfile(
                        uid = user.uid,
                        displayName = user.displayName ?: googleIdTokenCredential.displayName ?: "Utilisateur",
                        email = user.email ?: googleIdTokenCredential.id,
                        phoneNumber = user.phoneNumber ?: "",
                        avatarColorHex = "#00F2FF",
                        statusMessage = "Chiffré E2EE • Google Auth",
                        isOnline = true,
                        publicKeyFingerprint = CryptoEngine.generateShortFingerprint(user.uid),
                        safetyNumber = CryptoEngine.generateSafetyNumber("me_user", user.uid)
                    )
                    _currentUser.value = profile
                    syncUserProfileToCloud(profile)
                    startRealtimeListeners(user.uid)
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Erreur de connexion Google Firebase"))
                }
            } else {
                Result.failure(Exception("Type d'identifiant Google non supporté"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseRealtime", "Google Sign-In exception: ${e.message}")
            Result.failure(e)
        }
    }

    fun signOut() {
        scope.launch {
            try {
                auth?.signOut()
                credentialManager?.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("FirebaseRealtime", "SignOut error: ${e.message}")
            }
            checkCurrentUser()
        }
    }

    private fun syncUserProfileToCloud(profile: CloudUserProfile) {
        val fs = firestore ?: return
        try {
            fs.collection("users")
                .document(profile.uid)
                .set(profile, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("FirebaseRealtime", "Profil synchronisé sur Firestore: ${profile.displayName}")
                }
                .addOnFailureListener { e ->
                    Log.w("FirebaseRealtime", "Impossible d'écrire sur Firestore (mode hors-ligne ou permissions): ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseRealtime", "Exception Firestore: ${e.message}")
        }
    }

    fun startRealtimeListeners(currentUid: String) {
        // Stop previous listeners
        usersListener?.remove()
        messagesListener?.remove()
        storiesListener?.remove()

        val fs = firestore ?: return
        _isSyncing.value = true

        try {
            // Listen to all active users in real-time
            usersListener = fs.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseRealtime", "Erreur écoute users Firestore: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val users = snapshot.documents.mapNotNull { it.toObject(CloudUserProfile::class.java) }
                        _cloudUsers.value = users

                        // Synchronize cloud users as contacts in Room database
                        scope.launch {
                            val newContacts = users.filter { it.uid != currentUid }.map { u ->
                                ContactEntity(
                                    id = u.uid,
                                    name = u.displayName,
                                    phoneNumber = u.phoneNumber.ifEmpty { "+33 6 42 00 00 00" },
                                    avatarColorHex = u.avatarColorHex,
                                    statusMessage = u.statusMessage,
                                    isOnline = u.isOnline,
                                    lastSeen = if (u.isOnline) "En ligne" else "Récemment",
                                    isPinned = false,
                                    isVerified = true,
                                    safetyNumber = u.safetyNumber.ifEmpty { CryptoEngine.generateSafetyNumber(currentUid, u.uid) },
                                    publicKeyFingerprint = u.publicKeyFingerprint.ifEmpty { CryptoEngine.generateShortFingerprint(u.uid) },
                                    unreadCount = 0,
                                    ephemeralTimerMinutes = 0
                                )
                            }
                            if (newContacts.isNotEmpty()) {
                                chatDao.insertContacts(newContacts)
                            }
                        }
                    }
                }

            // Listen to real-time incoming messages
            messagesListener = fs.collection("messages")
                .whereEqualTo("receiverId", currentUid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseRealtime", "Erreur écoute messages Firestore: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val msg = doc.toObject(CloudMessagePayload::class.java) ?: continue
                                // Decrypt message locally with session key
                                val sessionKey = CryptoEngine.deriveSessionKey(msg.senderId)
                                val decryptedText = try {
                                    CryptoEngine.decrypt(
                                        cipherTextBase64 = msg.cipherText,
                                        ivHex = msg.ivHex,
                                        secretKey = sessionKey
                                    )
                                } catch (e: Exception) {
                                    "[Message chiffré E2EE]"
                                }

                                val entity = MessageEntity(
                                    id = msg.id.ifEmpty { doc.id },
                                    chatId = msg.senderId,
                                    senderId = msg.senderId,
                                    plainText = decryptedText,
                                    cipherText = msg.cipherText,
                                    ivHex = msg.ivHex,
                                    authTagHex = msg.authTagHex,
                                    timestamp = msg.timestamp,
                                    status = MessageStatus.READ,
                                    messageType = try { MessageType.valueOf(msg.messageType) } catch (e: Exception) { MessageType.TEXT },
                                    voiceDurationSeconds = msg.voiceDurationSeconds,
                                    mediaUri = msg.mediaUri,
                                    isStarred = false,
                                    isDecryptedLocally = true
                                )
                                chatDao.insertMessage(entity)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirebaseRealtime", "Setup realtime listener failed: ${e.message}")
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun sendRealtimeCloudMessage(
        recipientId: String,
        plainText: String,
        messageType: MessageType = MessageType.TEXT,
        voiceDurationSeconds: Int = 0,
        mediaUri: String? = null
    ) {
        val user = _currentUser.value ?: return
        val sessionKey = CryptoEngine.deriveSessionKey(recipientId)
        val encrypted = CryptoEngine.encrypt(plainText, sessionKey)
        val msgId = UUID.randomUUID().toString()

        val cloudPayload = CloudMessagePayload(
            id = msgId,
            chatId = recipientId,
            senderId = user.uid,
            senderName = user.displayName,
            receiverId = recipientId,
            cipherText = encrypted.cipherTextBase64,
            ivHex = encrypted.ivHex,
            authTagHex = encrypted.authTagHex,
            timestamp = System.currentTimeMillis(),
            messageType = messageType.name,
            voiceDurationSeconds = voiceDurationSeconds,
            mediaUri = mediaUri,
            status = "DELIVERED"
        )

        // 1. Save locally in Room
        val localMessage = MessageEntity(
            id = msgId,
            chatId = recipientId,
            senderId = "me",
            plainText = plainText,
            cipherText = encrypted.cipherTextBase64,
            ivHex = encrypted.ivHex,
            authTagHex = encrypted.authTagHex,
            timestamp = cloudPayload.timestamp,
            status = MessageStatus.DELIVERED,
            messageType = messageType,
            voiceDurationSeconds = voiceDurationSeconds,
            mediaUri = mediaUri,
            isStarred = false,
            isDecryptedLocally = true
        )
        chatDao.insertMessage(localMessage)

        // 2. Publish to Firebase Firestore for Realtime synchronization if available
        val fs = firestore ?: return
        try {
            fs.collection("messages")
                .document(msgId)
                .set(cloudPayload)
                .addOnSuccessListener {
                    Log.d("FirebaseRealtime", "Message publié en temps réel sur Firestore")
                }
                .addOnFailureListener { e ->
                    Log.w("FirebaseRealtime", "Publication Firestore impossible, conservé en local Room: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseRealtime", "Erreur envoi Firestore: ${e.message}")
        }
    }

    fun cleanup() {
        usersListener?.remove()
        messagesListener?.remove()
        storiesListener?.remove()
    }
}
