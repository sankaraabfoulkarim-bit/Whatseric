package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MessageStatus
import com.example.ui.theme.CheckmarkBlue
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextGraySecondary

@Composable
fun NeonAvatar(
    name: String,
    avatarColorHex: String,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    isVerified: Boolean = false,
    modifier: Modifier = Modifier
) {
    val neonColors = LocalNeonColors.current
    val parsedColor = remember(avatarColorHex) {
        try {
            Color(android.graphics.Color.parseColor(avatarColorHex))
        } catch (e: Exception) {
            neonColors.neonAccent
        }
    }

    val initials = remember(name) {
        val parts = name.trim().split(" ").filter { it.isNotEmpty() }
        if (parts.size >= 2) {
            "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        } else if (parts.isNotEmpty() && parts[0].isNotEmpty()) {
            parts[0].take(2).uppercase()
        } else "?"
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glowing halo for active online contacts
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size + 4.dp)
                    .clip(CircleShape)
                    .background(neonColors.neonGlow)
            )
        }

        // Avatar Core
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(parsedColor.copy(alpha = 0.35f), parsedColor.copy(alpha = 0.15f))
                    )
                )
                .border(1.5.dp, parsedColor.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = parsedColor,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38f).sp
            )
        }

        // Online neon green pulse dot
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676))
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        // Verified padlock badge
        if (isVerified && !isOnline) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Verified Key",
                    tint = Color(0xFF070B10),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
fun E2EELockBadge(
    modifier: Modifier = Modifier,
    label: String = "Chiffré de bout en bout",
    compact: Boolean = false
) {
    val neonColors = LocalNeonColors.current
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(0.8.dp, neonColors.neonAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        color = neonColors.securityNoticeBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "E2EE Lock",
                tint = neonColors.neonAccent,
                modifier = Modifier.size(if (compact) 12.dp else 14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = neonColors.neonAccent,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MessageStatusIndicator(status: MessageStatus) {
    when (status) {
        MessageStatus.SENDING -> {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = "Sending",
                tint = TextGrayMuted,
                modifier = Modifier.size(12.dp)
            )
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = TextGraySecondary,
                modifier = Modifier.size(14.dp)
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = TextGraySecondary,
                modifier = Modifier.size(14.dp)
            )
        }
        MessageStatus.READ -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = CheckmarkBlue,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun AudioVoiceMessageItem(
    durationSeconds: Int,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    val neonColors = LocalNeonColors.current
    var isPlaying by remember { mutableStateOf(false) }
    val primaryColor = if (isOutgoing) neonColors.neonAccent else NeonCyan

    // Waveform simulation bars
    val waveHeights = remember {
        listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 0.6f, 0.3f, 0.85f, 0.95f, 0.45f, 0.65f, 0.8f, 0.5f, 0.35f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.2f))
                .border(1.dp, primaryColor, CircleShape)
                .testTag("voice_play_button")
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause vocal" else "Lire vocal",
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Animated Waveform Bars
        Row(
            modifier = Modifier
                .weight(1f)
                .height(28.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            waveHeights.forEachIndexed { index, heightScale ->
                val barAlpha = if (isPlaying && (index % 3 == 0)) 1f else 0.7f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height((24 * heightScale).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(primaryColor.copy(alpha = barAlpha))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "0:${durationSeconds.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SafetyNumberGrid(
    safetyNumber: String,
    modifier: Modifier = Modifier
) {
    val neonColors = LocalNeonColors.current
    val blocks = remember(safetyNumber) {
        safetyNumber.split(" ").filter { it.isNotBlank() }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = neonColors.cardBackground
        ),
        border = BorderStroke(1.dp, neonColors.cardBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield",
                    tint = neonColors.neonAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Empreinte Cryptographique 60 Chiffres",
                    style = MaterialTheme.typography.titleMedium,
                    color = neonColors.neonAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 4
            ) {
                blocks.forEach { block ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, neonColors.neonAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = block,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CiphertextPayloadViewer(
    plainText: String,
    cipherText: String,
    ivHex: String,
    authTag: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val neonColors = LocalNeonColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { isExpanded = !isExpanded }
            .border(0.5.dp, neonColors.neonSecondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        color = neonColors.cardBackground.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Chiffrement",
                        tint = neonColors.neonSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Données Chiffrées AES-256-GCM",
                        style = MaterialTheme.typography.labelSmall,
                        color = neonColors.neonSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "Masquer" else "Inspecter",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGraySecondary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Ciphertext",
                        tint = TextGraySecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "📦 Charge chiffrée (Base64) :",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGraySecondary
                    )
                    Text(
                        text = cipherText,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = neonColors.neonAccent,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔑 Vecteur d'Initialisation (IV 96-bit) :",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGraySecondary
                    )
                    Text(
                        text = ivHex,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = NeonCyan
                    )

                    if (authTag.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🛡️ Tag d'authentification GCM (128-bit) :",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGraySecondary
                        )
                        Text(
                            text = authTag,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}
