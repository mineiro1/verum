package com.example.data

import android.content.Context
import android.content.SharedPreferences
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
    companion object {
        private var sharedPrefs: SharedPreferences? = null

        fun initPreferences(context: Context) {
            sharedPrefs = context.getSharedPreferences("whatsapp_user_prefs", Context.MODE_PRIVATE)
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val supabaseService = SupabaseService()

    // Active User Accounts (Starts completely empty for clean production install)
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "",
            name = "",
            username = "",
            phone = "",
            statusText = "Olá! Estou usando o Verum.",
            avatarUrl = null,
            publicKeyFingerprint = ""
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
        loadPersistedUser()
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

    private fun loadPersistedUser() {
        val prefs = sharedPrefs ?: return
        val savedPhone = prefs.getString("user_phone", "") ?: ""
        val savedName = prefs.getString("user_name", "") ?: ""
        val savedUsername = prefs.getString("user_username", "") ?: ""
        val savedId = prefs.getString("user_id", "") ?: ""
        val savedAvatar = prefs.getString("user_avatar", null)

        if (savedPhone.isNotBlank() && savedName.isNotBlank()) {
            val profile = UserProfile(
                id = savedId.ifBlank { UUID.randomUUID().toString() },
                name = savedName,
                username = savedUsername,
                phone = savedPhone,
                statusText = "Olá! Estou usando o Verum.",
                avatarUrl = savedAvatar,
                publicKeyFingerprint = "7392 4810 5928 1048 3920 4819 0284 8192 4819 4028 1948 2910"
            )
            _currentUser.value = profile
            val account = Account(
                id = profile.id,
                name = profile.name,
                phone = profile.phone,
                username = "@$savedUsername",
                avatarUrl = profile.avatarUrl,
                isActive = true
            )
            _accounts.value = listOf(account)
        }
    }

    private fun seedInitialData() {
        // Clean production setup: No mock contacts or fake accounts
        val metaAiChat = Chat(
            id = "chat_meta_ai",
            name = "Meta AI",
            username = "@meta.ai",
            avatarUrl = null,
            isGroup = false,
            isMetaAi = true,
            unreadCount = 0,
            isPinned = false,
            lastMessage = "Olá! Como posso ajudar você hoje no Verum?",
            lastMessageTime = "",
            lastMessageTimestamp = System.currentTimeMillis(),
            lastMessageStatus = MessageStatus.READ,
            aboutStatus = "Assistente de Inteligência Artificial Oficial"
        )
        _chats.value = listOf(metaAiChat)
        _messages.value = emptyMap()
        _stories.value = emptyList()
        _channels.value = emptyList()
        _channelPosts.value = emptyMap()
        _communities.value = emptyList()
        _calls.value = emptyList()
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
        supabaseService.registerUser(_currentUser.value)
    }

    fun registerAccount(name: String, phone: String, username: String, avatarUrl: String?) {
        val cleanUsername = username.trim().removePrefix("@")
        val newProfile = UserProfile(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            username = cleanUsername,
            phone = phone.trim(),
            statusText = "Olá! Estou usando o Verum.",
            avatarUrl = avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            publicKeyFingerprint = "7392 4810 5928 1048 3920 4819 0284 8192 4819 4028 1948 2910"
        )
        _currentUser.value = newProfile

        val newAccount = Account(
            id = newProfile.id,
            name = newProfile.name,
            phone = newProfile.phone,
            username = "@$cleanUsername",
            avatarUrl = newProfile.avatarUrl,
            isActive = true
        )

        _accounts.update { list ->
            list.map { it.copy(isActive = false) } + newAccount
        }

        sharedPrefs?.edit()
            ?.putString("user_id", newProfile.id)
            ?.putString("user_name", newProfile.name)
            ?.putString("user_phone", newProfile.phone)
            ?.putString("user_username", cleanUsername)
            ?.putString("user_avatar", newProfile.avatarUrl)
            ?.apply()

        supabaseService.registerUser(newProfile)
    }

    fun deleteCurrentAccount() {
        val current = _currentUser.value
        supabaseService.deleteUser(current.id)

        val remaining = _accounts.value.filter { it.id != current.id }
        if (remaining.isNotEmpty()) {
            val next = remaining.first().copy(isActive = true)
            _accounts.value = remaining.map { if (it.id == next.id) next else it.copy(isActive = false) }
            _currentUser.value = _currentUser.value.copy(
                id = next.id,
                name = next.name,
                phone = next.phone,
                username = next.username.removePrefix("@"),
                avatarUrl = next.avatarUrl
            )
            sharedPrefs?.edit()
                ?.putString("user_id", next.id)
                ?.putString("user_name", next.name)
                ?.putString("user_phone", next.phone)
                ?.putString("user_username", next.username.removePrefix("@"))
                ?.putString("user_avatar", next.avatarUrl)
                ?.apply()
        } else {
            _accounts.value = emptyList()
            _currentUser.value = UserProfile(
                id = "",
                name = "",
                username = "",
                phone = "",
                statusText = "Olá! Estou usando o Verum.",
                avatarUrl = null,
                publicKeyFingerprint = ""
            )
            sharedPrefs?.edit()?.clear()?.apply()
        }
    }

    fun getOrCreateChat(contactName: String, contactHandle: String): String {
        val cleanName = contactName.trim()
        val cleanHandle = if (contactHandle.trim().startsWith("@")) contactHandle.trim() else "@${contactHandle.trim()}"

        val existing = _chats.value.find {
            it.name.equals(cleanName, ignoreCase = true) ||
                (it.username != null && it.username.equals(cleanHandle, ignoreCase = true))
        }
        if (existing != null) return existing.id

        val newId = "chat_${UUID.randomUUID().toString().take(8)}"
        val newChat = Chat(
            id = newId,
            name = cleanName,
            username = cleanHandle,
            avatarUrl = null,
            isGroup = false,
            isMetaAi = false,
            unreadCount = 0,
            isPinned = false,
            lastMessage = "",
            lastMessageTime = "",
            lastMessageTimestamp = System.currentTimeMillis(),
            lastMessageStatus = MessageStatus.SENT,
            aboutStatus = "Disponível"
        )
        _chats.update { listOf(newChat) + it }
        return newId
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
