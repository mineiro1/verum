package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.Message
import com.example.ui.theme.WhatsAppGreen
import kotlinx.coroutines.delay

@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSendAudioNote: (durationSec: Int, waveform: List<Float>) -> Unit,
    replyingToMessage: Message?,
    onCancelReply: () -> Unit,
    editingMessage: Message?,
    onCancelEdit: () -> Unit,
    onOpenAttachments: () -> Unit,
    onQuickCameraClick: () -> Unit
) {
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }

    // Audio recording timer simulation
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingDuration = 0
            while (isRecordingAudio) {
                delay(1000)
                recordingDuration++
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .testTag("chat_input_bar")
    ) {
        // Reply Preview Banner
        AnimatedVisibility(visible = replyingToMessage != null) {
            if (replyingToMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(32.dp)
                                .background(WhatsAppGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Respondendo a ${replyingToMessage.senderName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppGreen
                            )
                            Text(
                                text = replyingToMessage.text.ifBlank { "Mídia" },
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onCancelReply) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar resposta")
                        }
                    }
                }
            }
        }

        // Edit Preview Banner
        AnimatedVisibility(visible = editingMessage != null) {
            if (editingMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Editando mensagem",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppGreen
                            )
                            Text(
                                text = editingMessage.text,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onCancelEdit) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar edição")
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRecordingAudio) {
                // Audio Recording Live Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(26.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pulsing Red Recording Dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("%02d:%02d", recordingDuration / 60, recordingDuration % 60),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Simulated audio waveform live bars
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dummyHeights = listOf(8, 16, 24, 12, 28, 18, 10, 22, 14, 20)
                            dummyHeights.forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(h.dp)
                                        .background(WhatsAppGreen)
                                )
                            }
                        }

                        // Cancel / Trash button
                        IconButton(onClick = { isRecordingAudio = false }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Cancelar gravação",
                                tint = Color.Red
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Audio Note button
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(WhatsAppGreen)
                        .clickable {
                            val generatedWaveform = listOf(
                                0.2f, 0.6f, 0.8f, 0.4f, 0.9f, 0.7f, 0.3f, 0.85f,
                                0.5f, 0.95f, 0.6f, 0.3f, 0.7f, 0.5f, 0.8f, 0.4f
                            )
                            onSendAudioNote(maxOf(1, recordingDuration), generatedWaveform)
                            isRecordingAudio = false
                        }
                        .testTag("send_audio_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar Áudio",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

            } else {
                // Standard Text and Attachment Input Box
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(26.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji icon
                        IconButton(
                            onClick = { /* Emoji picker toggle */ },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = "Emojis",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Text Field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = onInputTextChange,
                            placeholder = { Text("Mensagem") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field"),
                            maxLines = 4
                        )

                        // Attachments paperclip
                        IconButton(
                            onClick = onOpenAttachments,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "Anexar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Camera icon (when no text)
                        if (inputText.isBlank()) {
                            IconButton(
                                onClick = onQuickCameraClick,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Câmera",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Action Button: Send message or Voice note
                val isTextEmpty = inputText.isBlank()

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(WhatsAppGreen)
                        .clickable {
                            if (!isTextEmpty) {
                                onSendMessage(inputText)
                            } else {
                                isRecordingAudio = true
                            }
                        }
                        .testTag(if (!isTextEmpty) "send_message_button" else "voice_record_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (!isTextEmpty) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = if (!isTextEmpty) "Enviar Mensagem" else "Gravar Áudio",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
