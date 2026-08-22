package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class MessageType {
    TEXT,
    VOICE_NOTE,
    PHOTO,
    SECURITY_ALERT
}

enum class CallType {
    AUDIO,
    VIDEO
}

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

enum class NightThemeMode {
    OLED_PURE_BLACK,   // #000000 true black for OLED battery & dark comfort
    MIDNIGHT_SLATE,    // #0B111A sleek dark navy slate
    AMBER_NIGHT_FILTER // #130E07 warm amber night reading mode for blue-light reduction
}

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val avatarColorHex: String,
    val statusMessage: String,
    val isOnline: Boolean = false,
    val lastSeen: String = "En ligne",
    val isPinned: Boolean = false,
    val isVerified: Boolean = false,
    val safetyNumber: String = "",
    val publicKeyFingerprint: String = "",
    val unreadCount: Int = 0,
    val ephemeralTimerMinutes: Int = 0, // 0 = désactivé, 5 min, 60 min, 1440 min (24h)
    val customNotes: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String, // "me" or contact id
    val plainText: String,
    val cipherText: String, // Base64 encrypted payload
    val ivHex: String,
    val authTagHex: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val messageType: MessageType = MessageType.TEXT,
    val voiceDurationSeconds: Int = 0,
    val mediaUri: String? = null,
    val isStarred: Boolean = false,
    val isDecryptedLocally: Boolean = true
)

@Entity(tableName = "status_stories")
data class StatusStoryEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val contactName: String,
    val avatarColorHex: String,
    val textCaption: String,
    val backgroundGradientIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val contactName: String,
    val avatarColorHex: String,
    val callType: CallType = CallType.AUDIO,
    val direction: CallDirection = CallDirection.OUTGOING,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val isEncrypted: Boolean = true
)

class AppTypeConverters {
    @TypeConverter
    fun fromMessageStatus(status: MessageStatus): String = status.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = try {
        MessageStatus.valueOf(value)
    } catch (e: Exception) {
        MessageStatus.SENT
    }

    @TypeConverter
    fun fromMessageType(type: MessageType): String = type.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = try {
        MessageType.valueOf(value)
    } catch (e: Exception) {
        MessageType.TEXT
    }

    @TypeConverter
    fun fromCallType(type: CallType): String = type.name

    @TypeConverter
    fun toCallType(value: String): CallType = try {
        CallType.valueOf(value)
    } catch (e: Exception) {
        CallType.AUDIO
    }

    @TypeConverter
    fun fromCallDirection(dir: CallDirection): String = dir.name

    @TypeConverter
    fun toCallDirection(value: String): CallDirection = try {
        CallDirection.valueOf(value)
    } catch (e: Exception) {
        CallDirection.OUTGOING
    }
}
