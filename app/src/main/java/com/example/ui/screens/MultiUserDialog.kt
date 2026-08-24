package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CloudUserProfile
import com.example.ui.components.NeonAvatar
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekNeonCyan
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

data class PresetProfile(
    val name: String,
    val avatarHex: String,
    val status: String,
    val role: String
)

@Composable
fun MultiUserDialog(
    currentUser: CloudUserProfile?,
    isSyncing: Boolean,
    onDismiss: () -> Unit,
    onSwitchProfile: (name: String, avatarHex: String, status: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    val neonColors = LocalNeonColors.current
    var customName by remember { mutableStateOf("") }
    var customStatus by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    val presetProfiles = listOf(
        PresetProfile("Kylian (Vous)", "#00F2FF", "Disponible", "Compte Principal"),
        PresetProfile("Alice", "#00F59B", "En réunion 📱", "Profil Utilisateur A"),
        PresetProfile("Marc Dubois", "#FF9100", "Au bureau", "Profil Utilisateur B"),
        PresetProfile("Sophie Martin", "#B388FF", "Mode nuit activé 🌙", "Profil Utilisateur C"),
        PresetProfile("Lucas", "#00E5FF", "En vacances 🌴", "Profil Utilisateur D")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
                .testTag("multi_user_dialog"),
            color = SleekBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = neonColors.neonAccent.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = neonColors.neonAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Multi-Utilisateurs Realtime",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(neonColors.neonAccent)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Firebase Firestore • E2EE Cloud Sync",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = neonColors.neonAccent,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Active Profile Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SleekSurface,
                    border = BorderStroke(1.dp, neonColors.neonAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PROFIL ACTIF CONNECTÉ",
                            style = MaterialTheme.typography.labelSmall,
                            color = neonColors.neonAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NeonAvatar(
                                name = currentUser?.displayName ?: "Moi",
                                avatarColorHex = currentUser?.avatarColorHex ?: "#00F2FF",
                                size = 44.dp,
                                isOnline = true,
                                isVerified = true
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.displayName ?: "Kylian",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = currentUser?.statusMessage ?: "Chiffré E2EE",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "UID: ${currentUser?.uid?.take(16) ?: "user_default"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = SleekTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign In Button (Credential Manager)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SleekSurfaceVariant,
                    border = BorderStroke(1.dp, SleekBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGoogleSignIn() }
                        .testTag("google_signin_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google Sign In",
                            tint = neonColors.neonAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connexion Google (Credential Manager)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "BASCULER D'UTILISATEUR (TEST TEMPS RÉEL) :",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextMuted,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetProfiles) { preset ->
                        val isSelected = currentUser?.displayName == preset.name
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SleekSurfaceVariant else SleekSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) neonColors.neonAccent else SleekBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSwitchProfile(preset.name, preset.avatarHex, preset.status)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NeonAvatar(
                                    name = preset.name,
                                    avatarColorHex = preset.avatarHex,
                                    size = 34.dp,
                                    isOnline = true
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = preset.role,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SleekTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Actif",
                                        tint = neonColors.neonAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom User creation toggle
                if (!showCustomInput) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, SleekBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomInput = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = neonColors.neonAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ajouter un autre compte de test", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekSurface)
                            .padding(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            placeholder = { Text("Nom d'affichage (ex: Thomas)", color = SleekTextMuted, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = neonColors.neonAccent,
                                unfocusedBorderColor = SleekBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = neonColors.neonAccent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (customName.isNotBlank()) {
                                        onSwitchProfile(customName.trim(), "#00F2FF", "Disponible • E2EE Realtime")
                                        showCustomInput = false
                                        customName = ""
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                                Text("Activer ce profil", color = Color(0xFF09090B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dismiss Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SleekSurface,
                    border = BorderStroke(1.dp, SleekBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDismiss)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text("Fermer", color = SleekTextSecondary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
