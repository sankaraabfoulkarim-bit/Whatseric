package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserAccountEntity
import com.example.data.ai.OpenRouterConfig
import com.example.data.ai.OpenRouterModelOption
import com.example.data.integration.ThirdPartyApiKey
import com.example.data.integration.ThirdPartyApiLog
import com.example.ui.components.NeonAvatar
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextGraySecondary
import com.example.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ADMIN_SECRET_PIN = "761278"

enum class AdminTab(val title: String) {
    USERS("Utilisateurs"),
    OPENROUTER_AI("Chatbot IA"),
    THIRD_PARTY_TOOLS("Outils Tiers")
}

@Composable
fun AdminConsoleDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val users by viewModel.registeredUsers.collectAsStateWithLifecycle()
    val openRouterConfig by viewModel.openRouterConfig.collectAsStateWithLifecycle()
    val availableModels = viewModel.availableAiModels
    val aiTestResult by viewModel.aiTestResult.collectAsStateWithLifecycle()
    val isAiTesting by viewModel.isAiTesting.collectAsStateWithLifecycle()
    val thirdPartyKeys by viewModel.thirdPartyApiKeys.collectAsStateWithLifecycle()
    val thirdPartyLogs by viewModel.thirdPartyApiLogs.collectAsStateWithLifecycle()
    val webhookTestResult by viewModel.webhookTestResult.collectAsStateWithLifecycle()
    val isWebhookTesting by viewModel.isWebhookTesting.collectAsStateWithLifecycle()

    var isUnlocked by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showPinText by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090D12))
                .testTag("admin_console_dialog"),
            color = Color(0xFF090D12)
        ) {
            if (!isUnlocked) {
                // PIN AUTH VIEW
                AdminPinAuthView(
                    enteredPin = enteredPin,
                    pinError = pinError,
                    showPinText = showPinText,
                    onPinChange = {
                        enteredPin = it
                        pinError = null
                        if (it == ADMIN_SECRET_PIN) {
                            isUnlocked = true
                        }
                    },
                    onToggleShowPin = { showPinText = !showPinText },
                    onValidatePin = {
                        if (enteredPin == ADMIN_SECRET_PIN) {
                            isUnlocked = true
                        } else {
                            pinError = "Code secret invalide. Accès refusé."
                        }
                    },
                    onDismiss = onDismiss
                )
            } else {
                // UNLOCKED DASHBOARD WITH 3 TABS
                AdminConsoleMainDashboard(
                    users = users,
                    openRouterConfig = openRouterConfig,
                    availableModels = availableModels,
                    aiTestResult = aiTestResult,
                    isAiTesting = isAiTesting,
                    thirdPartyKeys = thirdPartyKeys,
                    thirdPartyLogs = thirdPartyLogs,
                    webhookTestResult = webhookTestResult,
                    isWebhookTesting = isWebhookTesting,
                    onDismiss = onDismiss,
                    onLock = {
                        isUnlocked = false
                        enteredPin = ""
                    },
                    onToggleUserStatus = { id, active -> viewModel.adminToggleUserStatus(id, active) },
                    onUpdatePassword = { id, pass -> viewModel.adminUpdatePassword(id, pass) },
                    onUpdateUser = { user -> viewModel.adminUpdateUser(user) },
                    onDeleteUser = { id -> viewModel.adminDeleteUser(id) },
                    onAddUser = { name, username, phone, pass -> viewModel.adminAddUser(name, username, phone, pass) },
                    onLoginAsUser = { user -> viewModel.loginAsUser(user) },
                    onUpdateOpenRouterSettings = { key, model, prompt, enabled, temp, maxTokens, autoReply ->
                        viewModel.updateOpenRouterSettings(key, model, prompt, enabled, temp, maxTokens, autoReply)
                    },
                    onToggleUserAi = { userId, connected -> viewModel.toggleUserConnectedToChatbot(userId, connected) },
                    onTestAiApi = { key, model -> viewModel.testOpenRouterApi(key, model) },
                    onClearAiTest = { viewModel.clearAiTestResult() },
                    onCreateApiKey = { name, scopes, webhook -> viewModel.createThirdPartyApiKey(name, scopes, webhook) },
                    onToggleApiKey = { keyId, active -> viewModel.toggleThirdPartyApiKey(keyId, active) },
                    onDeleteApiKey = { keyId -> viewModel.deleteThirdPartyApiKey(keyId) },
                    onTestWebhook = { url -> viewModel.testWebhookUrl(url) },
                    onSimulateExternalMessage = { token, chatId, text, name ->
                        viewModel.simulateExternalInboundMessage(token, chatId, text, name)
                    },
                    onClearWebhookTest = { viewModel.clearWebhookTestResult() }
                )
            }
        }
    }
}

