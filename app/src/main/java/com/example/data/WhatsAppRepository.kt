package com.example.data

import com.example.model.Account
import com.example.model.ActiveCallState
import com.example.model.AudioPlayerState
import com.example.model.CallLog
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.model.Channel
import com.example.model.ChannelPost
import com.example.model.Chat
import com.example.model.Community
import com.example.model.CommunityTopicGroup
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.model.PollData
import com.example.model.PollOption
import com.example.model.Reaction
import com.example.model.StatusStory
import com.example.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WhatsAppRepository {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val supabaseService = SupabaseService()

    // Active User Accounts
    private val _accounts = MutableStateFlow(
        listOf(
            Account(
                id = "acc_primary",
                name = "Carlos Silva",
                phone = "+55 (11) 98765-4321",
                username = "@carlos.silva",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                isActive = true
            ),
            Account(
                id = "acc_business",
                name = "Carlos Silva (Trabalho)",
                phone = "+55 (11) 91234-5678",
                username = "@carlos.biz",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                isActive = false
            )
        )
    )
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user_me",
            name = "Carlos Silva",
            username = "carlos.silva",
            phone = "+55 (11) 98765-4321",
            statusText = "Olá! Estou usando o Verum.",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            publicKeyFingerprint = "7392 4810 5928 1048 3920 4819 0284 8192 4819 4028 1948 2910"
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Chats List
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    // Messages grouped by Chat ID
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Status Stories
    private val _stories = MutableStateFlow<List<StatusStory>>(emptyList())
    val stories: StateFlow<List<StatusStory>> = _stories.asStateFlow()

    // Channels
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _channelPosts = MutableStateFlow<Map<String, List<ChannelPost>>>(emptyMap())
    val channelPosts: StateFlow<Map<String, List<ChannelPost>>> = _channelPosts.asStateFlow()

    // Communities
    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    // Call Logs
    private val _calls = MutableStateFlow<List<CallLog>>(emptyList())
    val calls: StateFlow<List<CallLog>> = _calls.asStateFlow()

    // Chat Lock unlocked state
    private val _isChatLockUnlocked = MutableStateFlow(false)
    val isChatLockUnlocked: StateFlow<Boolean> = _isChatLockUnlocked.asStateFlow()

    private val _audioPlayerState = MutableStateFlow(AudioPlayerState())
    val audioPlayerState: StateFlow<AudioPlayerState> = _audioPlayerState.asStateFlow()

    private val _activeCall = MutableStateFlow(ActiveCallState())
    val activeCall: StateFlow<ActiveCallState> = _activeCall.asStateFlow()

    init {
        seedInitialData()
        startAudioTicker()
        startCallTimerTicker()
        if (supabaseService.isConfigured) {
            supabaseService.startRealtimeListener { incomingMsg ->
                if (incomingMsg.senderId != "user_me") {
                    addMessageToChat(incomingMsg.chatId, incomingMsg)
                }
            }
        }
    }

    private fun seedInitialData() {
        val metaAiChat = Chat(
            id = "chat_meta_ai",
            name = "Meta AI",
            username = "@meta.ai",
            avatarUrl = null,
            isGroup = false,
            isMetaAi = true,
            unreadCount = 0,
            isPinned = true,
            lastMessage = "Posso criar imagens, responder perguntas ou ajudar com código!",
            lastMessageTime = "10:14",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 15,
            lastMessageStatus = MessageStatus.READ,
            aboutStatus = "Assistente de Inteligência Artificial Oficial"
        )

        val chat1 = Chat(
            id = "chat_1",
            name = "Mariana Costa",
            username = "@marianacosta",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
            unreadCount = 2,
            isPinned = true,
            isFavorite = true,
            lastMessage = "Vou te mandar as fotos em HD da viagem agora!",
            lastMessageTime = "10:28",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 2,
            lastMessageStatus = MessageStatus.READ,
            lastMessageIsFromMe = false,
            typingUser = null,
            aboutStatus = "A vida é feita de momentos ✨"
        )

        val chat2 = Chat(
            id = "chat_2",
            name = "Desenvolvimento Android 🚀",
            avatarUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=150",
            isGroup = true,
            membersCount = 48,
            unreadCount = 5,
            isPinned = true,
            lastMessage = "Rodrigo: A nova versão do Kotlin 2.2 já está disponível para testes.",
            lastMessageTime = "09:50",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 45,
            lastMessageStatus = MessageStatus.READ,
            aboutStatus = "Grupo oficial de devs Mobile"
        )

        val chat3 = Chat(
            id = "chat_3",
            name = "Lucas Mendes",
            username = "@lucasmendes",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
            unreadCount = 0,
            lastMessage = "Mensagem de voz (0:24)",
            lastMessageTime = "Ontem",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 20,
            lastMessageStatus = MessageStatus.READ,
            lastMessageIsFromMe = true,
            aboutStatus = "Ocupado no momento."
        )

        val chat4 = Chat(
            id = "chat_4",
            name = "Família Silva 🏡",
            avatarUrl = "https://images.unsplash.com/photo-1511895426328-dc8714191300?w=150",
            isGroup = true,
            membersCount = 12,
            unreadCount = 0,
            lastMessage = "Tia Lúcia: Quem vai levar a sobremesa no domingo?",
            lastMessageTime = "Ontem",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
            lastMessageStatus = MessageStatus.READ,
            aboutStatus = "Família unida"
        )

        val chatLocked = Chat(
            id = "chat_locked_1",
            name = "Projetos Secretos 🔒",
            avatarUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=150",
            isGroup = false,
            isLocked = true,
            unreadCount = 0,
            lastMessage = "Os dados confidenciais foram arquivados com sucesso.",
            lastMessageTime = "08:12",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 30,
            lastMessageStatus = MessageStatus.READ,
            aboutStatus = "Sigiloso"
        )

        _chats.value = listOf(metaAiChat, chat1, chat2, chat3, chat4, chatLocked)

        // Seed Messages for Mariana Costa (chat_1)
        val initialMarianaMessages = listOf(
            Message(
                id = "m_1",
                chatId = "chat_1",
                senderId = "chat_1",
                senderName = "Mariana Costa",
                text = "Oi Carlos! Tudo bem? Você conseguiu ver aquele documento do projeto?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60,
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                isFromMe = false
            ),
            Message(
                id = "m_2",
                chatId = "chat_1",
                senderId = "user_me",
                senderName = "Carlos Silva",
                text = "Oi Mari! Sim, dei uma olhada. Ficou excelente! *Destaque para os prazos* que ficaram bem realistas.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 50,
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                isFromMe = true,
                reactions = listOf(Reaction("👍", 1, false), Reaction("❤️", 1, true))
            ),
            Message(
                id = "m_3",
                chatId = "chat_1",
                senderId = "user_me",
                senderName = "Carlos Silva",
                type = MessageType.AUDIO,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 35,
                status = MessageStatus.READ,
                isFromMe = true,
                audioDurationSeconds = 18,
                audioWaveform = listOf(0.2f, 0.5f, 0.8f, 0.4f, 0.9f, 0.7f, 0.3f, 0.6f, 0.85f, 0.45f, 0.95f, 0.6f, 0.3f, 0.5f, 0.7f, 0.4f),
                audioTranscription = "Deixei anotado também os pontos da reunião com a equipe de UI. A gente alinha tudo amanhã às 14h."
            ),
            Message(
                id = "m_4",
                chatId = "chat_1",
                senderId = "chat_1",
                senderName = "Mariana Costa",
                text = "Perfeito! Também criei uma enquete com o time sobre o formato:",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 20,
                type = MessageType.POLL,
                status = MessageStatus.READ,
                isFromMe = false,
                pollData = PollData(
                    question = "Qual melhor horário para nossa daily?",
                    options = listOf(
                        PollOption("opt_1", "09:30 da manhã", votes = 3, voterIds = listOf("user_1", "user_2", "user_me")),
                        PollOption("opt_2", "14:00 da tarde", votes = 1, voterIds = listOf("user_3")),
                        PollOption("opt_3", "17:00 encerramento", votes = 0, voterIds = emptyList())
                    ),
                    allowMultipleAnswers = false
                )
            ),
            Message(
                id = "m_5",
                chatId = "chat_1",
                senderId = "chat_1",
                senderName = "Mariana Costa",
                text = "Foto de visualização única do rascunho do protótipo:",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 10,
                type = MessageType.IMAGE,
                status = MessageStatus.READ,
                isFromMe = false,
                isViewOnce = true,
                isViewOnceOpened = false,
                mediaUrl = "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800",
                mediaCaption = "Wireframe conceitual"
            ),
            Message(
                id = "m_6",
                chatId = "chat_1",
                senderId = "chat_1",
                senderName = "Mariana Costa",
                text = "Vou te mandar as fotos em HD da viagem agora!",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 2,
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                isFromMe = false
            )
        )

        // Seed Messages for Meta AI (chat_meta_ai)
        val initialMetaAiMessages = listOf(
            Message(
                id = "meta_1",
                chatId = "chat_meta_ai",
                senderId = "chat_meta_ai",
                senderName = "Meta AI",
                text = "Olá, Carlos! Eu sou a Meta AI. Você pode me fazer perguntas de qualquer assunto ou digitar `/imagine` seguido de uma descrição para gerar imagens incríveis.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                isFromMe = false
            ),
            Message(
                id = "meta_2",
                chatId = "chat_meta_ai",
                senderId = "user_me",
                senderName = "Carlos Silva",
                text = "/imagine um astronauta tomando café em marte com vista para a Terra ao fundo em estilo aquarela",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 16,
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                isFromMe = true
            ),
            Message(
                id = "meta_3",
                chatId = "chat_meta_ai",
                senderId = "chat_meta_ai",
                senderName = "Meta AI",
                text = "Aqui está a imagem gerada para o seu pedido:",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 15,
                type = MessageType.IMAGE,
                status = MessageStatus.READ,
                isFromMe = false,
                mediaUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800",
                mediaCaption = "Aquarela: Astronauta contemplando a Terra a partir de Marte",
                reactions = listOf(Reaction("✨", 1, true))
            )
        )

        _messages.value = mapOf(
            "chat_1" to initialMarianaMessages,
            "chat_meta_ai" to initialMetaAiMessages
        )

        // Seed Stories (Status)
        _stories.value = listOf(
            StatusStory(
                id = "story_me",
                userId = "user_me",
                userName = "Meu status",
                userAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                caption = "Dia produtivo programando em Jetpack Compose!",
                imageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                isMine = true,
                viewersCount = 14
            ),
            StatusStory(
                id = "story_1",
                userId = "chat_1",
                userName = "Mariana Costa",
                userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                caption = "Pôr do sol incrível na praia hoje 🌅",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                isViewed = false
            ),
            StatusStory(
                id = "story_2",
                userId = "chat_3",
                userName = "Lucas Mendes",
                userAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                textStatus = "Foco total na entrega do sprint! Não me procurem até as 18h ☕💻",
                backgroundColorHex = 0xFF5C6BC0,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 240,
                isViewed = true
            )
        )

        // Seed Channels
        _channels.value = listOf(
            Channel(
                id = "ch_whatsapp",
                name = "WhatsApp",
                handle = "@whatsapp",
                avatarUrl = "https://images.unsplash.com/photo-1614680376593-902f749f7ffc?w=150",
                description = "Notícias oficiais, lançamentos de novos recursos e dicas de segurança do WhatsApp.",
                followersCount = "158M seguidores",
                isVerified = true,
                isFollowing = true,
                unreadUpdatesCount = 1,
                lastPostTime = "Hoje 08:30"
            ),
            Channel(
                id = "ch_tech",
                name = "Tech Brasil News",
                handle = "@technewsbr",
                avatarUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=150",
                description = "As principais novidades em tecnologia, startups, inteligência artificial e gadgets.",
                followersCount = "1,4M seguidores",
                isVerified = true,
                isFollowing = true,
                unreadUpdatesCount = 3,
                lastPostTime = "Hoje 10:15"
            ),
            Channel(
                id = "ch_meta",
                name = "Meta AI Hub",
                handle = "@metaai",
                avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                description = "Descubra prompts criativos e inovações em Inteligência Artificial generativa.",
                followersCount = "12M seguidores",
                isVerified = true,
                isFollowing = false,
                unreadUpdatesCount = 0,
                lastPostTime = "Ontem"
            )
        )

        _channelPosts.value = mapOf(
            "ch_whatsapp" to listOf(
                ChannelPost(
                    id = "post_1",
                    channelId = "ch_whatsapp",
                    text = "Apresentando as mensagens de visualização única com proteção aprimorada e suporte a envio em Alta Definição (HD)! Atualize seu app agora.",
                    imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 180,
                    viewsCount = "1.8M",
                    reactions = listOf(Reaction("🔥", 1240, true), Reaction("❤️", 890, false), Reaction("👏", 450, false))
                )
            )
        )

        // Seed Communities
        _communities.value = listOf(
            Community(
                id = "comm_1",
                name = "Comunidade Tech & Mobile BR",
                description = "Espaço unificado para desenvolvedores mobile trocarem experiências, vagas e códigos.",
                avatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150",
                announcementGroupId = "ann_1",
                announcementLastMessage = "Avisos: Inscrições abertas para o Hackathon Android 2026!",
                topicGroups = listOf(
                    CommunityTopicGroup("tg_1", "Kotlin & Jetpack Compose", "Qual a melhor prática para MVI?", "10:12", 2),
                    CommunityTopicGroup("tg_2", "Oportunidades & Vagas Dev", "Vaga Sênior Android Remoto", "Ontem", 0),
                    CommunityTopicGroup("tg_3", "Dúvidas & Arquitetura", "Clean Arch vs Modularização", "2 dias atrás", 0)
                ),
                membersCount = 384
            )
        )

        // Seed Calls
        _calls.value = listOf(
            CallLog(
                id = "call_1",
                contactName = "Mariana Costa",
                contactAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                type = CallType.VIDEO,
                status = CallStatus.INCOMING,
                timestamp = "Hoje, 09:15",
                durationSeconds = 340
            ),
            CallLog(
                id = "call_2",
                contactName = "Lucas Mendes",
                contactAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                type = CallType.AUDIO,
                status = CallStatus.MISSED,
                timestamp = "Hoje, 08:04",
                durationSeconds = 0
            ),
            CallLog(
                id = "call_3",
                contactName = "Desenvolvimento Android 🚀",
                contactAvatarUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=150",
                type = CallType.AUDIO,
                status = CallStatus.OUTGOING,
                timestamp = "Ontem, 18:22",
                durationSeconds = 1250
            )
        )
    }

    // Audio Playback Simulation Ticker
    private fun startAudioTicker() {
        scope.launch {
            while (true) {
                delay(100)
                val current = _audioPlayerState.value
                if (current.isPlaying && current.playingMessageId != null) {
                    val step = (0.015f * current.speed)
                    val newProgress = current.progress + step
                    if (newProgress >= 1f) {
                        _audioPlayerState.value = current.copy(isPlaying = false, progress = 0f)
                    } else {
                        _audioPlayerState.value = current.copy(progress = newProgress)
                    }
                }
            }
        }
    }

    // Active Call Ticker
    private fun startCallTimerTicker() {
        scope.launch {
            while (true) {
                delay(1000)
                val call = _activeCall.value
                if (call.isInCall) {
                    _activeCall.value = call.copy(durationSeconds = call.durationSeconds + 1)
                }
            }
        }
    }

    // Chat Lock Operations
    fun unlockChatLock() {
        _isChatLockUnlocked.value = true
    }

    fun lockChatLock() {
        _isChatLockUnlocked.value = false
    }

    fun toggleChatLock(chatId: String) {
        _chats.update { list ->
            list.map { chat ->
                if (chat.id == chatId) chat.copy(isLocked = !chat.isLocked) else chat
            }
        }
    }

    fun togglePinChat(chatId: String) {
        _chats.update { list ->
            val pinnedCount = list.count { it.isPinned }
            list.map { chat ->
                if (chat.id == chatId) {
                    if (!chat.isPinned && pinnedCount >= 3) {
                        // Max 3 pinned chats
                        chat
                    } else {
                        chat.copy(isPinned = !chat.isPinned)
                    }
                } else chat
            }
        }
    }

    fun setEphemeralMessages(chatId: String, durationHours: Int) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(ephemeralSettingHours = durationHours) else it }
        }
        val durationLabel = when (durationHours) {
            24 -> "24 horas"
            168 -> "7 dias"
            2160 -> "90 dias"
            else -> "desativadas"
        }
        sendSystemMessage(chatId, "As mensagens temporárias foram definidas para $durationLabel.")
    }

    private fun sendSystemMessage(chatId: String, text: String) {
        val sysMsg = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "system",
            senderName = "Sistema",
            text = text,
            timestamp = System.currentTimeMillis(),
            type = MessageType.SYSTEM,
            status = MessageStatus.READ,
            isFromMe = false
        )
        _messages.update { currentMap ->
            val currentList = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (currentList + sysMsg))
        }
    }

    // Sending Messages
    fun sendTextMessage(chatId: String, text: String, replyToMessage: Message? = null) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return

        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "user_me",
            senderName = _currentUser.value.name,
            text = cleanText,
            timestamp = System.currentTimeMillis(),
            type = MessageType.TEXT,
            status = MessageStatus.SENT,
            isFromMe = true,
            replyToMessageId = replyToMessage?.id,
            replyToText = replyToMessage?.text ?: replyToMessage?.mediaCaption,
            replyToSender = replyToMessage?.senderName
        )

        addMessageToChat(chatId, newMessage)

        // Handle Meta AI responses
        if (chatId == "chat_meta_ai") {
            handleMetaAiQuery(cleanText)
        } else {
            // Simulate delivery and read receipts
            simulateMessageDelivery(chatId, newMessage.id)
        }
    }

    fun sendAudioNote(chatId: String, durationSec: Int, waveform: List<Float>) {
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "user_me",
            senderName = _currentUser.value.name,
            type = MessageType.AUDIO,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            isFromMe = true,
            audioDurationSeconds = durationSec,
            audioWaveform = waveform,
            audioTranscription = "Transcrição automática do áudio gravado em alta definição."
        )
        addMessageToChat(chatId, newMessage)
        simulateMessageDelivery(chatId, newMessage.id)
    }

    fun sendMediaMessage(
        chatId: String,
        type: MessageType,
        url: String,
        caption: String? = null,
        fileName: String? = null,
        fileSize: String? = null,
        isViewOnce: Boolean = false,
        isHd: Boolean = true
    ) {
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "user_me",
            senderName = _currentUser.value.name,
            type = type,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            isFromMe = true,
            mediaUrl = url,
            mediaCaption = caption,
            mediaFileName = fileName,
            mediaFileSize = fileSize,
            isViewOnce = isViewOnce,
            isHd = isHd
        )
        addMessageToChat(chatId, newMessage)
        simulateMessageDelivery(chatId, newMessage.id)
    }

    fun sendLocationMessage(chatId: String, lat: Double, lng: Double, address: String) {
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "user_me",
            senderName = _currentUser.value.name,
            type = MessageType.LOCATION,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            isFromMe = true,
            locationLatitude = lat,
            locationLongitude = lng,
            locationAddress = address
        )
        addMessageToChat(chatId, newMessage)
        simulateMessageDelivery(chatId, newMessage.id)
    }

    fun sendContactMessage(chatId: String, contactName: String, contactPhone: String) {
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "user_me",
            senderName = _currentUser.value.name,
            type = MessageType.CONTACT,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            isFromMe = true,
            contactName = contactName,
            contactPhone = contactPhone
        )
        addMessageToChat(chatId, newMessage)
        simulateMessageDelivery(chatId, newMessage.id)
    }

    fun createPoll(chatId: String, question: String, options: List<String>, allowMultiple: Boolean) {
        val pollData = PollData(
            question = question,
            options = options.filter { it.isNotBlank() }.mapIndexed { index, opt ->
                PollOption(id = "opt_$index", text = opt, votes = 0, voterIds = emptyList())
            },
            allowMultipleAnswers = allowMultiple
        )
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "user_me",
            senderName = _currentUser.value.name,
            type = MessageType.POLL,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            isFromMe = true,
            pollData = pollData
        )
        addMessageToChat(chatId, newMessage)
        simulateMessageDelivery(chatId, newMessage.id)
    }

    fun votePoll(chatId: String, messageId: String, optionId: String) {
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: return@update currentMap
            val updatedList = list.map { msg ->
                if (msg.id == messageId && msg.pollData != null) {
                    val currentPoll = msg.pollData
                    val myId = "user_me"
                    val updatedOptions = currentPoll.options.map { opt ->
                        val alreadyVoted = opt.voterIds.contains(myId)
                        if (opt.id == optionId) {
                            if (alreadyVoted) {
                                opt.copy(votes = maxOf(0, opt.votes - 1), voterIds = opt.voterIds - myId)
                            } else {
                                opt.copy(votes = opt.votes + 1, voterIds = opt.voterIds + myId)
                            }
                        } else {
                            if (!currentPoll.allowMultipleAnswers && alreadyVoted) {
                                opt.copy(votes = maxOf(0, opt.votes - 1), voterIds = opt.voterIds - myId)
                            } else {
                                opt
                            }
                        }
                    }
                    msg.copy(pollData = currentPoll.copy(options = updatedOptions))
                } else msg
            }
            currentMap + (chatId to updatedList)
        }
    }

    fun addReaction(chatId: String, messageId: String, emoji: String) {
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: return@update currentMap
            val updatedList = list.map { msg ->
                if (msg.id == messageId) {
                    val existingReactionIndex = msg.reactions.indexOfFirst { it.emoji == emoji }
                    val newReactions = if (existingReactionIndex != -1) {
                        val existing = msg.reactions[existingReactionIndex]
                        if (existing.userReacted) {
                            msg.reactions.toMutableList().apply {
                                if (existing.count <= 1) removeAt(existingReactionIndex)
                                else this[existingReactionIndex] = existing.copy(count = existing.count - 1, userReacted = false)
                            }
                        } else {
                            msg.reactions.toMutableList().apply {
                                this[existingReactionIndex] = existing.copy(count = existing.count + 1, userReacted = true)
                            }
                        }
                    } else {
                        msg.reactions + Reaction(emoji = emoji, count = 1, userReacted = true)
                    }
                    msg.copy(reactions = newReactions)
                } else msg
            }
            currentMap + (chatId to updatedList)
        }
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: return@update currentMap
            val updatedList = list.map { msg ->
                if (msg.id == messageId && msg.isFromMe) {
                    msg.copy(text = newText, isEdited = true)
                } else msg
            }
            currentMap + (chatId to updatedList)
        }
        // Update last message in chat if this was the last message
        updateChatLastMessage(chatId, newText, System.currentTimeMillis())
    }

    fun openViewOnceMessage(chatId: String, messageId: String) {
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: return@update currentMap
            val updatedList = list.map { msg ->
                if (msg.id == messageId && msg.isViewOnce) {
                    msg.copy(isViewOnceOpened = true)
                } else msg
            }
            currentMap + (chatId to updatedList)
        }
    }

    // Audio Player Controls
    fun playAudio(messageId: String) {
        val current = _audioPlayerState.value
        if (current.playingMessageId == messageId) {
            _audioPlayerState.value = current.copy(isPlaying = !current.isPlaying)
        } else {
            _audioPlayerState.value = AudioPlayerState(
                playingMessageId = messageId,
                isPlaying = true,
                progress = 0f,
                speed = current.speed
            )
        }
    }

    fun seekAudio(progress: Float) {
        _audioPlayerState.update { it.copy(progress = progress.coerceIn(0f, 1f)) }
    }

    fun toggleAudioSpeed() {
        val nextSpeed = when (_audioPlayerState.value.speed) {
            1.0f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
        _audioPlayerState.update { it.copy(speed = nextSpeed) }
    }

    fun toggleAudioTranscription() {
        _audioPlayerState.update { it.copy(isTranscriptionExpanded = !it.isTranscriptionExpanded) }
    }

    // WebRTC Call Controls
    fun startCall(contactName: String, contactAvatar: String?, callType: CallType) {
        _activeCall.value = ActiveCallState(
            isInCall = true,
            contactName = contactName,
            contactAvatar = contactAvatar,
            callType = callType,
            isMuted = false,
            isCameraOff = false,
            isSpeakerOn = true,
            isScreenSharing = false,
            durationSeconds = 0
        )
    }

    fun toggleCallMute() {
        _activeCall.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleCallCamera() {
        _activeCall.update { it.copy(isCameraOff = !it.isCameraOff) }
    }

    fun toggleCallSpeaker() {
        _activeCall.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleCallScreenShare() {
        _activeCall.update { it.copy(isScreenSharing = !it.isScreenSharing) }
    }

    fun endCall() {
        val current = _activeCall.value
        if (current.isInCall) {
            // Log the call
            val newLog = CallLog(
                id = UUID.randomUUID().toString(),
                contactName = current.contactName,
                contactAvatarUrl = current.contactAvatar,
                type = current.callType,
                status = CallStatus.OUTGOING,
                timestamp = "Hoje, ${timeFormat.format(Date())}",
                durationSeconds = current.durationSeconds
            )
            _calls.update { listOf(newLog) + it }
            _activeCall.value = ActiveCallState()
        }
    }

    // Multi-Account Switcher
    fun switchAccount(accountId: String) {
        val target = _accounts.value.find { it.id == accountId } ?: return
        _accounts.update { list ->
            list.map { it.copy(isActive = it.id == accountId) }
        }
        _currentUser.value = _currentUser.value.copy(
            name = target.name,
            phone = target.phone,
            username = target.username.removePrefix("@"),
            avatarUrl = target.avatarUrl
        )
    }

    fun updateProfile(name: String, status: String, username: String) {
        _currentUser.update {
            it.copy(
                name = name,
                statusText = status,
                username = username.removePrefix("@")
            )
        }
    }

    // Channel interactions
    fun toggleFollowChannel(channelId: String) {
        _channels.update { list ->
            list.map { if (it.id == channelId) it.copy(isFollowing = !it.isFollowing) else it }
        }
    }

    fun addChannelReaction(channelId: String, postId: String, emoji: String) {
        _channelPosts.update { currentMap ->
            val posts = currentMap[channelId] ?: return@update currentMap
            val updatedPosts = posts.map { post ->
                if (post.id == postId) {
                    val existingIndex = post.reactions.indexOfFirst { it.emoji == emoji }
                    val newReactions = if (existingIndex != -1) {
                        val existing = post.reactions[existingIndex]
                        post.reactions.toMutableList().apply {
                            this[existingIndex] = existing.copy(
                                count = if (existing.userReacted) existing.count - 1 else existing.count + 1,
                                userReacted = !existing.userReacted
                            )
                        }
                    } else {
                        post.reactions + Reaction(emoji, 1, true)
                    }
                    post.copy(reactions = newReactions)
                } else post
            }
            currentMap + (channelId to updatedPosts)
        }
    }

    // Status Story creation
    fun postStatus(text: String?, imageUrl: String?, bgColor: Long = 0xFF00A884) {
        val newStory = StatusStory(
            id = UUID.randomUUID().toString(),
            userId = "user_me",
            userName = "Meu status",
            userAvatarUrl = _currentUser.value.avatarUrl,
            imageUrl = imageUrl,
            textStatus = text,
            backgroundColorHex = bgColor,
            timestamp = System.currentTimeMillis(),
            isMine = true,
            viewersCount = 0
        )
        _stories.update { listOf(newStory) + it }
    }

    // Helper: Add message to state & update chat preview
    private fun addMessageToChat(chatId: String, message: Message) {
        _messages.update { currentMap ->
            val currentList = currentMap[chatId] ?: emptyList()
            currentMap + (chatId to (currentList + message))
        }

        if (message.isFromMe && message.type != MessageType.SYSTEM) {
            supabaseService.sendMessage(message)
        }

        val preview = when (message.type) {
            MessageType.TEXT -> message.text
            MessageType.AUDIO -> "🎤 Mensagem de voz (${message.audioDurationSeconds}s)"
            MessageType.IMAGE -> "📷 Foto"
            MessageType.VIDEO -> "🎥 Vídeo"
            MessageType.DOCUMENT -> "📄 Documento"
            MessageType.LOCATION -> "📍 Localização"
            MessageType.CONTACT -> "👤 Contato"
            MessageType.POLL -> "📊 Enquete: ${message.pollData?.question}"
            MessageType.SYSTEM -> message.text
        }
        updateChatLastMessage(chatId, preview, message.timestamp)
    }

    private fun updateChatLastMessage(chatId: String, preview: String, timestamp: Long) {
        _chats.update { list ->
            list.map { chat ->
                if (chat.id == chatId) {
                    chat.copy(
                        lastMessage = preview,
                        lastMessageTime = timeFormat.format(Date(timestamp)),
                        lastMessageTimestamp = timestamp,
                        lastMessageStatus = MessageStatus.SENT,
                        lastMessageIsFromMe = true
                    )
                } else chat
            }
        }
    }

    private fun simulateMessageDelivery(chatId: String, messageId: String) {
        scope.launch {
            delay(600)
            updateMessageStatus(chatId, messageId, MessageStatus.DELIVERED)
            delay(1200)
            updateMessageStatus(chatId, messageId, MessageStatus.READ)
        }
    }

    private fun updateMessageStatus(chatId: String, messageId: String, newStatus: MessageStatus) {
        _messages.update { currentMap ->
            val list = currentMap[chatId] ?: return@update currentMap
            val updated = list.map { if (it.id == messageId) it.copy(status = newStatus) else it }
            currentMap + (chatId to updated)
        }
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(lastMessageStatus = newStatus) else it }
        }
    }

    private fun handleMetaAiQuery(prompt: String) {
        scope.launch {
            // Show typing indicator in chat
            _chats.update { list ->
                list.map { if (it.id == "chat_meta_ai") it.copy(typingUser = "Meta AI está digitando...") else it }
            }
            delay(1500)

            val isImagineCommand = prompt.trim().startsWith("/imagine", ignoreCase = true)
            val responseMsg = if (isImagineCommand) {
                val imagePrompt = prompt.replaceFirst("/imagine", "", ignoreCase = true).trim()
                Message(
                    id = UUID.randomUUID().toString(),
                    chatId = "chat_meta_ai",
                    senderId = "chat_meta_ai",
                    senderName = "Meta AI",
                    text = "Gerei esta imagem com base no seu prompt: \"$imagePrompt\"",
                    timestamp = System.currentTimeMillis(),
                    type = MessageType.IMAGE,
                    status = MessageStatus.READ,
                    isFromMe = false,
                    mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
                    mediaCaption = imagePrompt.ifBlank { "Imagem gerada por Meta AI" },
                    reactions = listOf(Reaction("✨", 1, true))
                )
            } else {
                val replyText = generateAiAnswer(prompt)
                Message(
                    id = UUID.randomUUID().toString(),
                    chatId = "chat_meta_ai",
                    senderId = "chat_meta_ai",
                    senderName = "Meta AI",
                    text = replyText,
                    timestamp = System.currentTimeMillis(),
                    type = MessageType.TEXT,
                    status = MessageStatus.READ,
                    isFromMe = false
                )
            }

            _chats.update { list ->
                list.map { if (it.id == "chat_meta_ai") it.copy(typingUser = null) else it }
            }
            addMessageToChat("chat_meta_ai", responseMsg)
        }
    }

    private fun generateAiAnswer(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("como") || lower.contains("o que") ->
                "Ótima pergunta! No desenvolvimento moderno com Kotlin e Jetpack Compose, a separação de responsabilidades com ViewModel, StateFlow e design Material 3 garante alta fluidez e reatividade em tempo real. Além disso, as novidades do WhatsApp incluem canais interativos, suporte nativo a WebRTC e criptografia ponta a ponta com curva Signal Protocol."
            lower.contains("ajuda") || lower.contains("help") ->
                "Estou aqui para ajudar com qualquer tarefa! Você pode pedir explicações técnicas, resumos de mensagens, traduções, itinerários ou usar `/imagine <descrição>` para gerar imagens conceituais direto no chat."
            lower.contains("código") || lower.contains("programar") ->
                "No ecossistema Android nativo, recomendamos usar Kotlin Coroutines para operações assíncronas sem travar a thread principal de UI. Exemplo: `viewModelScope.launch { repository.fetchUpdates() }`."
            else ->
                "Entendido! Estou processando as informações em tempo real para te dar a resposta mais precisa possível. Posso te ajudar com mais detalhes sobre isso ou sugerir ideias adicionais?"
        }
    }
}
