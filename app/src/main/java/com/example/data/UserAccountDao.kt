package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    suspend fun getAllUsersSync(): List<UserAccountEntity>

    @Query("SELECT * FROM user_accounts WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE LOWER(username) = LOWER(:username) AND password = :password LIMIT 1")
    suspend fun authenticate(username: String, password: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity)

    @Update
    suspend fun updateUser(user: UserAccountEntity)

    @Query("UPDATE user_accounts SET isActive = :isActive WHERE id = :userId")
    suspend fun setUserActiveStatus(userId: String, isActive: Boolean)

    @Query("UPDATE user_accounts SET password = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: String, newPassword: String)

    @Query("UPDATE user_accounts SET lastLoginAt = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM user_accounts WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserCount(): Int
}
