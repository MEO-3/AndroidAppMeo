package com.avis.app.ptalk.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.thingai.android.meo.navigation.Route
import org.thingai.android.meo.ui.screen.auth.OtpVerifyScreen
import org.thingai.android.meo.ui.screen.auth.ForgotPasswordScreen
import org.thingai.android.meo.ui.screen.auth.LoginScreen
import org.thingai.android.meo.ui.screen.auth.ResetPasswordScreen
import org.thingai.android.meo.ui.screen.auth.SignUpScreen
import org.thingai.android.meo.ui.screen.device.DeviceListScreen

const val ANIM_DURATION = 300

@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Route.LOGIN,
        modifier = modifier,
        // Slide animation
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(ANIM_DURATION)
            )
        }
    ) {
        composable(Route.LOGIN) { LoginScreen(navController) }
        composable(Route.SIGNUP) { SignUpScreen(navController) }
        composable(Route.FORGOT_PASSWORD) { ForgotPasswordScreen(navController) }
        composable(Route.VERIFY_OTP+"?phone={phone}", arguments = listOf(navArgument("phone") {
            type = NavType.StringType;
            defaultValue = "";
        })) { OtpVerifyScreen(navController) }
        composable(Route.RESET_PASSWORD+"?phone={phone}", arguments = listOf(navArgument("phone") {
            type = NavType.StringType;
            defaultValue = "";
        })) { ResetPasswordScreen(navController) }
        composable(Route.DEVICE_LIST) { DeviceListScreen(navController) }
    }
}

fun String.baseRoute(): String =
    this.substringBefore("?")     // strip query part e.g. otp?phone={phone} -> otp
        .substringBefore("/{")    // strip path args e.g. device/{id} -> device

fun NavDestination.baseRouteOrNull(): String? =
    this.route?.baseRoute()