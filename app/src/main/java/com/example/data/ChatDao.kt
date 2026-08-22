package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class ContactWithLastMessage(
    val contact: ContactEntity,
    val lastMessage: MessageEntity?
)

@Dao
interface ChatDao {
    // --- Contacts ---
    @Query("SELECT * FROM contacts ORDER BY isPinned DESC, name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :contactId LIMIT 1")
    fun getContactById(contactId: String): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE id = :contactId LIMIT 1")
    suspend fun getContactByIdSync(contactId: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("UPDATE contacts SET isVerified = :isVerified WHERE id = :contactId")
    suspend fun updateVerification(contactId: String, isVerified: Boolean)

    @Query("UPDATE contacts SET ephemeralTimerMinutes = :minutes WHERE id = :contactId")
    suspend fun updateEphemeralTimer(contactId: String, minutes: Int)

    @Query("UPDATE contacts SET unreadCount = 0 WHERE id = :contactId")
    suspend fun clearUnreadCount(contactId: String)

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)

    // --- Messages ---
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    fun getLastMessageForChat(chatId: String): Flow<MessageEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET isStarred = NOT isStarred WHERE id = :messageId")
    suspend fun toggleStar(messageId: String)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun clearChat(chatId: String)

    @Query("SELECT * FROM messages WHERE isStarred = 1 ORDER BY timestamp DESC")
    fun getStarredMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE plainText LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<MessageEntity>>

    // --- Stories ---
    @Query("SELECT * FROM status_stories ORDER BY timestamp DESC")
    fun getAllStories(): Flow<List<StatusStoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StatusStoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StatusStoryEntity>)

    @Query("UPDATE status_stories SET isViewed = 1 WHERE id = :storyId")
    suspend fun markStoryViewed(storyId: String)

    // --- Calls ---
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(calls: List<CallLogEntity>)

    @Query("DELETE FROM call_logs WHERE id = :callId")
    suspend fun deleteCall(callId: String)

    @Query("DELETE FROM call_logs")
    suspend fun clearAllCalls()
}
