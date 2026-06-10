package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PremiumDarkColorScheme = darkColorScheme(
    primary = ElectricSky,
    secondary = MintFlame,
    tertiary = IndigoDusk,
    background = Slate900,
    surface = Slate800,
    onPrimary = Slate900,
    onSecondary = Slate900,
    onTertiary = Slate900,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Slate700,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // We enforce our premium dark color scheme across all devices to guarantee
    // a polished, executive startup incubator design system that avoids default bland elements.
    MaterialTheme(
        colorScheme = PremiumDarkColorScheme,
        typography = Typography,
        content = content
    )
}
