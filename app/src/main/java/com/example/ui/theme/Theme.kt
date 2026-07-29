package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.EditorTheme

val BlackThemeColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color.Black,
    surface = EditorBlackMenuBg,
    onSurface = Color.White,
    background = EditorBlackBg,
    onBackground = Color.White
)

val DarkThemeColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color.Black,
    surface = EditorDarkMenuBg,
    onSurface = Color.White,
    background = EditorDarkBg,
    onBackground = Color.White
)

val WhiteThemeColorScheme = lightColorScheme(
    primary = AccentCyan,
    onPrimary = Color.White,
    surface = EditorWhiteMenuBg,
    onSurface = Color.Black,
    background = EditorWhiteBg,
    onBackground = Color.Black
)

val LightThemeColorScheme = lightColorScheme(
    primary = AccentCyan,
    onPrimary = Color.White,
    surface = EditorLightMenuBg,
    onSurface = Color.Black,
    background = EditorLightBg,
    onBackground = Color.Black
)

val RetroThemeColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color.Black,
    surface = EditorRetroMenuBg,
    onSurface = EditorRetroText,
    background = EditorRetroBg,
    onBackground = EditorRetroText
)

@Composable
fun TextEditorTheme(
    theme: EditorTheme = EditorTheme.BLACK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        EditorTheme.BLACK -> BlackThemeColorScheme
        EditorTheme.WHITE -> WhiteThemeColorScheme
        EditorTheme.DARK -> DarkThemeColorScheme
        EditorTheme.LIGHT -> LightThemeColorScheme
        EditorTheme.RETRO -> RetroThemeColorScheme
        EditorTheme.SYSTEM -> if (isSystemInDarkTheme()) BlackThemeColorScheme else LightThemeColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
