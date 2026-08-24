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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
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
    val plainText: String = "",
    val cipherText: String = "",
    val ivHex: String = "",
    val authTagHex: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: String = "TEXT",
    val voiceDurationSeconds: Int = 0,
    val mediaUri: String? = null,
    val status: String = "DELIVERED"
)

class FirebaseRealtimeManager(
    private val context: Context,
    private val chatDao: ChatDao,
    private val userAccountDao: UserAccountDao? = null,
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

    val firestore: FirebaseFirestore? by lazy {
        try {
            val db = FirebaseFirestore.getInstance()
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                db.firestoreSettings = settings
            } catch (e: Exception) {
                // Settings already set or default
            }
            db
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

    private val activeListeners = mutableListOf<ListenerRegistration>()

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
                Log.d("FirebaseRealtime", "FirebaseApp initialisé avec succès")
            }
        } catch (e: Exception) {
            Log.w("FirebaseRealtime", "Initialisation FirebaseApp: ${e.message}")
        }
        // Initialize current user state and start real-time listeners
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

    fun switchToProfile(profileName: String, avatarHex: String, statusText: String, explicitUid: String? = null) {
        val uid = explicitUid ?: ("user_" + profileName.lowercase().replace(" ", "_").replace("•", "").trim())
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

    fun syncUserProfileToCloud(profile: CloudUserProfile) {
        val fs = firestore ?: return
        try {
            val userMap = hashMapOf(
                "uid" to profile.uid,
                "id" to profile.uid,
                "displayName" to profile.displayName,
                "name" to profile.displayName,
                "email" to profile.email,
                "phoneNumber" to profile.phoneNumber,
                "avatarColorHex" to profile.avatarColorHex,
                "statusMessage" to profile.statusMessage,
                "isOnline" to profile.isOnline,
                "lastSeenTimestamp" to profile.lastSeenTimestamp,
                "publicKeyFingerprint" to profile.publicKeyFingerprint,
                "safetyNumber" to profile.safetyNumber
            )
            fs.collection("users")
                .document(profile.uid)
                .set(userMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("FirebaseRealtime", "Profil synchronisé sur Firestore: ${profile.displayName}")
                }
                .addOnFailureListener { e ->
                    Log.w("FirebaseRealtime", "Erreur écriture Firestore users: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseRealtime", "Exception Firestore: ${e.message}")
        }
    }

    fun syncUserAccountToCloud(user: UserAccountEntity) {
        val fs = firestore ?: return
        try {
            val accountMap = hashMapOf(
                "id" to user.id,
                "uid" to user.id,
                "fullName" to user.fullName,
                "displayName" to user.fullName,
                "username" to user.username,
                "whatsappNumber" to user.whatsappNumber,
                "phoneNumber" to user.whatsappNumber,
                "password" to user.password,
                "avatarColorHex" to user.avatarColorHex,
                "statusMessage" to user.statusMessage,
                "isActive" to user.isActive,
                "isAdmin" to user.isAdmin,
                "createdAt" to user.createdAt,
                "lastLoginAt" to user.lastLoginAt
            )
            fs.collection("user_accounts")
                .document(user.id)
                .set(accountMap, SetOptions.merge())

            fs.collection("users")
                .document(user.id)
                .set(accountMap, SetOptions.merge())
        } catch (e: Exception) {
            Log.e("FirebaseRealtime", "Sync account to cloud exception: ${e.message}")
        }
    }

    fun deleteUserAccountFromCloud(userId: String) {
        val fs = firestore ?: return
        try {
            fs.collection("user_accounts").document(userId).delete()
            fs.collection("users").document(userId).delete()
            fs.collection("contacts").document(userId).delete()
        } catch (e: Exception) {
            Log.e("FirebaseRealtime", "Delete user from cloud: ${e.message}")
        }
    }

    fun startRealtimeListeners(currentUid: String) {
        // Stop previous listeners
        for (listener in activeListeners) {
            listener.remove()
        }
        activeListeners.clear()

        val fs = firestore ?: return
        _isSyncing.value = true

        try {
            // 1. LISTEN TO USERS COLLECTION (Real-time contact sync and console edits)
            val usersRegistration = fs.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseRealtime", "Erreur écoute users Firestore: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            val userProfiles = mutableListOf<CloudUserProfile>()
                            for (doc in snapshot.documents) {
                                val u = parseUserProfile(doc) ?: continue
                                userProfiles.add(u)

                                // Sync contact if not current user
                                if (u.uid != currentUid) {
                                    val contact = ContactEntity(
                                        id = u.uid,
                                        name = u.displayName,
                                        phoneNumber = u.phoneNumber.ifEmpty { "+33 6 42 00 00 00" },
                                        avatarColorHex = u.avatarColorHex,
                                        statusMessage = u.statusMessage,
                                        isOnline = u.isOnline,
                                        lastSeen = if (u.isOnline) "En ligne" else "Récemment",
                                        isPinned = doc.getBoolean("isPinned") ?: false,
                                        isVerified = doc.getBoolean("isVerified") ?: true,
                                        safetyNumber = u.safetyNumber.ifEmpty { CryptoEngine.generateSafetyNumber(currentUid, u.uid) },
                                        publicKeyFingerprint = u.publicKeyFingerprint.ifEmpty { CryptoEngine.generateShortFingerprint(u.uid) },
                                        unreadCount = 0,
                                        ephemeralTimerMinutes = 0
                                    )
                                    chatDao.insertContact(contact)
                                }
                            }
                            _cloudUsers.value = userProfiles
                        }
                    }
                }
            activeListeners.add(usersRegistration)

            // 2. LISTEN TO CONTACTS COLLECTION (If user creates/edits in 'contacts' directly)
            val contactsRegistration = fs.collection("contacts")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseRealtime", "Erreur écoute contacts Firestore: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val cId = doc.getString("id") ?: doc.id
                                if (cId == currentUid) continue
                                val name = doc.getString("name") ?: doc.getString("displayName") ?: doc.getString("fullName") ?: "Contact"
                                val phone = doc.getString("phoneNumber") ?: doc.getString("phone") ?: doc.getString("whatsappNumber") ?: "+33 6 00 00 00 00"
                                val avatarColor = doc.getString("avatarColorHex") ?: doc.getString("avatarColor") ?: "#00F2FF"
                                val status = doc.getString("statusMessage") ?: doc.getString("status") ?: "Disponible"
                                val isOnline = doc.getBoolean("isOnline") ?: true
                                val isVerified = doc.getBoolean("isVerified") ?: true
                                val isPinned = doc.getBoolean("isPinned") ?: false

                                val contact = ContactEntity(
                                    id = cId,
                                    name = name,
                                    phoneNumber = phone,
                                    avatarColorHex = avatarColor,
                                    statusMessage = status,
                                    isOnline = isOnline,
                                    lastSeen = if (isOnline) "En ligne" else "Récemment",
                                    isPinned = isPinned,
                                    isVerified = isVerified,
                                    safetyNumber = doc.getString("safetyNumber") ?: CryptoEngine.generateSafetyNumber(currentUid, cId),
                                    publicKeyFingerprint = doc.getString("publicKeyFingerprint") ?: CryptoEngine.generateShortFingerprint(cId),
                                    unreadCount = 0,
                                    ephemeralTimerMinutes = doc.getLong("ephemeralTimerMinutes")?.toInt() ?: 0
                                )
                                chatDao.insertContact(contact)
                            }
                        }
                    }
                }
            activeListeners.add(contactsRegistration)

            // 3. LISTEN TO USER_ACCOUNTS COLLECTION (Cross-device registration & Admin Console sync)
            if (userAccountDao != null) {
                val accountsRegistration = fs.collection("user_accounts")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w("FirebaseRealtime", "Erreur écoute user_accounts: ${error.message}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            scope.launch {
                                for (doc in snapshot.documents) {
                                    val id = doc.getString("id") ?: doc.id
                                    val fullName = doc.getString("fullName") ?: doc.getString("displayName") ?: doc.getString("name") ?: id
                                    val username = doc.getString("username") ?: id
                                    val whatsappNumber = doc.getString("whatsappNumber") ?: doc.getString("phoneNumber") ?: "+33 6 00 00 00 00"
                                    val password = doc.getString("password") ?: "123"
                                    val avatarHex = doc.getString("avatarColorHex") ?: "#00F2FF"
                                    val statusMsg = doc.getString("statusMessage") ?: "Disponible"
                                    val isActive = doc.getBoolean("isActive") ?: true
                                    val isAdmin = doc.getBoolean("isAdmin") ?: false
                                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                    val lastLoginAt = doc.getLong("lastLoginAt") ?: System.currentTimeMillis()

                                    val account = UserAccountEntity(
                                        id = id,
                                        fullName = fullName,
                                        username = username,
                                        whatsappNumber = whatsappNumber,
                                        password = password,
                                        avatarColorHex = avatarHex,
                                        statusMessage = statusMsg,
                                        isActive = isActive,
                                        isAdmin = isAdmin,
                                        createdAt = createdAt,
                                        lastLoginAt = lastLoginAt
                                    )
                                    userAccountDao.insertUser(account)
                                }
                            }
                        }
                    }
                activeListeners.add(accountsRegistration)
            }

            // 4. LISTEN TO ALL REAL-TIME MESSAGES (No compound index required, handles console edits and cross-device sync)
            val messagesRegistration = fs.collection("messages")
                .limit(200)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseRealtime", "Erreur écoute messages Firestore: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val id = doc.getString("id") ?: doc.id
                                val senderId = doc.getString("senderId") ?: ""
                                val receiverId = doc.getString("receiverId") ?: ""
                                val rawChatId = doc.getString("chatId") ?: ""

                                // Determine message text (plainText edited in console or decrypted cipherText)
                                val explicitPlainText = doc.getString("plainText")
                                    ?: doc.getString("text")
                                    ?: doc.getString("content")
                                    ?: doc.getString("message")

                                val cipherText = doc.getString("cipherText") ?: ""
                                val ivHex = doc.getString("ivHex") ?: ""
                                val authTagHex = doc.getString("authTagHex") ?: ""

                                val finalPlainText = if (!explicitPlainText.isNullOrBlank()) {
                                    explicitPlainText
                                } else if (cipherText.isNotBlank()) {
                                    try {
                                        val sessionKey = CryptoEngine.deriveSessionKey(if (senderId.isNotBlank() && senderId != currentUid) senderId else receiverId)
                                        CryptoEngine.decrypt(
                                            cipherTextBase64 = cipherText,
                                            ivHex = ivHex,
                                            secretKey = sessionKey
                                        )
                                    } catch (e: Exception) {
                                        cipherText
                                    }
                                } else {
                                    ""
                                }

                                if (finalPlainText.isBlank() && cipherText.isBlank()) continue

                                // Resolve chatId and senderId relative to this device
                                val isSentByMe = (senderId == currentUid || senderId == "me" || senderId == _currentUser.value?.displayName)
                                val resolvedChatId = when {
                                    rawChatId.isNotBlank() && rawChatId != currentUid -> rawChatId
                                    isSentByMe && receiverId.isNotBlank() -> receiverId
                                    !isSentByMe && senderId.isNotBlank() -> senderId
                                    rawChatId.isNotBlank() -> rawChatId
                                    else -> "alice_sec"
                                }

                                val resolvedSenderId = if (isSentByMe) "me" else senderId.ifEmpty { resolvedChatId }

                                val timestamp = doc.getLong("timestamp")
                                    ?: doc.getDate("timestamp")?.time
                                    ?: System.currentTimeMillis()

                                val statusStr = doc.getString("status") ?: "DELIVERED"
                                val status = try { MessageStatus.valueOf(statusStr) } catch (e: Exception) { MessageStatus.DELIVERED }

                                val typeStr = doc.getString("messageType") ?: doc.getString("type") ?: "TEXT"
                                val messageType = try { MessageType.valueOf(typeStr) } catch (e: Exception) { MessageType.TEXT }

                                val voiceDuration = doc.getLong("voiceDurationSeconds")?.toInt() ?: 0
                                val mediaUri = doc.getString("mediaUri")
                                val isStarred = doc.getBoolean("isStarred") ?: false

                                val entity = MessageEntity(
                                    id = id,
                                    chatId = resolvedChatId,
                                    senderId = resolvedSenderId,
                                    plainText = finalPlainText,
                                    cipherText = cipherText.ifEmpty { finalPlainText },
                                    ivHex = ivHex,
                                    authTagHex = authTagHex,
                                    timestamp = timestamp,
                                    status = status,
                                    messageType = messageType,
                                    voiceDurationSeconds = voiceDuration,
                                    mediaUri = mediaUri,
                                    isStarred = isStarred,
                                    isDecryptedLocally = true
                                )
                                chatDao.insertMessage(entity)
                            }
                        }
                    }
                }
            activeListeners.add(messagesRegistration)

            // 5. LISTEN TO STATUS STORIES / STATUTS (Real-time story sync)
            val storiesRegistration = fs.collection("status_stories")
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseRealtime", "Erreur écoute stories: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val id = doc.getString("id") ?: doc.id
                                val contactId = doc.getString("contactId") ?: "contact"
                                val contactName = doc.getString("contactName") ?: "Contact"
                                val avatarColorHex = doc.getString("avatarColorHex") ?: "#00F2FF"
                                val textCaption = doc.getString("textCaption") ?: doc.getString("caption") ?: ""
                                val gradientIndex = doc.getLong("backgroundGradientIndex")?.toInt() ?: 0
                                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                val isViewed = doc.getBoolean("isViewed") ?: false

                                val story = StatusStoryEntity(
                                    id = id,
                                    contactId = contactId,
                                    contactName = contactName,
                                    avatarColorHex = avatarColorHex,
                                    textCaption = textCaption,
                                    backgroundGradientIndex = gradientIndex,
                                    timestamp = timestamp,
                                    isViewed = isViewed
                                )
                                chatDao.insertStory(story)
                            }
                        }
                    }
                }
            activeListeners.add(storiesRegistration)

            // 6. LISTEN TO CALL LOGS (Real-time call sync)
            val callsRegistration = fs.collection("call_logs")
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseRealtime", "Erreur écoute call logs: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val id = doc.getString("id") ?: doc.id
                                val contactId = doc.getString("contactId") ?: "contact"
                                val contactName = doc.getString("contactName") ?: "Contact"
                                val avatarColorHex = doc.getString("avatarColorHex") ?: "#00F2FF"
                                val callTypeStr = doc.getString("callType") ?: "AUDIO"
                                val directionStr = doc.getString("direction") ?: "INCOMING"
                                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                val durationSeconds = doc.getLong("durationSeconds")?.toInt() ?: 0
                                val isEncrypted = doc.getBoolean("isEncrypted") ?: true

                                val call = CallLogEntity(
                                    id = id,
                                    contactId = contactId,
                                    contactName = contactName,
                                    avatarColorHex = avatarColorHex,
                                    callType = try { CallType.valueOf(callTypeStr) } catch (e: Exception) { CallType.AUDIO },
                                    direction = try { CallDirection.valueOf(directionStr) } catch (e: Exception) { CallDirection.INCOMING },
                                    timestamp = timestamp,
                                    durationSeconds = durationSeconds,
                                    isEncrypted = isEncrypted
                                )
                                chatDao.insertCall(call)
                            }
                        }
                    }
                }
            activeListeners.add(callsRegistration)

        } catch (e: Exception) {
            Log.e("FirebaseRealtime", "Setup realtime listener failed: ${e.message}")
        } finally {
            _isSyncing.value = false
        }
    }

    private fun parseUserProfile(doc: DocumentSnapshot): CloudUserProfile? {
        return try {
            val uid = doc.getString("uid") ?: doc.getString("id") ?: doc.id
            val name = doc.getString("displayName") ?: doc.getString("name") ?: doc.getString("fullName") ?: doc.getString("username") ?: uid
            val email = doc.getString("email") ?: ""
            val phone = doc.getString("phoneNumber") ?: doc.getString("phone") ?: doc.getString("whatsappNumber") ?: ""
            val color = doc.getString("avatarColorHex") ?: doc.getString("avatarColor") ?: "#00F2FF"
            val status = doc.getString("statusMessage") ?: doc.getString("status") ?: "Disponible"
            val isOnline = doc.getBoolean("isOnline") ?: true
            val lastSeen = doc.getLong("lastSeenTimestamp") ?: System.currentTimeMillis()
            val fingerprint = doc.getString("publicKeyFingerprint") ?: CryptoEngine.generateShortFingerprint(uid)
            val safety = doc.getString("safetyNumber") ?: ""

            CloudUserProfile(
                uid = uid,
                displayName = name,
                email = email,
                phoneNumber = phone,
                avatarColorHex = color,
                statusMessage = status,
                isOnline = isOnline,
                lastSeenTimestamp = lastSeen,
                publicKeyFingerprint = fingerprint,
                safetyNumber = safety
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendRealtimeCloudMessage(
        recipientId: String,
        plainText: String,
        messageType: MessageType = MessageType.TEXT,
        voiceDurationSeconds: Int = 0,
        mediaUri: String? = null
    ) {
        val user = _currentUser.value
        val senderId = user?.uid ?: "user_default_me"
        val senderName = user?.displayName ?: "Moi"
        val sessionKey = CryptoEngine.deriveSessionKey(recipientId)
        val encrypted = CryptoEngine.encrypt(plainText, sessionKey)
        val msgId = UUID.randomUUID().toString()

        val cloudPayload = hashMapOf(
            "id" to msgId,
            "chatId" to recipientId,
            "senderId" to senderId,
            "senderName" to senderName,
            "receiverId" to recipientId,
            "plainText" to plainText,
            "cipherText" to encrypted.cipherTextBase64,
            "ivHex" to encrypted.ivHex,
            "authTagHex" to encrypted.authTagHex,
            "timestamp" to System.currentTimeMillis(),
            "messageType" to messageType.name,
            "voiceDurationSeconds" to voiceDurationSeconds,
            "mediaUri" to mediaUri,
            "status" to "DELIVERED"
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
            timestamp = cloudPayload["timestamp"] as Long,
            status = MessageStatus.DELIVERED,
            messageType = messageType,
            voiceDurationSeconds = voiceDurationSeconds,
            mediaUri = mediaUri,
            isStarred = false,
            isDecryptedLocally = true
        )
        chatDao.insertMessage(localMessage)

        // 2. Publish to Firebase Firestore for Realtime synchronization
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

    fun publishStoryToCloud(story: StatusStoryEntity) {
        val fs = firestore ?: return
        try {
            val map = hashMapOf(
                "id" to story.id,
                "contactId" to story.contactId,
                "contactName" to story.contactName,
                "avatarColorHex" to story.avatarColorHex,
                "textCaption" to story.textCaption,
                "backgroundGradientIndex" to story.backgroundGradientIndex,
                "timestamp" to story.timestamp,
                "isViewed" to story.isViewed
            )
            fs.collection("status_stories")
                .document(story.id)
                .set(map, SetOptions.merge())
        } catch (e: Exception) {
            Log.e("FirebaseRealtime", "Publish story error: ${e.message}")
        }
    }

    fun publishCallToCloud(call: CallLogEntity) {
        val fs = firestore ?: return
        try {
            val map = hashMapOf(
                "id" to call.id,
                "contactId" to call.contactId,
                "contactName" to call.contactName,
                "avatarColorHex" to call.avatarColorHex,
                "callType" to call.callType.name,
                "direction" to call.direction.name,
                "timestamp" to call.timestamp,
                "durationSeconds" to call.durationSeconds,
                "isEncrypted" to call.isEncrypted
            )
            fs.collection("call_logs")
                .document(call.id)
                .set(map, SetOptions.merge())
        } catch (e: Exception) {
            Log.e("FirebaseRealtime", "Publish call error: ${e.message}")
        }
    }

    fun cleanup() {
        for (listener in activeListeners) {
            listener.remove()
        }
        activeListeners.clear()
    }
}
