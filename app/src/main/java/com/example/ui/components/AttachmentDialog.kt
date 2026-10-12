package com.example.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MessageType
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onSelectOption: (AttachmentOption) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .testTag("attachment_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentItem(
                    label = "Documento",
                    icon = Icons.Default.Description,
                    bgColor = Color(0xFF7F66FF),
                    onClick = { onSelectOption(AttachmentOption.DOCUMENT) }
                )
                AttachmentItem(
                    label = "Câmera",
                    icon = Icons.Default.CameraAlt,
                    bgColor = Color(0xFFD3396D),
                    onClick = { onSelectOption(AttachmentOption.CAMERA) }
                )
                AttachmentItem(
                    label = "Galeria",
                    icon = Icons.Default.Image,
                    bgColor = Color(0xFFAC44CF),
                    onClick = { onSelectOption(AttachmentOption.GALLERY) }
                )
                AttachmentItem(
                    label = "Áudio",
                    icon = Icons.Default.AudioFile,
                    bgColor = Color(0xFFE37B30),
                    onClick = { onSelectOption(AttachmentOption.AUDIO) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentItem(
                    label = "Localização",
                    icon = Icons.Default.LocationOn,
                    bgColor = Color(0xFF1EA264),
                    onClick = { onSelectOption(AttachmentOption.LOCATION) }
                )
                AttachmentItem(
                    label = "Contato",
                    icon = Icons.Default.Person,
                    bgColor = Color(0xFF009DE2),
                    onClick = { onSelectOption(AttachmentOption.CONTACT) }
                )
                AttachmentItem(
                    label = "Enquete",
                    icon = Icons.Default.Poll,
                    bgColor = Color(0xFF00BFA5),
                    onClick = { onSelectOption(AttachmentOption.POLL) }
                )
                // Spacer for symmetry
                Spacer(modifier = Modifier.size(56.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

enum class AttachmentOption {
    DOCUMENT,
    CAMERA,
    GALLERY,
    AUDIO,
    LOCATION,
    CONTACT,
    POLL
}

@Composable
fun AttachmentItem(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("attachment_item_${label.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onCreatePoll: (question: String, options: List<String>, allowMultiple: Boolean) -> Unit
) {
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("Opção 1", "Opção 2") }
    var allowMultiple by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar Enquete", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Faça uma pergunta") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Opções",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WhatsAppGreen
                )

                Spacer(modifier = Modifier.height(4.dp))

                options.forEachIndexed { index, opt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = opt,
                            onValueChange = { options[index] = it },
                            placeholder = { Text("Adicionar opção") },
                            modifier = Modifier.weight(1f)
                        )
                        if (options.size > 2) {
                            IconButton(onClick = { options.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remover")
                            }
                        }
                    }
                }

                if (options.size < 6) {
                    TextButton(
                        onClick = { options.add("Opção ${options.size + 1}") }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = WhatsAppGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar mais opção", color = WhatsAppGreen)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Permitir várias respostas", fontSize = 13.sp)
                    Switch(
                        checked = allowMultiple,
                        onCheckedChange = { allowMultiple = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = WhatsAppGreen)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank()) {
                        onCreatePoll(question, options.toList(), allowMultiple)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
                Text("Criar Enquete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun LocationDialog(
    onDismiss: () -> Unit,
    onSendLocation: (lat: Double, lng: Double, address: String) -> Unit
) {
    var address by remember { mutableStateOf("Av. Paulista, 1000 - Bela Vista, São Paulo, SP") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compartilhar Localização", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Localização em tempo real e estática fixada via GPS:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Endereço / Ponto de Referência") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSendLocation(-23.561414, -46.6558819, address)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
                Text("Enviar Localização")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ContactDialog(
    onDismiss: () -> Unit,
    onSendContact: (name: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("Beatriz Albuquerque") }
    var phone by remember { mutableStateOf("+55 (11) 97654-3210") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compartilhar Contato", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do contato") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone com DDD") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSendContact(name, phone)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
                Text("Enviar Contato")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
