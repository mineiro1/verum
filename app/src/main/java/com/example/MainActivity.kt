package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ActiveCallState
import com.example.model.AudioPlayerState
import com.example.model.CallType
import com.example.model.MessageType
import com.example.ui.NavigationTab
import com.example.ui.WhatsAppViewModel
import com.example.ui.components.AccountsModalBottomSheet
import com.example.ui.components.ChatLockAuthDialog
import com.example.ui.components.NewChatDialog
import com.example.ui.components.WhatsAppBottomBar
import com.example.ui.components.WhatsAppTopBar
import com.example.ui.screens.ActiveCallScreen
import com.example.ui.screens.CallsScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.CommunitiesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StatusViewerScreen
import com.example.ui.screens.UpdatesScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WhatsAppMainApp()
            }
        }
    }
}

@Composable
fun WhatsAppMainApp(
    viewModel: WhatsAppViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredChats by viewModel.filteredChats.collectAsState()
    val lockedChats by viewModel.lockedChats.collectAsState()
    val activeChatId by viewModel.activeChatId.collectAsState()
    val messagesMap by viewModel.messages.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val viewingStory by viewModel.viewingStory.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val channelPosts by viewModel.channelPosts.collectAsState()
    val communities by viewModel.communities.collectAsState()
    val calls by viewModel.calls.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val audioPlayerState by viewModel.audioPlayerState.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()
    val isChatLockUnlocked by viewModel.isChatLockUnlocked.collectAsState()
    val isProfileOpen by viewModel.isProfileOpen.collectAsState()
    val isAccountsModalOpen by viewModel.isAccountsModalOpen.collectAsState()
    val isChatLockAuthOpen by viewModel.isChatLockAuthOpen.collectAsState()
    val replyingToMessage by viewModel.replyingToMessage.collectAsState()
    val editingMessage by viewModel.editingMessage.collectAsState()

    var isNewChatDialogOpen by remember { mutableStateOf(false) }

    // 1. FULL SCREEN: Active VoIP / WebRTC Call Screen
    if (activeCall.isInCall) {
        ActiveCallScreen(
            callState = activeCall,
            onEndCall = { viewModel.endCall() },
            onToggleMute = { viewModel.toggleCallMute() },
            onToggleCamera = { viewModel.toggleCallCamera() },
            onToggleSpeaker = { viewModel.toggleCallSpeaker() },
            onToggleScreenShare = { viewModel.toggleCallScreenShare() }
        )
        return
    }

    // 2. FULL SCREEN: Status / Story Viewer
    if (viewingStory != null) {
        StatusViewerScreen(
            story = viewingStory!!,
            onClose = { viewModel.viewStory(null) },
            onReplyToStatus = { reply ->
                val authorChatId = viewingStory!!.userId
                viewModel.sendTextMessage(authorChatId, "Resposta ao status: $reply")
            }
        )
        return
    }

    // 3. FULL SCREEN: Profile & Security Keys Screen
    if (isProfileOpen) {
        ProfileScreen(
            profile = currentUser,
            onBack = { viewModel.setProfileOpen(false) },
            onUpdateProfile = { name, status, username ->
                viewModel.updateProfile(name, status, username)
            },
            onOpenAccountsModal = { viewModel.setAccountsModalOpen(true) }
        )
        return
    }

    // 4. FULL SCREEN: Open Chat Conversation Detail
    if (activeChatId != null) {
        val allChats by viewModel.repository.chats.collectAsState()
        val chat = allChats.find { it.id == activeChatId }
        val messages = messagesMap[activeChatId] ?: emptyList()

        if (chat != null) {
            ChatDetailScreen(
                chat = chat,
                messages = messages,
                audioPlayerState = audioPlayerState,
                replyingToMessage = replyingToMessage,
                editingMessage = editingMessage,
                onBack = { viewModel.closeChat() },
                onSendMessage = { text -> viewModel.sendTextMessage(chat.id, text) },
                onEditMessage = { msgId, text -> viewModel.editMessage(chat.id, msgId, text) },
                onSendAudioNote = { duration, waveform -> viewModel.sendAudioNote(chat.id, duration, waveform) },
                onSendMedia = { type, url, caption, isViewOnce, isHd ->
                    viewModel.sendMediaMessage(chat.id, type, url, caption, null, null, isViewOnce, isHd)
                },
                onSendLocation = { lat, lng, addr -> viewModel.sendLocationMessage(chat.id, lat, lng, addr) },
                onSendContact = { name, phone -> viewModel.sendContactMessage(chat.id, name, phone) },
                onCreatePoll = { q, opts, multi -> viewModel.createPoll(chat.id, q, opts, multi) },
                onVotePoll = { msgId, optId -> viewModel.votePoll(chat.id, msgId, optId) },
                onAddReaction = { msgId, emoji -> viewModel.addReaction(chat.id, msgId, emoji) },
                onOpenViewOnce = { msgId -> viewModel.openViewOnce(chat.id, msgId) },
                onPlayAudio = { msgId -> viewModel.playAudio(msgId) },
                onSeekAudio = { prog -> viewModel.seekAudio(prog) },
                onToggleAudioSpeed = { viewModel.toggleAudioSpeed() },
                onToggleAudioTranscription = { viewModel.toggleAudioTranscription() },
                onSetReplyingTo = { msg -> viewModel.setReplyingTo(msg) },
                onSetEditingMessage = { msg -> viewModel.setEditingMessage(msg) },
                onStartCall = { name, avatar, type -> viewModel.startCall(name, avatar, type) },
                onToggleLockChat = { cid -> viewModel.toggleChatLock(cid) },
                onSetEphemeral = { cid, hours -> viewModel.setEphemeralMessages(cid, hours) }
            )
            return
        }
    }

    // 5. MAIN NAVIGATION FRAMEWORK (Chats, Updates, Communities, Calls)
    Scaffold(
        topBar = {
            WhatsAppTopBar(
                currentTab = currentTab,
                selectedFilter = currentFilter,
                onFilterSelected = { viewModel.setFilter(it) },
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onMetaAiClick = { viewModel.openChat("chat_meta_ai") },
                onProfileClick = { viewModel.setProfileOpen(true) },
                onAccountsClick = { viewModel.setAccountsModalOpen(true) },
                onLockedChatsClick = {
                    if (isChatLockUnlocked) {
                        // Already unlocked, lock it
                        viewModel.lockChatLock()
                    } else {
                        // Request auth
                        viewModel.setChatLockAuthOpen(true)
                    }
                },
                onCameraClick = {
                    viewModel.postStatus(
                        null,
                        "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800"
                    )
                }
            )
        },
        bottomBar = {
            val unreadCount = filteredChats.count { it.unreadCount > 0 }
            WhatsAppBottomBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) },
                unreadChatsCount = unreadCount
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                NavigationTab.CHATS -> {
                    ChatsScreen(
                        chats = filteredChats,
                        lockedChatsCount = lockedChats.size,
                        isChatLockUnlocked = isChatLockUnlocked,
                        onChatClick = { chatId -> viewModel.openChat(chatId) },
                        onTogglePin = { chatId -> viewModel.togglePinChat(chatId) },
                        onToggleLock = { chatId -> viewModel.toggleChatLock(chatId) },
                        onLockedChatsClick = {
                            if (isChatLockUnlocked) {
                                viewModel.lockChatLock()
                            } else {
                                viewModel.setChatLockAuthOpen(true)
                            }
                        },
                        onMetaAiClick = { viewModel.openChat("chat_meta_ai") },
                        onNewChatClick = { isNewChatDialogOpen = true }
                    )
                }

                NavigationTab.UPDATES -> {
                    UpdatesScreen(
                        stories = stories,
                        channels = channels,
                        channelPosts = channelPosts,
                        currentUser = currentUser,
                        onStoryClick = { story -> viewModel.viewStory(story) },
                        onToggleFollowChannel = { channelId -> viewModel.toggleFollowChannel(channelId) },
                        onChannelReaction = { chId, pId, emoji -> viewModel.addChannelReaction(chId, pId, emoji) },
                        onPostStatus = { text, url -> viewModel.postStatus(text, url) }
                    )
                }

                NavigationTab.COMMUNITIES -> {
                    CommunitiesScreen(
                        communities = communities,
                        onCommunityGroupClick = { groupId -> viewModel.openChat("chat_2") }
                    )
                }

                NavigationTab.CALLS -> {
                    CallsScreen(
                        calls = calls,
                        onStartCall = { name, avatar, type -> viewModel.startCall(name, avatar, type) }
                    )
                }
            }
        }
    }

    // Multi-Account Switcher Modal
    if (isAccountsModalOpen) {
        AccountsModalBottomSheet(
            accounts = accounts,
            onSelectAccount = { accountId -> viewModel.switchAccount(accountId) },
            onDismiss = { viewModel.setAccountsModalOpen(false) }
        )
    }

    // Chat Lock Authentication Modal
    if (isChatLockAuthOpen) {
        ChatLockAuthDialog(
            onDismiss = { viewModel.setChatLockAuthOpen(false) },
            onAuthenticated = { viewModel.unlockChatLock() }
        )
    }

    // New Chat Dialog
    if (isNewChatDialogOpen) {
        NewChatDialog(
            onDismiss = { isNewChatDialogOpen = false },
            onSelectContact = { name, handle ->
                // Opens Mariana Costa or first chat as target
                viewModel.openChat("chat_1")
            }
        )
    }
}
