package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NightThemeMode
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextGraySecondary

import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.example.data.UserAccountEntity
import com.example.ui.components.NeonAvatar

@Composable
fun SecurityHubScreen(
    currentNightMode: NightThemeMode,
    nightBrightness: Float,
    onNightModeChange: (NightThemeMode) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    currentUserAccount: UserAccountEntity? = null,
    onLogout: () -> Unit = {},
    onOpenAdminConsole: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val neonColors = LocalNeonColors.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var highQualityMedia by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("security_hub_screen")
    ) {
        // Top Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = neonColors.neonAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Paramètres & Compte",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 0: MON COMPTE UTILISATEUR
        if (currentUserAccount != null) {
            Text(
                text = "MON COMPTE UTILISATEUR",
                style = MaterialTheme.typography.labelSmall,
                color = neonColors.neonAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = neonColors.cardBackground),
                border = BorderStroke(1.dp, neonColors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeonAvatar(
                            name = currentUserAccount.fullName,
                            avatarColorHex = currentUserAccount.avatarColorHex,
                            size = 48.dp,
                            isOnline = true
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUserAccount.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "@${currentUserAccount.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = neonColors.neonAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentUserAccount.whatsappNumber,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextGraySecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenAdminConsole,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SleekBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = neonColors.neonAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Admin",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onLogout,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5252).copy(alpha = 0.2f),
                                contentColor = Color(0xFFFF5252)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Déconnexion",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // SECTION: ADMIN CONSOLE SHORTCUT
        Text(
            text = "ADMINISTRATION SYSTÈME",
            style = MaterialTheme.typography.labelSmall,
            color = neonColors.neonAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onOpenAdminConsole() },
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF5252).copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Console Administrateur",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Gérer les utilisateurs inscrits (Code secret: 761278)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGraySecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = neonColors.neonAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 1: NIGHT & NEON THEME MODES
        Text(
            text = "THÈMES & AFFICHAGE NOCTURNE",
            style = MaterialTheme.typography.labelSmall,
            color = neonColors.neonAccent,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 1: OLED Pure Black
        NightModeOptionCard(
            title = "OLED Pure Black (Émeraude)",
            subtitle = "Noir absolu ultra contrasté, économise la batterie.",
            mode = NightThemeMode.OLED_PURE_BLACK,
            selected = currentNightMode == NightThemeMode.OLED_PURE_BLACK,
            badgeColor = NeonEmerald,
            onClick = { onNightModeChange(NightThemeMode.OLED_PURE_BLACK) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Option 2: Midnight Slate Neon
        NightModeOptionCard(
            title = "Midnight Slate (Cyan Néon)",
            subtitle = "Nuances ardoise profonde avec accents cyan néon vifs.",
            mode = NightThemeMode.MIDNIGHT_SLATE,
            selected = currentNightMode == NightThemeMode.MIDNIGHT_SLATE,
            badgeColor = NeonCyan,
            onClick = { onNightModeChange(NightThemeMode.MIDNIGHT_SLATE) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Option 3: Amber Night Reading
        NightModeOptionCard(
            title = "Filtre Ambre (Confort Lecture)",
            subtitle = "Ton chaud apaisant sans lumière bleue pour la nuit.",
            mode = NightThemeMode.AMBER_NIGHT_FILTER,
            selected = currentNightMode == NightThemeMode.AMBER_NIGHT_FILTER,
            badgeColor = AmberPrimary,
            onClick = { onNightModeChange(NightThemeMode.AMBER_NIGHT_FILTER) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: LUMINOSITÉ & INTENSITÉ NÉON
        Text(
            text = "LUMINOSITÉ D'AFFICHAGE (${(nightBrightness * 100).toInt()}%)",
            style = MaterialTheme.typography.labelSmall,
            color = neonColors.neonAccent,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = neonColors.cardBackground),
            border = BorderStroke(1.dp, neonColors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessMedium,
                        contentDescription = null,
                        tint = TextGraySecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Slider(
                        value = nightBrightness,
                        onValueChange = onBrightnessChange,
                        valueRange = 0.2f..1.0f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = neonColors.neonAccent,
                            activeTrackColor = neonColors.neonAccent,
                            inactiveTrackColor = Color(0xFF2C3E50)
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = neonColors.neonAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: NOTIFICATIONS & PRÉFÉRENCES
        Text(
            text = "PRÉFÉRENCES DES NOTIFICATIONS",
            style = MaterialTheme.typography.labelSmall,
            color = neonColors.neonAccent,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = neonColors.cardBackground),
            border = BorderStroke(1.dp, neonColors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Notifications toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = neonColors.neonAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Notifications de messages",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Recevoir les alertes en direct",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySecondary
                            )
                        }
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF002A1C),
                            checkedTrackColor = neonColors.neonAccent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High Quality Media toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = neonColors.neonAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Médias haute définition",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Charger photos et audios en HD",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySecondary
                            )
                        }
                    }
                    Switch(
                        checked = highQualityMedia,
                        onCheckedChange = { highQualityMedia = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF002A1C),
                            checkedTrackColor = neonColors.neonAccent
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: STOCKAGE & SYNCHRONISATION
        Text(
            text = "SYNCHRONISATION & DONNÉES",
            style = MaterialTheme.typography.labelSmall,
            color = neonColors.neonAccent,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = neonColors.cardBackground),
            border = BorderStroke(1.dp, neonColors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = neonColors.neonAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Cloud Realtime",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Messages et contacts synchronisés",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGraySecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = neonColors.neonAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Base de données Room locale",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Stockage instantané et hors-ligne",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGraySecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun NightModeOptionCard(
    title: String,
    subtitle: String,
    mode: NightThemeMode,
    selected: Boolean,
    badgeColor: Color,
    onClick: () -> Unit
) {
    val neonColors = LocalNeonColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 1.8.dp else 0.8.dp,
                color = if (selected) badgeColor else neonColors.cardBorder,
                shape = RoundedCornerShape(14.dp)
            ),
        color = if (selected) badgeColor.copy(alpha = 0.12f) else neonColors.cardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (mode == NightThemeMode.AMBER_NIGHT_FILTER) Icons.Default.Nightlight else Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGraySecondary
                )
            }

            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
