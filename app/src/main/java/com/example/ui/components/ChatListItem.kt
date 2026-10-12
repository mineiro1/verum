package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Chat
import com.example.model.MessageStatus
import com.example.ui.theme.CheckmarkBlue
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.MetaAiBlue
import com.example.ui.theme.MetaAiCyan
import com.example.ui.theme.MetaAiPurple
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleLock: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showContextMenu = true }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_item_${chat.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            ChatAvatar(chat = chat, modifier = Modifier.size(52.dp))

            Spacer(modifier = Modifier.width(14.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.name,
                        fontSize = 16.sp,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = chat.lastMessageTime,
                        fontSize = 12.sp,
                        color = if (chat.unreadCount > 0) WhatsAppGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Message Status Checkmark (if sent by me)
                    if (chat.lastMessageIsFromMe && !chat.isMetaAi) {
                        when (chat.lastMessageStatus) {
                            MessageStatus.READ -> {
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = "Lida",
                                    tint = CheckmarkBlue,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(end = 4.dp)
                                )
                            }
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = "Entregue",
                                    tint = CheckmarkGrey,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(end = 4.dp)
                                )
                            }
                            MessageStatus.SENT, MessageStatus.SENDING -> {
                                Icon(
                                    Icons.Default.Done,
                                    contentDescription = "Enviada",
                                    tint = CheckmarkGrey,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(end = 4.dp)
                                )
                            }
                        }
                    }

                    // Typing indicator or last message
                    val displayMessage = chat.typingUser ?: chat.lastMessage
                    val isTyping = chat.typingUser != null

                    Text(
                        text = displayMessage,
                        fontSize = 14.sp,
                        color = if (isTyping) WhatsAppGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (chat.unreadCount > 0 || isTyping) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Icons on the right: Muted, Pinned, Unread count
                    if (chat.isMuted) {
                        Icon(
                            Icons.Outlined.NotificationsOff,
                            contentDescription = "Silenciado",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    if (chat.isPinned) {
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = "Fixado",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    if (chat.unreadCount > 0) {
                        Badge(
                            containerColor = WhatsAppGreen,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Long Press Context Menu
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (chat.isPinned) "Desafixar conversa" else "Fixar conversa") },
                leadingIcon = { Icon(Icons.Filled.PushPin, contentDescription = null) },
                onClick = {
                    showContextMenu = false
                    onTogglePin()
                }
            )
            DropdownMenuItem(
                text = { Text(if (chat.isLocked) "Desbloquear conversa" else "Bloquear conversa") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                onClick = {
                    showContextMenu = false
                    onToggleLock()
                }
            )
            DropdownMenuItem(
                text = { Text("Silenciar notificações") },
                leadingIcon = { Icon(Icons.Outlined.NotificationsOff, contentDescription = null) },
                onClick = { showContextMenu = false }
            )
        }
    }
}

@Composable
fun ChatAvatar(chat: Chat, modifier: Modifier = Modifier) {
    if (chat.isMetaAi) {
        // Distinctive Meta AI gradient avatar
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(MetaAiBlue, MetaAiPurple, MetaAiCyan, MetaAiBlue)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
            )
        }
    } else if (chat.avatarUrl != null) {
        AsyncImage(
            model = chat.avatarUrl,
            contentDescription = chat.name,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    } else {
        // Fallback initials or group icon
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (chat.isGroup) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = "Grupo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = chat.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WhatsAppGreen
                )
            }
        }
    }
}
