package com.ggdover.wodapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ggdover.wodapp.R
import com.ggdover.wodapp.ui.moments.MomentDetailScreen
import com.ggdover.wodapp.ui.moments.MomentsScreen
import com.ggdover.wodapp.ui.navigation.Routes
import com.ggdover.wodapp.ui.personalbests.PersonalBestsScreen
import com.ggdover.wodapp.ui.workouts.WorkoutDetailScreen
import com.ggdover.wodapp.ui.workouts.WorkoutEditScreen
import com.ggdover.wodapp.ui.workouts.WorkoutsScreen

private sealed class Tab(
    val route: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    data object Moments : Tab(Routes.Moments, R.string.tab_moments, Icons.Filled.History)
    data object Workouts : Tab(Routes.Workouts, R.string.tab_workouts, Icons.Filled.FitnessCenter)
    data object Pbs : Tab(Routes.PersonalBests, R.string.tab_pbs, Icons.Filled.Star)
}

@Composable
fun WodApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomTabs = listOf(Tab.Moments, Tab.Workouts, Tab.Pbs)
    val showBottomBar = bottomTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Moments,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Moments) {
                MomentsScreen(
                    onOpenMoment = { id -> navController.navigate(Routes.momentDetail(id)) },
                )
            }
            composable(Routes.Workouts) {
                WorkoutsScreen(
                    onOpenWorkout = { id -> navController.navigate(Routes.workoutDetail(id)) },
                    onCreateWorkout = { navController.navigate(Routes.WorkoutNew) },
                )
            }
            composable(Routes.PersonalBests) {
                PersonalBestsScreen()
            }
            composable(Routes.WorkoutNew) {
                WorkoutEditScreen(
                    workoutId = null,
                    onDone = { navController.popBackStack() },
                )
            }
            composable(
                Routes.WorkoutDetail,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                WorkoutDetailScreen(
                    workoutId = id,
                    onEdit = { navController.navigate(Routes.workoutEdit(id)) },
                    onBack = { navController.popBackStack() },
                    onLogResult = { momentId ->
                        navController.navigate(Routes.momentDetail(momentId)) {
                            launchSingleTop = true
                        }
                    },
                    onOpenMoment = { mid -> navController.navigate(Routes.momentDetail(mid)) },
                )
            }
            composable(
                Routes.WorkoutEdit,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                WorkoutEditScreen(
                    workoutId = id,
                    onDone = { navController.popBackStack() },
                )
            }
            composable(
                Routes.MomentDetail,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                MomentDetailScreen(
                    momentId = id,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
