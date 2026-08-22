package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.NightThemeMode
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCryptTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextGraySecondary
import com.example.viewmodel.AppTab
import com.example.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ChatViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedContact by viewModel.selectedContact.collectAsStateWithLifecycle()
    val activeChatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    val filteredContacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
    val allStories by viewModel.allStories.collectAsStateWithLifecycle()
    val allCalls by viewModel.allCalls.collectAsStateWithLifecycle()
    val activeStory by viewModel.activeStory.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val verifyingContact by viewModel.verifyingContact.collectAsStateWithLifecycle()
    val showNewChatDialog by viewModel.showNewChatDialog.collectAsStateWithLifecycle()
    val showEphemeralDialog by viewModel.showEphemeralDialog.collectAsStateWithLifecycle()
    val nightMode by viewModel.nightThemeMode.collectAsStateWithLifecycle()
    val nightBrightness by viewModel.nightReadingBrightness.collectAsStateWithLifecycle()
    val isSearchOpen by viewModel.isSearchOpen.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    NeonCryptTheme(nightMode = nightMode) {
        val neonColors = LocalNeonColors.current

        // Apply night reading brightness factor on root container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .alpha(nightBrightness)
        ) {
            // If a conversation is opened, show ChatScreen full screen
            if (selectedContact != null) {
                ChatScreen(
                    contact = selectedContact!!,
                    messages = activeChatMessages,
                    onBack = { viewModel.closeChat() },
                    onSendMessage = { text -> viewModel.sendMessage(text) },
                    onSendVoiceNote = { seconds -> viewModel.sendVoiceNote(seconds) },
                    onSendPhoto = { viewModel.sendPhotoAttachment() },
                    onToggleStar = { id -> viewModel.toggleStarMessage(id) },
                    onDeleteMessage = { id -> viewModel.deleteMessage(id) },
                    onClearChat = { viewModel.clearCurrentChat() },
                    onOpenVerification = { viewModel.showVerificationDialog(selectedContact!!) },
                    onOpenEphemeralSettings = { viewModel.showEphemeralDialog(true) },
                    onStartCall = { contact, type -> viewModel.startCall(contact, type) }
                )
            } else {
                // Main WhatsApp-like Tabs Scaffold
                Scaffold(
                    modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                actionIconContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            title = {
                                if (isSearchOpen) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { viewModel.setSearchQuery(it) },
                                        placeholder = { Text("Rechercher dans NeonCrypt...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("search_text_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = neonColors.neonAccent,
                                            unfocusedBorderColor = Color.Transparent
                                        ),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "NeonCrypt Logo",
                                            tint = neonColors.neonAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "NeonCrypt",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.toggleSearch() },
                                    modifier = Modifier.testTag("toggle_search_button")
                                ) {
                                    Icon(
                                        imageVector = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = "Recherche"
                                    )
                                }

                                // Quick night theme toggle
                                IconButton(
                                    onClick = {
                                        val nextMode = when (nightMode) {
                                            NightThemeMode.OLED_PURE_BLACK -> NightThemeMode.MIDNIGHT_SLATE
                                            NightThemeMode.MIDNIGHT_SLATE -> NightThemeMode.AMBER_NIGHT_FILTER
                                            NightThemeMode.AMBER_NIGHT_FILTER -> NightThemeMode.OLED_PURE_BLACK
                                        }
                                        viewModel.setNightMode(nextMode)
                                    },
                                    modifier = Modifier.testTag("quick_night_mode_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (nightMode == NightThemeMode.AMBER_NIGHT_FILTER) Icons.Default.Nightlight else Icons.Default.DarkMode,
                                        contentDescription = "Changer mode nuit",
                                        tint = neonColors.neonAccent
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            val totalUnread = filteredContacts.sumOf { it.unreadCount }

                            // 1. Discussions
                            NavigationBarItem(
                                selected = currentTab == AppTab.CHATS,
                                onClick = { viewModel.setTab(AppTab.CHATS) },
                                icon = {
                                    if (totalUnread > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(
                                                    containerColor = neonColors.neonAccent,
                                                    contentColor = Color(0xFF002A1C)
                                                ) {
                                                    Text(totalUnread.toString(), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "Discussions")
                                        }
                                    } else {
                                        Icon(Icons.Default.Chat, contentDescription = "Discussions")
                                    }
                                },
                                label = { Text("Discussions", fontWeight = if (currentTab == AppTab.CHATS) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF002A1C),
                                    indicatorColor = neonColors.neonAccent,
                                    selectedTextColor = neonColors.neonAccent,
                                    unselectedIconColor = TextGraySecondary,
                                    unselectedTextColor = TextGraySecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_chats")
                            )

                            // 2. Statuts
                            NavigationBarItem(
                                selected = currentTab == AppTab.STATUS,
                                onClick = { viewModel.setTab(AppTab.STATUS) },
                                icon = {
                                    Icon(Icons.Default.Security, contentDescription = "Statuts")
                                },
                                label = { Text("Statuts", fontWeight = if (currentTab == AppTab.STATUS) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF002A1C),
                                    indicatorColor = neonColors.neonAccent,
                                    selectedTextColor = neonColors.neonAccent,
                                    unselectedIconColor = TextGraySecondary,
                                    unselectedTextColor = TextGraySecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_status")
                            )

                            // 3. Appels
                            NavigationBarItem(
                                selected = currentTab == AppTab.CALLS,
                                onClick = { viewModel.setTab(AppTab.CALLS) },
                                icon = {
                                    Icon(Icons.Default.Call, contentDescription = "Appels")
                                },
                                label = { Text("Appels", fontWeight = if (currentTab == AppTab.CALLS) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF002A1C),
                                    indicatorColor = neonColors.neonAccent,
                                    selectedTextColor = neonColors.neonAccent,
                                    unselectedIconColor = TextGraySecondary,
                                    unselectedTextColor = TextGraySecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_calls")
                            )

                            // 4. Sécurité
                            NavigationBarItem(
                                selected = currentTab == AppTab.SECURITY,
                                onClick = { viewModel.setTab(AppTab.SECURITY) },
                                icon = {
                                    Icon(Icons.Default.Shield, contentDescription = "Sécurité & Nuit")
                                },
                                label = { Text("Sécurité", fontWeight = if (currentTab == AppTab.SECURITY) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF002A1C),
                                    indicatorColor = neonColors.neonAccent,
                                    selectedTextColor = neonColors.neonAccent,
                                    unselectedIconColor = TextGraySecondary,
                                    unselectedTextColor = TextGraySecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_security")
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentTab == AppTab.CHATS) {
                            FloatingActionButton(
                                onClick = { viewModel.showNewChatDialog(true) },
                                containerColor = neonColors.neonAccent,
                                contentColor = Color(0xFF002A1C),
                                shape = CircleShape,
                                modifier = Modifier.testTag("new_chat_fab")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddComment,
                                    contentDescription = "Nouvelle discussion"
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                            when (tab) {
                                AppTab.CHATS -> {
                                    ChatListScreen(
                                        contacts = filteredContacts,
                                        onSelectContact = { contact -> viewModel.openChat(contact) },
                                        onOpenSecurityVerification = { contact -> viewModel.showVerificationDialog(contact) }
                                    )
                                }
                                AppTab.STATUS -> {
                                    StatusScreen(
                                        stories = allStories,
                                        activeStory = activeStory,
                                        onOpenStory = { story -> viewModel.openStory(story) },
                                        onCloseStory = { viewModel.closeStory() },
                                        onPostStatus = { caption, gradientIndex -> viewModel.postNewStatus(caption, gradientIndex) }
                                    )
                                }
                                AppTab.CALLS -> {
                                    CallsScreen(
                                        calls = allCalls,
                                        contacts = filteredContacts,
                                        onStartCall = { contact, type -> viewModel.startCall(contact, type) }
                                    )
                                }
                                AppTab.SECURITY -> {
                                    SecurityHubScreen(
                                        currentNightMode = nightMode,
                                        nightBrightness = nightBrightness,
                                        onNightModeChange = { mode -> viewModel.setNightMode(mode) },
                                        onBrightnessChange = { brightness -> viewModel.setNightReadingBrightness(brightness) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Safety Verification Dialog Overlay
            if (verifyingContact != null) {
                SafetyVerificationDialog(
                    contact = verifyingContact!!,
                    onDismiss = { viewModel.hideVerificationDialog() },
                    onConfirmVerification = { id -> viewModel.confirmKeyVerification(id) }
                )
            }

            // Active Call Screen Overlay
            if (activeCall != null) {
                ActiveCallDialog(
                    callState = activeCall!!,
                    onMuteToggle = { viewModel.toggleMuteCall() },
                    onSpeakerToggle = { viewModel.toggleSpeakerCall() },
                    onEndCall = { viewModel.endCall() }
                )
            }

            // New Encrypted Contact Dialog
            if (showNewChatDialog) {
                NewChatDialog(
                    onDismiss = { viewModel.showNewChatDialog(false) },
                    onCreateContact = { name, phone, status -> viewModel.createNewContact(name, phone, status) }
                )
            }

            // Ephemeral Timer Dialog
            if (showEphemeralDialog && selectedContact != null) {
                EphemeralTimerDialog(
                    currentMinutes = selectedContact!!.ephemeralTimerMinutes,
                    onDismiss = { viewModel.showEphemeralDialog(false) },
                    onSelectMinutes = { mins -> viewModel.setEphemeralTimer(mins) }
                )
            }
        }
    }
}

@Composable
fun NewChatDialog(
    onDismiss: () -> Unit,
    onCreateContact: (String, String, String) -> Unit
) {
    val neonColors = LocalNeonColors.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, neonColors.neonAccent, RoundedCornerShape(20.dp)),
            color = neonColors.cardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = neonColors.neonAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nouvelle discussion chiffrée",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du contact") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = neonColors.cardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Numéro / ID de sécurité") },
                    placeholder = { Text("+33 6 ...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = neonColors.cardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = statusMsg,
                    onValueChange = { statusMsg = it },
                    label = { Text("Message de statut") },
                    placeholder = { Text("Clé E2EE vérifiée...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = neonColors.cardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler", color = TextGraySecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onCreateContact(name, phone, statusMsg) },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = neonColors.neonAccent,
                            contentColor = Color(0xFF002A1C)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Chiffrer & Ouvrir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EphemeralTimerDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onSelectMinutes: (Int) -> Unit
) {
    val neonColors = LocalNeonColors.current
    val options = listOf(
        Pair(0, "Désactivé"),
        Pair(5, "5 minutes"),
        Pair(60, "1 heure"),
        Pair(1440, "24 heures"),
        Pair(10080, "7 jours")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, neonColors.neonAccent, RoundedCornerShape(20.dp)),
            color = neonColors.cardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = neonColors.neonAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Délai des messages éphémères",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pour plus de confidentialité, les nouveaux messages disparaîtront automatiquement après le délai sélectionné.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGraySecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                options.forEach { (minutes, label) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectMinutes(minutes) },
                        color = if (currentMinutes == minutes) neonColors.neonAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (currentMinutes == minutes) BorderStroke(1.dp, neonColors.neonAccent) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (currentMinutes == minutes) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentMinutes == minutes) neonColors.neonAccent else MaterialTheme.colorScheme.onSurface
                            )

                            if (currentMinutes == minutes) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = neonColors.neonAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fermer", color = TextGraySecondary)
                    }
                }
            }
        }
    }
}
