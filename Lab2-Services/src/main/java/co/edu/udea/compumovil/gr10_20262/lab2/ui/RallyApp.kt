package co.edu.udea.compumovil.gr10_20262.lab2.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.udea.compumovil.gr10_20262.lab2.R

@Composable
fun RallyApp(onSyncNow: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryFlow.collectAsState(initial = null).value
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = RallyBackground,
        bottomBar = {
            NavigationBar(containerColor = RallyCard) {
                val tabs = listOf(
                    Triple("overview", stringResource(R.string.overview), Icons.Default.Home),
                    Triple("accounts", stringResource(R.string.accounts), Icons.Default.AccountBalance),
                    Triple("bills", stringResource(R.string.bills), Icons.AutoMirrored.Filled.ReceiptLong)
                )
                tabs.forEach { (route, label, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.Gray,
                            selectedIconColor = RallyGreen,
                            unselectedTextColor = Color.Gray,
                            selectedTextColor = Color.White,
                            indicatorColor = RallyGreen.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "overview",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("overview") {
                OverviewScreen(
                    onNavigateToAccounts = { navController.navigate("accounts") },
                    onNavigateToBills = { navController.navigate("bills") },
                    onSyncNow = onSyncNow
                )
            }
            composable("accounts") {
                AccountsScreen()
            }
            composable("bills") {
                BillsScreen()
            }
        }
    }
}

