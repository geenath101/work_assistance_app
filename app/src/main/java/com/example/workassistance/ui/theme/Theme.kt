package com.example.workassistance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * AWS-inspired color palette for WorkAssistance app.
 * Matches website theme with orange accents, navy backgrounds, and light surfaces.
 */
object AwsColors {
    // Brand colors (exact match with website CSS variables)
    val awsInk = Color(0xFF16191F)          // --aws-ink (dark text)
    val awsMuted = Color(0xFF5F6B7A)        // --aws-muted (secondary text)
    val awsNavy = Color(0xFF161E2D)         // --aws-navy (dark backgrounds)
    val awsNavyStrong = Color(0xFF0F141D)   // --aws-navy-strong (very dark)
    val awsOrange = Color(0xFFFF9900)       // --aws-orange (primary accent)
    val awsOrangeStrong = Color(0xFFEC7211) // --aws-orange-strong (darker orange)
    val awsBlue = Color(0xFF0972D3)         // --aws-blue (secondary accent)
    val awsSurface = Color(0xFFF2F3F3)      // --aws-surface (light bg)
    val awsSurfaceAlt = Color(0xFFFFFFFF)   // --aws-surface-alt (white)
    val awsBorder = Color(0xFFD5DBDB)       // --aws-border (dividers/strokes)
    val awsNavText = Color(0xFF16191F)      // --aws-nav-text (nav text)
}

@Composable
fun WorkAssistanceTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // Light mode: AWS website-inspired with orange primary and light surfaces
    val lightColors = lightColorScheme(
        // Primary: AWS Orange (matches website orange accent)
        primary = AwsColors.awsOrange,
        onPrimary = AwsColors.awsSurfaceAlt,
        primaryContainer = Color(0xFFFFE8CC), // Light orange tint
        onPrimaryContainer = Color(0xFF3D2400),
        
        // Secondary: AWS Blue
        secondary = AwsColors.awsBlue,
        onSecondary = AwsColors.awsSurfaceAlt,
        secondaryContainer = Color(0xFFD6E8FF), // Light blue tint
        onSecondaryContainer = Color(0xFF001D3D),
        
        // Tertiary: AWS Muted (for additional actions)
        tertiary = AwsColors.awsMuted,
        onTertiary = AwsColors.awsSurfaceAlt,
        tertiaryContainer = Color(0xFFE8EEF8), // Light muted tint
        onTertiaryContainer = Color(0xFF1A1E28),
        
        // Surface: AWS Light Surface
        surface = AwsColors.awsSurface,
        onSurface = AwsColors.awsInk,
        surfaceVariant = AwsColors.awsBorder,
        onSurfaceVariant = AwsColors.awsMuted,
        
        // Error colors
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410E0B),
        
        // Background (same as surface for consistency)
        background = AwsColors.awsSurface,
        onBackground = AwsColors.awsInk
    )

    // Dark mode: Navy-based with orange accents (matches website's dark theme capability)
    val darkColors = darkColorScheme(
        // Primary: AWS Orange (bright enough for dark mode)
        primary = AwsColors.awsOrange,
        onPrimary = AwsColors.awsNavyStrong,
        primaryContainer = AwsColors.awsOrangeStrong,
        onPrimaryContainer = AwsColors.awsSurfaceAlt,
        
        // Secondary: AWS Blue
        secondary = AwsColors.awsBlue,
        onSecondary = AwsColors.awsNavyStrong,
        secondaryContainer = Color(0xFF004BA3),
        onSecondaryContainer = Color(0xFFD6E8FF),
        
        // Tertiary: AWS Muted
        tertiary = AwsColors.awsMuted,
        onTertiary = AwsColors.awsNavyStrong,
        tertiaryContainer = Color(0xFF3D4655),
        onTertiaryContainer = Color(0xFFE8EEF8),
        
        // Surface: AWS Navy
        surface = AwsColors.awsNavy,
        onSurface = Color(0xFFE8EAEF),
        surfaceVariant = Color(0xFF25313E),
        onSurfaceVariant = Color(0xFFC8CFD9),
        
        // Error colors
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        
        // Background (same as surface for consistency)
        background = AwsColors.awsNavy,
        onBackground = Color(0xFFE8EAEF)
    )

    val colorScheme = if (darkTheme) darkColors else lightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
