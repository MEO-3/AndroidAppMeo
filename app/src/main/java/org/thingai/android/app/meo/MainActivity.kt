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
import org.thingai.android.app.meo.navigation.AppNavGraph
import org.thingai.android.app.meo.ui.shared.appbar.MainNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import org.thingai.android.app.meo.ui.theme.AndroidMeoTheme
import org.thingai.base.log.ILog

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ILog.logLevel = ILog.DEBUG
        ILog.ENABLE_LOGGING = true

        ILog.d("MainActivity", "onCreate")

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current

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