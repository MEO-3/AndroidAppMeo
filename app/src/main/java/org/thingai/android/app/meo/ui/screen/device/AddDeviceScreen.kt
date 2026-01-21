package org.thingai.android.app.meo.ui.screen.device

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import org.thingai.android.app.meo.ui.shared.appbar.BaseTopAppBar
import org.thingai.android.app.meo.ui.viewmodel.device.VMAddDevice
import org.thingai.meo.common.entity.device.MDeviceConfigBle

@Composable
fun AddDeviceScreen(navController: NavController, vm: VMAddDevice = hiltViewModel()) {
    val ui by vm.ui.collectAsState()

    // Start scanning when composed, stop when disposed
    LaunchedEffect(Unit) {
        vm.startScan()
    }
    DisposableEffect(Unit) {
        onDispose {
            vm.stopScan()
        }
    }

    Scaffold(
        topBar = {
            BaseTopAppBar(
                title = "Add Device",
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (ui.scanning) "Scanning..." else "Not scanning", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (ui.scanning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { if (ui.scanning) vm.stopScan() else vm.startScan() }) {
                        Text(if (ui.scanning) "Stop" else "Start")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Discovered devices:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                if (ui.devices.isEmpty()) {
                    Text(text = "No devices found yet")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(ui.devices) { device ->
                            DeviceRow(device) { vm.connectToDevice(device) }
                        }
                    }
                }
            }

            // Error dialog
            if (ui.error != null) {
                AlertDialog(
                    onDismissRequest = { /* ignore */ },
                    confirmButton = {
                        TextButton(onClick = { vm.stopScan() }) {
                            Text("OK")
                        }
                    },
                    title = { Text("Error") },
                    text = { Text(ui.error ?: "Unknown error") }
                )
            }
        }
    }
}

@Composable
private fun DeviceRow(device: MDeviceConfigBle, onClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .clickable { onClick() }) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.bleName ?: "Unknown", style = MaterialTheme.typography.titleMedium)
                Text(text = device.bleAddress ?: "", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "RSSI: ${device.rssi}")
        }
    }
}