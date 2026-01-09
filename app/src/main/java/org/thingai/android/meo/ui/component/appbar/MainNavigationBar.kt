package org.thingai.android.meo.ui.component.appbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import org.thingai.android.meo.navigation.Route

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val matchRoutes: Set<String> = setOf(route)
)

/**
 * Main bottom navigation bar with animated show/hide.
 *
 * Control visibility by either:
 * 1) hideOnRoutes: set of routes where the bar MUST NOT show
 * 2) bottomBarRoutes: set of routes where the bar SHOULD show (whitelist)
 * If both are provided, bottomBarRoutes takes precedence.
 */
@Composable
fun MainNavigationBar(
    navController: NavController,
    hideOnRoutes: Set<String> = setOf(Route.LOGIN, Route.SIGNUP),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    // Decide visibility based on the destination hierarchy (supports nested graphs)
    val shouldShow = !destination.isInRoutes(hideOnRoutes)

    // Optional: animate initial appearance (false -> true on first eligible route)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(shouldShow) { visible = shouldShow }

    val items = listOf(
        BottomNavItem(Route.DEVICE, "Thiết bị", Icons.Default.Star, matchRoutes = setOf(
            Route.DEVICE,
            Route.DEVICE_ADDDEVICE,
            Route.DEVICE_DETAIL,
            Route.DEVICE_INFO,
            Route.DEVICE_SETTING
        )),
        BottomNavItem(Route.ANALYTICS, "Thống kê", Icons.Default.Analytics),
        BottomNavItem(Route.SETTING, "Cài đặt", Icons.Default.Settings)
    )

    val duration = 220
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it }, // slide up from below
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(duration)),
        exit = slideOutVertically(
            targetOffsetY = { it }, // slide down offscreen
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(duration))
    ) {
        NavigationBar {
            items.forEach { item ->
                NavigationBarItem(
                    selected = destination.isInRoutes(item.matchRoutes),
                    onClick = {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            // Keep one instance per tab and restore each tab's state
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                        }
                    },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                )
            }
        }
    }
}

private fun NavDestination?.isInRoutes(routes: Set<String>): Boolean {
    if (this == null) return false
    return hierarchy.any { it.route != null && it.route in routes }
}