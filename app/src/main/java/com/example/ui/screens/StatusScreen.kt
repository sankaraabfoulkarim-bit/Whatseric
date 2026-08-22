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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.StatusStoryEntity
import com.example.ui.components.NeonAvatar
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextGraySecondary
import kotlinx.coroutines.delay

@Composable
fun StatusScreen(
    stories: List<StatusStoryEntity>,
    activeStory: StatusStoryEntity?,
    onOpenStory: (StatusStoryEntity) -> Unit,
    onCloseStory: () -> Unit,
    onPostStatus: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val neonColors = LocalNeonColors.current
    var showNewStatusDialog by remember { mutableStateOf(false) }
    var newStatusText by remember { mutableStateOf("") }
    var selectedGradientIndex by remember { mutableStateOf(0) }

    val gradients = listOf(
        Brush.verticalGradient(listOf(Color(0xFF004D40), Color(0xFF001F18))),
        Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF0A0E2E))),
        Brush.verticalGradient(listOf(Color(0xFF4A148C), Color(0xFF1D0538))),
        Brush.verticalGradient(listOf(Color(0xFFE65100), Color(0xFF2A0C00)))
    )

    // Full Story Viewer Dialog
    if (activeStory != null) {
        Dialog(
            onDismissRequest = onCloseStory,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var progress by remember { mutableFloatStateOf(0f) }

            LaunchedEffect(activeStory.id) {
                progress = 0f
                for (i in 1..100) {
                    delay(50)
                    progress = i / 100f
                }
                onCloseStory()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradients[activeStory.backgroundGradientIndex % gradients.size])
                    .clickable { onCloseStory() }
                    .testTag("story_viewer_modal"),
                contentAlignment = Alignment.Center
            ) {
                // Top Progress Bar & Header
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Progress line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(neonColors.neonAccent)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Author row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NeonAvatar(
                                    name = activeStory.contactName,
                                    avatarColorHex = activeStory.avatarColorHex,
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = activeStory.contactName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = neonColors.neonAccent,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Statut chiffré E2EE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = neonColors.neonAccent
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = onCloseStory) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Centered Text Content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeStory.textCaption,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )
                    }

                    // Bottom notice
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = neonColors.neonAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Visible 24h • Chiffré de bout en bout",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    // New Status Dialog
    if (showNewStatusDialog) {
        Dialog(onDismissRequest = { showNewStatusDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, neonColors.neonAccent, RoundedCornerShape(20.dp)),
                color = neonColors.cardBackground
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Nouveau statut chiffré",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newStatusText,
                        onValueChange = { newStatusText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Écrivez votre statut...") },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonColors.neonAccent,
                            unfocusedBorderColor = neonColors.cardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ambiance de fond néon :",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGraySecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Émeraude", "Cyan", "Violet", "Ambre").forEachIndexed { index, label ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedGradientIndex = index }
                                    .border(
                                        width = if (selectedGradientIndex == index) 2.dp else 0.5.dp,
                                        color = if (selectedGradientIndex == index) neonColors.neonAccent else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedGradientIndex == index) neonColors.neonAccent else TextGraySecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        androidx.compose.material3.TextButton(onClick = { showNewStatusDialog = false }) {
                            Text("Annuler", color = TextGraySecondary)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        androidx.compose.material3.Button(
                            onClick = {
                                onPostStatus(newStatusText, selectedGradientIndex)
                                newStatusText = ""
                                showNewStatusDialog = false
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = neonColors.neonAccent,
                                contentColor = Color(0xFF002A1C)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Publier", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("status_screen_list")
    ) {
        // "Mon statut" Row
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNewStatusDialog = true }
                    .padding(vertical = 8.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                        NeonAvatar(
                            name = "Moi",
                            avatarColorHex = "#00F59B",
                            size = 52.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(neonColors.neonAccent)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Ajouter statut",
                                tint = Color(0xFF002A1C),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mon statut",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Appuyez pour ajouter une actualité chiffrée",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGraySecondary
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "MISES À JOUR RÉCENTES",
                style = MaterialTheme.typography.labelSmall,
                color = neonColors.neonAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Contact Stories List
        items(stories, key = { it.id }) { story ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenStory(story) }
                    .testTag("story_item_${story.id}"),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glowing Neon Story Ring
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.5.dp,
                                color = if (story.isViewed) TextGrayMuted else neonColors.neonAccent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        NeonAvatar(
                            name = story.contactName,
                            avatarColorHex = story.avatarColorHex,
                            size = 46.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = story.contactName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (story.isViewed) "Vu • Statut E2EE" else "Nouveau statut chiffré",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (story.isViewed) TextGrayMuted else neonColors.neonAccent
                        )
                    }
                }
            }
        }
    }
}