@Composable
private fun AdminPinAuthView(
    enteredPin: String,
    pinError: String?,
    showPinText: Boolean,
    onPinChange: (String) -> Unit,
    onToggleShowPin: () -> Unit,
    onValidatePin: () -> Unit,
    onDismiss: () -> Unit
) {
    val neonColors = LocalNeonColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SleekBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SleekSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = TextGraySecondary
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF5252).copy(alpha = 0.15f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Console Administrateur",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Accès restreint. Veuillez saisir le code PIN de sécurité administrateur.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGraySecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { if (it.length <= 10) onPinChange(it) },
                    label = { Text("Code PIN de sécurité") },
                    placeholder = { Text("Entrez le PIN...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onValidatePin() }),
                    visualTransformation = if (showPinText) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = neonColors.neonAccent
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onToggleShowPin) {
                            Icon(
                                imageVector = if (showPinText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextGraySecondary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = neonColors.neonAccent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = neonColors.neonAccent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (pinError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pinError,
                        color = Color(0xFFFF5252),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onValidatePin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_pin_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = neonColors.neonAccent,
                        contentColor = Color(0xFF002A1C)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Déverrouiller l'Admin",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { onPinChange("761278") }) {
                    Text(
                        text = "Code par défaut (761278)",
                        style = MaterialTheme.typography.labelSmall,
                        color = neonColors.neonAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminConsoleMainDashboard(
    users: List<UserAccountEntity>,
    openRouterConfig: OpenRouterConfig,
    availableModels: List<OpenRouterModelOption>,
    aiTestResult: String?,
    isAiTesting: Boolean,
    thirdPartyKeys: List<ThirdPartyApiKey>,
    thirdPartyLogs: List<ThirdPartyApiLog>,
    webhookTestResult: String?,
    isWebhookTesting: Boolean,
    onDismiss: () -> Unit,
    onLock: () -> Unit,
    onToggleUserStatus: (String, Boolean) -> Unit,
    onUpdatePassword: (String, String) -> Unit,
    onUpdateUser: (UserAccountEntity) -> Unit,
    onDeleteUser: (String) -> Unit,
    onAddUser: (String, String, String, String) -> Unit,
    onLoginAsUser: (UserAccountEntity) -> Unit,
    onUpdateOpenRouterSettings: (String?, String?, String?, Boolean?, Float?, Int?, Boolean?) -> Unit,
    onToggleUserAi: (String, Boolean) -> Unit,
    onTestAiApi: (String, String) -> Unit,
    onClearAiTest: () -> Unit,
    onCreateApiKey: (String, List<String>, String?) -> Unit,
    onToggleApiKey: (String, Boolean) -> Unit,
    onDeleteApiKey: (String) -> Unit,
    onTestWebhook: (String) -> Unit,
    onSimulateExternalMessage: (String, String, String, String) -> Unit,
    onClearWebhookTest: () -> Unit
) {
    val neonColors = LocalNeonColors.current
    var selectedTab by remember { mutableStateOf(AdminTab.USERS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF5252).copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Console Admin Sécurisée",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Protection PIN • Code 761278",
                        style = MaterialTheme.typography.labelSmall,
                        color = neonColors.neonAccent
                    )
                }
            }

            Row {
                IconButton(onClick = onLock) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Verrouiller",
                        tint = TextGraySecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // TAB BAR
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = SleekSurface,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = neonColors.neonAccent,
                    height = 3.dp
                )
            },
            divider = {},
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            AdminTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (tab) {
                                    AdminTab.USERS -> Icons.Default.Person
                                    AdminTab.OPENROUTER_AI -> Icons.Default.SmartToy
                                    AdminTab.THIRD_PARTY_TOOLS -> Icons.Default.Hub
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) neonColors.neonAccent else TextGraySecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextGraySecondary,
                                fontSize = 12.sp
                            )
                        }
                    },
                    modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB CONTENT
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                AdminTab.USERS -> {
                    AdminUsersManagementTab(
                        users = users,
                        openRouterConfig = openRouterConfig,
                        onToggleUserStatus = onToggleUserStatus,
                        onUpdatePassword = onUpdatePassword,
                        onUpdateUser = onUpdateUser,
                        onDeleteUser = onDeleteUser,
                        onAddUser = onAddUser,
                        onLoginAsUser = onLoginAsUser,
                        onToggleUserAi = onToggleUserAi
                    )
                }
                AdminTab.OPENROUTER_AI -> {
                    AdminOpenRouterAiTab(
                        users = users,
                        config = openRouterConfig,
                        availableModels = availableModels,
                        aiTestResult = aiTestResult,
                        isAiTesting = isAiTesting,
                        onUpdateSettings = onUpdateOpenRouterSettings,
                        onToggleUserAi = onToggleUserAi,
                        onTestAiApi = onTestAiApi,
                        onClearAiTest = onClearAiTest
                    )
                }
                AdminTab.THIRD_PARTY_TOOLS -> {
                    AdminThirdPartyToolsTab(
                        keys = thirdPartyKeys,
                        logs = thirdPartyLogs,
                        users = users,
                        webhookTestResult = webhookTestResult,
                        isWebhookTesting = isWebhookTesting,
                        onCreateKey = onCreateApiKey,
                        onToggleKey = onToggleApiKey,
                        onDeleteKey = onDeleteApiKey,
                        onTestWebhook = onTestWebhook,
                        onSimulateExternalMessage = onSimulateExternalMessage,
                        onClearWebhookTest = onClearWebhookTest
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: USERS MANAGEMENT
// -------------------------------------------------------------
@Composable
private fun AdminUsersManagementTab(
    users: List<UserAccountEntity>,
    openRouterConfig: OpenRouterConfig,
    onToggleUserStatus: (String, Boolean) -> Unit,
    onUpdatePassword: (String, String) -> Unit,
    onUpdateUser: (UserAccountEntity) -> Unit,
    onDeleteUser: (String) -> Unit,
    onAddUser: (String, String, String, String) -> Unit,
    onLoginAsUser: (UserAccountEntity) -> Unit,
    onToggleUserAi: (String, Boolean) -> Unit
) {
    val neonColors = LocalNeonColors.current
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var userToEditPassword by remember { mutableStateOf<UserAccountEntity?>(null) }
    var userToEditInfo by remember { mutableStateOf<UserAccountEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserAccountEntity?>(null) }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.whatsappNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    val activeCount = users.count { it.isActive }
    val suspendedCount = users.count { !it.isActive }

    Column(modifier = Modifier.fillMaxSize()) {
        // STATS SUMMARY CARDS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminStatCard(
                title = "Total Inscrits",
                count = users.size.toString(),
                color = neonColors.neonAccent,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Actifs",
                count = activeCount.toString(),
                color = NeonEmerald,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Suspendus",
                count = suspendedCount.toString(),
                color = if (suspendedCount > 0) Color(0xFFFF5252) else TextGrayMuted,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SEARCH & ADD USER BUTTON
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher (@user, nom, WhatsApp)...", fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextGraySecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Effacer",
                                tint = TextGraySecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonColors.neonAccent,
                    unfocusedBorderColor = SleekBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = neonColors.neonAccent,
                    contentColor = Color(0xFF002A1C)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Inscrire", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // USERS LIST
        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "Aucun utilisateur inscrit." else "Aucun résultat pour '$searchQuery'",
                    color = TextGraySecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    val isAiConnected = openRouterConfig.connectedUserIds.contains(user.id)

                    AdminUserItemCard(
                        user = user,
                        isAiConnected = isAiConnected,
                        onToggleStatus = { onToggleUserStatus(user.id, !user.isActive) },
                        onEditPassword = { userToEditPassword = user },
                        onEditInfo = { userToEditInfo = user },
                        onDelete = { userToDelete = user },
                        onLoginAs = { onLoginAsUser(user) },
                        onToggleAi = { onToggleUserAi(user.id, !isAiConnected) }
                    )
                }
            }
        }
    }

    // DIALOGS
    if (showAddDialog) {
        AdminAddUserDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, username, phone, pass ->
                onAddUser(name, username, phone, pass)
                showAddDialog = false
            }
        )
    }

    userToEditPassword?.let { user ->
        AdminEditPasswordDialog(
            user = user,
            onDismiss = { userToEditPassword = null },
            onConfirm = { newPass ->
                onUpdatePassword(user.id, newPass)
                userToEditPassword = null
            }
        )
    }

    userToEditInfo?.let { user ->
        AdminEditUserDialog(
            user = user,
            onDismiss = { userToEditInfo = null },
            onConfirm = { updatedUser ->
                onUpdateUser(updatedUser)
                userToEditInfo = null
            }
        )
    }

    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Supprimer l'utilisateur ?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Voulez-vous vraiment supprimer le compte de ${user.fullName} (@${user.username}) ? Cette action est irréversible.",
                    color = TextGraySecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(user.id)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Supprimer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Annuler", color = TextGraySecondary)
                }
            },
            containerColor = SleekSurface
        )
    }
}

