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
import org.thingai.android.app.meo.ui.shared.dialog.LoadingDialog
import org.thingai.android.app.meo.ui.viewmodel.device.VMAddDevice
import org.thingai.meo.common.entity.device.MDeviceConfigBle
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.thingai.android.app.meo.ui.shared.dialog.BaseDialog
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

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

    // WiFi config dialog state
    var showWifiDialog by remember { mutableStateOf(false) }
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            // also cancel any ongoing provisioning when leaving the screen
        }
    }

    // If deviceId becomes available (non-empty), show wifi dialog once
    LaunchedEffect(ui.deviceId) {
        if (ui.deviceId.isNotBlank()) {
            // show dialog only if not already provisioning
            if (!ui.provisioning) {
                showWifiDialog = true
            }
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

        // WiFi config dialog shown when device identified
        if (showWifiDialog) {
            WifiConfigDialog(
                onDismiss = {
                    showWifiDialog = false
                    // cancel provisioning when user dismisses dialog
                },
                onConfirm = { enteredSsid, enteredPassword ->
                    ssid = enteredSsid
                    password = enteredPassword
                    showWifiDialog = false
                    // Trigger provisioning via VM
                    vm.connectWifi(ssid, password)
                },
                provisioning = ui.provisioning
            )
        }

        // Show loading dialog while connecting and identifying the device
        LoadingDialog(
            show = ui.connecting,
            message = "Connecting to device…",
            cancellable = false,
            onDismiss = { /* no-op, not cancellable */ }
        )

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


@Composable
private fun WifiConfigDialog(
    onDismiss: () -> Unit,
    onConfirm: (ssid: String, password: String) -> Unit,
    provisioning: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    BaseDialog(
        onDismiss = onDismiss,
        position = org.thingai.android.app.meo.ui.shared.custom.DialogPosition.BOTTOM,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(8.dp)
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title + Close
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    val focusManager = LocalFocusManager.current
                    val keyboardController = LocalSoftwareKeyboardController.current
                    Text(
                        text = "Configure Wi‑Fi",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // SSID field styled like auth screens
                OutlinedTextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Wifi,
                            contentDescription = "ssid",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    placeholder = { Text("Enter SSID") }
                )

                // Password field styled like auth screens with visibility toggle
                var showPassword by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    placeholder = { Text("Enter password") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                )

                if (provisioning) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onDismiss()
                    }, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        // clear focus and hide keyboard before confirm
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onConfirm(ssid, password)
                    }, modifier = Modifier.weight(1f)) {
                        Text("Connect")
                    }
                }
            }
        }
    }
}
