package com.relationshipradar.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.relationshipradar.app.engine.RadarStatus
import com.relationshipradar.app.ui.Avatar
import com.relationshipradar.app.ui.Hint
import com.relationshipradar.app.ui.RadarViewModel
import com.relationshipradar.app.ui.Sheet
import com.relationshipradar.app.ui.SignalDot
import com.relationshipradar.app.ui.theme.Radar
import com.relationshipradar.app.ui.theme.StatusColors
import com.relationshipradar.app.work.Notifications
import kotlinx.coroutines.delay

/** First run. Three pages, each with its own living illustration, one ask each, all skippable. */
@Composable
fun OnboardingScreen(vm: RadarViewModel, onDone: () -> Unit) {
    val ctx = LocalContext.current
    var step by remember { mutableStateOf(0) }
    var syncMsg by remember { mutableStateOf<String?>(null) }
    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) vm.syncContacts { syncMsg = it; step = 2 } else step = 2
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.finishOnboarding(); onDone() }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(Radar.sp4.dp)) {
        Spacer(Modifier.height(Radar.sp4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                Box(Modifier.size(width = if (i == step) 24.dp else 8.dp, height = 8.dp).background(if (i <= step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape))
            }
        }
        AnimatedContent(
            step, label = "onboarding", modifier = Modifier.weight(1f),
            transitionSpec = { (slideInHorizontally { it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 4 } + fadeOut()) },
        ) { s ->
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    when (s) { 0 -> ShapeBloom(); 1 -> PeopleCluster(); else -> MockNudge() }
                }
                Spacer(Modifier.height(Radar.sp4.dp))
                when (s) {
                    0 -> {
                        Text("Stay close to the people who matter.", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(Radar.sp2.dp))
                        Text("Relationship Radar keeps track of one thing: when you last reached out. Calls, texts, chats you send. Not what was said, and never what they did.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(Radar.sp2.dp))
                        Hint("Everything stays on this phone. No account.")
                    }
                    1 -> {
                        Text("Start with your contacts.", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(Radar.sp2.dp))
                        Text("Everyone saved on your phone joins so history builds on its own. Reminders stay off until you turn them on for someone.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> {
                        Text("One quiet nudge a day.", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(Radar.sp2.dp))
                        Text("A morning roundup of who's due. Your closest people can get their own alert. After a week of nudges it goes quiet.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        syncMsg?.let { Spacer(Modifier.height(Radar.sp1.dp)); Hint(it) }
                    }
                }
            }
        }
        Spacer(Modifier.height(Radar.sp4.dp))
        when (step) {
            0 -> Button(onClick = { step = 1 }, Modifier.fillMaxWidth().height(56.dp)) { Text("Get started") }
            1 -> {
                Button(onClick = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }, Modifier.fillMaxWidth().height(56.dp)) { Text("Import contacts") }
                TextButton(onClick = { step = 2 }, Modifier.fillMaxWidth()) { Text("Skip for now") }
            }
            else -> if (Notifications.canPost(ctx)) {
                Button(onClick = { vm.finishOnboarding(); onDone() }, Modifier.fillMaxWidth().height(56.dp)) { Text("Done") }
            } else {
                Button(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }, Modifier.fillMaxWidth().height(56.dp)) { Text("Allow notifications") }
                TextButton(onClick = { vm.finishOnboarding(); onDone() }, Modifier.fillMaxWidth()) { Text("Not now") }
            }
        }
    }
}

/** Page 1: a single large shape that keeps morphing between Material's expressive forms. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShapeBloom() {
    val forms: List<RoundedPolygon> = remember { listOf(MaterialShapes.Cookie12Sided, MaterialShapes.Sunny, MaterialShapes.Clover8Leaf, MaterialShapes.Flower, MaterialShapes.SoftBurst) }
    var idx by remember { mutableStateOf(0) }
    val t = remember { Animatable(0f) }
    val morph = remember(idx) { Morph(forms[idx % forms.size], forms[(idx + 1) % forms.size]) }
    LaunchedEffect(idx) {
        t.snapTo(0f)
        delay(900)
        t.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 60f))
        idx++
    }
    val a = MaterialTheme.colorScheme.primaryContainer
    val b = MaterialTheme.colorScheme.primary
    val path = remember { Path() }
    val m = remember { Matrix() }
    Box(Modifier.fillMaxWidth(0.72f).aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            morph.toPath(t.value, path); m.reset(); m.scale(size.width, size.height); path.transform(m)
            drawPath(path, a)
        }
        Canvas(Modifier.fillMaxSize(0.42f)) {
            morph.toPath(1f - t.value, path); m.reset(); m.scale(size.width, size.height); path.transform(m)
            drawPath(path, b)
        }
    }
}

/** Page 2: a loose cluster of shaped avatars in every status colour. */
@Composable
private fun PeopleCluster() {
    val people = listOf(
        Triple(11L, "Sam", RadarStatus.GOOD), Triple(12L, "Dad", RadarStatus.DUE_SOON), Triple(13L, "Ana", RadarStatus.GOOD),
        Triple(14L, "Jo", RadarStatus.OVERDUE), Triple(15L, "Lee", RadarStatus.GOOD), Triple(16L, "Mia", RadarStatus.TRACK_ONLY),
    )
    androidx.compose.foundation.layout.FlowRow(
        Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(14.dp), maxItemsInEachRow = 3,
    ) { people.forEach { (id, n, s) -> Avatar(id, n, s, 88) } }
}

/** Page 3: what a nudge looks like, rendered in-app so the ask isn't abstract. */
@Composable
private fun MockNudge() {
    Column(Modifier.fillMaxWidth(0.94f), verticalArrangement = Arrangement.spacedBy(Radar.sp2.dp)) {
        Sheet(padding = Radar.sp3) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SignalDot(MaterialTheme.colorScheme.primary, 8)
                Text("Relationship Radar · 9:00 AM", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(6.dp))
            Text("3 people to reach out to", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            for ((n, d, s) in listOf(Triple("Sarah", "2 wk ago", RadarStatus.OVERDUE), Triple("Dad", "9 days ago", RadarStatus.DUE_SOON), Triple("Priya", "5 wk ago", RadarStatus.VERY_OVERDUE))) {
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    SignalDot(StatusColors.accent(s), 8)
                    Spacer(Modifier.size(10.dp))
                    Text(n, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(d, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Hint("Then nothing until tomorrow.", Modifier.padding(start = 4.dp))
    }
}

