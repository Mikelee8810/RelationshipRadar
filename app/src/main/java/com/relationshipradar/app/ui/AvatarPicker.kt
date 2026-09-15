package com.relationshipradar.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.relationshipradar.app.ui.theme.Radar

/** Bottom sheet: contact photo · initials · 24 bundled 3D avatars. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPicker(current: String?, hasContactPhoto: Boolean, onPick: (String?) -> Unit, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = Radar.sp4.dp).padding(bottom = Radar.sp5.dp)) {
            Text("Choose a face", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(Radar.sp3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(Radar.sp2.dp)) {
                if (hasContactPhoto) FilledTonalButton(onClick = { onPick("photo") }) { Text("Contact photo") }
                FilledTonalButton(onClick = { onPick(null) }) { Text("Initials") }
            }
            Spacer(Modifier.height(Radar.sp3.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), contentPadding = PaddingValues(bottom = Radar.sp3.dp),
                horizontalArrangement = Arrangement.spacedBy(Radar.sp2.dp), verticalArrangement = Arrangement.spacedBy(Radar.sp2.dp),
                modifier = Modifier.height(420.dp),
            ) {
                items((1..BundledAvatars.COUNT).toList()) { n ->
                    val key = BundledAvatars.key(n)
                    val on = current == key
                    Box(
                        Modifier.aspectRatio(1f).clip(CircleShape)
                            .background(if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                            .then(if (on) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                            .clickable { onPick(key) },
                        contentAlignment = Alignment.Center,
                    ) { Image(painterResource(BundledAvatars.resId(ctx, n)), null, Modifier.size(52.dp)) }
                }
            }
        }
    }
}
