package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Badge
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Chat
import com.example.ui.components.ChatListItem
import com.example.ui.theme.WhatsAppGreen

@Composable
fun ChatsScreen(
    chats: List<Chat>,
    lockedChatsCount: Int,
    isChatLockUnlocked: Boolean,
    onChatClick: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onLockedChatsClick: () -> Unit,
    onMetaAiClick: () -> Unit,
    onNewChatClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("chats_list")
        ) {
            // Locked Chats Entry Header
            if (lockedChatsCount > 0) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLockedChatsClick() }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("locked_chats_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Conversas Bloqueadas",
                                        tint = WhatsAppGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Conversas bloqueadas",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = if (isChatLockUnlocked) "$lockedChatsCount conversas desbloqueadas" else "Toque para autenticar com biometria",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!isChatLockUnlocked) {
                                Badge(
                                    containerColor = WhatsAppGreen,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = lockedChatsCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 78.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }

            // Archived Chats Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Archived */ }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Archive,
                            contentDescription = "Arquivadas",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Arquivadas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 78.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }

            // Chat Items
            items(chats, key = { it.id }) { chat ->
                ChatListItem(
                    chat = chat,
                    onClick = { onChatClick(chat.id) },
                    onTogglePin = { onTogglePin(chat.id) },
                    onToggleLock = { onToggleLock(chat.id) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 82.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            }

            if (chats.none { !it.isMetaAi }) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nenhuma conversa iniciada",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toque no botão verde abaixo para iniciar uma nova conversa.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Bottom padding for FAB
            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        // Floating Action Buttons (New Chat + Meta AI shortcut)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Secondary Meta AI FAB
            SmallFloatingActionButton(
                onClick = onMetaAiClick,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = WhatsAppGreen,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("meta_ai_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Meta AI",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Primary New Chat FAB
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = WhatsAppGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("new_chat_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = "Nova Conversa",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
