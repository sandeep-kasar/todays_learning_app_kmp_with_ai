package com.todays.learning.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.todays.learning.ui.screens.calender.CalenderScreen
import com.todays.learning.ui.screens.details.DetailsScreen
import com.todays.learning.utils.WindowSize
import com.todays.learning.ui.screens.home.HomeScreen
import com.todays.learning.ui.screens.me.MeScreen
import io.github.aakira.napier.Napier

@Composable
fun Navigation(
    navHostController: NavHostController,
    windowSize: WindowSize,
    mainPaddingValues: PaddingValues = PaddingValues()
) {
    NavHost(navController = navHostController, startDestination = NavigationItem.Home.route) {
        composable(route = NavigationItem.Home.route) {
            HomeScreen(
                navigator = navHostController,
                windowSize = windowSize,
                mainPaddingValues = mainPaddingValues
            )
        }

        composable(route = NavigationItem.Calender.route) {
            CalenderScreen()
        }

        composable(route = NavigationItem.Me.route) {
            MeScreen(
                mainPaddingValues = mainPaddingValues
            )
        }

        composable(
            route = NavigationItem.Details.route,
            arguments = listOf(navArgument("subject") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("subject")?.let { subject ->
                Napier.e("Subject ID: $subject")
                DetailsScreen(
                    navigator = navHostController,
                    subject = subject
                )
            }
        }
    }
}
