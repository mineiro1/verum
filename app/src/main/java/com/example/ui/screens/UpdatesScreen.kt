package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Channel
import com.example.model.ChannelPost
import com.example.model.StatusStory
import com.example.model.UserProfile
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UpdatesScreen(
    stories: List<StatusStory>,
    channels: List<Channel>,
    channelPosts: Map<String, List<ChannelPost>>,
    currentUser: UserProfile,
    onStoryClick: (StatusStory) -> Unit,
    onToggleFollowChannel: (String) -> Unit,
    onChannelReaction: (channelId: String, postId: String, emoji: String) -> Unit,
    onPostStatus: (text: String?, imageUrl: String?) -> Unit
) {
    var showNewStatusDialog by remember { mutableStateOf(false) }
    val myStory = stories.find { it.isMine }
    val otherStories = stories.filter { !it.isMine }
    val recentStories = otherStories.filter { !it.isViewed }
    val viewedStories = otherStories.filter { it.isViewed }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("updates_screen")
        ) {
            // STATUS SECTION HEADER
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Status",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // My Status Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (myStory != null) {
                                onStoryClick(myStory)
                            } else {
                                showNewStatusDialog = true
                            }
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(56.dp)) {
                        AsyncImage(
                            model = currentUser.avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                            contentDescription = "Meu Status",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                        // Add plus badge on corner
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(WhatsAppGreen)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar status",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Meu status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (myStory != null) "Há ${timeFormat.format(Date(myStory.timestamp))} • ${myStory.viewersCount} visualizações" else "Toque para atualizar seu status",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Recent Updates
            if (recentStories.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Atualizações recentes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(recentStories, key = { it.id }) { story ->
                    StoryItemRow(story = story, isViewed = false, onClick = { onStoryClick(story) })
                }
            }

            // Viewed Updates
            if (viewedStories.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Atualizações visualizadas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(viewedStories, key = { it.id }) { story ->
                    StoryItemRow(story = story, isViewed = true, onClick = { onStoryClick(story) })
                }
            }

            // CHANNELS SECTION
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Canais",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fique por dentro dos assuntos do seu interesse",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { /* Explorar canais */ },
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Explorar", color = WhatsAppGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Channels List
            items(channels, key = { it.id }) { channel ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("channel_item_${channel.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = channel.avatarUrl,
                            contentDescription = channel.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = channel.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (channel.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verificado",
                                        tint = WhatsAppGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${channel.followersCount} • ${channel.lastPostTime}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onToggleFollowChannel(channel.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (channel.isFollowing) MaterialTheme.colorScheme.surfaceVariant else WhatsAppGreen.copy(alpha = 0.15f),
                                contentColor = if (channel.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else WhatsAppGreen
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                text = if (channel.isFollowing) "Seguindo" else "Seguir",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Channel Post Preview
                    val posts = channelPosts[channel.id]
                    if (!posts.isNullOrEmpty()) {
                        val post = posts.first()
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = post.text,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (post.imageUrl != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    AsyncImage(
                                        model = post.imageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("🔥", "❤️", "👏").forEach { emoji ->
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.clickable {
                                                    onChannelReaction(channel.id, post.id, emoji)
                                                }
                                            ) {
                                                Text(
                                                    text = emoji,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${post.viewsCount} visualizações",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        // Action FABs (Text Status + Camera Status)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { showNewStatusDialog = true },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("text_status_fab")
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Escrever status", modifier = Modifier.size(18.dp))
            }

            FloatingActionButton(
                onClick = {
                    onPostStatus(
                        null,
                        "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800"
                    )
                },
                containerColor = WhatsAppGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("camera_status_fab")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Tirar foto para status", modifier = Modifier.size(24.dp))
            }
        }
    }

    // New Text Status Dialog
    if (showNewStatusDialog) {
        var statusText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewStatusDialog = false },
            title = { Text("Novo Status de Texto", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = statusText,
                        onValueChange = { statusText = it },
                        placeholder = { Text("O que você está pensando?") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (statusText.isNotBlank()) {
                            onPostStatus(statusText, null)
                        }
                        showNewStatusDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                ) {
                    Text("Publicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewStatusDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun StoryItemRow(
    story: StatusStory,
    isViewed: Boolean,
    onClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp)
            .testTag("story_item_${story.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status avatar with green ring if not viewed, grey ring if viewed
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(
                    width = 2.5.dp,
                    color = if (isViewed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else WhatsAppGreen,
                    shape = CircleShape
                )
                .padding(3.dp)
        ) {
            AsyncImage(
                model = story.userAvatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                contentDescription = story.userName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = story.userName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = "Hoje às ${timeFormat.format(Date(story.timestamp))}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
