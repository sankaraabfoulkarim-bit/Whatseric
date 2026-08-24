package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallType
import com.example.data.ContactEntity
import com.example.data.MessageEntity
import com.example.data.MessageType
import com.example.ui.components.AudioVoiceMessageItem
import com.example.ui.components.CiphertextPayloadViewer
import com.example.ui.components.MessageStatusIndicator
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekIncomingBubble
import com.example.ui.theme.SleekNeonCyan
import com.example.ui.theme.SleekOutgoingBubble
import com.example.ui.theme.SleekOutgoingText
import com.example.ui.theme.SleekOutgoingTime
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextGraySecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contact: ContactEntity,
    messages: List<MessageEntity>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendVoiceNote: (Int) -> Unit,
    onSendPhoto: () -> Unit,
    onToggleStar: (String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onOpenVerification: () -> Unit,
    onOpenEphemeralSettings: () -> Unit,
    onStartCall: (ContactEntity, CallType) -> Unit,
    modifier: Modifier = Modifier
) {
    val neonColors = LocalNeonColors.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    // Auto-scroll to bottom when messages update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .testTag("chat_screen"),
        containerColor = neonColors.chatBackground,
        topBar = {
            // Sleek Header with subtle bottom border
            Surface(
                color = SleekBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = SleekBorderSubtle,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = SleekNeonCyan,
                        actionIconContentColor = SleekTextSecondary
                    ),
                    navigationIcon = {
                        // Sleek circular back button
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp, end = 4.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SleekSurface)
                                .clickable(onClick = onBack)
                                .testTag("chat_back_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = neonColors.neonAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    title = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenVerification() }
                        ) {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                // Sleek glowing cyan online status dot
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(neonColors.neonAccent)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (contact.isOnline) "ONLINE" else "OFFLINE • ${contact.lastSeen.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = neonColors.neonAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    actions = {
                        // Video call button
                        IconButton(
                            onClick = { onStartCall(contact, CallType.VIDEO) },
                            modifier = Modifier.testTag("start_video_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Appel vidéo chiffré",
                                tint = SleekTextSecondary
                            )
                        }

                        // Audio call button
                        IconButton(
                            onClick = { onStartCall(contact, CallType.AUDIO) },
                            modifier = Modifier.testTag("start_audio_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Appel vocal chiffré",
                                tint = SleekTextSecondary
                            )
                        }

                        // Overflow Menu
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Plus d'options",
                                    tint = SleekTextSecondary
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .background(SleekSurface)
                                    .border(1.dp, SleekBorder, RoundedCornerShape(8.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Infos du contact", color = Color.White) },
                                    onClick = {
                                        showMenu = false
                                        onOpenVerification()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = neonColors.neonAccent)
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        val timerText = if (contact.ephemeralTimerMinutes > 0)
                                            "Messages éphémères (${contact.ephemeralTimerMinutes}m)"
                                        else "Messages éphémères"
                                        Text(timerText, color = Color.White)
                                    },
                                    onClick = {
                                        showMenu = false
                                        onOpenEphemeralSettings()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = SleekNeonCyan)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Effacer la discussion", color = Color(0xFFFF5252)) },
                                    onClick = {
                                        showMenu = false
                                        onClearChat()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252))
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            // Sleek Footer Input Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SleekBackground)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Attachment Menu Popup
                AnimatedVisibility(visible = showAttachmentMenu) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = SleekSurface,
                        border = BorderStroke(1.dp, SleekBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Photo
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    showAttachmentMenu = false
                                    onSendPhoto()
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SleekNeonCyan.copy(alpha = 0.15f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Image, contentDescription = "Photo", tint = SleekNeonCyan)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Photo", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                            }

                            // Voice Note
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    showAttachmentMenu = false
                                    onSendVoiceNote(12)
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = neonColors.neonAccent.copy(alpha = 0.15f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Mic, contentDescription = "Vocal", tint = neonColors.neonAccent)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Vocal", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                            }

                            // Ephemeral timer
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    showAttachmentMenu = false
                                    onOpenEphemeralSettings()
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = NeonViolet.copy(alpha = 0.15f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Timer, contentDescription = "Éphémère", tint = NeonViolet)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Éphémère", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sleek Integrated Input Pill
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SleekSurface)
                            .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji / Mood Icon
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = "Emojis",
                            tint = SleekTextMuted,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { /* Emoji Picker */ }
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Seamless text input
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = "Message...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SleekTextMuted
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = neonColors.neonAccent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Attachment toggle icon
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Pièce jointe",
                            tint = SleekTextMuted,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { showAttachmentMenu = !showAttachmentMenu }
                                .testTag("attachment_menu_button")
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sleek Neon Send Action Button
                    if (inputText.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = neonColors.neonAccent,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    onSendMessage(inputText)
                                    inputText = ""
                                }
                                .testTag("send_message_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Envoyer",
                                    tint = Color(0xFF09090B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = SleekSurface,
                            border = BorderStroke(1.dp, SleekBorder),
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { onSendVoiceNote(8) }
                                .testTag("quick_voice_note_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Enregistrer un vocal",
                                    tint = neonColors.neonAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Sleek bottom indicator bar
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(width = 120.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SleekBorder.copy(alpha = 0.6f))
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            state = listState,
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sleek Date pill in center
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clip(CircleShape),
                        color = Color(0x801A1C1E)
                    ) {
                        Text(
                            text = "Aujourd'hui",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Message Items
            items(messages, key = { it.id }) { message ->
                MessageBubbleItem(
                    message = message,
                    timeFormatted = timeFormat.format(Date(message.timestamp)),
                    onToggleStar = { onToggleStar(message.id) },
                    onDelete = { onDeleteMessage(message.id) },
                    onOpenVerification = onOpenVerification
                )
            }
        }
    }
}

@Composable
fun MessageBubbleItem(
    message: MessageEntity,
    timeFormatted: String,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit,
    onOpenVerification: () -> Unit
) {
    val neonColors = LocalNeonColors.current
    val isOutgoing = message.senderId == "me"
    val isSystemNotice = message.messageType == MessageType.SECURITY_ALERT

    if (isSystemNotice) {
        // Centered system alert pill
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0x801A1C1E),
                shape = CircleShape,
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .clickable { onOpenVerification() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = neonColors.neonAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = message.plainText,
                        style = MaterialTheme.typography.bodySmall,
                        color = neonColors.neonAccent,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        return
    }

    // Sleek Chat Bubble Item
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        // Outgoing: rounded-2xl rounded-tr-none, border-r-2 #00F2FF, bg #003B3E
        // Incoming: rounded-2xl rounded-tl-none, border-l-2 #00F2FF, bg #1A1C1E
        val bubbleShape = if (isOutgoing) {
            RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 0.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        } else {
            RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        }

        val bubbleBg = if (isOutgoing) neonColors.outgoingBubble else neonColors.incomingBubble
        val accentColor = neonColors.neonAccent

        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(bubbleShape)
                .drawBehind {
                    // Draw sleek 2.5dp left or right accent line
                    if (isOutgoing) {
                        drawLine(
                            color = accentColor,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    } else {
                        drawLine(
                            color = accentColor,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                }
                .testTag(if (isOutgoing) "outgoing_bubble" else "incoming_bubble"),
            color = bubbleBg,
            shape = bubbleShape,
            border = BorderStroke(0.8.dp, if (isOutgoing) accentColor.copy(alpha = 0.3f) else SleekBorder)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Message Content (Text, Voice Note, Photo)
                when (message.messageType) {
                    MessageType.VOICE_NOTE -> {
                        AudioVoiceMessageItem(
                            durationSeconds = message.voiceDurationSeconds,
                            isOutgoing = isOutgoing
                        )
                    }

                    MessageType.PHOTO -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            color = SleekSurfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = neonColors.neonAccent,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Photo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = neonColors.neonAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = message.plainText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isOutgoing) SleekOutgoingText else SleekTextPrimary
                        )
                    }

                    else -> {
                        Text(
                            text = message.plainText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = if (isOutgoing) neonColors.outgoingText else SleekTextPrimary,
                            lineHeight = 21.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Meta Row: Timestamp, Star, Status Checkmarks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isStarred) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Starred",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOutgoing) neonColors.outgoingTime else SleekTextMuted,
                        fontSize = 10.sp
                    )

                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIndicator(status = message.status)
                    }
                }
            }
        }
    }
}

