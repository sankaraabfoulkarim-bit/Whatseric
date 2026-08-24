package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CallDirection
import com.example.data.CallLogEntity
import com.example.data.CallType
import com.example.data.ChatRepository
import com.example.data.CloudUserProfile
import com.example.data.ContactEntity
import com.example.data.FirebaseRealtimeManager
import com.example.data.MessageEntity
import com.example.data.MessageType
import com.example.data.NightThemeMode
import com.example.data.StatusStoryEntity
import com.example.data.UserAccountEntity
import com.example.data.ai.OpenRouterChatbotManager
import com.example.data.ai.OpenRouterConfig
import com.example.data.ai.OpenRouterModelOption
import com.example.data.integration.ThirdPartyApiKey
import com.example.data.integration.ThirdPartyApiLog
import com.example.data.integration.ThirdPartyBridgeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveCallState(
    val contact: ContactEntity,
    val callType: CallType,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val sessionKeyFingerprint: String = "E2EE-89A4-B3C1"
)

enum class AppTab {
    CHATS,
    STATUS,
    CALLS,
    SECURITY
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val prefs = application.getSharedPreferences("neoncrypt_auth_prefs", Context.MODE_PRIVATE)
    val firebaseManager = FirebaseRealtimeManager(application.applicationContext, database.chatDao())
    val openRouterManager = OpenRouterChatbotManager(application.applicationContext)
    val thirdPartyBridgeManager = ThirdPartyBridgeManager(application.applicationContext)

    private val repository = ChatRepository(
        chatDao = database.chatDao(),
        userAccountDao = database.userAccountDao(),
        firebaseManager = firebaseManager,
        openRouterManager = openRouterManager,
        thirdPartyBridgeManager = thirdPartyBridgeManager
    )

    // --- OpenRouter Chatbot State ---
    val openRouterConfig: StateFlow<OpenRouterConfig> = openRouterManager.configState
    val availableAiModels: List<OpenRouterModelOption> = openRouterManager.availableFreeModels
    private val _aiTestResult = MutableStateFlow<String?>(null)
    val aiTestResult: StateFlow<String?> = _aiTestResult.asStateFlow()
    private val _isAiTesting = MutableStateFlow(false)
    val isAiTesting: StateFlow<Boolean> = _isAiTesting.asStateFlow()

    // --- Third-Party Integrations State ---
    val thirdPartyApiKeys: StateFlow<List<ThirdPartyApiKey>> = thirdPartyBridgeManager.apiKeys
    val thirdPartyApiLogs: StateFlow<List<ThirdPartyApiLog>> = thirdPartyBridgeManager.apiLogs
    private val _webhookTestResult = MutableStateFlow<String?>(null)
    val webhookTestResult: StateFlow<String?> = _webhookTestResult.asStateFlow()
    private val _isWebhookTesting = MutableStateFlow(false)
    val isWebhookTesting: StateFlow<Boolean> = _isWebhookTesting.asStateFlow()

    // --- Authentication & User Accounts ---
    private val _currentUserAccount = MutableStateFlow<UserAccountEntity?>(null)
    val currentUserAccount: StateFlow<UserAccountEntity?> = _currentUserAccount.asStateFlow()

    val registeredUsers: StateFlow<List<UserAccountEntity>> = repository.allRegisteredUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _showAdminConsoleDialog = MutableStateFlow(false)
    val showAdminConsoleDialog: StateFlow<Boolean> = _showAdminConsoleDialog.asStateFlow()

    // --- Firebase Multi-User & Realtime State ---
    val currentUser: StateFlow<CloudUserProfile?> = firebaseManager.currentUser
    val cloudUsers: StateFlow<List<CloudUserProfile>> = firebaseManager.cloudUsers
    val isSyncing: StateFlow<Boolean> = firebaseManager.isSyncing

    private val _showUserSwitcherDialog = MutableStateFlow(false)
    val showUserSwitcherDialog: StateFlow<Boolean> = _showUserSwitcherDialog.asStateFlow()

