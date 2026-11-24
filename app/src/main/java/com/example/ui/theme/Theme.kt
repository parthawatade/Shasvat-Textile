package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ShasvatLightColorScheme = lightColorScheme(
    primary = DarkEspresso,
    onPrimary = PureWhite,
    primaryContainer = SoftSand,
    onPrimaryContainer = DeepWalnut,
    secondary = SubtleGold,
    onSecondary = DeepWalnut,
    secondaryContainer = PaleGoldContainer,
    onSecondaryContainer = DeepGoldText,
    tertiary = TerracottaRed,
    onTertiary = PureWhite,
    background = WarmIvory,
    onBackground = DeepWalnut,
    surface = PureWhite,
    onSurface = DeepWalnut,
    surfaceVariant = WarmBeige,
    onSurfaceVariant = MutedTaupe,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = WarmIvory,
    surfaceContainer = WarmBeige,
    surfaceContainerHigh = SoftSand,
    surfaceContainerHighest = DeepSand,
    outline = OutlineWarm,
    outlineVariant = SoftSand,
    error = ErrorCrimson,
    onError = PureWhite,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorCrimson
)

private val ShasvatDarkColorScheme = darkColorScheme(
    primary = BrightGold,
    onPrimary = DeepWalnut,
    primaryContainer = WarmCocoa,
    onPrimaryContainer = WarmBeige,
    secondary = SubtleGold,
    onSecondary = DeepWalnut,
    secondaryContainer = DarkSurfaceHigh,
    onSecondaryContainer = PaleGoldContainer,
    tertiary = TerracottaRed,
    onTertiary = PureWhite,
    background = DarkSurface,
    onBackground = LightCreamText,
    surface = DarkSurfaceContainer,
    onSurface = LightCreamText,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = WarmSandText,
    surfaceContainerLowest = DarkSurface,
    surfaceContainerLow = DarkSurfaceContainer,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = WarmCocoa,
    outline = WarmCocoa,
    outlineVariant = DarkSurfaceHigh
)

val ShasvatShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun ShasvatTextilesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ShasvatDarkColorScheme else ShasvatLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ShasvatTypography,
        shapes = ShasvatShapes,
        content = content
    )
}
