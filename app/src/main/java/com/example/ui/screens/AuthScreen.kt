package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.WhatsAppGreen

enum class AuthStep {
    WELCOME,
    PHONE_INPUT,
    OTP_VERIFY,
    PROFILE_SETUP
}

@Composable
fun AuthScreen(
    onCompleteAuth: (name: String, phone: String, username: String, avatarUrl: String?) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    var step by remember { mutableStateOf(AuthStep.WELCOME) }
    var phone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var avatarUrl by remember {
        mutableStateOf<String?>("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "auth_steps"
        ) { currentStep ->
            when (currentStep) {
                AuthStep.WELCOME -> {
                    WelcomeStep(
                        onContinue = { step = AuthStep.PHONE_INPUT }
                    )
                }

                AuthStep.PHONE_INPUT -> {
                    PhoneInputStep(
                        phone = phone,
                        onPhoneChange = { phone = it },
                        onNext = {
                            if (phone.isNotBlank()) {
                                step = AuthStep.OTP_VERIFY
                            }
                        }
                    )
                }

                AuthStep.OTP_VERIFY -> {
                    OtpVerifyStep(
                        phone = phone,
                        otpCode = otpCode,
                        onOtpChange = { otpCode = it },
                        onVerifySuccess = { step = AuthStep.PROFILE_SETUP }
                    )
                }

                AuthStep.PROFILE_SETUP -> {
                    ProfileSetupStep(
                        name = name,
                        onNameChange = { name = it },
                        username = username,
                        onUsernameChange = { username = it },
                        avatarUrl = avatarUrl,
                        onComplete = {
                            if (name.isNotBlank() && username.isNotBlank()) {
                                onCompleteAuth(name, phone, username, avatarUrl)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Bem-vindo ao Verum",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = WhatsAppGreen,
                    modifier = Modifier.size(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Mensagens privadas, chamadas de áudio e vídeo em alta definição com criptografia de ponta a ponta.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Toque em \"Concordar e continuar\" para aceitar os Termos de Serviço e Política de Privacidade.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("welcome_continue_button")
            ) {
                Text(
                    text = "CONCORDAR E CONTINUAR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PhoneInputStep(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Insira seu número de telefone",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "O Verum precisará confirmar seu número de telefone por SMS para ativar sua conta.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Country Selector (Brasil)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Brasil", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("+55", color = WhatsAppGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                placeholder = { Text("(11) 98765-4321") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = WhatsAppGreen)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhatsAppGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_input_field")
            )
        }

        Button(
            onClick = onNext,
            enabled = phone.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("phone_next_button")
        ) {
            Text("AVANÇAR", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OtpVerifyStep(
    phone: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    onVerifySuccess: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Confirmando seu número",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Insira o código de 6 dígitos enviado por SMS para\n$phone",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input
            OutlinedTextField(
                value = otpCode,
                onValueChange = {
                    if (it.length <= 6) {
                        onOtpChange(it)
                        if (it.length == 6) {
                            onVerifySuccess()
                        }
                    }
                },
                placeholder = { Text("6 1 9 - 4 8 2", textAlign = TextAlign.Center) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhatsAppGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("otp_code_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Test helper shortcut button
            TextButton(
                onClick = {
                    onOtpChange("619482")
                    onVerifySuccess()
                }
            ) {
                Text(
                    text = "Preencher código de teste (619-482)",
                    fontSize = 13.sp,
                    color = WhatsAppGreen
                )
            }
        }

        Button(
            onClick = onVerifySuccess,
            enabled = otpCode.length >= 4,
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("verify_otp_button")
        ) {
            Text("VERIFICAR CÓDIGO", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileSetupStep(
    name: String,
    onNameChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    avatarUrl: String?,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Dados do perfil",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Defina seu nome e crie seu @username exclusivo.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Avatar with camera icon
            Box(modifier = Modifier.size(110.dp)) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WhatsAppGreen)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Alterar foto",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Seu Nome") },
                placeholder = { Text("ex: Carlos Silva") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = WhatsAppGreen)
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_name_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Nome de Usuário (@)") },
                placeholder = { Text("ex: carlos.dev") },
                leadingIcon = {
                    Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = WhatsAppGreen)
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_username_field")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Seus contatos poderão iniciar conversas com você usando @$username sem expor seu telefone.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = onComplete,
            enabled = name.isNotBlank() && username.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("finish_registration_button")
        ) {
            Text("CONCLUIR E ENTRAR", fontWeight = FontWeight.Bold)
        }
    }
}
