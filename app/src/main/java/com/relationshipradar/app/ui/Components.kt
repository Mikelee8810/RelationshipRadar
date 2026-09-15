package com.relationshipradar.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.relationshipradar.app.engine.RadarStatus
import com.relationshipradar.app.ui.theme.Radar
import com.relationshipradar.app.ui.theme.StatusColors
import com.relationshipradar.app.work.Health

// ---- People as Material shapes -------------------------------------------------------------

/**
 * Each person gets a stable expressive shape (by id) and the shape *relaxes* with their state:
 * on-track people are full and round, overdue people go angular. Morphs are animated.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object PersonShapes {
    val palette: List<RoundedPolygon> get() = listOf(
        MaterialShapes.Cookie12Sided, MaterialShapes.Clover8Leaf, MaterialShapes.SoftBurst, MaterialShapes.Sunny,
        MaterialShapes.Cookie9Sided, MaterialShapes.Flower, MaterialShapes.Puffy, MaterialShapes.Cookie7Sided,
    )
    fun forId(id: Long): RoundedPolygon = palette[((id % palette.size) + palette.size).toInt() % palette.size]

    /** The "cooled" version of any shape: fewer, sharper features. */
    val cold: RoundedPolygon get() = MaterialShapes.Cookie4Sided
    val ash: RoundedPolygon get() = MaterialShapes.Slanted

    fun stress(status: RadarStatus): Float = when (status) {
        RadarStatus.GOOD, RadarStatus.TRACK_ONLY, RadarStatus.PAUSED, RadarStatus.SNOOZED -> 0f
        RadarStatus.DUE_SOON -> 0.35f
        RadarStatus.OVERDUE -> 0.7f
        RadarStatus.VERY_OVERDUE -> 1f
    }
}

/**
 * A person's face. Photo if they have one, else their chosen 3D avatar, else initials — always
 * clipped to their expressive shape, which morphs angular as they go overdue. Overdue photos
 * lose saturation so the list reads at a glance even without colour.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Face(id: Long, name: String, status: RadarStatus, avatar: String?, lookupKey: String?, size: Int = 56, modifier: Modifier = Modifier, hiRes: Boolean = size >= 96) {
    val base = remember(id) { PersonShapes.forId(id) }
    val morph = remember(base) { Morph(base, PersonShapes.cold) }
    val progress by animateFloatAsState(PersonShapes.stress(status), label = "shape")
    val shape = remember(progress) { MorphShape(morph, progress) }
    val photo = if (avatar == "photo") ContactPhotos.remember(lookupKey, hiRes) else null
    val bundled = BundledAvatars.parse(avatar)
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val sat by animateFloatAsState(1f - 0.8f * PersonShapes.stress(status), label = "sat")

    if (photo != null) {
        androidx.compose.foundation.Image(
            photo, contentDescription = name,
            modifier = modifier.size(size.dp).clip(shape),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(sat) }),
        )
    } else if (bundled != null) {
        Box(modifier.size(size.dp).clip(shape).background(StatusColors.container(status)), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Image(
                androidx.compose.ui.res.painterResource(BundledAvatars.resId(ctx, bundled)), contentDescription = name,
                modifier = Modifier.size((size * 0.72f).dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(sat) }),
            )
        }
    } else {
        Avatar(id, name, status, size, modifier)
    }
}

/** A Compose Shape backed by a Morph at a fixed progress. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MorphShape(private val morph: Morph, private val progress: Float) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: androidx.compose.ui.unit.LayoutDirection, density: androidx.compose.ui.unit.Density): androidx.compose.ui.graphics.Outline {
        val p = Path()
        morph.toPath(progress, p)
        val m = Matrix(); m.scale(size.width, size.height); p.transform(m)
        return androidx.compose.ui.graphics.Outline.Generic(p)
    }
}

/** Initials inside a morphing Material shape. Colour and shape both carry the status. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Avatar(id: Long, name: String, status: RadarStatus, size: Int = 56, modifier: Modifier = Modifier) {
    val initials = name.split(' ', '-').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercaseChar().toString() }.ifEmpty { "?" }
    val base = remember(id) { PersonShapes.forId(id) }
    val morph = remember(base) { Morph(base, PersonShapes.cold) }
    val target = PersonShapes.stress(status)
    val progress by animateFloatAsState(target, label = "shape")
    val fill by animateColorAsState(StatusColors.container(status), label = "fill")
    val ink = StatusColors.onContainer(status)
    val path = remember { Path() }
    val matrix = remember { Matrix() }

    Box(modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            morph.toPath(progress, path)
            matrix.reset()
            matrix.scale(this.size.width, this.size.height)
            path.transform(matrix)
            drawPath(path, fill)
        }
        Text(initials, style = if (size >= 72) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium, color = ink)
    }
}

// ---- Small signals -------------------------------------------------------------------------

@Composable
fun SignalDot(color: Color, size: Int = 10) = Box(Modifier.size(size.dp).background(color, CircleShape))

@Composable fun HealthDot(level: Health.Level) = SignalDot(StatusColors.of(level), 12)

@Composable
fun StatusChip(status: RadarStatus) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SignalDot(StatusColors.accent(status), 8)
        Text(StatusColors.label(status), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---- Containers ------------------------------------------------------------------------------

/** The one grouped container. surfaceContainer on surface, 28dp corners — Material's own recipe. */
@Composable
fun Sheet(modifier: Modifier = Modifier, padding: Int = 0, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) { Column(Modifier.padding(padding.dp), content = content) }
}

@Composable
fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = { Switch(checked, onChange) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
fun LinkRow(title: String, sub: String? = null, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = sub?.let { { Text(it) } },
        trailingContent = { Text("→", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
fun EmptyState(title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(Radar.sp5.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Radar.sp2.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = Radar.sp4.dp, vertical = Radar.sp2.dp),
    )
}

@Composable
fun Hint(text: String, modifier: Modifier = Modifier) =
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier)

@Composable
fun Hairline(modifier: Modifier = Modifier) = HorizontalDivider(modifier, color = MaterialTheme.colorScheme.outlineVariant)
