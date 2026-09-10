package ph.gov.deped.region12.soxclmd.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ph.gov.deped.region12.soxclmd.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(vm: AppViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "home"
    val highlights by vm.highlights.collectAsState()
    val context = LocalContext.current

    val bottomItems = listOf(
        BottomItem("home", "Home", Icons.Filled.Home),
        BottomItem("resources", "Resources", Icons.Filled.GridView),
        BottomItem("activities", "Activities", Icons.Filled.Event),
        BottomItem("news", "News", Icons.AutoMirrored.Filled.Article)
    )

    Scaffold(
        topBar = {
            if (route != "home") {
                TopAppBar(
                    title = {
                        Text("SOXCLMD", fontWeight = FontWeight.Black)
                    },
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { nav.navigate("search") }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ph.gov.deped.region12.soxclmd.ui.theme.BrandPrimaryDark,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = route == item.route,
                        onClick = {
                            nav.navigate(item.route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
                NavigationBarItem(
                    selected = route == "more" || route.startsWith("area") || route == "mapeh" ||
                        route == "about" || route == "settings" || route == "calendar" ||
                        route == "learning-areas" || route == "search" || route.startsWith("article"),
                    onClick = {
                        nav.navigate("more") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        BadgedBox(badge = {
                            if (highlights.total > 0) Badge { Text("${highlights.total}") }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "More")
                        }
                    },
                    label = { Text("More") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen(vm, nav) }
            composable("resources") { ResourcesScreen(vm) }
            composable("activities") { ActivitiesScreen(vm) }
            composable("news") { NewsScreen(vm, nav) }
            composable("article/{id}") { entry ->
                NewsDetailScreen(vm, entry.arguments?.getString("id") ?: "")
            }
            composable("more") { MoreScreen(vm, nav) }
            composable("learning-areas") { LearningAreasScreen(vm, nav) }
            composable("mapeh") { MapehScreen(vm, nav) }
            composable("area/{slug}") { entry ->
                LearningAreaDetailScreen(vm, nav, entry.arguments?.getString("slug") ?: "")
            }
            composable("search") { SearchScreen(vm) }
            composable("calendar") { CalendarScreen(vm) }
            composable("about") { AboutScreen(vm) }
            composable("settings") { SettingsScreen(vm) }
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
