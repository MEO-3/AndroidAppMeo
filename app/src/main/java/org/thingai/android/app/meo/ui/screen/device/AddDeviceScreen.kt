package org.thingai.android.app.meo.ui.screen.device

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import org.thingai.android.app.meo.ui.shared.appbar.BaseTopAppBar
import org.thingai.android.app.meo.ui.shared.dialog.ConfirmDialog
import org.thingai.android.app.meo.ui.shared.dialog.ErrorDialog
import org.thingai.android.app.meo.ui.viewmodel.device.VMAddDevice
import org.thingai.meo.common.entity.device.MDeviceConfigBle
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.lifecycle.compose.LocalLifecycleOwner

enum class RequirementType { PERMISSION, BLUETOOTH, LOCATION }

@Composable
fun AddDeviceScreen(navController: NavController, vm: VMAddDevice = hiltViewModel()) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Helpers to check status
    fun isBluetoothEnabled(): Boolean {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return manager?.adapter?.isEnabled == true
    }

    fun isLocationEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return try {
            lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true || lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        } catch (_: Exception) {
            false
        }
    }

    fun hasScanPermission(): Boolean {
        val bluetoothScan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        val location = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return bluetoothScan || location // accept either depending on platform
    }

    // Compose state mirroring current statuses
    var bluetoothEnabled by remember { mutableStateOf(isBluetoothEnabled()) }
    var locationEnabled by remember { mutableStateOf(isLocationEnabled()) }
    var permissionGranted by remember { mutableStateOf(hasScanPermission()) }

    // Dialog state: which requirement to show when Start pressed
    var showRequirementDialog by remember { mutableStateOf(false) }
    var currentRequirement by remember { mutableStateOf<RequirementType?>(null) }

    // Permission launcher (moved after state vars so it can update permissionGranted)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { _ ->
            // Refresh permission state and restart scan if granted
            val nowGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            permissionGranted = nowGranted
            if (nowGranted) {
                // If permissions granted as a result of dialog action, start scanning
                vm.startScan()
            }
        }
    )

    // Update statuses when composable enters
    LaunchedEffect(Unit) {
        bluetoothEnabled = isBluetoothEnabled()
        locationEnabled = isLocationEnabled()
        permissionGranted = hasScanPermission()
        // Do NOT auto-start scanning; user must press Start
    }

    // Observe lifecycle to refresh statuses on resume and stop scanning on stop
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // refresh statuses
                    bluetoothEnabled = isBluetoothEnabled()
                    locationEnabled = isLocationEnabled()
                    permissionGranted = hasScanPermission()
                }
                Lifecycle.Event.ON_STOP -> {
                    vm.stopScan()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            vm.stopScan()
        }
    }

    Column() {
        BaseTopAppBar(
            title = "Add Device",
            onBack = { navController.popBackStack() }
        )
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            // Scanner controls — start will show dialog if requirements missing
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (ui.scanning) "Scanning..." else "Not scanning", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                if (ui.scanning) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    // On Start: check requirements and either show dialog or start
                    if (!permissionGranted) {
                        currentRequirement = RequirementType.PERMISSION
                        showRequirementDialog = true
                        return@Button
                    }
                    if (!bluetoothEnabled) {
                        currentRequirement = RequirementType.BLUETOOTH
                        showRequirementDialog = true
                        return@Button
                    }
                    if (!locationEnabled) {
                        currentRequirement = RequirementType.LOCATION
                        showRequirementDialog = true
                        return@Button
                    }
                    // all good -> toggle scanning
                    if (ui.scanning) vm.stopScan() else vm.startScan()
                }) {
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

        // Requirement dialog shown on Start if missing
        val req = currentRequirement
        if (showRequirementDialog && req != null) {
            when (req) {
                RequirementType.PERMISSION -> {
                    ConfirmDialog(
                        show = true,
                        title = "Permissions required",
                        message = "Bluetooth scan or location permission is required to discover devices.",
                        onDismiss = { showRequirementDialog = false; currentRequirement = null },
                        onConfirm = {
                            // Request permissions
                            val perms = mutableListOf<String>()
                            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
                            perms.add(Manifest.permission.BLUETOOTH_SCAN)
                            permissionLauncher.launch(perms.toTypedArray())
                            showRequirementDialog = false
                            currentRequirement = null
                        },
                        confirmText = "Grant",
                        cancelText = "Cancel"
                    )
                }
                RequirementType.BLUETOOTH -> {
                    ConfirmDialog(
                        show = true,
                        title = "Bluetooth is off",
                        message = "Please enable Bluetooth to discover devices.",
                        onDismiss = { showRequirementDialog = false; currentRequirement = null },
                        onConfirm = {
                            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                            context.startActivity(intent)
                            showRequirementDialog = false
                            currentRequirement = null
                        },
                        confirmText = "Open settings",
                        cancelText = "Cancel"
                    )
                }
                RequirementType.LOCATION -> {
                    ConfirmDialog(
                        show = true,
                        title = "Location is off",
                        message = "Please enable location services to discover devices on this device.",
                        onDismiss = { showRequirementDialog = false; currentRequirement = null },
                        onConfirm = {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                            showRequirementDialog = false
                            currentRequirement = null
                        },
                        confirmText = "Open settings",
                        cancelText = "Cancel"
                    )
                }
            }
        }

        // Error dialog (keeps existing behavior)
        ErrorDialog(
            show = ui.error != null,
            title = "Error",
            message = ui.error,
            onDismiss = {
                vm.clearError()
            }
        )
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