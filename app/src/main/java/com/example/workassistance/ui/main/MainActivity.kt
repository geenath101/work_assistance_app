package com.example.workassistance.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.repository.AuthRepository
import com.example.workassistance.ui.auth.LoginActivity
import com.example.workassistance.ui.consumables.ConsumablesScreen
import com.example.workassistance.ui.home.HomeScreen
import com.example.workassistance.ui.profile.ProfileScreen
import com.example.workassistance.ui.site.SiteDetailActivity
import com.example.workassistance.ui.theme.WorkAssistanceTheme

sealed class BottomNavRoute(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavRoute("home", "Home", Icons.Default.Home)
    object Consumables : BottomNavRoute("consumables", "Consumables", Icons.Default.ShoppingCart)
    object Profile : BottomNavRoute("profile", "Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permission result handled silently; HomeScreen will show error state if needed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermissionIfNeeded()

        setContent {
            WorkAssistanceTheme {
                MainShell(
                    onSiteClicked = { site -> openSiteDetail(site) },
                    onSignOut = { signOut() }
                )
            }
        }
    }

    private fun openSiteDetail(site: SiteAssignment) {
        startActivity(Intent(this, SiteDetailActivity::class.java).apply {
            putExtra(SiteDetailActivity.EXTRA_ASSIGNMENT_ID, site.assignmentId)
            putExtra(SiteDetailActivity.EXTRA_SITE_ID, site.siteId)
            putExtra(SiteDetailActivity.EXTRA_SITE_NAME, site.siteName)
            putExtra(SiteDetailActivity.EXTRA_SITE_ADDRESS, site.siteAddress)
            putExtra(SiteDetailActivity.EXTRA_SITE_LAT, site.latitude)
            putExtra(SiteDetailActivity.EXTRA_SITE_LNG, site.longitude)
            putExtra(SiteDetailActivity.EXTRA_SITE_RADIUS, site.radiusMeters)
        })
    }

    private fun signOut() {
        AuthRepository.signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun requestLocationPermissionIfNeeded() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    onSiteClicked: (SiteAssignment) -> Unit,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val tabs = listOf(BottomNavRoute.Home, BottomNavRoute.Consumables, BottomNavRoute.Profile)

    val user = AuthRepository.getCurrentUser()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = user?.displayName ?: "Worker",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    Text(
                        text = user?.companyName ?: "WorkAssist Co.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavRoute.Home.route) {
                HomeScreen(onSiteClicked = onSiteClicked)
            }
            composable(BottomNavRoute.Consumables.route) {
                ConsumablesScreen()
            }
            composable(BottomNavRoute.Profile.route) {
                ProfileScreen(onSignOut = onSignOut)
            }
        }
    }
}
