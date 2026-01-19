package org.thingai.android.app.meo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.thingai.android.app.meo.navigation.AppNavGraph
import org.thingai.android.app.meo.ui.shared.appbar.MainNavigationBar
import org.thingai.android.app.meo.navigation.Route
import org.thingai.android.app.meo.ui.theme.AndroidMeoTheme
import org.thingai.android.module.meo.MeoSdk
import org.thingai.base.log.ILog
import org.thingai.meo.common.callback.RequestCallback
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ILog.logLevel = ILog.DEBUG
        ILog.ENABLE_LOGGING = true

        MeoSdk.init(this.applicationContext)

        // Install the platform splash screen and keep it until auth check completes
        val splashScreen = installSplashScreen()
        var keepOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepOnScreen }

        // Flow to hold authentication result; null = unknown/loading
        val authStateFlow = MutableStateFlow<Boolean?>(null)

        // Kick off connect; update authStateFlow and release splash when done
        MeoSdk.connect(object : RequestCallback<Boolean> {
            override fun onSuccess(p0: Boolean?, p1: String?) {
                runOnUiThread {
                    authStateFlow.value = p0 ?: false
                    keepOnScreen = false
                }
            }

            override fun onFailure(p0: Int, p1: String?) {
                runOnUiThread {
                    authStateFlow.value = false
                    keepOnScreen = false
                }
            }
        })

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current

            // collect auth state into Compose
            val authState by authStateFlow.collectAsStateWithLifecycle()

            // When authState becomes known, navigate accordingly once
            LaunchedEffect(authState) {
                if (authState == true) {
                    ILog.d("MainActivity", "Authenticated")
                    navController.navigate(Route.DEVICE_LIST) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) { saveState = false }
                    }
                } else if (authState == false) {
                    ILog.d("MainActivity", "Not authenticated")
                    navController.navigate(Route.LOGIN) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) { saveState = false }
                    }
                }
            }

            AndroidMeoTheme(
                content = {
                    Scaffold(
                        bottomBar = {
                            MainNavigationBar(navController = navController)
                        },
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        }
                    ) { innerPadding ->
                        AppNavGraph(navController = navController, modifier = Modifier.padding(paddingValues = innerPadding))
                    }
                }
            )
        }
    }
}