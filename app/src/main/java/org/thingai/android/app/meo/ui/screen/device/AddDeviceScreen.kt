package org.thingai.android.app.meo.ui.screen.device

import org.thingai.android.app.meo.ui.viewmodel.device.VMAddDevice

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun AddDeviceScreen(navController: NavController, vm: VMAddDevice = hiltViewModel()) {
    vm.startScan()
}