    // --- State ---
    private val _currentTab = MutableStateFlow(AppTab.CHATS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedContact = MutableStateFlow<ContactEntity?>(null)
    val selectedContact: StateFlow<ContactEntity?> = _selectedContact.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    private val _nightThemeMode = MutableStateFlow<NightThemeMode>(NightThemeMode.NEON)
    val nightThemeMode: StateFlow<NightThemeMode> = _nightThemeMode.asStateFlow()

    private val _nightReadingBrightness = MutableStateFlow(1.0f) // 0.6f - 1.0f
    val nightReadingBrightness: StateFlow<Float> = _nightReadingBrightness.asStateFlow()

    private val _verifyingContact = MutableStateFlow<ContactEntity?>(null)
    val verifyingContact: StateFlow<ContactEntity?> = _verifyingContact.asStateFlow()

    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()
    private var callTimerJob: Job? = null

    private val _activeStory = MutableStateFlow<StatusStoryEntity?>(null)
    val activeStory: StateFlow<StatusStoryEntity?> = _activeStory.asStateFlow()

    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog: StateFlow<Boolean> = _showNewChatDialog.asStateFlow()

    private val _showEphemeralDialog = MutableStateFlow(false)
    val showEphemeralDialog: StateFlow<Boolean> = _showEphemeralDialog.asStateFlow()

    // Flows from repository
    val allContacts = repository.allContacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredContacts = combine(repository.allContacts, _searchQuery) { contacts, query ->
        if (query.isBlank()) contacts
        else contacts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.phoneNumber.contains(query, ignoreCase = true) ||
            it.statusMessage.contains(query, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allStories = repository.allStories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCalls = repository.allCalls.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _activeChatMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeChatMessages: StateFlow<List<MessageEntity>> = _activeChatMessages.asStateFlow()

    private var messagesObservationJob: Job? = null

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            val savedUserId = prefs.getString("last_logged_in_user_id", null)
            if (savedUserId != null) {
                val user = database.userAccountDao().getUserById(savedUserId)
                if (user != null && user.isActive) {
                    _currentUserAccount.value = user
                    firebaseManager.switchToProfile(user.fullName, user.avatarColorHex, user.statusMessage)
                }
            }
        }
    }

    // --- AUTHENTICATION ACTIONS ---
    fun login(username: String, password: String) {
        _authErrorMessage.value = null
        viewModelScope.launch {
            val result = repository.loginUser(username, password)
            result.onSuccess { user ->
                _currentUserAccount.value = user
                prefs.edit().putString("last_logged_in_user_id", user.id).apply()
                firebaseManager.switchToProfile(user.fullName, user.avatarColorHex, user.statusMessage)
            }.onFailure { error ->
                _authErrorMessage.value = error.message ?: "Erreur de connexion"
            }
        }
    }

    fun register(fullName: String, username: String, whatsappNumber: String, password: String, confirmation: String) {
        _authErrorMessage.value = null
        if (password != confirmation) {
            _authErrorMessage.value = "Les mots de passe ne correspondent pas"
            return
        }
        viewModelScope.launch {
            val result = repository.registerUser(fullName, username, whatsappNumber, password)
            result.onSuccess { user ->
                _currentUserAccount.value = user
                prefs.edit().putString("last_logged_in_user_id", user.id).apply()
                firebaseManager.switchToProfile(user.fullName, user.avatarColorHex, user.statusMessage)
            }.onFailure { error ->
                _authErrorMessage.value = error.message ?: "Erreur d'inscription"
            }
        }
    }

    fun logout() {
        _currentUserAccount.value = null
        prefs.edit().remove("last_logged_in_user_id").apply()
        closeChat()
    }

    fun showAdminConsole(show: Boolean) {
        _showAdminConsoleDialog.value = show
    }

    fun adminToggleUserStatus(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.adminToggleUserStatus(userId, isActive)
            // If current user is deactivated, log them out
            if (!isActive && _currentUserAccount.value?.id == userId) {
                logout()
            }
        }
    }

    fun adminUpdatePassword(userId: String, newPassword: String) {
        viewModelScope.launch {
            repository.adminUpdatePassword(userId, newPassword)
            if (_currentUserAccount.value?.id == userId) {
                _currentUserAccount.value = _currentUserAccount.value?.copy(password = newPassword)
            }
        }
    }

    fun adminUpdateUser(user: UserAccountEntity) {
        viewModelScope.launch {
            repository.adminUpdateUser(user)
            if (_currentUserAccount.value?.id == user.id) {
                _currentUserAccount.value = user
                firebaseManager.switchToProfile(user.fullName, user.avatarColorHex, user.statusMessage)
            }
        }
    }

    fun adminDeleteUser(userId: String) {
        viewModelScope.launch {
            repository.adminDeleteUser(userId)
            if (_currentUserAccount.value?.id == userId) {
                logout()
            }
        }
    }

    fun adminAddUser(fullName: String, username: String, whatsappNumber: String, password: String) {
        viewModelScope.launch {
            repository.registerUser(fullName, username, whatsappNumber, password)
        }
    }

