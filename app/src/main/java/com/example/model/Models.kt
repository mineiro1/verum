package com.example.model

enum class MessageType {
    TEXT,
    AUDIO,
    IMAGE,
    VIDEO,
    DOCUMENT,
    LOCATION,
    CONTACT,
    POLL,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

data class Reaction(
    val emoji: String,
    val count: Int,
    val userReacted: Boolean = false
)

data class PollOption(
    val id: String,
    val text: String,
    val votes: Int = 0,
    val voterIds: List<String> = emptyList()
)

data class PollData(
    val question: String,
    val options: List<PollOption>,
    val allowMultipleAnswers: Boolean = false
)

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENT,
    val isFromMe: Boolean = true,
    val isEdited: Boolean = false,
    val isViewOnce: Boolean = false,
    val isViewOnceOpened: Boolean = false,
    val ephemeralDurationHours: Int = 0, // 0 = permanent, 24 = 24h, 168 = 7 days, 2160 = 90 days
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val mediaUrl: String? = null,
    val mediaCaption: String? = null,
    val mediaFileName: String? = null,
    val mediaFileSize: String? = null,
    val isHd: Boolean = false,
    val audioDurationSeconds: Int = 0,
    val audioWaveform: List<Float> = emptyList(),
    val audioTranscription: String? = null,
    val pollData: PollData? = null,
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val locationAddress: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val reactions: List<Reaction> = emptyList()
)

data class Chat(
    val id: String,
    val name: String,
    val username: String? = null,
    val avatarUrl: String? = null,
    val isGroup: Boolean = false,
    val isChannel: Boolean = false,
    val isMetaAi: Boolean = false,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val isFavorite: Boolean = false,
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val lastMessageTimestamp: Long = 0L,
    val lastMessageStatus: MessageStatus = MessageStatus.READ,
    val lastMessageIsFromMe: Boolean = false,
    val typingUser: String? = null,
    val membersCount: Int = 1,
    val ephemeralSettingHours: Int = 0,
    val aboutStatus: String = "Disponível"
)

data class StatusStory(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String? = null,
    val imageUrl: String? = null,
    val caption: String? = null,
    val textStatus: String? = null,
    val backgroundColorHex: Long = 0xFF00A884,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false,
    val isMine: Boolean = false,
    val viewersCount: Int = 0
)

data class Channel(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String? = null,
    val description: String,
    val followersCount: String,
    val isVerified: Boolean = true,
    val isFollowing: Boolean = false,
    val unreadUpdatesCount: Int = 0,
    val lastPostTime: String = "Hoje"
)

data class ChannelPost(
    val id: String,
    val channelId: String,
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val viewsCount: String = "128 mil",
    val reactions: List<Reaction> = emptyList()
)

data class CommunityTopicGroup(
    val id: String,
    val name: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0
)

data class Community(
    val id: String,
    val name: String,
    val description: String,
    val avatarUrl: String? = null,
    val announcementGroupId: String,
    val announcementLastMessage: String,
    val topicGroups: List<CommunityTopicGroup> = emptyList(),
    val membersCount: Int = 142
)

enum class CallType {
    AUDIO,
    VIDEO
}

enum class CallStatus {
    INCOMING,
    OUTGOING,
    MISSED
}

data class CallLog(
    val id: String,
    val contactName: String,
    val contactAvatarUrl: String? = null,
    val type: CallType,
    val status: CallStatus,
    val timestamp: String,
    val durationSeconds: Int = 0
)

data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val phone: String,
    val statusText: String,
    val avatarUrl: String? = null,
    val publicKeyFingerprint: String
)

data class Account(
    val id: String,
    val name: String,
    val phone: String,
    val username: String,
    val avatarUrl: String? = null,
    val isActive: Boolean = false
)

data class AudioPlayerState(
    val playingMessageId: String? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val speed: Float = 1.0f,
    val isTranscriptionExpanded: Boolean = false
)

data class ActiveCallState(
    val isInCall: Boolean = false,
    val contactName: String = "",
    val contactAvatar: String? = null,
    val callType: CallType = CallType.AUDIO,
    val isMuted: Boolean = false,
    val isCameraOff: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isScreenSharing: Boolean = false,
    val durationSeconds: Int = 0
)

