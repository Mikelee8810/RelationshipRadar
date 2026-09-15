package com.relationshipradar.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.relationshipradar.app.engine.RadarStatus
import com.relationshipradar.app.work.Health

/** Semantic spacing/size tokens. Screens use these, never raw numbers. */
object Radar {
    const val sp1 = 4
    const val sp2 = 8
    const val sp3 = 16
    const val sp4 = 32
    const val dotSm = 10
    const val dotMd = 12
    const val dotLg = 14
    /** Bottom padding so the FAB never covers the last row. */
    const val fabClearance = 96
}

/** Status colours are fixed (not dynamic) so the traffic-light meaning never shifts. */
object StatusColors {
    val good = Color(0xFF2E9E5B)
    val dueSoon = Color(0xFFD9A400)
    val overdue = Color(0xFFE8731A)
    val veryOverdue = Color(0xFFD03A3A)
    val quiet = Color(0xFF8A8F98)

    fun of(status: RadarStatus): Color = when (status) {
        RadarStatus.GOOD -> good
        RadarStatus.DUE_SOON -> dueSoon
        RadarStatus.OVERDUE -> overdue
        RadarStatus.VERY_OVERDUE -> veryOverdue
        RadarStatus.TRACK_ONLY, RadarStatus.PAUSED, RadarStatus.SNOOZED -> quiet
    }

    /** Health levels reuse the same three meanings: fine / look at this / broken. */
    fun of(level: Health.Level): Color = when (level) {
        Health.Level.OK -> good
        Health.Level.ATTENTION -> dueSoon
        Health.Level.OFF -> veryOverdue
    }

    fun label(status: RadarStatus): String = when (status) {
        RadarStatus.GOOD -> "Good"
        RadarStatus.DUE_SOON -> "Due soon"
        RadarStatus.OVERDUE -> "Overdue"
        RadarStatus.VERY_OVERDUE -> "Very overdue"
        RadarStatus.TRACK_ONLY -> "Tracking only"
        RadarStatus.PAUSED -> "Paused"
        RadarStatus.SNOOZED -> "Snoozed"
    }
}

private val Light = lightColorScheme(primary = Color(0xFF1F6F5A), secondary = Color(0xFF4A6572), tertiary = Color(0xFF8C5E2A))
private val Dark = darkColorScheme(primary = Color(0xFF7FD3B5), secondary = Color(0xFFA8C3CF), tertiary = Color(0xFFE2B98A))

@Composable
fun RadarTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> Dark
        else -> Light
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
