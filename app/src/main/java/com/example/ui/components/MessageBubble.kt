package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AudioPlayerState
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.ui.theme.ChatBubbleReceivedDark
import com.example.ui.theme.ChatBubbleReceivedLight
import com.example.ui.theme.ChatBubbleSentDark
import com.example.ui.theme.ChatBubbleSentLight
import com.example.ui.theme.CheckmarkBlue
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    audioPlayerState: AudioPlayerState,
    onPlayAudio: (String) -> Unit,
    onSeekAudio: (Float) -> Unit,
    onToggleAudioSpeed: () -> Unit,
    onToggleAudioTranscription: () -> Unit,
    onVotePoll: (messageId: String, optionId: String) -> Unit,
    onAddReaction: (messageId: String, emoji: String) -> Unit,
    onReplyToMessage: (Message) -> Unit,
    onEditMessage: (Message) -> Unit,
    onOpenViewOnce: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var showContextMenu by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // System message banner (e.g. security encryption or disappearing messages)
    if (message.type == MessageType.SYSTEM) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = message.text,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        return
    }

    val bubbleColor = if (message.isFromMe) {
        if (isDark) ChatBubbleSentDark else ChatBubbleSentLight
    } else {
        if (isDark) ChatBubbleReceivedDark else ChatBubbleReceivedLight
    }

    val alignment = if (message.isFromMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalAlignment = alignment
    ) {
        Box {
            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (message.isFromMe) 12.dp else 2.dp,
                    bottomEnd = if (message.isFromMe) 2.dp else 12.dp
                ),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .widthIn(min = 80.dp, max = 320.dp)
                    .combinedClickable(
                        onClick = {
                            if (message.isViewOnce && !message.isViewOnceOpened) {
                                onOpenViewOnce(message.id)
                            }
                        },
                        onLongClick = { showContextMenu = true }
                    )
                    .testTag("message_bubble_${message.id}")
            ) {
                Column(modifier = Modifier.padding(6.dp)) {

                    // Quoted Reply Preview
                    if (message.replyToText != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(32.dp)
                                        .background(WhatsAppGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = message.replyToSender ?: "Você",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WhatsAppGreen
                                    )
                                    Text(
                                        text = message.replyToText,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Content based on Message Type
                    when (message.type) {
                        MessageType.TEXT -> {
                            Text(
                                text = parseWhatsAppFormatting(message.text),
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        MessageType.AUDIO -> {
                            AudioWaveformPlayer(
                                messageId = message.id,
                                durationSeconds = message.audioDurationSeconds,
                                waveform = message.audioWaveform,
                                transcription = message.audioTranscription,
                                isPlaying = audioPlayerState.playingMessageId == message.id && audioPlayerState.isPlaying,
                                progress = if (audioPlayerState.playingMessageId == message.id) audioPlayerState.progress else 0f,
                                speed = audioPlayerState.speed,
                                isTranscriptionExpanded = audioPlayerState.isTranscriptionExpanded,
                                onPlayPause = { onPlayAudio(message.id) },
                                onSeek = onSeekAudio,
                                onToggleSpeed = onToggleAudioSpeed,
                                onToggleTranscription = onToggleAudioTranscription,
                                isFromMe = message.isFromMe
                            )
                        }

                        MessageType.IMAGE, MessageType.VIDEO -> {
                            if (message.isViewOnce) {
                                // View Once Card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, WhatsAppGreen, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "1",
                                            fontWeight = FontWeight.Bold,
                                            color = WhatsAppGreen,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (message.isViewOnceOpened) "Foto aberta" else "Foto",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (message.isViewOnceOpened) "Visualizada" else "Toque para abrir uma vez",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Column {
                                    Box {
                                        AsyncImage(
                                            model = message.mediaUrl,
                                            contentDescription = message.mediaCaption ?: "Mídia",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        if (message.isHd) {
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .align(Alignment.TopStart)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Hd,
                                                        contentDescription = "HD",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = " HD",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (!message.mediaCaption.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = parseWhatsAppFormatting(message.mediaCaption),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        MessageType.POLL -> {
                            if (message.pollData != null) {
                                PollCard(
                                    messageId = message.id,
                                    pollData = message.pollData,
                                    onVoteOption = { optionId -> onVotePoll(message.id, optionId) }
                                )
                            }
                        }

                        MessageType.DOCUMENT -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF8C52FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = "Documento",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = message.mediaFileName ?: "Documento.pdf",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = message.mediaFileSize ?: "1.4 MB • PDF",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        MessageType.LOCATION -> {
                            Column(modifier = Modifier.padding(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Localização",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Localização em tempo real",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.locationAddress ?: "Av. Paulista, 1000 - Bela Vista, São Paulo",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        MessageType.CONTACT -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(WhatsAppGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Contato",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = message.contactName ?: "Contato",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = message.contactPhone ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        else -> {}
                    }

                    // Footer with Time, Edited tag, Ephemeral timer, Status checkmarks
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp, start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isEdited) {
                            Text(
                                text = "editada ",
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (message.ephemeralDurationHours > 0) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Temporária",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(11.dp)
                                    .padding(end = 2.dp)
                            )
                        }

                        Text(
                            text = timeFormat.format(Date(message.timestamp)),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (message.isFromMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            when (message.status) {
                                MessageStatus.READ -> {
                                    Icon(
                                        Icons.Default.DoneAll,
                                        contentDescription = "Lida",
                                        tint = CheckmarkBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                MessageStatus.DELIVERED -> {
                                    Icon(
                                        Icons.Default.DoneAll,
                                        contentDescription = "Entregue",
                                        tint = CheckmarkGrey,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                MessageStatus.SENT -> {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = "Enviada",
                                        tint = CheckmarkGrey,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                MessageStatus.SENDING -> {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = "Enviando",
                                        tint = CheckmarkGrey,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reactions pill on bottom edge of bubble
            if (message.reactions.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        message.reactions.forEach { reaction ->
                            Text(
                                text = "${reaction.emoji} ${if (reaction.count > 1) reaction.count else ""}".trim(),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }

            // Context Menu on Long Click
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                // Quick emoji reaction bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val emojis = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 22.sp,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        showContextMenu = false
                                        onAddReaction(message.id, emoji)
                                    }
                                )
                                .padding(2.dp)
                        )
                    }
                }

                DropdownMenuItem(
                    text = { Text("Responder") },
                    leadingIcon = { Icon(Icons.Outlined.Reply, contentDescription = null) },
                    onClick = {
                        showContextMenu = false
                        onReplyToMessage(message)
                    }
                )

                if (message.isFromMe && message.type == MessageType.TEXT) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = {
                            showContextMenu = false
                            onEditMessage(message)
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text("Copiar texto") },
                    leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                    onClick = { showContextMenu = false }
                )
            }
        }
    }
}

/**
 * WhatsApp Markdown formatting parser:
 * - *bold text*
 * - _italic text_
 * - ~strikethrough text~
 * - `monospaced code`
 */
fun parseWhatsAppFormatting(text: String) = buildAnnotatedString {
    var i = 0
    val length = text.length

    while (i < length) {
        val char = text[i]

        when (char) {
            '*' -> {
                val nextStar = text.indexOf('*', i + 1)
                if (nextStar != -1) {
                    val boldContent = text.substring(i + 1, nextStar)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(boldContent)
                    }
                    i = nextStar + 1
                    continue
                }
            }
            '_' -> {
                val nextUnderscore = text.indexOf('_', i + 1)
                if (nextUnderscore != -1) {
                    val italicContent = text.substring(i + 1, nextUnderscore)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(italicContent)
                    }
                    i = nextUnderscore + 1
                    continue
                }
            }
            '~' -> {
                val nextTilde = text.indexOf('~', i + 1)
                if (nextTilde != -1) {
                    val strikeContent = text.substring(i + 1, nextTilde)
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(strikeContent)
                    }
                    i = nextTilde + 1
                    continue
                }
            }
            '`' -> {
                val nextBacktick = text.indexOf('`', i + 1)
                if (nextBacktick != -1) {
                    val codeContent = text.substring(i + 1, nextBacktick)
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color.Black.copy(alpha = 0.08f)
                        )
                    ) {
                        append(codeContent)
                    }
                    i = nextBacktick + 1
                    continue
                }
            }
        }

        append(char)
        i++
    }
}
