package com.example.kotlinaktiengame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kotlinaktiengame.ui.GameViewModel
import com.example.kotlinaktiengame.ui.screens.DepotScreen
import com.example.kotlinaktiengame.ui.screens.HomeScreen
import com.example.kotlinaktiengame.ui.screens.MarketScreen
import com.example.kotlinaktiengame.ui.theme.KotlinAktienGameTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinAktienGameTheme {
                AktienGameApp()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String) {
    data object Home : Screen("home", "Home")
    data object Markt : Screen("markt", "Markt")
    data object Depot : Screen("depot", "Depot")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AktienGameApp(viewModel: GameViewModel = viewModel()) {
    val navController = rememberNavController()
    // Lifecycle-aware collection: pausiert wenn Screen nicht sichtbar -> spart CPU im Emulator
    val guthaben by viewModel.guthaben.collectAsStateWithLifecycle()
    val depot by viewModel.depot.collectAsStateWithLifecycle()
    val aktien by viewModel.aktien.collectAsStateWithLifecycle()
    val depotWert by viewModel.depotGesamtwert.collectAsStateWithLifecycle()
    val gewinn by viewModel.gesamtGewinnVerlust.collectAsStateWithLifecycle()
    val vermoegen by viewModel.gesamtVermoegen.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(message) {
        message?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessage()
            }
        }
    }

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kotlin Aktien Game", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screen.Home.route,
                    onClick = { navController.navigate(Screen.Home.route) { launchSingleTop = true; popUpTo(Screen.Home.route) { inclusive = false } } },
                    icon = { Text("⌂", fontSize = 20.sp) },
                    label = { Text("Start") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Markt.route,
                    onClick = { navController.navigate(Screen.Markt.route) { launchSingleTop = true } },
                    icon = { Text("▦", fontSize = 20.sp) },
                    label = { Text("Markt") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Depot.route,
                    onClick = { navController.navigate(Screen.Depot.route) { launchSingleTop = true } },
                    icon = {
                        BadgedBox(badge = {
                            if (depot.isNotEmpty()) Badge { Text("${depot.size}") }
                        }) {
                            Text("⬢", fontSize = 20.sp)
                        }
                    },
                    label = { Text("Depot") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                // remember callbacks -> stabile Lambdas, weniger Recomposition
                val onMarkt = remember(navController) { { navController.navigate(Screen.Markt.route) } }
                val onDepot = remember(navController) { { navController.navigate(Screen.Depot.route) } }
                val onReset = remember(viewModel) { { viewModel.resetSpiel() } }
                HomeScreen(
                    guthaben = guthaben,
                    depotWert = depotWert,
                    gesamtGewinn = gewinn,
                    gesamtVermoegen = vermoegen,
                    onNavigateMarkt = onMarkt,
                    onNavigateDepot = onDepot,
                    onReset = onReset
                )
            }
            composable(Screen.Markt.route) {
                val onKaufen = remember(viewModel) { { aktie: com.example.kotlinaktiengame.model.Aktie, anzahl: Int -> viewModel.kaufeAktie(aktie, anzahl) } }
                MarketScreen(
                    aktien = aktien,
                    guthaben = guthaben,
                    onKaufen = onKaufen
                )
            }
            composable(Screen.Depot.route) {
                val onVerkaufen = remember(viewModel) { { kuerzel: String, anzahl: Int -> viewModel.verkaufeAktie(kuerzel, anzahl) } }
                DepotScreen(
                    depot = depot,
                    onVerkaufen = onVerkaufen
                )
            }
        }
    }
}
