package com.relationshipradar.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.relationshipradar.app.data.db.Category
import com.relationshipradar.app.data.db.ReminderBehavior
import com.relationshipradar.app.ui.RadarViewModel

@Composable
fun CategoriesScreen(vm: RadarViewModel, showAdd: Boolean, onAddConsumed: () -> Unit) {
    val categories by vm.categories.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Category?>(null) }

    LazyColumn {
        items(categories, key = { it.id }) { c ->
            ListItem(
                headlineContent = { Text(c.name) },
                supportingContent = {
                    Text(
                        (c.defaultIntervalDays?.let { "Every $it days" } ?: "Custom per person") +
                            " · " + if (c.reminderBehavior == ReminderBehavior.INDIVIDUAL) "own alert" else "daily roundup",
                    )
                },
                modifier = Modifier.clickable { editing = c },
            )
        }
    }

    if (showAdd) editing = Category(name = "", defaultIntervalDays = 30).also { onAddConsumed() }

    editing?.let { c ->
        var name by remember(c) { mutableStateOf(c.name) }
        var days by remember(c) { mutableStateOf(c.defaultIntervalDays?.toString() ?: "") }
        var behavior by remember(c) { mutableStateOf(c.reminderBehavior) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (c.id == 0L) "New category" else "Edit category") },
            text = {
                Column {
                    OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, enabled = !c.builtIn)
                    OutlinedTextField(days, { days = it.filter(Char::isDigit).take(4) }, label = { Text("Default: every N days (blank = custom)") }, singleLine = true, modifier = Modifier.padding(top = 8.dp))
                    Row(Modifier.padding(top = 8.dp)) {
                        FilterChip(behavior == ReminderBehavior.INDIVIDUAL, { behavior = ReminderBehavior.INDIVIDUAL }, { Text("Own alert") })
                        FilterChip(behavior == ReminderBehavior.ROUNDUP, { behavior = ReminderBehavior.ROUNDUP }, { Text("Roundup") }, Modifier.padding(start = 8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    val updated = c.copy(name = name.trim(), defaultIntervalDays = days.toIntOrNull(), reminderBehavior = behavior, sortOrder = if (c.id == 0L) 100 else c.sortOrder)
                    if (c.id == 0L) vm.addCategory(updated) else vm.updateCategory(updated)
                    editing = null
                }) { Text("Save") }
            },
            dismissButton = {
                Row {
                    if (!c.builtIn && c.id != 0L) TextButton(onClick = { vm.deleteCategory(c); editing = null }) { Text("Delete") }
                    TextButton(onClick = { editing = null }) { Text("Cancel") }
                }
            },
        )
    }
}
