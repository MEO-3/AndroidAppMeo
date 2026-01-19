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
import org.thingai.android.app.meo.navigation.Route
import org.thingai.android.app.meo.ui.theme.AndroidMeoTheme
import org.thingai.android.module.meo.MeoSdk
import org.thingai.base.log.ILog
import org.thingai.meo.common.callback.RequestCallback

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ILog.logLevel = ILog.DEBUG
        ILog.ENABLE_LOGGING = true

        MeoSdk.init(this.applicationContext)

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current

            MeoSdk.connect(object : RequestCallback<Boolean> {
                override fun onSuccess(p0: Boolean?, p1: String?) {
                    if (p0 == false) {
                        ILog.d("MainActivity", "Not authenticated")
                        navController.navigate(Route.LOGIN)
                    } else {
                        ILog.d("MainActivity", "Authenticated")
                        navController.navigate(Route.DEVICE_LIST)
                    }
                }

                override fun onFailure(p0: Int, p1: String?) {
                    ILog.d("MainActivity", "connect failed")
                }

            })

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