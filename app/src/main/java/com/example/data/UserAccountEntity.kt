package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String,
    val fullName: String,            // Nom complet
    val username: String,            // Nom d'utilisateur (user name)
    val whatsappNumber: String,      // Numéro WhatsApp
    val password: String,            // Mot de passe
    val avatarColorHex: String = "#00F2FF",
    val statusMessage: String = "Disponible sur l'application",
    val isActive: Boolean = true,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
