package com.example.kotlinaktiengame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    val guthaben by viewModel.guthaben.collectAsState()
    val depot by viewModel.depot.collectAsState()
    val aktien by viewModel.aktien.collectAsState()
    val depotWert by viewModel.depotGesamtwert.collectAsState()
    val gewinn by viewModel.gesamtGewinnVerlust.collectAsState()
    val vermoegen by viewModel.gesamtVermoegen.collectAsState()
    val message by viewModel.message.collectAsState()

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
                title = { Text("Kotlin Aktien Game") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screen.Home.route,
                    onClick = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Start") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Markt.route,
                    onClick = { navController.navigate(Screen.Markt.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Filled.Store, contentDescription = null) },
                    label = { Text("Markt") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Depot.route,
                    onClick = { navController.navigate(Screen.Depot.route) { launchSingleTop = true } },
                    icon = {
                        BadgedBox(badge = {
                            if (depot.isNotEmpty()) Badge { Text("${depot.size}") }
                        }) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null)
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
                HomeScreen(
                    guthaben = guthaben,
                    depotWert = depotWert,
                    gesamtGewinn = gewinn,
                    gesamtVermoegen = vermoegen,
                    onNavigateMarkt = { navController.navigate(Screen.Markt.route) },
                    onNavigateDepot = { navController.navigate(Screen.Depot.route) },
                    onReset = { viewModel.resetSpiel() }
                )
            }
            composable(Screen.Markt.route) {
                MarketScreen(
                    aktien = aktien,
                    guthaben = guthaben,
                    onKaufen = { aktie, anzahl -> viewModel.kaufeAktie(aktie, anzahl) }
                )
            }
            composable(Screen.Depot.route) {
                DepotScreen(
                    depot = depot,
                    onVerkaufen = { kuerzel, anzahl -> viewModel.verkaufeAktie(kuerzel, anzahl) }
                )
            }
        }
    }
}
