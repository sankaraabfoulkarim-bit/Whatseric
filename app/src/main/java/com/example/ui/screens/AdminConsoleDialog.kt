package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.UserAccountEntity
import com.example.ui.components.NeonAvatar
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextGraySecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ADMIN_SECRET_PIN = "761278"

@Composable
fun AdminConsoleDialog(
    users: List<UserAccountEntity>,
    onDismiss: () -> Unit,
    onToggleUserStatus: (String, Boolean) -> Unit,
    onUpdatePassword: (String, String) -> Unit,
    onUpdateUser: (UserAccountEntity) -> Unit,
    onDeleteUser: (String) -> Unit,
    onAddUser: (String, String, String, String) -> Unit,
    onLoginAsUser: (UserAccountEntity) -> Unit
) {
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
                // PIN CODE ENTRY SCREEN
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
                // UNLOCKED ADMIN MANAGEMENT DASHBOARD
                AdminDashboardView(
                    users = users,
                    onDismiss = onDismiss,
                    onLock = {
                        isUnlocked = false
                        enteredPin = ""
                    },
                    onToggleUserStatus = onToggleUserStatus,
                    onUpdatePassword = onUpdatePassword,
                    onUpdateUser = onUpdateUser,
                    onDeleteUser = onDeleteUser,
                    onAddUser = onAddUser,
                    onLoginAsUser = onLoginAsUser
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
                // Top close button
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
                    text = "Entrez le code secret pour gérer les utilisateurs inscrits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGraySecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { if (it.length <= 10) onPinChange(it) },
                    label = { Text("Code secret (ex: 761278)") },
                    placeholder = { Text("761278") },
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
                        text = "Remplir code (761278)",
                        style = MaterialTheme.typography.labelSmall,
                        color = neonColors.neonAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardView(
    users: List<UserAccountEntity>,
    onDismiss: () -> Unit,
    onLock: () -> Unit,
    onToggleUserStatus: (String, Boolean) -> Unit,
    onUpdatePassword: (String, String) -> Unit,
    onUpdateUser: (UserAccountEntity) -> Unit,
    onDeleteUser: (String) -> Unit,
    onAddUser: (String, String, String, String) -> Unit,
    onLoginAsUser: (UserAccountEntity) -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header
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
                        text = "Gestion des Inscrits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Console Admin Sécurisée (Code 761278)",
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

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(14.dp))

        // SEARCH & ADD USER BUTTON
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher (nom, @username, WhatsApp)...", fontSize = 13.sp) },
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
                    .height(50.dp),
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
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = neonColors.neonAccent,
                    contentColor = Color(0xFF002A1C)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ajouter", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // USERS LIST
        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "Aucun utilisateur inscrit pour le moment." else "Aucun résultat pour '$searchQuery'",
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
                    AdminUserItemCard(
                        user = user,
                        onToggleStatus = { onToggleUserStatus(user.id, !user.isActive) },
                        onEditPassword = { userToEditPassword = user },
                        onEditInfo = { userToEditInfo = user },
                        onDelete = { userToDelete = user },
                        onLoginAs = { onLoginAsUser(user) }
                    )
                }
            }
        }
    }

    // DIALOG: ADD NEW USER
    if (showAddDialog) {
        AdminAddUserDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, username, phone, pass ->
                onAddUser(name, username, phone, pass)
                showAddDialog = false
            }
        )
    }

    // DIALOG: EDIT PASSWORD
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

    // DIALOG: EDIT INFO
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

    // DIALOG: DELETE CONFIRMATION
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
    onToggleStatus: () -> Unit,
    onEditPassword: () -> Unit,
    onEditInfo: () -> Unit,
    onDelete: () -> Unit,
    onLoginAs: () -> Unit
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

            // Phone & Date details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SleekSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // WhatsApp Phone
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

                // Password & Inscription date
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
                Text("Mettre à jour", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextGraySecondary) }
        },
        containerColor = SleekSurface
    )
}
