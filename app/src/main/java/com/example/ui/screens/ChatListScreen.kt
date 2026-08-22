package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ContactEntity
import com.example.ui.components.NeonAvatar
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.CheckmarkBlue
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextGraySecondary

@Composable
fun ChatListScreen(
    contacts: List<ContactEntity>,
    onSelectContact: (ContactEntity) -> Unit,
    onOpenSecurityVerification: (ContactEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val neonColors = LocalNeonColors.current

    if (contacts.isEmpty()) {
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
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = neonColors.neonAccent,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aucune discussion trouvée",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Démarrez une nouvelle conversation chiffrée avec le bouton '+' en bas à droite.",
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
                .testTag("chat_list_view")
        ) {
            // E2EE Banner for conversation list
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
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security",
                            tint = neonColors.neonAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vos discussions personnelles sont chiffrées de bout en bout.",
                            style = MaterialTheme.typography.labelSmall,
                            color = neonColors.neonAccent,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            items(contacts, key = { it.id }) { contact ->
                ChatItemRow(
                    contact = contact,
                    onClick = { onSelectContact(contact) },
                    onVerifyClick = { onOpenSecurityVerification(contact) }
                )
            }
        }
    }
}

@Composable
fun ChatItemRow(
    contact: ContactEntity,
    onClick: () -> Unit,
    onVerifyClick: () -> Unit
) {
    val neonColors = LocalNeonColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("chat_item_${contact.id}"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contact Avatar with Online Pulse & Verified Badge
            NeonAvatar(
                name = contact.name,
                avatarColorHex = contact.avatarColorHex,
                size = 52.dp,
                isOnline = contact.isOnline,
                isVerified = contact.isVerified
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Main Info: Name, Last message preview
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (contact.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Clé validée",
                                tint = neonColors.neonAccent,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Time or Status
                    Text(
                        text = if (contact.isOnline) "maintenant" else contact.lastSeen.takeLast(5),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (contact.unreadCount > 0) neonColors.neonAccent else TextGrayMuted,
                        fontWeight = if (contact.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = CheckmarkBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = contact.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGraySecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Pinned icon or Unread badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (contact.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Épinglé",
                                tint = TextGrayMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        if (contact.unreadCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = neonColors.neonAccent,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = contact.unreadCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF002A1C),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
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
