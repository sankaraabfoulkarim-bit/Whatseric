package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserAccountEntity
import com.example.ui.components.NeonAvatar
import com.example.ui.theme.LocalNeonColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextGraySecondary

@Composable
fun AuthScreen(
    registeredUsers: List<UserAccountEntity>,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String, String) -> Unit,
    onOpenAdminConsole: () -> Unit,
    authErrorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val neonColors = LocalNeonColors.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Se connecter, 1: S'inscrire

    // LOGIN FIELDS
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // SIGNUP FIELDS
    var regFullName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regWhatsapp by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }
    var showRegConfirmPassword by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    // SECRET ADMIN TRIGGER: Tap count on logo
    var logoTapCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D12))
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // APP LOGO & HEADER
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x3300F2FF))
                    .border(1.5.dp, neonColors.neonAccent, RoundedCornerShape(20.dp))
                    .clickable {
                        logoTapCount++
                        if (logoTapCount >= 3) {
                            logoTapCount = 0
                            onOpenAdminConsole()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Logo",
                    tint = neonColors.neonAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "MaelysCryp Messenger",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Messagerie instantanée moderne et chiffrée",
                style = MaterialTheme.typography.bodySmall,
                color = TextGraySecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // TAB ROW (CONNEXION / INSCRIPTION)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SleekSurface,
                border = BorderStroke(1.dp, SleekBorder)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = neonColors.neonAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = neonColors.neonAccent,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            localValidationError = null
                        },
                        text = {
                            Text(
                                text = "Se connecter",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) neonColors.neonAccent else TextGraySecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            localValidationError = null
                        },
                        text = {
                            Text(
                                text = "S'inscrire",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) neonColors.neonAccent else TextGraySecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ERROR DISPLAY
            val displayError = localValidationError ?: authErrorMessage
            if (displayError != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFF5252).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = displayError,
                        color = Color(0xFFFF8080),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // FORM CONTAINER
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedTab == 0) {
                        // ==================== TAB 0: SE CONNECTER ====================
                        Text(
                            text = "Connexion à votre compte",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // USERNAME FIELD
                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = { loginUsername = it },
                            label = { Text("Nom d'utilisateur (user name)") },
                            placeholder = { Text("ex: kylian ou alice") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = neonColors.neonAccent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonColors.neonAccent,
                                unfocusedBorderColor = SleekBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = neonColors.neonAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // PASSWORD FIELD
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Mot de passe") },
                            placeholder = { Text("Entrez votre mot de passe") },
                            singleLine = true,
                            visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (loginUsername.isNotBlank() && loginPassword.isNotBlank()) {
                                        onLogin(loginUsername, loginPassword)
                                    }
                                }
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null, tint = neonColors.neonAccent)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                    Icon(
                                        imageVector = if (showLoginPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextGraySecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonColors.neonAccent,
                                unfocusedBorderColor = SleekBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = neonColors.neonAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // SUBMIT BUTTON
                        Button(
                            onClick = {
                                if (loginUsername.isBlank() || loginPassword.isBlank()) {
                                    localValidationError = "Veuillez saisir votre nom d'utilisateur et mot de passe."
                                } else {
                                    localValidationError = null
                                    onLogin(loginUsername, loginPassword)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = neonColors.neonAccent,
                                contentColor = Color(0xFF002A1C)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Se connecter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // DEMO FAST LOGIN SELECTOR
                        if (registeredUsers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Comptes disponibles (connexion 1-clic) :",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGrayMuted,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(registeredUsers.filter { it.isActive }) { user ->
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable {
                                                loginUsername = user.username
                                                loginPassword = user.password
                                                onLogin(user.username, user.password)
                                            }
                                            .border(0.8.dp, SleekBorder, RoundedCornerShape(20.dp)),
                                        color = SleekSurfaceVariant
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            NeonAvatar(
                                                name = user.fullName,
                                                avatarColorHex = user.avatarColorHex,
                                                size = 22.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = user.fullName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        // ==================== TAB 1: S'INSCRIRE ====================
                        Text(
                            text = "Créer un nouveau compte",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. NOM (FULL NAME)
                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Nom complet (ex: Jean Dupont)") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = neonColors.neonAccent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_fullname_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonColors.neonAccent,
                                unfocusedBorderColor = SleekBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = neonColors.neonAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. USER NAME (IDENTIFIANT)
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it.trim().lowercase() },
                            label = { Text("Nom d'utilisateur (user name)") },
                            placeholder = { Text("ex: jean_dupont") },
                            singleLine = true,
                            leadingIcon = {
                                Text(
                                    text = "@",
                                    color = neonColors.neonAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_username_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonColors.neonAccent,
                                unfocusedBorderColor = SleekBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = neonColors.neonAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. NUMÉRO WHATSAPP
                        OutlinedTextField(
                            value = regWhatsapp,
                            onValueChange = { regWhatsapp = it },
                            label = { Text("Numéro WhatsApp") },
                            placeholder = { Text("+33 6 12 34 56 78") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = NeonEmerald)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_whatsapp_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonColors.neonAccent,
                                unfocusedBorderColor = SleekBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = neonColors.neonAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. MOT DE PASSE
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Mot de passe") },
                            singleLine = true,
                            visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = neonColors.neonAccent)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                    Icon(
                                        imageVector = if (showRegPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextGraySecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonColors.neonAccent,
                                unfocusedBorderColor = SleekBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = neonColors.neonAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 5. CONFIRMATION MOT DE PASSE
                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it },
                            label = { Text("Confirmation mot de passe") },
                            singleLine = true,
                            visualTransformation = if (showRegConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = neonColors.neonAccent)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showRegConfirmPassword = !showRegConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showRegConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextGraySecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_confirm_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonColors.neonAccent,
                                unfocusedBorderColor = SleekBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = neonColors.neonAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // SUBMIT SIGNUP BUTTON
                        Button(
                            onClick = {
                                if (regFullName.isBlank()) {
                                    localValidationError = "Veuillez entrer votre nom complet."
                                } else if (regUsername.isBlank()) {
                                    localValidationError = "Veuillez entrer un nom d'utilisateur."
                                } else if (regWhatsapp.isBlank()) {
                                    localValidationError = "Veuillez entrer votre numéro WhatsApp."
                                } else if (regPassword.length < 3) {
                                    localValidationError = "Le mot de passe doit contenir au moins 3 caractères."
                                } else if (regPassword != regConfirmPassword) {
                                    localValidationError = "Les mots de passe ne correspondent pas."
                                } else {
                                    localValidationError = null
                                    onRegister(regFullName, regUsername, regWhatsapp, regPassword, regConfirmPassword)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("signup_submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = neonColors.neonAccent,
                                contentColor = Color(0xFF002A1C)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Créer mon compte",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // HIDDEN / DISCREET ADMIN ACCESS BUTTON
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenAdminConsole() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Console Admin",
                    tint = TextGraySecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Console Admin (Code: 761278)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGraySecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
