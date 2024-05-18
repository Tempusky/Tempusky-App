package com.example.tempusky.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tempusky.MainActivity
import com.example.tempusky.MainViewModel
import com.example.tempusky.domain.appNavigation.NavigationRoutes
import com.example.tempusky.ui.screens.home.HomeScreen
import com.example.tempusky.ui.screens.login.ForgotPasswordScreen
import com.example.tempusky.ui.screens.login.LoginScreen
import com.example.tempusky.ui.screens.profile.ProfileScreen
import com.example.tempusky.ui.screens.search.SearchScreen
import com.example.tempusky.ui.screens.search.SearchViewModel
import com.example.tempusky.ui.screens.settings.SettingsScreen
import com.example.tempusky.ui.screens.signup.SignupScreen

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun TempuskyNavHost(context: MainActivity, navController: NavController, mainViewModel:  MainViewModel, searchViewModel: SearchViewModel) {
    NavHost(startDestination = NavigationRoutes.LOGIN, navController = navController as NavHostController) {
        composable(NavigationRoutes.LOGIN){
            LoginScreen(context = context, navController = navController, mainViewModel = mainViewModel)
        }
        composable(NavigationRoutes.FORGOT_PASSWORD){
            ForgotPasswordScreen(navController = navController)
        }
        composable(NavigationRoutes.HOME,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(700)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(700)
                )
            }){
            HomeScreen(context, mainViewModel, searchViewModel)
        }
        composable(NavigationRoutes.PROFILE,
            enterTransition = {
                when(initialState.destination.route){
                    NavigationRoutes.SETTINGS -> slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(700)
                    )
                    else -> slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(700)
                    )
                }
            },
            exitTransition = {
                when(targetState.destination.route){
                    NavigationRoutes.SETTINGS -> slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(700)
                    )
                    else -> slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(700)
                    )
                }
            },
            popEnterTransition = {
                when(initialState.destination.route){
                    NavigationRoutes.SETTINGS -> slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(700)
                    )
                    else -> slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(700)
                    )
                }
            },
            popExitTransition = {
                when(targetState.destination.route){
                    NavigationRoutes.SETTINGS -> slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(700)
                    )
                    else -> slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(700)
                    )
                }

            }) {
            ProfileScreen(context = context, navController = navController)
        }
        composable(NavigationRoutes.SETTINGS,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(700)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(700)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(700)
                )
            }
        ){
            SettingsScreen(navController, mainViewModel)
        }
        composable(NavigationRoutes.SIGNUP){
            SignupScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(NavigationRoutes.SEARCH,
            enterTransition = {
            when (initialState.destination.route) {
                NavigationRoutes.HOME ->
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(700)
                    )
                NavigationRoutes.PROFILE ->
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(700)
                    )
                else -> null
            }
        },
            exitTransition = {
                when (targetState.destination.route) {
                    NavigationRoutes.HOME ->
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )

                    NavigationRoutes.PROFILE ->
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(700)
                        )
                    else -> null
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    NavigationRoutes.HOME ->
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )

                    else -> null
                }
            },
            popExitTransition = {
                when (targetState.destination.route) {
                    NavigationRoutes.HOME ->
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )

                    else -> null
                }
            }){
            SearchScreen(context, searchViewModel = searchViewModel)
        }
    }
}

