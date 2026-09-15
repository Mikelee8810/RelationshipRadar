package com.relationshipradar.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.relationshipradar.app.ui.screens.CategoriesScreen
import com.relationshipradar.app.ui.screens.ConnectorsScreen
import com.relationshipradar.app.ui.screens.DashboardScreen
import com.relationshipradar.app.ui.screens.HealthScreen
import com.relationshipradar.app.ui.screens.NewPeopleScreen
import com.relationshipradar.app.ui.screens.OnboardingScreen
import com.relationshipradar.app.ui.screens.PersonScreen
import com.relationshipradar.app.ui.screens.QuickLogScreen
import com.relationshipradar.app.ui.screens.SettingsScreen
import com.relationshipradar.app.ui.screens.WhoIsThisScreen

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val isPerson = route == Routes.PERSON
    val title = when {
        route == Routes.RADAR -> "Circle"
        route == Routes.NEW_PEOPLE -> "New people"
        route == Routes.SETTINGS -> "Settings"
        route == Routes.CATEGORIES -> "Categories"
        route == Routes.CONNECTORS -> "Sources"
        route == Routes.WHO -> "Who is this?"
        route == Routes.HEALTH -> "Health"
        route.startsWith("log") -> "Log a moment"
        else -> ""
    }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            if (!isPerson) LargeTopAppBar(
                title = { Text(title) },
                scrollBehavior = scroll,
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface, scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer),
                navigationIcon = { if (!isTop) IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
                actions = {
                    if (route == Routes.RADAR) IconButton(onClick = { showAddPerson = true }) { Icon(Icons.Outlined.PersonAddAlt, "Add person") }
                    if (route == Routes.CATEGORIES) IconButton(onClick = { showAddCategory = true }) { Icon(Icons.Rounded.Add, "Add category") }
                },
            )
        },
        bottomBar = {
            if (isTop) ShortNavigationBar {
                ShortNavigationBarItem(route == Routes.RADAR, { nav.navigate(Routes.RADAR) { popUpTo(Routes.RADAR); launchSingleTop = true } }, { Icon(if (route == Routes.RADAR) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder, null) }, label = { Text("Circle") })
                ShortNavigationBarItem(route == Routes.NEW_PEOPLE, { nav.navigate(Routes.NEW_PEOPLE) { popUpTo(Routes.RADAR); launchSingleTop = true } }, { Icon(if (route == Routes.NEW_PEOPLE) Icons.Rounded.Groups else Icons.Outlined.Groups, null) }, label = { Text("New people") })
                ShortNavigationBarItem(route == Routes.SETTINGS, { nav.navigate(Routes.SETTINGS) { popUpTo(Routes.RADAR); launchSingleTop = true } }, { Icon(if (route == Routes.SETTINGS) Icons.Rounded.Tune else Icons.Outlined.Tune, null) }, label = { Text("Settings") })
            }
        },
        floatingActionButton = {
            if (route == Routes.RADAR) MediumExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.log()) },
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("I saw someone") },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(nav, startDestination = Routes.RADAR) {
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
