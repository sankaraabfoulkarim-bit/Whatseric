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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NightThemeMode
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextGraySecondary

@Composable
fun SecurityHubScreen(
    currentNightMode: NightThemeMode,
    nightBrightness: Float,
    onNightModeChange: (NightThemeMode) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val neonColors = LocalNeonColors.current

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
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = neonColors.neonAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sécurité E2EE & Mode Nuit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 1: NIGHT & NEON THEME MODES
        Text(
            text = "MODES SOMBRES & NÉON OPTIMISÉS NUIT",
            style = MaterialTheme.typography.labelSmall,
            color = neonColors.neonAccent,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        NightModeOptionCard(
            title = "OLED Noir Absolu (Recommandé)",
            subtitle = "Noir #000000 véritable pour écran OLED, économie d'énergie et accents néon émeraude.",
            mode = NightThemeMode.OLED_PURE_BLACK,
            selected = currentNightMode == NightThemeMode.OLED_PURE_BLACK,
            badgeColor = NeonEmerald,
            onClick = { onNightModeChange(NightThemeMode.OLED_PURE_BLACK) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        NightModeOptionCard(
            title = "Bleu Nuit Minuit (Midnight Slate)",
            subtitle = "Palette bleu ardoise foncé avec accents cyan néon pour un confort visuel équilibré.",
            mode = NightThemeMode.MIDNIGHT_SLATE,
            selected = currentNightMode == NightThemeMode.MIDNIGHT_SLATE,
            badgeColor = NeonCyan,
            onClick = { onNightModeChange(NightThemeMode.MIDNIGHT_SLATE) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        NightModeOptionCard(
            title = "Filtre Ambre Anti-Lumière Bleue",
            subtitle = "Teinte sombre chaude ambrée conçue pour éliminer la fatigue oculaire lors d'une lecture tardive.",
            mode = NightThemeMode.AMBER_NIGHT_FILTER,
            selected = currentNightMode == NightThemeMode.AMBER_NIGHT_FILTER,
            badgeColor = AmberPrimary,
            onClick = { onNightModeChange(NightThemeMode.AMBER_NIGHT_FILTER) }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 2: NIGHT READING COMFORT SLIDER
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = neonColors.cardBackground),
            border = BorderStroke(1.dp, neonColors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = null,
                            tint = neonColors.neonAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Adoucissement de contraste nuit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${(nightBrightness * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = neonColors.neonAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ajuste l'intensité de luminosité pour reposer la vision dans l'obscurité totale.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGraySecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = nightBrightness,
                    onValueChange = onBrightnessChange,
                    valueRange = 0.5f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = neonColors.neonAccent,
                        activeTrackColor = neonColors.neonAccent,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // SECTION 3: END-TO-END CRYPTOGRAPHIC PROTOCOL AUDIT
        Text(
            text = "ARCHITECTURE DE CHIFFREMENT (ZERO-KNOWLEDGE)",
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
                // Item 1
                CryptoSpecRow(
                    icon = Icons.Default.Lock,
                    title = "Algorithme de chiffrement",
                    value = "AES-256-GCM (Authentifié)"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 2
                CryptoSpecRow(
                    icon = Icons.Default.Key,
                    title = "Vérification d'intégrité",
                    value = "HMAC-SHA256 & Tag 128-bit"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 3
                CryptoSpecRow(
                    icon = Icons.Default.Shield,
                    title = "Clés de chiffrement sur l'appareil",
                    value = "Stockage sécurisé Room / SQLite local"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 4
                CryptoSpecRow(
                    icon = Icons.Default.CheckCircle,
                    title = "Confidentialité persistante (PFS)",
                    value = "Rotation automatique des clés éphémères"
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Master Fingerprint Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = neonColors.securityNoticeBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.8.dp, neonColors.neonAccent.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🔑 Empreinte publique de votre appareil :",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGraySecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SHA256: 7F9A-48B1-C3D2-98EF-6A01-BC77-44E2",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = neonColors.neonAccent,
                    fontWeight = FontWeight.Bold
                )
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

@Composable
fun CryptoSpecRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    val neonColors = LocalNeonColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = neonColors.neonAccent,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextGraySecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
