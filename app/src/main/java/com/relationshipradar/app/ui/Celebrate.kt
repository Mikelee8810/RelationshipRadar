package com.relationshipradar.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.graphics.shapes.Morph
import kotlinx.coroutines.delay

/**
 * The moment after you log a contact: a Material shape blooms from the centre — Cookie4 → Sunny,
 * scaling up with an expressive spring — then fades. One haptic tick. ~700 ms, then [onDone].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Celebrate(onDone: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scale = remember { Animatable(0.2f) }
    val morph = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val shape = remember { Morph(MaterialShapes.Cookie4Sided, MaterialShapes.Sunny) }
    val color = MaterialTheme.colorScheme.primary
    val path = remember { Path() }
    val matrix = remember { Matrix() }

    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 380f))
    }
    LaunchedEffect(Unit) { morph.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 300f)) }
    LaunchedEffect(Unit) { delay(420); alpha.animateTo(0f, spring(stiffness = 200f)); onDone() }

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val s = size.minDimension * 0.5f * scale.value
            shape.toPath(morph.value, path)
            matrix.reset()
            matrix.translate(center.x - s / 2f, center.y - s / 2f)
            matrix.scale(s, s)
            path.transform(matrix)
            drawPath(path, color.copy(alpha = alpha.value * 0.9f))
        }
    }
}
