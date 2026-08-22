package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CallType
import com.example.ui.components.NeonAvatar
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextGraySecondary
import com.example.viewmodel.ActiveCallState

@Composable
fun ActiveCallDialog(
    callState: ActiveCallState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    val neonColors = LocalNeonColors.current
    val contact = callState.contact

    // Pulsing wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val minutes = callState.durationSeconds / 60
    val seconds = callState.durationSeconds % 60
    val formattedDuration = String.format("%02d:%02d", minutes, seconds)

    Dialog(
        onDismissRequest = onEndCall,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF040910),
                            Color(0xFF091420),
                            Color(0xFF020406)
                        )
                    )
                )
                .testTag("active_call_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top E2EE security info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Surface(
                        color = Color(0x3300F59B),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, neonColors.neonAccent.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = neonColors.neonAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Appel chiffré de bout en bout",
                                style = MaterialTheme.typography.labelSmall,
                                color = neonColors.neonAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Clé de session : ${callState.sessionKeyFingerprint}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextGraySecondary
                    )
                }

                // Middle: Avatar + Pulsing circle + Name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(180.dp)
                    ) {
                        // Outer pulsing wave
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(neonColors.neonAccent.copy(alpha = 0.15f))
                        )

                        // Middle wave
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(neonColors.neonAccent.copy(alpha = 0.25f))
                        )

                        NeonAvatar(
                            name = contact.name,
                            avatarColorHex = contact.avatarColorHex,
                            size = 100.dp,
                            isVerified = contact.isVerified
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (callState.callType == CallType.VIDEO) "Appel Vidéo Sécurisé • $formattedDuration" else "Appel Vocal Sécurisé • $formattedDuration",
                        style = MaterialTheme.typography.bodyMedium,
                        color = neonColors.neonAccent
                    )
                }

                // Bottom: Action Controls (Mute, Speaker, Hangup)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute button
                    IconButton(
                        onClick = onMuteToggle,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (callState.isMuted) Color(0xFF2E3842) else Color(0xFF14202C))
                            .border(1.dp, Color(0xFF2C3E50), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (callState.isMuted) NeonRed else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Hangup button (Red)
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(NeonRed)
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Raccrocher",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Speaker button
                    IconButton(
                        onClick = onSpeakerToggle,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (callState.isSpeakerOn) NeonCyan.copy(alpha = 0.25f) else Color(0xFF14202C))
                            .border(1.dp, if (callState.isSpeakerOn) NeonCyan else Color(0xFF2C3E50), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Haut-parleur",
                            tint = if (callState.isSpeakerOn) NeonCyan else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
