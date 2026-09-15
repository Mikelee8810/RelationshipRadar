package com.relationshipradar.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.relationshipradar.app.ui.screens.CategoriesScreen
import com.relationshipradar.app.ui.screens.ConnectorsScreen
import com.relationshipradar.app.ui.screens.HealthScreen
import com.relationshipradar.app.ui.screens.WhoIsThisScreen
import com.relationshipradar.app.ui.screens.DashboardScreen
import com.relationshipradar.app.ui.screens.NewPeopleScreen
import com.relationshipradar.app.ui.screens.OnboardingScreen
import com.relationshipradar.app.ui.screens.PersonScreen
import com.relationshipradar.app.ui.screens.QuickLogScreen
import com.relationshipradar.app.ui.screens.SettingsScreen

object Routes {
    const val RADAR = "radar"
    const val NEW_PEOPLE = "new_people"
    const val SETTINGS = "settings"
    const val CATEGORIES = "categories"
    const val CONNECTORS = "connectors"
    const val WHO = "who"
    const val HEALTH = "health"
    const val PERSON = "person/{id}"
    const val LOG = "log?personId={personId}"
    fun person(id: Long) = "person/$id"
    fun log(personId: Long? = null) = if (personId == null) "log" else "log?personId=$personId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarNavHost(vm: RadarViewModel, openPersonId: Long?, openLog: Boolean = false) {
    val nav = rememberNavController()
    val settings by vm.appSettings.collectAsStateWithLifecycle()
    var onboardingSeen by remember { mutableStateOf(false) }
    if (!settings.onboardingDone && !onboardingSeen) {
        OnboardingScreen(vm) { onboardingSeen = true }
        return
    }
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: Routes.RADAR
    var showAddPerson by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }

    LaunchedEffect(openPersonId, openLog) {
        openPersonId?.let { nav.navigate(Routes.person(it)) }
        if (openLog) nav.navigate(Routes.log())
    }

    val isTop = route in listOf(Routes.RADAR, Routes.NEW_PEOPLE, Routes.SETTINGS)
    val title = when {
        route == Routes.RADAR -> "Radar"
        route == Routes.NEW_PEOPLE -> "New people"
        route == Routes.SETTINGS -> "Settings"
        route == Routes.CATEGORIES -> "Categories"
        route == Routes.CONNECTORS -> "Sources"
        route == Routes.WHO -> "Who is this?"
        route == Routes.HEALTH -> "Health"
        route.startsWith("log") -> "Log contact"
        else -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { if (!isTop) IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    if (route == Routes.RADAR) IconButton(onClick = { showAddPerson = true }) { Icon(Icons.Default.PersonAdd, "Add person") }
                    if (route == Routes.CATEGORIES) IconButton(onClick = { showAddCategory = true }) { Icon(Icons.Default.Add, "Add category") }
                },
            )
        },
        bottomBar = {
            if (isTop) NavigationBar {
                NavigationBarItem(route == Routes.RADAR, { nav.navigate(Routes.RADAR) { popUpTo(Routes.RADAR); launchSingleTop = true } }, { Icon(Icons.Default.Radar, null) }, label = { Text("Radar") })
                NavigationBarItem(route == Routes.NEW_PEOPLE, { nav.navigate(Routes.NEW_PEOPLE) { popUpTo(Routes.RADAR); launchSingleTop = true } }, { Icon(Icons.Default.Category, null) }, label = { Text("New people") })
                NavigationBarItem(route == Routes.SETTINGS, { nav.navigate(Routes.SETTINGS) { popUpTo(Routes.RADAR); launchSingleTop = true } }, { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") })
            }
        },
        floatingActionButton = {
            // Thumb-reach primary action: the quick "I saw someone" log.
            if (route == Routes.RADAR) ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.log()) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("I saw someone") },
            )
        },
    ) { padding ->
        NavHost(nav, startDestination = Routes.RADAR, modifier = Modifier.padding(padding)) {
            composable(Routes.RADAR) { DashboardScreen(vm, { nav.navigate(Routes.person(it)) }, { nav.navigate(Routes.NEW_PEOPLE) }, { nav.navigate(Routes.WHO) }) }
            composable(Routes.NEW_PEOPLE) { NewPeopleScreen(vm) }
            composable(Routes.SETTINGS) { SettingsScreen(vm, onOpenCategories = { nav.navigate(Routes.CATEGORIES) }, onOpenConnectors = { nav.navigate(Routes.CONNECTORS) }, onOpenWho = { nav.navigate(Routes.WHO) }, onOpenHealth = { nav.navigate(Routes.HEALTH) }) }
            composable(Routes.HEALTH) { HealthScreen(vm) }
            composable(Routes.CONNECTORS) { ConnectorsScreen(vm) }
            composable(Routes.WHO) { WhoIsThisScreen(vm) }
            composable(Routes.CATEGORIES) { CategoriesScreen(vm, showAddCategory) { showAddCategory = false } }
            composable(Routes.PERSON, arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                val id = e.arguments!!.getLong("id")
                PersonScreen(vm, id, onLog = { nav.navigate(Routes.log(id)) }, onBack = { nav.popBackStack() })
            }
            composable(Routes.LOG, arguments = listOf(navArgument("personId") { type = NavType.LongType; defaultValue = -1L })) { e ->
                val pid = e.arguments!!.getLong("personId").takeIf { it > 0 }
                QuickLogScreen(vm, pid) { nav.popBackStack() }
            }
        }
    }

    if (showAddPerson) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddPerson = false },
            title = { Text("Add a person") },
            text = { OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true) },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    vm.createPerson(name, null) { id -> nav.navigate(Routes.person(id)) }
                    showAddPerson = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddPerson = false }) { Text("Cancel") } },
        )
    }
}
