package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.StopScreenShare
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ActiveCallState
import com.example.model.CallType
import com.example.ui.theme.WhatsAppGreen

@Composable
fun ActiveCallScreen(
    callState: ActiveCallState,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleScreenShare: () -> Unit
) {
    BackHandler { onEndCall() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1A20))
            .testTag("active_call_screen")
    ) {
        // Video background simulation if VIDEO call and camera is not off
        if (callState.callType == CallType.VIDEO && !callState.isCameraOff) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1000",
                contentDescription = "Fluxo de Vídeo WebRTC",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Dark gradient overlay for readable text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
        }

        // Header: Security Badge + Contact Info + Call Timer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // End-to-end encryption pill
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Criptografia",
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Criptografia de ponta a ponta",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = callState.contactName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            val minutes = callState.durationSeconds / 60
            val seconds = callState.durationSeconds % 60
            val durationText = String.format("%02d:%02d", minutes, seconds)

            Text(
                text = durationText,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            // Screen sharing notification pill
            if (callState.isScreenSharing) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = WhatsAppGreen.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenShare,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Você está compartilhando sua tela",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Central Avatar (for Audio Call or Camera Off)
        if (callState.callType == CallType.AUDIO || callState.isCameraOff) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .border(3.dp, WhatsAppGreen, CircleShape)
            ) {
                AsyncImage(
                    model = callState.contactAvatar ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                    contentDescription = callState.contactName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // WebRTC Bottom Controls Bar
        Surface(
            color = Color(0xFF1B272E).copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speaker toggle
                    CallControlButton(
                        icon = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        label = "Alto-falante",
                        isActive = callState.isSpeakerOn,
                        onClick = onToggleSpeaker
                    )

                    // Video toggle
                    CallControlButton(
                        icon = if (callState.isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        label = "Vídeo",
                        isActive = !callState.isCameraOff,
                        onClick = onToggleCamera
                    )

                    // Screen share toggle
                    CallControlButton(
                        icon = if (callState.isScreenSharing) Icons.Default.StopScreenShare else Icons.Default.ScreenShare,
                        label = "Compartilhar",
                        isActive = callState.isScreenSharing,
                        onClick = onToggleScreenShare
                    )

                    // Mute mic toggle
                    CallControlButton(
                        icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = "Microfone",
                        isActive = !callState.isMuted,
                        onClick = onToggleMute
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Big Red End Call Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .clickable { onEndCall() }
                        .testTag("end_call_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Desligar chamada",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
