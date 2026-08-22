package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallDirection
import com.example.data.CallLogEntity
import com.example.data.CallType
import com.example.data.ContactEntity
import com.example.ui.components.NeonAvatar
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextGraySecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallsScreen(
    calls: List<CallLogEntity>,
    contacts: List<ContactEntity>,
    onStartCall: (ContactEntity, CallType) -> Unit,
    modifier: Modifier = Modifier
) {
    val neonColors = LocalNeonColors.current
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    if (calls.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = neonColors.neonAccent.copy(alpha = 0.15f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = neonColors.neonAccent,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aucun appel récent",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tous vos appels vocaux et vidéo sont chiffrés de bout en bout.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGraySecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("calls_screen_list")
        ) {
            // E2EE Call Security Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = neonColors.securityNoticeBg,
                    border = BorderStroke(0.6.dp, neonColors.neonAccent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Chiffré",
                            tint = neonColors.neonAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vos appels vocaux et vidéo sont protégés par le protocole E2EE.",
                            style = MaterialTheme.typography.labelSmall,
                            color = neonColors.neonAccent,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "RÉCENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = neonColors.neonAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(calls, key = { it.id }) { call ->
                val associatedContact = contacts.find { it.id == call.contactId } ?: ContactEntity(
                    id = call.contactId,
                    name = call.contactName,
                    phoneNumber = "",
                    avatarColorHex = call.avatarColorHex,
                    statusMessage = ""
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartCall(associatedContact, call.callType) }
                        .testTag("call_item_${call.id}"),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeonAvatar(
                            name = call.contactName,
                            avatarColorHex = call.avatarColorHex,
                            size = 48.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = call.contactName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (call.direction == CallDirection.MISSED) NeonRed else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (call.direction) {
                                    CallDirection.INCOMING -> {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.CallReceived,
                                            contentDescription = "Entrant",
                                            tint = neonColors.neonAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    CallDirection.OUTGOING -> {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.CallMade,
                                            contentDescription = "Sortant",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    CallDirection.MISSED -> {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.CallMissed,
                                            contentDescription = "Manqué",
                                            tint = NeonRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = dateFormat.format(Date(call.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGraySecondary
                                )

                                if (call.durationSeconds > 0) {
                                    Text(
                                        text = " (${call.durationSeconds}s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextGrayMuted
                                    )
                                }
                            }
                        }

                        // Call Action Button
                        IconButton(
                            onClick = { onStartCall(associatedContact, call.callType) },
                            modifier = Modifier.testTag("callback_button_${call.id}")
                        ) {
                            Icon(
                                imageVector = if (call.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                                contentDescription = "Rappeler",
                                tint = neonColors.neonAccent
                            )
                        }
                    }
                }
            }
        }
    }
}