// -------------------------------------------------------------
// TAB 2: OPENROUTER AI CONFIGURATION & USER CHATBOT CONNECTIONS
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminOpenRouterAiTab(
    users: List<UserAccountEntity>,
    config: OpenRouterConfig,
    availableModels: List<OpenRouterModelOption>,
    aiTestResult: String?,
    isAiTesting: Boolean,
    onUpdateSettings: (String?, String?, String?, Boolean?, Float?, Int?, Boolean?) -> Unit,
    onToggleUserAi: (String, Boolean) -> Unit,
    onTestAiApi: (String, String) -> Unit,
    onClearAiTest: () -> Unit
) {
    val context = LocalContext.current
    val neonColors = LocalNeonColors.current

    var apiKeyInput by remember(config.apiKey) { mutableStateOf(config.apiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var selectedModelId by remember(config.selectedModel) { mutableStateOf(config.selectedModel) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var systemPromptInput by remember(config.systemPrompt) { mutableStateOf(config.systemPrompt) }
    var temperatureValue by remember(config.temperature) { mutableFloatStateOf(config.temperature) }
    var maxTokensValue by remember(config.maxTokens) { mutableIntStateOf(config.maxTokens) }
    var isEnabled by remember(config.isChatbotEnabled) { mutableStateOf(config.isChatbotEnabled) }
    var autoReplyEnabled by remember(config.enableAutoReplyForConnectedUsers) { mutableStateOf(config.enableAutoReplyForConnectedUsers) }

    val currentModelObj = availableModels.find { it.id == selectedModelId } ?: availableModels.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. MASTER TOGGLE CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEnabled) SleekSurface else Color(0xFF1E1517)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isEnabled) neonColors.neonAccent.copy(alpha = 0.5f) else Color(0xFFFF5252).copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isEnabled) neonColors.neonAccent.copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = if (isEnabled) neonColors.neonAccent else Color(0xFFFF5252),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isEnabled) "Chatbot OpenRouter Activé" else "Chatbot Désactivé",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isEnabled) "Les utilisateurs connectés peuvent discuter avec l'IA" else "Toutes les requêtes IA sont mises en pause",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = {
                            isEnabled = it
                            onUpdateSettings(null, null, null, it, null, null, null)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF002A1C),
                            checkedTrackColor = neonColors.neonAccent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF333D47)
                        )
                    )
                }
            }
        }

        // 2. OPENROUTER API KEY CONFIGURATION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(0.8.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = neonColors.neonAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Clé API OpenRouter",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        TextButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Ouvrez https://openrouter.ai/keys", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Obtenir clé gratuite ↗", color = neonColors.neonAccent, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            onUpdateSettings(it.trim(), null, null, null, null, null, null)
                        },
                        label = { Text("Clé API (sk-or-v1-...)") },
                        placeholder = { Text("sk-or-v1-xxxxxxxxxxxx...") },
                        singleLine = true,
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextGraySecondary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("openrouter_api_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonColors.neonAccent,
                            unfocusedBorderColor = SleekBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test API Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (apiKeyInput.isBlank()) "Clé manquante : simulation locale active" else "Prêt pour les appels temps réel",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (apiKeyInput.isBlank()) Color(0xFFFF9100) else NeonEmerald,
                            fontSize = 11.sp
                        )

                        Button(
                            onClick = {
                                onTestAiApi(apiKeyInput.trim(), selectedModelId)
                            },
                            enabled = !isAiTesting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = neonColors.neonAccent.copy(alpha = 0.2f),
                                contentColor = neonColors.neonAccent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, neonColors.neonAccent)
                        ) {
                            if (isAiTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = neonColors.neonAccent, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tester la connexion", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Test Result Display
                    if (aiTestResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = SleekSurfaceVariant,
                            border = BorderStroke(0.8.dp, SleekBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = aiTestResult,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (aiTestResult.startsWith("✅")) NeonEmerald else Color(0xFFFF5252),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onClearAiTest, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextGraySecondary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. MODEL SELECTOR & FREE MODELS PICKER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(0.8.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = neonColors.neonAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Modèle IA OpenRouter (Gratuits)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = modelDropdownExpanded,
                        onExpandedChange = { modelDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentModelObj?.name ?: selectedModelId,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Modèle sélectionné") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonColors.neonAccent,
                                unfocusedBorderColor = SleekBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = modelDropdownExpanded,
                            onDismissRequest = { modelDropdownExpanded = false },
                            modifier = Modifier.background(SleekSurface)
                        ) {
                            availableModels.forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(model.name, fontWeight = FontWeight.Bold, color = Color.White)
                                                if (model.isFree) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = NeonEmerald.copy(alpha = 0.2f)
                                                    ) {
                                                        Text("GRATUIT", color = NeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Text(model.description, color = TextGraySecondary, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedModelId = model.id
                                        modelDropdownExpanded = false
                                        onUpdateSettings(null, model.id, null, null, null, null, null)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Temperature & Max tokens sliders
                    Text(
                        text = "Température créative: ${"%.1f".format(temperatureValue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                    Slider(
                        value = temperatureValue,
                        onValueChange = {
                            temperatureValue = it
                            onUpdateSettings(null, null, null, null, it, null, null)
                        },
                        valueRange = 0.0f..1.5f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = neonColors.neonAccent,
                            activeTrackColor = neonColors.neonAccent
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // System Prompt Editor
                    Text(
                        text = "Prompt Système & Personnalité du Bot",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = systemPromptInput,
                        onValueChange = {
                            systemPromptInput = it
                            onUpdateSettings(null, null, it, null, null, null, null)
                        },
                        placeholder = { Text("Tu es Neon AI, un assistant cryptographique...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonColors.neonAccent,
                            unfocusedBorderColor = SleekBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 4. USERS CONNECTED TO CHATBOT TOGGLE LIST (CRITICAL USER REQUEST)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(0.8.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = neonColors.neonAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Connecter les Utilisateurs au Chatbot",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = neonColors.neonAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${config.connectedUserIds.size} connectés",
                                color = neonColors.neonAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Activez le chatbot pour des utilisateurs spécifiques afin qu'ils reçoivent automatiquement des réponses de l'IA.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGraySecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // User toggles list
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        users.forEach { user ->
                            val isConnected = config.connectedUserIds.contains(user.id)

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = SleekSurfaceVariant,
                                border = BorderStroke(
                                    0.8.dp,
                                    if (isConnected) neonColors.neonAccent.copy(alpha = 0.4f) else SleekBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        NeonAvatar(
                                            name = user.fullName,
                                            avatarColorHex = user.avatarColorHex,
                                            size = 32.dp,
                                            isOnline = user.isActive
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = user.fullName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "@${user.username} • ${user.whatsappNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextGraySecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isConnected,
                                        onCheckedChange = { onToggleUserAi(user.id, it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFF002A1C),
                                            checkedTrackColor = neonColors.neonAccent,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFF333D47)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: THIRD-PARTY TOOLS & API BRIDGE INTEGRATION
// -------------------------------------------------------------
@Composable
private fun AdminThirdPartyToolsTab(
    keys: List<ThirdPartyApiKey>,
    logs: List<ThirdPartyApiLog>,
    users: List<UserAccountEntity>,
    webhookTestResult: String?,
    isWebhookTesting: Boolean,
    onCreateKey: (String, List<String>, String?) -> Unit,
    onToggleKey: (String, Boolean) -> Unit,
    onDeleteKey: (String) -> Unit,
    onTestWebhook: (String) -> Unit,
    onSimulateExternalMessage: (String, String, String, String) -> Unit,
    onClearWebhookTest: () -> Unit
) {
    val context = LocalContext.current
    val neonColors = LocalNeonColors.current
    var showCreateKeyDialog by remember { mutableStateOf(false) }
    var webhookUrlInput by remember { mutableStateOf("https://webhook.site/demo-endpoint") }
    var simulationChatId by remember { mutableStateOf(users.firstOrNull()?.id ?: "bot_openrouter_ai") }
    var simulationSenderName by remember { mutableStateOf("Zapier / CRM Bot") }
    var simulationText by remember { mutableStateOf("Alerte système : Facture validée par l'API externe.") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. HEADER & CREATE API KEY
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(0.8.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hub, contentDescription = null, tint = neonColors.neonAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Passerelle Outils Tiers (API REST & Webhooks)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { showCreateKeyDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = neonColors.neonAccent,
                                contentColor = Color(0xFF002A1C)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Créer Clé API", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Permettez à vos applications tierces (CRM, Zapier, Bots externes, Webhooks) de se connecter de façon sécurisée à NeonCrypt via des jetons d'accès chiffrés.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGraySecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 2. ACTIVE API KEYS LIST
        item {
            Text(
                text = "Clés API Actives (${keys.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (keys.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SleekSurfaceVariant
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aucune clé API tierce configurée. Cliquez sur 'Créer Clé API' ci-dessus.",
                            color = TextGraySecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(keys, key = { it.id }) { keyObj ->
                ThirdPartyKeyCard(
                    keyObj = keyObj,
                    onToggleActive = { onToggleKey(keyObj.id, !keyObj.isActive) },
                    onDelete = { onDeleteKey(keyObj.id) },
                    onCopyToken = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("API Token", keyObj.token))
                        Toast.makeText(context, "Jeton copié dans le presse-papiers", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // 3. INBOUND MESSAGE SIMULATOR (TEST LIVE API INJECTION)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(0.8.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = neonColors.neonAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simulateur d'Injection Message Externe (API -> Chat)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Simulez une requête POST /api/v1/messages/send venant d'un outil externe vers une conversation de l'app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGraySecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = simulationSenderName,
                        onValueChange = { simulationSenderName = it },
                        label = { Text("Nom de l'outil tiers") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonColors.neonAccent,
                            unfocusedBorderColor = SleekBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = simulationText,
                        onValueChange = { simulationText = it },
                        label = { Text("Corps du message à injecter") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonColors.neonAccent,
                            unfocusedBorderColor = SleekBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val activeToken = keys.firstOrNull { it.isActive }?.token ?: "test_dev_token"
                            onSimulateExternalMessage(activeToken, simulationChatId, simulationText, simulationSenderName)
                            Toast.makeText(context, "Message injecté dans la discussion avec succès !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = neonColors.neonAccent,
                            contentColor = Color(0xFF002A1C)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Injecter le Message via l'API", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. WEBHOOK TESTER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(0.8.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = neonColors.neonAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Testeur de Webhook Externe (Push HTTP)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = webhookUrlInput,
                        onValueChange = { webhookUrlInput = it },
                        label = { Text("URL Webhook Cible (ex: https://webhook.site/...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonColors.neonAccent,
                            unfocusedBorderColor = SleekBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onTestWebhook(webhookUrlInput.trim()) },
                            enabled = !isWebhookTesting && webhookUrlInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = neonColors.neonAccent.copy(alpha = 0.2f),
                                contentColor = neonColors.neonAccent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, neonColors.neonAccent)
                        ) {
                            if (isWebhookTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = neonColors.neonAccent, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Envoi HTTP...", fontSize = 12.sp)
                            } else {
                                Text("Déclencher Événement Test", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    if (webhookTestResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = SleekSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = webhookTestResult,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (webhookTestResult.startsWith("✅")) NeonEmerald else Color(0xFFFF5252),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onClearWebhookTest, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextGraySecondary, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. LIVE INTEGRATION EVENT LOGS
        item {
            Text(
                text = "Journal des Événements Tiers & API (${logs.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (logs.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SleekSurfaceVariant
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Aucun appel API ou événement tiers enregistré.", color = TextGraySecondary, fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(logs.take(15)) { logItem ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SleekSurfaceVariant,
                    border = BorderStroke(0.6.dp, SleekBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (logItem.isSuccess) NeonEmerald.copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${logItem.method} ${logItem.status}",
                                        color = if (logItem.isSuccess) NeonEmerald else Color(0xFFFF5252),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = logItem.toolName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${logItem.endpoint} • ${logItem.payloadSnippet}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGraySecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // DIALOG: CREATE NEW API KEY
    if (showCreateKeyDialog) {
        AdminCreateApiKeyDialog(
            onDismiss = { showCreateKeyDialog = false },
            onConfirm = { name, scopes, webhook ->
                onCreateKey(name, scopes, webhook)
                showCreateKeyDialog = false
            }
        )
    }
}

@Composable
private fun ThirdPartyKeyCard(
    keyObj: ThirdPartyApiKey,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onCopyToken: () -> Unit
) {
    val neonColors = LocalNeonColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (keyObj.isActive) SleekSurface else Color(0xFF1E1517),
        border = BorderStroke(
            0.8.dp,
            if (keyObj.isActive) SleekBorder else Color(0xFFFF5252).copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = if (keyObj.isActive) neonColors.neonAccent else TextGrayMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = keyObj.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = keyObj.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF002A1C),
                            checkedTrackColor = neonColors.neonAccent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF333D47)
                        )
                    )

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Token preview and copy
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onCopyToken() },
                color = SleekSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bearer " + keyObj.token.take(16) + "••••••••",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = neonColors.neonAccent,
                        fontSize = 11.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copier",
                        tint = TextGraySecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Scopes list
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                keyObj.scopes.forEach { scope ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = neonColors.neonAccent.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = scope,
                            color = neonColors.neonAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminCreateApiKeyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>, String?) -> Unit
) {
    var keyName by remember { mutableStateOf("") }
    var webhookUrl by remember { mutableStateOf("") }
    val availableScopes = listOf("messages:read", "messages:write", "contacts:read", "security:verify")
    var selectedScopes by remember { mutableStateOf(setOf("messages:read", "messages:write")) }
    val neonColors = LocalNeonColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Générer une Clé API pour Outil Tiers", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = keyName,
                    onValueChange = { keyName = it },
                    label = { Text("Nom de l'outil (ex: Bot Zapier / CRM)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = webhookUrl,
                    onValueChange = { webhookUrl = it },
                    label = { Text("URL Webhook facultative (HTTPS)") },
                    placeholder = { Text("https://mon-outil.com/webhook") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Text("Permissions / Scopes autorisés :", color = TextGraySecondary, fontSize = 11.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    availableScopes.forEach { scope ->
                        val isSelected = selectedScopes.contains(scope)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedScopes = if (isSelected) selectedScopes - scope else selectedScopes + scope
                            },
                            label = { Text(scope, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = neonColors.neonAccent.copy(alpha = 0.2f),
                                selectedLabelColor = neonColors.neonAccent,
                                containerColor = SleekSurfaceVariant,
                                labelColor = TextGraySecondary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (keyName.isNotBlank()) {
                        onConfirm(keyName.trim(), selectedScopes.toList(), webhookUrl.trim().ifBlank { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = neonColors.neonAccent, contentColor = Color(0xFF002A1C))
            ) {
                Text("Générer la Clé", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextGraySecondary) }
        },
        containerColor = SleekSurface
    )
}

// -------------------------------------------------------------
// USER CARD & DIALOG HELPERS
// -------------------------------------------------------------
@Composable
private fun AdminStatCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(0.8.dp, SleekBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextGraySecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun AdminUserItemCard(
    user: UserAccountEntity,
    isAiConnected: Boolean,
    onToggleStatus: () -> Unit,
    onEditPassword: () -> Unit,
    onEditInfo: () -> Unit,
    onDelete: () -> Unit,
    onLoginAs: () -> Unit,
    onToggleAi: () -> Unit
) {
    val context = LocalContext.current
    val neonColors = LocalNeonColors.current
    var showMenu by remember { mutableStateOf(false) }

    val formattedDate = remember(user.createdAt) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(user.createdAt))
        } catch (e: Exception) {
            ""
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isActive) SleekSurface else Color(0xFF201315)
        ),
        border = BorderStroke(
            width = if (!user.isActive) 1.dp else 0.8.dp,
            color = if (!user.isActive) Color(0xFFFF5252).copy(alpha = 0.5f) else SleekBorder
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // User info
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeonAvatar(
                        name = user.fullName,
                        avatarColorHex = user.avatarColorHex,
                        size = 40.dp,
                        isOnline = user.isActive
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Status tag
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (user.isActive) NeonEmerald.copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (user.isActive) "Actif" else "Suspendu",
                                    color = if (user.isActive) NeonEmerald else Color(0xFFFF5252),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (isAiConnected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = neonColors.neonAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "IA 🤖",
                                        color = neonColors.neonAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "@${user.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = neonColors.neonAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Action menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = TextGraySecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(SleekSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Se connecter avec ce compte", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onLoginAs()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = neonColors.neonAccent)
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isAiConnected) "Déconnecter du Chatbot IA" else "Connecter au Chatbot IA",
                                    color = if (isAiConnected) Color(0xFFFF9100) else neonColors.neonAccent
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleAi()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = neonColors.neonAccent)
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (user.isActive) "Suspendre / Bloquer" else "Réactiver le compte",
                                    color = if (user.isActive) Color(0xFFFF5252) else NeonEmerald
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleStatus()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (user.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (user.isActive) Color(0xFFFF5252) else NeonEmerald
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Modifier le mot de passe", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onEditPassword()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null, tint = neonColors.neonAccent)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Modifier les informations", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onEditInfo()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = TextGraySecondary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Supprimer l'utilisateur", color = Color(0xFFFF5252)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // WhatsApp link & Password preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SleekSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        try {
                            val cleanNumber = user.whatsappNumber.replace(Regex("[^0-9+]"), "")
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/$cleanNumber")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp: ${user.whatsappNumber}", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "WhatsApp",
                        tint = NeonEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = user.whatsappNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "MDP: ${user.password}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = neonColors.neonAccent,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGrayMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminAddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val neonColors = LocalNeonColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inscrire un utilisateur", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nom complet") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.trim().lowercase() },
                    label = { Text("Nom d'utilisateur (@user)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("Numéro WhatsApp") },
                    placeholder = { Text("+33 6 12 34 56 78") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (error != null) {
                    Text(text = error!!, color = Color(0xFFFF5252), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank() || username.isBlank() || whatsapp.isBlank() || password.isBlank()) {
                        error = "Veuillez remplir tous les champs"
                    } else {
                        onConfirm(fullName, username, whatsapp, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = neonColors.neonAccent, contentColor = Color(0xFF002A1C))
            ) {
                Text("Inscrire", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextGraySecondary) }
        },
        containerColor = SleekSurface
    )
}

@Composable
private fun AdminEditPasswordDialog(
    user: UserAccountEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf(user.password) }
    val neonColors = LocalNeonColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le mot de passe", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Utilisateur : ${user.fullName} (@${user.username})", color = TextGraySecondary)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nouveau mot de passe") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword.isNotBlank()) onConfirm(newPassword)
                },
                colors = ButtonDefaults.buttonColors(containerColor = neonColors.neonAccent, contentColor = Color(0xFF002A1C))
            ) {
                Text("Enregistrer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextGraySecondary) }
        },
        containerColor = SleekSurface
    )
}

@Composable
private fun AdminEditUserDialog(
    user: UserAccountEntity,
    onDismiss: () -> Unit,
    onConfirm: (UserAccountEntity) -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var whatsapp by remember { mutableStateOf(user.whatsappNumber) }
    var statusMessage by remember { mutableStateOf(user.statusMessage) }
    val neonColors = LocalNeonColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier les informations", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nom complet") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("Numéro WhatsApp") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = statusMessage,
                    onValueChange = { statusMessage = it },
                    label = { Text("Statut / Bio") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonColors.neonAccent,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        user.copy(
                            fullName = fullName.trim(),
                            whatsappNumber = whatsapp.trim(),
                            statusMessage = statusMessage.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = neonColors.neonAccent, contentColor = Color(0xFF002A1C))
            ) {
                Text("Sauvegarder", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextGraySecondary) }
        },
        containerColor = SleekSurface
    )
}
