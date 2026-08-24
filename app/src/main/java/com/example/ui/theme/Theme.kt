package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
    val isLightMode: Boolean = false,
    val outgoingText: Color = SleekOutgoingText,
    val outgoingTime: Color = SleekOutgoingTime
)

val LocalNeonColors = staticCompositionLocalOf {
    CustomNeonColors(
        neonAccent = NeonPrimary,
        neonSecondary = NeonSecondary,
        neonGlow = NeonPrimaryGlow,
        outgoingBubble = NeonOutgoingBubble,
        incomingBubble = NeonIncomingBubble,
        chatBackground = NeonBackground,
        cardBackground = NeonCard,
        cardBorder = NeonBorder,
        securityNoticeBg = Color(0x801A1C1E),
        isAmberWarm = false,
        isLightMode = false,
        outgoingText = NeonOutgoingText,
        outgoingTime = NeonOutgoingTime
    )
}

@Composable
fun MaelysCrypTheme(
    nightMode: NightThemeMode = NightThemeMode.NEON,
    content: @Composable () -> Unit
) {
    val (colorScheme, neonColors) = when (nightMode) {
        NightThemeMode.LIGHT -> {
            val scheme = lightColorScheme(
                primary = LightPrimary,
                onPrimary = Color.White,
                primaryContainer = LightPrimaryContainer,
                onPrimaryContainer = LightPrimary,
                secondary = LightPrimary,
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE2E8F0),
                onSecondaryContainer = LightPrimary,
                tertiary = Color(0xFF0284C7),
                background = LightBackground,
                onBackground = LightTextPrimary,
                surface = LightSurface,
                onSurface = LightTextPrimary,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightTextSecondary,
                outline = LightBorder
            )
            val custom = CustomNeonColors(
                neonAccent = LightPrimary,
                neonSecondary = Color(0xFF0EA5E9),
                neonGlow = Color(0x330284C7),
                outgoingBubble = LightOutgoingBubble,
                incomingBubble = LightIncomingBubble,
                chatBackground = LightBackground,
                cardBackground = LightCard,
                cardBorder = LightBorder,
                securityNoticeBg = Color(0xFFE0F2FE),
                isAmberWarm = false,
                isLightMode = true,
                outgoingText = LightOutgoingText,
                outgoingTime = LightOutgoingTime
            )
            Pair(scheme, custom)
        }

        NightThemeMode.NEON -> {
            val scheme = darkColorScheme(
                primary = NeonPrimary,
                onPrimary = Color(0xFF09090B),
                primaryContainer = Color(0xFF003B3E),
                onPrimaryContainer = NeonPrimary,
                secondary = NeonSecondary,
                onSecondary = Color(0xFF09090B),
                secondaryContainer = Color(0xFF1A1C1E),
                onSecondaryContainer = NeonPrimary,
                tertiary = NeonViolet,
                background = NeonBackground,
                onBackground = NeonTextPrimary,
                surface = NeonSurface,
                onSurface = Color.White,
                surfaceVariant = NeonSurfaceVariant,
                onSurfaceVariant = NeonTextSecondary,
                outline = NeonBorder
            )
            val custom = CustomNeonColors(
                neonAccent = NeonPrimary,
                neonSecondary = NeonSecondary,
                neonGlow = NeonPrimaryGlow,
                outgoingBubble = NeonOutgoingBubble,
                incomingBubble = NeonIncomingBubble,
                chatBackground = NeonBackground,
                cardBackground = NeonCard,
                cardBorder = NeonBorder,
                securityNoticeBg = Color(0x801A1C1E),
                isAmberWarm = false,
                isLightMode = false,
                outgoingText = NeonOutgoingText,
                outgoingTime = NeonOutgoingTime
            )
            Pair(scheme, custom)
        }

        NightThemeMode.SKY_BLUE -> {
            val scheme = darkColorScheme(
                primary = SkyBluePrimary,
                onPrimary = Color(0xFF082F49),
                primaryContainer = Color(0xFF0C4A6E),
                onPrimaryContainer = SkyBluePrimary,
                secondary = SkyBlueSecondary,
                onSecondary = Color(0xFF082F49),
                secondaryContainer = Color(0xFF1E293B),
                onSecondaryContainer = SkyBlueSecondary,
                tertiary = Color(0xFF38BDF8),
                background = SkyBlueBackground,
                onBackground = SkyBlueTextPrimary,
                surface = SkyBlueSurface,
                onSurface = Color.White,
                surfaceVariant = SkyBlueSurfaceVariant,
                onSurfaceVariant = SkyBlueTextSecondary,
                outline = SkyBlueBorder
            )
            val custom = CustomNeonColors(
                neonAccent = SkyBluePrimary,
                neonSecondary = SkyBlueSecondary,
                neonGlow = SkyBluePrimaryGlow,
                outgoingBubble = SkyBlueOutgoingBubble,
                incomingBubble = SkyBlueIncomingBubble,
                chatBackground = SkyBlueBackground,
                cardBackground = SkyBlueCard,
                cardBorder = SkyBlueBorder,
                securityNoticeBg = Color(0xFF0F263E),
                isAmberWarm = false,
                isLightMode = false,
                outgoingText = SkyBlueOutgoingText,
                outgoingTime = SkyBlueOutgoingTime
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

// Backward-compatibility alias
@Composable
fun NeonCryptTheme(
    nightMode: NightThemeMode = NightThemeMode.NEON,
    content: @Composable () -> Unit
) {
    MaelysCrypTheme(nightMode = nightMode, content = content)
}


