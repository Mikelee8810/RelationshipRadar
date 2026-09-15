package com.relationshipradar.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.relationshipradar.app.ui.Hint
import com.relationshipradar.app.ui.RadarViewModel
import com.relationshipradar.app.ui.theme.Radar
import com.relationshipradar.app.work.Notifications

/**
 * First run. Three screens, one ask each, skippable. Nothing here can't be redone in Settings.
 */
@Composable
fun OnboardingScreen(vm: RadarViewModel, onDone: () -> Unit) {
    val ctx = LocalContext.current
    var step by remember { mutableStateOf(0) }
    var syncMsg by remember { mutableStateOf<String?>(null) }

    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) vm.syncContacts { syncMsg = it; step = 2 } else step = 2
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { finish(vm, onDone) }

    Column(Modifier.fillMaxSize().padding(Radar.sp4.dp), verticalArrangement = Arrangement.Center) {
        when (step) {
            0 -> {
                Text("A quiet radar for the people you care about", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.padding(Radar.sp2.dp))
                Text("It watches one thing: have you reached out lately? Calls, texts, chats you send. Not what they said. Not who liked what.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.padding(Radar.sp2.dp))
                Hint("Everything stays on this phone. No account, no cloud.")
                Spacer(Modifier.weight(1f))
                Button(onClick = { step = 1 }, Modifier.fillMaxWidth()) { Text("Let's set it up") }
            }
            1 -> {
                Text("Start with your contacts", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.padding(Radar.sp2.dp))
                Text("Every saved contact gets tracked so history builds up on its own. Reminders stay off until you turn them on per person.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                Button(onClick = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }, Modifier.fillMaxWidth()) { Text("Import contacts") }
                TextButton(onClick = { step = 2 }, Modifier.fillMaxWidth()) { Text("Skip for now") }
            }
            else -> {
                Text("One notification a day", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.padding(Radar.sp2.dp))
                Text("A morning roundup of who's due. High-priority people can get their own alert. After a week of nudges the app goes quiet and just shows the colour.", style = MaterialTheme.typography.bodyLarge)
                syncMsg?.let { Spacer(Modifier.padding(Radar.sp1.dp)); Hint(it) }
                Spacer(Modifier.weight(1f))
                if (Notifications.canPost(ctx)) {
                    Button(onClick = { finish(vm, onDone) }, Modifier.fillMaxWidth()) { Text("Done") }
                } else {
                    Button(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }, Modifier.fillMaxWidth()) { Text("Allow notifications") }
                    OutlinedButton(onClick = { finish(vm, onDone) }, Modifier.fillMaxWidth()) { Text("Not now") }
                }
            }
        }
    }
}

private fun finish(vm: RadarViewModel, onDone: () -> Unit) { vm.finishOnboarding(); onDone() }