    fun loginAsUser(user: UserAccountEntity) {
        _currentUserAccount.value = user
        prefs.edit().putString("last_logged_in_user_id", user.id).apply()
        firebaseManager.switchToProfile(user.fullName, user.avatarColorHex, user.statusMessage)
        _showAdminConsoleDialog.value = false
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchOpen.value = !_isSearchOpen.value
        if (!_isSearchOpen.value) {
            _searchQuery.value = ""
        }
    }

    fun setNightMode(mode: NightThemeMode) {
        _nightThemeMode.value = mode
    }

    fun setNightReadingBrightness(brightness: Float) {
        _nightReadingBrightness.value = brightness.coerceIn(0.5f, 1.0f)
    }

    fun openChat(contact: ContactEntity) {
        _selectedContact.value = contact
        viewModelScope.launch {
            repository.clearUnread(contact.id)
        }

        messagesObservationJob?.cancel()
        messagesObservationJob = viewModelScope.launch {
            repository.getMessagesForChat(contact.id).collect { messages ->
                _activeChatMessages.value = messages
            }
        }
    }

    fun closeChat() {
        _selectedContact.value = null
        messagesObservationJob?.cancel()
        messagesObservationJob = null
        _activeChatMessages.value = emptyList()
    }

    fun sendMessage(text: String) {
        val contact = _selectedContact.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.sendMessage(
                chatId = contact.id,
                text = text.trim(),
                messageType = MessageType.TEXT
            )
        }
    }

    fun sendVoiceNote(durationSeconds: Int = 8) {
        val contact = _selectedContact.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                chatId = contact.id,
                text = "Message vocal sécurisé ($durationSeconds s)",
                messageType = MessageType.VOICE_NOTE,
                voiceDurationSeconds = durationSeconds
            )
        }
    }

    fun sendPhotoAttachment() {
        val contact = _selectedContact.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                chatId = contact.id,
                text = "📷 Image chiffrée de bout en bout (AES-GCM)",
                messageType = MessageType.PHOTO,
                mediaUri = "sample_photo"
            )
        }
    }

    fun toggleStarMessage(messageId: String) {
        viewModelScope.launch {
            repository.toggleStarMessage(messageId)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun clearCurrentChat() {
        val contact = _selectedContact.value ?: return
        viewModelScope.launch {
            repository.clearChat(contact.id)
        }
    }

    fun showVerificationDialog(contact: ContactEntity) {
        _verifyingContact.value = contact
    }

    fun hideVerificationDialog() {
        _verifyingContact.value = null
    }

    fun confirmKeyVerification(contactId: String) {
        viewModelScope.launch {
            repository.updateContactVerification(contactId, true)
            // Refresh current contact if open
            if (_selectedContact.value?.id == contactId) {
                _selectedContact.value = _selectedContact.value?.copy(isVerified = true)
            }
            hideVerificationDialog()
        }
    }

    fun setEphemeralTimer(minutes: Int) {
        val contact = _selectedContact.value ?: return
        viewModelScope.launch {
            repository.updateEphemeralTimer(contact.id, minutes)
            _selectedContact.value = _selectedContact.value?.copy(ephemeralTimerMinutes = minutes)
            _showEphemeralDialog.value = false
        }
    }

    fun showEphemeralDialog(show: Boolean) {
        _showEphemeralDialog.value = show
    }

    fun showNewChatDialog(show: Boolean) {
        _showNewChatDialog.value = show
    }

    fun createNewContact(name: String, phone: String, status: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val contact = repository.addNewContact(
                name = name.trim(),
                phone = if (phone.isBlank()) "+33 6 00 00 00 00" else phone.trim(),
                statusMsg = if (status.isBlank()) "Disponible • E2EE" else status.trim()
            )
            _showNewChatDialog.value = false
            openChat(contact)
        }
    }

    fun startCall(contact: ContactEntity, callType: CallType) {
        callTimerJob?.cancel()
        _activeCall.value = ActiveCallState(
            contact = contact,
            callType = callType,
            durationSeconds = 0,
            sessionKeyFingerprint = "E2EE-${contact.id.take(4).uppercase()}-9F"
        )

        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let {
                    it.copy(durationSeconds = it.durationSeconds + 1)
                }
            }
        }
    }

    fun toggleMuteCall() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isMuted = !it.isMuted)
        }
    }

    fun toggleSpeakerCall() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isSpeakerOn = !it.isSpeakerOn)
        }
    }

    fun endCall() {
        val currentCall = _activeCall.value
        callTimerJob?.cancel()
        callTimerJob = null
        _activeCall.value = null

        if (currentCall != null) {
            viewModelScope.launch {
                repository.recordCall(
                    contactId = currentCall.contact.id,
                    contactName = currentCall.contact.name,
                    avatarColorHex = currentCall.contact.avatarColorHex,
                    callType = currentCall.callType,
                    direction = CallDirection.OUTGOING,
                    durationSeconds = currentCall.durationSeconds
                )
            }
        }
    }

    fun openStory(story: StatusStoryEntity) {
        _activeStory.value = story
        viewModelScope.launch {
            repository.markStoryViewed(story.id)
        }
    }

    fun closeStory() {
        _activeStory.value = null
    }

    fun postNewStatus(caption: String, gradientIndex: Int = 0) {
        if (caption.isBlank()) return
        viewModelScope.launch {
            repository.addStatusStory(caption.trim(), gradientIndex)
        }
    }

    // --- OpenRouter Chatbot Admin Actions ---
    fun updateOpenRouterSettings(
        apiKey: String? = null,
        selectedModel: String? = null,
        systemPrompt: String? = null,
        isChatbotEnabled: Boolean? = null,
        temperature: Float? = null,
        maxTokens: Int? = null,
        enableAutoReply: Boolean? = null
    ) {
        openRouterManager.updateConfig(
            apiKey = apiKey,
            selectedModel = selectedModel,
            systemPrompt = systemPrompt,
            isChatbotEnabled = isChatbotEnabled,
            temperature = temperature,
            maxTokens = maxTokens,
            enableAutoReply = enableAutoReply
        )
    }

    fun toggleUserConnectedToChatbot(userId: String, isConnected: Boolean) {
        openRouterManager.toggleUserConnection(userId, isConnected)
    }

    fun isUserConnectedToChatbot(userId: String): Boolean {
        return openRouterManager.isUserConnectedToBot(userId)
    }

    fun testOpenRouterApi(apiKey: String, model: String) {
        _isAiTesting.value = true
        _aiTestResult.value = null
        viewModelScope.launch {
            val result = openRouterManager.testOpenRouterConnection(apiKey, model)
            result.onSuccess { reply ->
                _aiTestResult.value = "✅ Succès: $reply"
            }.onFailure { error ->
                _aiTestResult.value = "❌ Erreur: ${error.message}"
            }
            _isAiTesting.value = false
        }
    }

    fun clearAiTestResult() {
        _aiTestResult.value = null
    }

    // --- Third-Party Integrations Admin Actions ---
    fun createThirdPartyApiKey(name: String, scopes: List<String>, webhookUrl: String? = null) {
        thirdPartyBridgeManager.createApiKey(name, scopes, webhookUrl)
    }

    fun toggleThirdPartyApiKey(keyId: String, isActive: Boolean) {
        thirdPartyBridgeManager.toggleApiKeyStatus(keyId, isActive)
    }

    fun deleteThirdPartyApiKey(keyId: String) {
        thirdPartyBridgeManager.deleteApiKey(keyId)
    }

    fun testWebhookUrl(url: String, eventName: String = "message.received") {
        _isWebhookTesting.value = true
        _webhookTestResult.value = null
        viewModelScope.launch {
            val res = thirdPartyBridgeManager.testExternalWebhook(url, eventName)
            res.onSuccess {
                _webhookTestResult.value = "✅ $it"
            }.onFailure {
                _webhookTestResult.value = "❌ ${it.message}"
            }
            _isWebhookTesting.value = false
        }
    }

    fun simulateExternalInboundMessage(apiToken: String, chatId: String, messageText: String, senderName: String) {
        viewModelScope.launch {
            repository.executeThirdPartyMessage(apiToken, chatId, messageText, senderName)
        }
    }

    fun clearWebhookTestResult() {
        _webhookTestResult.value = null
    }

    // --- Multi-Users & Cloud Profile Management ---
    fun showUserSwitcherDialog(show: Boolean) {
        _showUserSwitcherDialog.value = show
    }

    fun switchActiveProfile(name: String, avatarHex: String, statusText: String) {
        firebaseManager.switchToProfile(name, avatarHex, statusText)
        _showUserSwitcherDialog.value = false
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            firebaseManager.signInWithGoogle()
            _showUserSwitcherDialog.value = false
        }
    }

    fun signOut() {
        firebaseManager.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        firebaseManager.cleanup()
        callTimerJob?.cancel()
        messagesObservationJob?.cancel()
    }
}
