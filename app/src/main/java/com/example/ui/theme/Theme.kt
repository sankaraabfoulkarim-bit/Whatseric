package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.NightThemeMode

data class CustomNeonColors(
    val neonAccent: Color,
    val neonSecondary: Color,
    val neonGlow: Color,
    val outgoingBubble: Color,
    val incomingBubble: Color,
    val chatBackground: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val securityNoticeBg: Color,
    val isAmberWarm: Boolean,
    val outgoingText: Color = SleekOutgoingText,
    val outgoingTime: Color = SleekOutgoingTime
)

val LocalNeonColors = staticCompositionLocalOf {
    CustomNeonColors(
        neonAccent = SleekNeonCyan,
        neonSecondary = SleekNeonCyan,
        neonGlow = SleekNeonCyanGlow,
        outgoingBubble = SleekOutgoingBubble,
        incomingBubble = SleekIncomingBubble,
        chatBackground = SleekBackground,
        cardBackground = SleekCard,
        cardBorder = SleekBorder,
        securityNoticeBg = Color(0x801A1C1E),
        isAmberWarm = false,
        outgoingText = SleekOutgoingText,
        outgoingTime = SleekOutgoingTime
    )
}

@Composable
fun NeonCryptTheme(
    nightMode: NightThemeMode = NightThemeMode.OLED_PURE_BLACK,
    content: @Composable () -> Unit
) {
    val (colorScheme, neonColors) = when (nightMode) {
        NightThemeMode.OLED_PURE_BLACK -> {
            val scheme = darkColorScheme(
                primary = SleekNeonCyan,
                onPrimary = Color(0xFF09090B),
                primaryContainer = Color(0xFF003B3E),
                onPrimaryContainer = SleekNeonCyan,
                secondary = SleekNeonCyan,
                onSecondary = Color(0xFF09090B),
                secondaryContainer = Color(0xFF1A1C1E),
                onSecondaryContainer = SleekNeonCyan,
                tertiary = NeonViolet,
                background = SleekBackground,
                onBackground = SleekTextPrimary,
                surface = SleekSurface,
                onSurface = SleekTextWhite,
                surfaceVariant = SleekSurfaceVariant,
                onSurfaceVariant = SleekTextSecondary,
                outline = SleekBorder
            )
            val custom = CustomNeonColors(
                neonAccent = SleekNeonCyan,
                neonSecondary = SleekNeonCyan,
                neonGlow = SleekNeonCyanGlow,
                outgoingBubble = SleekOutgoingBubble,
                incomingBubble = SleekIncomingBubble,
                chatBackground = SleekBackground,
                cardBackground = SleekCard,
                cardBorder = SleekBorder,
                securityNoticeBg = Color(0x801A1C1E),
                isAmberWarm = false,
                outgoingText = SleekOutgoingText,
                outgoingTime = SleekOutgoingTime
            )
            Pair(scheme, custom)
        }

        NightThemeMode.MIDNIGHT_SLATE -> {
            val scheme = darkColorScheme(
                primary = SleekNeonCyan,
                onPrimary = Color(0xFF09090B),
                primaryContainer = Color(0xFF003B3E),
                onPrimaryContainer = SleekNeonCyan,
                secondary = SleekNeonCyan,
                onSecondary = Color(0xFF09090B),
                secondaryContainer = Color(0xFF1A1C1E),
                onSecondaryContainer = SleekNeonCyan,
                tertiary = NeonViolet,
                background = SlateBackground,
                onBackground = SleekTextPrimary,
                surface = SleekSurface,
                onSurface = SleekTextWhite,
                surfaceVariant = SleekSurfaceVariant,
                onSurfaceVariant = SleekTextSecondary,
                outline = SleekBorder
            )
            val custom = CustomNeonColors(
                neonAccent = SleekNeonCyan,
                neonSecondary = SleekNeonCyan,
                neonGlow = SleekNeonCyanGlow,
                outgoingBubble = SleekOutgoingBubble,
                incomingBubble = SleekIncomingBubble,
                chatBackground = SlateBackground,
                cardBackground = SleekCard,
                cardBorder = SleekBorder,
                securityNoticeBg = Color(0x801A1C1E),
                isAmberWarm = false,
                outgoingText = SleekOutgoingText,
                outgoingTime = SleekOutgoingTime
            )
            Pair(scheme, custom)
        }

        NightThemeMode.AMBER_NIGHT_FILTER -> {
            val scheme = darkColorScheme(
                primary = AmberPrimary,
                onPrimary = Color(0xFF3E2200),
                primaryContainer = Color(0xFF5A3300),
                onPrimaryContainer = Color(0xFFFFD180),
                secondary = AmberSecondary,
                onSecondary = Color(0xFF381507),
                secondaryContainer = Color(0xFF54220E),
                onSecondaryContainer = Color(0xFFFFAB91),
                tertiary = Color(0xFFFFD54F),
                background = AmberBackground,
                onBackground = Color(0xFFFFEEDB),
                surface = AmberSurface,
                onSurface = Color(0xFFFFEEDB),
                surfaceVariant = AmberSurfaceVariant,
                onSurfaceVariant = Color(0xFFD7CCC8),
                outline = AmberBorder
            )
            val custom = CustomNeonColors(
                neonAccent = AmberPrimary,
                neonSecondary = AmberSecondary,
                neonGlow = Color(0x33FFA726),
                outgoingBubble = Color(0xFF42280E),
                incomingBubble = Color(0xFF1C1610),
                chatBackground = AmberBackground,
                cardBackground = AmberCard,
                cardBorder = AmberBorder,
                securityNoticeBg = Color(0xFF2E1C0A),
                isAmberWarm = true,
                outgoingText = Color(0xFFFFEEDB),
                outgoingTime = AmberSecondary
            )
            Pair(scheme, custom)
        }
    }

    CompositionLocalProvider(LocalNeonColors provides neonColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

