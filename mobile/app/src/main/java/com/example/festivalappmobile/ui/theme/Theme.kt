package com.example.festivalappmobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RosePrimaryDark,
    onPrimary = OnRosePrimaryDark,
    primaryContainer = RosePrimaryContainerDark,
    onPrimaryContainer = OnRosePrimaryContainerDark,
    secondary = SageSecondaryDark,
    onSecondary = OnSageSecondaryDark,
    secondaryContainer = SageSecondaryContainerDark,
    onSecondaryContainer = OnSageSecondaryContainerDark,
    tertiary = GoldTertiaryDark,
    onTertiary = OnGoldTertiaryDark,
    tertiaryContainer = GoldTertiaryContainerDark,
    onTertiaryContainer = OnGoldTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = RosePrimaryLight,
    onPrimary = OnRosePrimaryLight,
    primaryContainer = RosePrimaryContainerLight,
    onPrimaryContainer = OnRosePrimaryContainerLight,
    secondary = SageSecondaryLight,
    onSecondary = OnSageSecondaryLight,
    secondaryContainer = SageSecondaryContainerLight,
    onSecondaryContainer = OnSageSecondaryContainerLight,
    tertiary = GoldTertiaryLight,
    onTertiary = OnGoldTertiaryLight,
    tertiaryContainer = GoldTertiaryContainerLight,
    onTertiaryContainer = OnGoldTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun FestivalAppMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}