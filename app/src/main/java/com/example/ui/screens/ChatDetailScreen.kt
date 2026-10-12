package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioPlayerState
import com.example.model.CallType
import com.example.model.Chat
import com.example.model.Message
import com.example.model.MessageType
import com.example.ui.components.AttachmentBottomSheet
import com.example.ui.components.AttachmentOption
import com.example.ui.components.ChatAvatar
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ContactDialog
import com.example.ui.components.CreatePollDialog
import com.example.ui.components.LocationDialog
import com.example.ui.components.MessageBubble
import com.example.ui.theme.ChatBackgroundDark
import com.example.ui.theme.ChatBackgroundLight
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: Chat,
    messages: List<Message>,
    audioPlayerState: AudioPlayerState,
    replyingToMessage: Message?,
    editingMessage: Message?,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onEditMessage: (String, String) -> Unit,
    onSendAudioNote: (durationSec: Int, waveform: List<Float>) -> Unit,
    onSendMedia: (type: MessageType, url: String, caption: String?, isViewOnce: Boolean, isHd: Boolean) -> Unit,
    onSendLocation: (lat: Double, lng: Double, address: String) -> Unit,
    onSendContact: (name: String, phone: String) -> Unit,
    onCreatePoll: (question: String, options: List<String>, allowMultiple: Boolean) -> Unit,
    onVotePoll: (messageId: String, optionId: String) -> Unit,
    onAddReaction: (messageId: String, emoji: String) -> Unit,
    onOpenViewOnce: (String) -> Unit,
    onPlayAudio: (String) -> Unit,
    onSeekAudio: (Float) -> Unit,
    onToggleAudioSpeed: () -> Unit,
    onToggleAudioTranscription: () -> Unit,
    onSetReplyingTo: (Message?) -> Unit,
    onSetEditingMessage: (Message?) -> Unit,
    onStartCall: (String, String?, CallType) -> Unit,
    onToggleLockChat: (String) -> Unit,
    onSetEphemeral: (String, Int) -> Unit
) {
    BackHandler { onBack() }

    val isDark = isSystemInDarkTheme()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var showTopMenu by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showEphemeralDialog by remember { mutableStateOf(false) }

    // Synchronize edit text if editing
    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            inputText = editingMessage.text
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) ChatBackgroundDark else ChatBackgroundLight)
            .testTag("chat_detail_screen")
    ) {
        // Chat Top App Bar
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            navigationIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    ChatAvatar(chat = chat, modifier = Modifier.size(38.dp))
                }
            },
            title = {
                Column(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable { /* Profile info */ }
                ) {
                    Text(
                        text = chat.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    val subtitle = when {
                        chat.typingUser != null -> chat.typingUser
                        chat.isMetaAi -> "Assistente Virtual Oficial"
                        chat.isGroup -> "${chat.membersCount} participantes"
                        else -> "online"
                    }
                    Text(
                        text = subtitle ?: "online",
                        fontSize = 12.sp,
                        color = if (chat.typingUser != null) WhatsAppGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                if (!chat.isMetaAi) {
                    // Video Call
                    IconButton(
                        onClick = { onStartCall(chat.name, chat.avatarUrl, CallType.VIDEO) },
                        modifier = Modifier.testTag("video_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Chamada de Vídeo",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Audio Call
                    IconButton(
                        onClick = { onStartCall(chat.name, chat.avatarUrl, CallType.AUDIO) },
                        modifier = Modifier.testTag("audio_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Chamada de Voz",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // More Menu
                Box {
                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mensagens temporárias") },
                            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                            onClick = {
                                showTopMenu = false
                                showEphemeralDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (chat.isLocked) "Desbloquear conversa" else "Bloquear conversa") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                            onClick = {
                                showTopMenu = false
                                onToggleLockChat(chat.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ver contato") },
                            onClick = { showTopMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Mídia, links e docs") },
                            onClick = { showTopMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Pesquisar na conversa") },
                            onClick = { showTopMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Limpar conversa") },
                            onClick = { showTopMenu = false }
                        )
                    }
                }
            }
        )

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    audioPlayerState = audioPlayerState,
                    onPlayAudio = onPlayAudio,
                    onSeekAudio = onSeekAudio,
                    onToggleAudioSpeed = onToggleAudioSpeed,
                    onToggleAudioTranscription = onToggleAudioTranscription,
                    onVotePoll = onVotePoll,
                    onAddReaction = onAddReaction,
                    onReplyToMessage = onSetReplyingTo,
                    onEditMessage = onSetEditingMessage,
                    onOpenViewOnce = onOpenViewOnce
                )
            }
        }

        // Input Bar
        ChatInputBar(
            inputText = inputText,
            onInputTextChange = { inputText = it },
            onSendMessage = { text ->
                if (editingMessage != null) {
                    onEditMessage(editingMessage.id, text)
                } else {
                    onSendMessage(text)
                }
                inputText = ""
            },
            onSendAudioNote = onSendAudioNote,
            replyingToMessage = replyingToMessage,
            onCancelReply = { onSetReplyingTo(null) },
            editingMessage = editingMessage,
            onCancelEdit = {
                onSetEditingMessage(null)
                inputText = ""
            },
            onOpenAttachments = { showAttachmentSheet = true },
            onQuickCameraClick = {
                onSendMedia(
                    MessageType.IMAGE,
                    "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800",
                    "Foto instantânea capturada com a câmera",
                    false,
                    true
                )
            }
        )
    }

    // Attachment Sheet
    if (showAttachmentSheet) {
        AttachmentBottomSheet(
            onDismiss = { showAttachmentSheet = false },
            onSelectOption = { option ->
                showAttachmentSheet = false
                when (option) {
                    AttachmentOption.DOCUMENT -> {
                        onSendMedia(
                            MessageType.DOCUMENT,
                            "https://example.com/docs/relatorio_financeiro.pdf",
                            null,
                            false,
                            false
                        )
                    }
                    AttachmentOption.CAMERA -> {
                        onSendMedia(
                            MessageType.IMAGE,
                            "https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?w=800",
                            "Foto com visualização única",
                            true,
                            true
                        )
                    }
                    AttachmentOption.GALLERY -> {
                        onSendMedia(
                            MessageType.IMAGE,
                            "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800",
                            "Envio da galeria em Alta Definição (HD)",
                            false,
                            true
                        )
                    }
                    AttachmentOption.AUDIO -> {
                        onSendAudioNote(
                            25,
                            listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 0.6f, 0.3f, 0.85f, 0.6f, 0.4f)
                        )
                    }
                    AttachmentOption.LOCATION -> {
                        showLocationDialog = true
                    }
                    AttachmentOption.CONTACT -> {
                        showContactDialog = true
                    }
                    AttachmentOption.POLL -> {
                        showPollDialog = true
                    }
                }
            }
        )
    }

    // Poll Dialog
    if (showPollDialog) {
        CreatePollDialog(
            onDismiss = { showPollDialog = false },
            onCreatePoll = { question, options, allowMultiple ->
                onCreatePoll(question, options, allowMultiple)
                showPollDialog = false
            }
        )
    }

    // Location Dialog
    if (showLocationDialog) {
        LocationDialog(
            onDismiss = { showLocationDialog = false },
            onSendLocation = { lat, lng, address ->
                onSendLocation(lat, lng, address)
                showLocationDialog = false
            }
        )
    }

    // Contact Dialog
    if (showContactDialog) {
        ContactDialog(
            onDismiss = { showContactDialog = false },
            onSendContact = { name, phone ->
                onSendContact(name, phone)
                showContactDialog = false
            }
        )
    }

    // Ephemeral Disappearing Messages Dialog
    if (showEphemeralDialog) {
        AlertDialog(
            onDismissRequest = { showEphemeralDialog = false },
            title = { Text("Mensagens Temporárias", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Para mais privacidade, novas mensagens desaparecerão desta conversa após a duração selecionada.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val durations = listOf(
                        24 to "24 horas",
                        168 to "7 dias",
                        2160 to "90 dias",
                        0 to "Desativadas"
                    )
                    durations.forEach { (hours, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetEphemeral(chat.id, hours)
                                    showEphemeralDialog = false
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = chat.ephemeralSettingHours == hours,
                                onClick = {
                                    onSetEphemeral(chat.id, hours)
                                    showEphemeralDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = WhatsAppGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showEphemeralDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}
