package com.relationshipradar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.relationshipradar.app.ui.EmptyState
import com.relationshipradar.app.ui.RadarViewModel

/** "What is this person to you?" — asked once here, one soft re-ask, then we stop. */
@Composable
fun NewPeopleScreen(vm: RadarViewModel) {
    val people by vm.uncategorized.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()

    if (people.isEmpty()) {
        EmptyState("Everyone's sorted", "New contacts will show up here so you can say who they are to you.")
        return
    }
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(people, key = { it.person.id }) { pwc ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(pwc.person.displayName, style = MaterialTheme.typography.titleMedium)
                    Text("What is this person to you?", style = MaterialTheme.typography.bodySmall)
                    FlowChips(categories.map { it.name }, -1) { i -> vm.setCategory(pwc.person.id, categories[i].id) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { vm.updatePerson(pwc.person.copy(categorizationPromptCount = 2)) }) { Text("Track only") }
                        TextButton(onClick = { vm.dismissPrompt(pwc.person.id) }) { Text("Later") }
                    }
                }
            }
        }
    }
}
