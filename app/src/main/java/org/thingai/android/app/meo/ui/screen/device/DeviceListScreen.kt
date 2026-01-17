package org.thingai.android.app.meo.ui.screen.device

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.thingai.android.app.meo.ui.shared.custom.DialogPosition
import org.thingai.android.app.meo.ui.shared.dialog.BaseDialog
import org.thingai.android.app.meo.ui.viewmodel.device.VMDeviceList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    navController: NavController,
    vm: VMDeviceList = hiltViewModel(),
) {
    val ui = vm.uiState.collectAsStateWithLifecycle().value

    val showDeviceTypeSelectDialog = remember { mutableStateOf(false) }

    DeviceTypeSelectDialog(
        show = showDeviceTypeSelectDialog.value,
        onDismiss = { showDeviceTypeSelectDialog.value = false }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = { Text("MEO Devices", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = @Composable {
                    IconButton(onClick = { showDeviceTypeSelectDialog.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add device"
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {

            }
        }
    }
}

@Composable
private fun DeviceTypeSelectDialog(
    show: Boolean,
    onDismiss: () -> Unit,
) {
    if (!show) return

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BaseDialog(
        onDismiss = {
            focusManager.clearFocus()
            keyboardController?.hide()
            onDismiss()
        },
        position = DialogPosition.BOTTOM,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier.padding(all = 8.dp),
        content = {
            Surface(
                shape = RoundedCornerShape(16.dp),
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
                        Text(
                            text = "Select type of device",
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

                    // Device type row
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { }
                    ) {
                        Row(
                            modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("MEO Gateway")
                            Icon(
                                imageVector = Icons.Default.DeviceHub,
                                contentDescription = "Gateway"
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("MEO Device")
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "MEO Device"
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ThingEdges Device")
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = "ThingEdge Device"
                            )
                        }
                    }
                }
            }
        }
    )
}