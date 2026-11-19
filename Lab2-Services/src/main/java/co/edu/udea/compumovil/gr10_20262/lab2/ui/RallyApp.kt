package co.edu.udea.compumovil.gr10_20262.lab2.ui

import androidx.compose.foundation.layout.padding
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
                    "overview" to stringResource(R.string.overview),
                    "accounts" to stringResource(R.string.accounts),
                    "bills" to stringResource(R.string.bills)
                )
                tabs.forEach { (route, label) ->
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
                        icon = { }, // Empty icon since Rally uses mostly text, or we leave it empty to focus on labels
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
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
