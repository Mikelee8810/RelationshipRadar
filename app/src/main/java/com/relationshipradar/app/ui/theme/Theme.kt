package com.relationshipradar.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relationshipradar.app.R
import com.relationshipradar.app.engine.RadarStatus
import com.relationshipradar.app.work.Health

/*
 * Relationship Radar — visual system v3, "native expressive".
 *
 * The app looks like it shipped with the phone: Material 3 Expressive, colour pulled from the
 * user's wallpaper, one geometric sans at expressive sizes, large collapsing titles, spring
 * motion, and Material's shape language for people. No metaphor, no decoration — the content
 * (people, time) is the design.
 */

object Radar {
    const val sp1 = 4
    const val sp2 = 8
    const val sp3 = 16
    const val sp4 = 24
    const val sp5 = 32
    const val fabClearance = 120
    val cardRadius = 28.dp
}

/** Status is expressed through scheme roles so it re-tints with the wallpaper. */
object StatusColors {
    @Composable fun container(status: RadarStatus): Color = when (status) {
        RadarStatus.GOOD -> MaterialTheme.colorScheme.primaryContainer
        RadarStatus.DUE_SOON -> MaterialTheme.colorScheme.tertiaryContainer
        RadarStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer
        RadarStatus.VERY_OVERDUE -> MaterialTheme.colorScheme.errorContainer
        RadarStatus.TRACK_ONLY, RadarStatus.PAUSED, RadarStatus.SNOOZED -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    @Composable fun onContainer(status: RadarStatus): Color = when (status) {
        RadarStatus.GOOD -> MaterialTheme.colorScheme.onPrimaryContainer
        RadarStatus.DUE_SOON -> MaterialTheme.colorScheme.onTertiaryContainer
        RadarStatus.OVERDUE -> MaterialTheme.colorScheme.onErrorContainer
        RadarStatus.VERY_OVERDUE -> MaterialTheme.colorScheme.onErrorContainer
        RadarStatus.TRACK_ONLY, RadarStatus.PAUSED, RadarStatus.SNOOZED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    /** Solid accent for dots and rings. */
    @Composable fun accent(status: RadarStatus): Color = when (status) {
        RadarStatus.GOOD -> MaterialTheme.colorScheme.primary
        RadarStatus.DUE_SOON -> MaterialTheme.colorScheme.tertiary
        RadarStatus.OVERDUE, RadarStatus.VERY_OVERDUE -> MaterialTheme.colorScheme.error
        RadarStatus.TRACK_ONLY, RadarStatus.PAUSED, RadarStatus.SNOOZED -> MaterialTheme.colorScheme.outline
    }

    @Composable fun of(level: Health.Level): Color = when (level) {
        Health.Level.OK -> MaterialTheme.colorScheme.primary
        Health.Level.ATTENTION -> MaterialTheme.colorScheme.tertiary
        Health.Level.OFF -> MaterialTheme.colorScheme.error
    }

    fun label(status: RadarStatus): String = when (status) {
        RadarStatus.GOOD -> "On track"
        RadarStatus.DUE_SOON -> "Due soon"
        RadarStatus.OVERDUE -> "Overdue"
        RadarStatus.VERY_OVERDUE -> "Way overdue"
        RadarStatus.TRACK_ONLY -> "No reminders"
        RadarStatus.PAUSED -> "Paused"
        RadarStatus.SNOOZED -> "Snoozed"
    }
}

// ---- Type: one geometric sans, expressive sizes, tight display tracking ---------------------

val Sans = FontFamily(
    Font(R.font.manrope, FontWeight.Normal, variationSettings = FontVariation.Settings(FontWeight.Normal, FontStyle.Normal)),
    Font(R.font.manrope, FontWeight.Medium, variationSettings = FontVariation.Settings(FontWeight.Medium, FontStyle.Normal)),
    Font(R.font.manrope, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontWeight.SemiBold, FontStyle.Normal)),
    Font(R.font.manrope, FontWeight.Bold, variationSettings = FontVariation.Settings(FontWeight.Bold, FontStyle.Normal)),
    Font(R.font.manrope, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontWeight.ExtraBold, FontStyle.Normal)),
)

private val Type = Typography(
    displayLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 64.sp, lineHeight = 64.sp, letterSpacing = (-2.5).sp),
    displayMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-1.5).sp),
    displaySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.8).sp),
    headlineMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.4.sp),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RadarTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val scheme: ColorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> darkColorScheme()
        else -> expressiveLightColorScheme()
    }
    MaterialExpressiveTheme(
        colorScheme = scheme,
        motionScheme = MotionScheme.expressive(),
        typography = Type,
        content = content,
    )
}
