package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WhatsAppRepository
import com.example.model.Account
import com.example.model.ActiveCallState
import com.example.model.AudioPlayerState
import com.example.model.CallLog
import com.example.model.CallType
import com.example.model.Channel
import com.example.model.ChannelPost
import com.example.model.Chat
import com.example.model.Community
import com.example.model.Message
import com.example.model.MessageType
import com.example.model.StatusStory
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class NavigationTab {
    CHATS,
    UPDATES,
    COMMUNITIES,
    CALLS
}

enum class ChatFilter {
    ALL,
    UNREAD,
    FAVORITES,
    GROUPS
}

class WhatsAppViewModel(
    val repository: WhatsAppRepository = WhatsAppRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(NavigationTab.CHATS)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _currentFilter = MutableStateFlow(ChatFilter.ALL)
    val currentFilter: StateFlow<ChatFilter> = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _viewingStory = MutableStateFlow<StatusStory?>(null)
    val viewingStory: StateFlow<StatusStory?> = _viewingStory.asStateFlow()

    private val _isProfileOpen = MutableStateFlow(false)
    val isProfileOpen: StateFlow<Boolean> = _isProfileOpen.asStateFlow()

    private val _isMetaAiSearchOpen = MutableStateFlow(false)
    val isMetaAiSearchOpen: StateFlow<Boolean> = _isMetaAiSearchOpen.asStateFlow()

    private val _isAccountsModalOpen = MutableStateFlow(false)
    val isAccountsModalOpen: StateFlow<Boolean> = _isAccountsModalOpen.asStateFlow()

    private val _isChatLockAuthOpen = MutableStateFlow(false)
    val isChatLockAuthOpen: StateFlow<Boolean> = _isChatLockAuthOpen.asStateFlow()

    private val _isAuthScreenOpen = MutableStateFlow(
        repository.accounts.value.isEmpty() || repository.currentUser.value.phone.isBlank()
    )
    val isAuthScreenOpen: StateFlow<Boolean> = _isAuthScreenOpen.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<Message?>(null)
    val replyingToMessage: StateFlow<Message?> = _replyingToMessage.asStateFlow()

    private val _editingMessage = MutableStateFlow<Message?>(null)
    val editingMessage: StateFlow<Message?> = _editingMessage.asStateFlow()

    // Filtered chats flow
    val filteredChats: StateFlow<List<Chat>> = combine(
        repository.chats,
        _currentFilter,
        _searchQuery,
        repository.isChatLockUnlocked
    ) { chats, filter, query, isUnlocked ->
        chats.filter { chat ->
            // If chat is locked, only show if unlocked or in locked drawer
            if (chat.isLocked && !isUnlocked) return@filter false

            val matchesFilter = when (filter) {
                ChatFilter.ALL -> true
                ChatFilter.UNREAD -> chat.unreadCount > 0
                ChatFilter.FAVORITES -> chat.isFavorite
                ChatFilter.GROUPS -> chat.isGroup
            }

            val matchesQuery = query.isBlank() ||
                chat.name.contains(query, ignoreCase = true) ||
                chat.lastMessage.contains(query, ignoreCase = true) ||
                (chat.username?.contains(query, ignoreCase = true) == true)

            matchesFilter && matchesQuery
        }.sortedWith(
            compareByDescending<Chat> { it.isPinned }
                .thenByDescending { it.lastMessageTimestamp }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val lockedChats: StateFlow<List<Chat>> = repository.chats.combine(_searchQuery) { chats, query ->
        chats.filter { it.isLocked && (query.isBlank() || it.name.contains(query, ignoreCase = true)) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Direct repository delegates
    val messages: StateFlow<Map<String, List<Message>>> = repository.messages
    val stories: StateFlow<List<StatusStory>> = repository.stories
    val channels: StateFlow<List<Channel>> = repository.channels
    val channelPosts: StateFlow<Map<String, List<ChannelPost>>> = repository.channelPosts
    val communities: StateFlow<List<Community>> = repository.communities
    val calls: StateFlow<List<CallLog>> = repository.calls
    val currentUser: StateFlow<UserProfile> = repository.currentUser
    val accounts: StateFlow<List<Account>> = repository.accounts
    val audioPlayerState: StateFlow<AudioPlayerState> = repository.audioPlayerState
    val activeCall: StateFlow<ActiveCallState> = repository.activeCall
    val isChatLockUnlocked: StateFlow<Boolean> = repository.isChatLockUnlocked

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setFilter(filter: ChatFilter) {
        _currentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openChat(chatId: String) {
        _activeChatId.value = chatId
        _replyingToMessage.value = null
        _editingMessage.value = null
    }

    fun startNewChat(name: String, handle: String): String {
        val chatId = repository.getOrCreateChat(name, handle)
        openChat(chatId)
        return chatId
    }

    fun openCommunityGroup(groupId: String, groupName: String = "Grupo da Comunidade") {
        val existing = repository.chats.value.find { it.id == groupId }
        if (existing != null) {
            openChat(groupId)
        } else {
            val newId = repository.getOrCreateChat(groupName, "@comunidade")
            openChat(newId)
        }
    }

    fun closeChat() {
        _activeChatId.value = null
        _replyingToMessage.value = null
        _editingMessage.value = null
    }

    fun setReplyingTo(message: Message?) {
        _replyingToMessage.value = message
    }

    fun setEditingMessage(message: Message?) {
        _editingMessage.value = message
    }

    fun viewStory(story: StatusStory?) {
        _viewingStory.value = story
    }

    fun setProfileOpen(open: Boolean) {
        _isProfileOpen.value = open
    }

    fun setMetaAiSearchOpen(open: Boolean) {
        _isMetaAiSearchOpen.value = open
    }

    fun setAccountsModalOpen(open: Boolean) {
        _isAccountsModalOpen.value = open
    }

    fun setChatLockAuthOpen(open: Boolean) {
        _isChatLockAuthOpen.value = open
    }

    fun setAuthScreenOpen(open: Boolean) {
        _isAuthScreenOpen.value = open
    }

    fun registerAccount(name: String, phone: String, username: String, avatarUrl: String?) {
        repository.registerAccount(name, phone, username, avatarUrl)
        _isAuthScreenOpen.value = false
    }

    fun deleteAccount() {
        repository.deleteCurrentAccount()
        _isProfileOpen.value = false
        if (repository.accounts.value.isEmpty()) {
            _isAuthScreenOpen.value = true
        }
    }

    // Forwarding to repository
    fun sendTextMessage(chatId: String, text: String) {
        val replyTo = _replyingToMessage.value
        repository.sendTextMessage(chatId, text, replyTo)
        _replyingToMessage.value = null
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        repository.editMessage(chatId, messageId, newText)
        _editingMessage.value = null
    }

    fun sendAudioNote(chatId: String, durationSec: Int, waveform: List<Float>) {
        repository.sendAudioNote(chatId, durationSec, waveform)
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
        repository.sendMediaMessage(chatId, type, url, caption, fileName, fileSize, isViewOnce, isHd)
    }

    fun sendLocationMessage(chatId: String, lat: Double, lng: Double, address: String) {
        repository.sendLocationMessage(chatId, lat, lng, address)
    }

    fun sendContactMessage(chatId: String, contactName: String, contactPhone: String) {
        repository.sendContactMessage(chatId, contactName, contactPhone)
    }

    fun createPoll(chatId: String, question: String, options: List<String>, allowMultiple: Boolean) {
        repository.createPoll(chatId, question, options, allowMultiple)
    }

    fun votePoll(chatId: String, messageId: String, optionId: String) {
        repository.votePoll(chatId, messageId, optionId)
    }

    fun addReaction(chatId: String, messageId: String, emoji: String) {
        repository.addReaction(chatId, messageId, emoji)
    }

    fun openViewOnce(chatId: String, messageId: String) {
        repository.openViewOnceMessage(chatId, messageId)
    }

    fun togglePinChat(chatId: String) {
        repository.togglePinChat(chatId)
    }

    fun toggleChatLock(chatId: String) {
        repository.toggleChatLock(chatId)
    }

    fun unlockChatLock() {
        repository.unlockChatLock()
        _isChatLockAuthOpen.value = false
    }

    fun lockChatLock() {
        repository.lockChatLock()
    }

    fun setEphemeralMessages(chatId: String, durationHours: Int) {
        repository.setEphemeralMessages(chatId, durationHours)
    }

    fun playAudio(messageId: String) {
        repository.playAudio(messageId)
    }

    fun seekAudio(progress: Float) {
        repository.seekAudio(progress)
    }

    fun toggleAudioSpeed() {
        repository.toggleAudioSpeed()
    }

    fun toggleAudioTranscription() {
        repository.toggleAudioTranscription()
    }

    fun startCall(contactName: String, contactAvatar: String?, type: CallType) {
        repository.startCall(contactName, contactAvatar, type)
    }

    fun endCall() {
        repository.endCall()
    }

    fun toggleCallMute() {
        repository.toggleCallMute()
    }

    fun toggleCallCamera() {
        repository.toggleCallCamera()
    }

    fun toggleCallSpeaker() {
        repository.toggleCallSpeaker()
    }

    fun toggleCallScreenShare() {
        repository.toggleCallScreenShare()
    }

    fun switchAccount(accountId: String) {
        repository.switchAccount(accountId)
        _isAccountsModalOpen.value = false
    }

    fun updateProfile(name: String, status: String, username: String) {
        repository.updateProfile(name, status, username)
        _isProfileOpen.value = false
    }

    fun toggleFollowChannel(channelId: String) {
        repository.toggleFollowChannel(channelId)
    }

    fun addChannelReaction(channelId: String, postId: String, emoji: String) {
        repository.addChannelReaction(channelId, postId, emoji)
    }

    fun postStatus(text: String?, imageUrl: String?, bgColor: Long = 0xFF00A884) {
        repository.postStatus(text, imageUrl, bgColor)
    }
}
