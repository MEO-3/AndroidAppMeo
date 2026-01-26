package org.thingai.android.app.meo.ui.viewmodel.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.thingai.android.module.meo.MeoSdk
import org.thingai.base.log.ILog
import org.thingai.meo.common.callback.RequestCallback
import org.thingai.meo.common.callback.SetupDeviceCallback
import org.thingai.meo.common.entity.device.MDeviceConfigBle
import org.thingai.meo.common.entity.device.MDeviceInfo
import javax.inject.Inject

@HiltViewModel
class VMAddDevice @Inject constructor() : ViewModel() {
    private val TAG = "VMAddDevice"

    data class UiState(
        val scanning: Boolean = false,
        val devices: List<MDeviceConfigBle> = emptyList(),
        val error: String? = null,
        val loadingWifiList: Boolean = false,
        val deviceId: String = "",
        val provisioning: Boolean = false
    )

    private val _ui = MutableStateFlow(UiState())
    val ui = _ui.asStateFlow()

    private var callbackRegistered = false

    fun startScan() {
        if (callbackRegistered) {
            // already registered and probably scanning
            return
        }

        // Register callback to receive discovered devices
        MeoSdk.bleDiscoveryHandler().setSetupDeviceCallback(object: SetupDeviceCallback<MDeviceConfigBle> {
            override fun onDeviceFound(p0: MDeviceConfigBle?) {
                if (p0 == null) return
                viewModelScope.launch(Dispatchers.Main) {
                    val current = _ui.value
                    // append unique by address
                    val exists = current.devices.any { it.bleAddress == p0.bleAddress }
                    if (!exists) {
                        _ui.value = current.copy(devices = current.devices + p0, scanning = true)
                    }
                }
            }

            override fun onDeviceIdentifiedAndReady(p0: MDeviceInfo?) {
                if (p0 == null) return
                viewModelScope.launch(Dispatchers.Main) {
                    _ui.value = _ui.value.copy(deviceId = p0.macAddress ?: "")
                }
            }

            override fun onProgress(p0: Int, p1: String?) {
                // Optionally show progress
                viewModelScope.launch(Dispatchers.Main) {
                    _ui.value = _ui.value.copy(error = null)
                }
            }

            override fun onSetupFailed(p0: Int, p1: String?) {
                viewModelScope.launch(Dispatchers.Main) {
                    _ui.value = _ui.value.copy(error = p1 ?: "Scan failed", scanning = false)
                }
            }

        })
        callbackRegistered = true

        // Start discovery
        val started = MeoSdk.bleDiscoveryHandler().discovery()
        if (!started) {
            // already scanning or failed to start
            viewModelScope.launch(Dispatchers.Main) {
                _ui.value = _ui.value.copy(error = "Scan already in progress or failed to start")
            }
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _ui.value = _ui.value.copy(scanning = true, error = null, devices = emptyList())
            }
        }
    }

    fun stopScan() {
        try {
            MeoSdk.bleDiscoveryHandler().closeDiscovery()
        } catch (t: Throwable) {
            ILog.e(TAG, "stopScan error: ${t.message}")
        }
        viewModelScope.launch(Dispatchers.Main) {
            _ui.value = _ui.value.copy(scanning = false)
        }
        callbackRegistered = false
    }

    fun clearError() {
        viewModelScope.launch(Dispatchers.Main) {
            _ui.value = _ui.value.copy(error = null, scanning = false)
        }
        try {
            // also ensure scanning is stopped
            MeoSdk.bleDiscoveryHandler().closeDiscovery()
        } catch (t: Throwable) {
            ILog.e(TAG, "clearError stopScan error: ${t.message}")
        }
        callbackRegistered = false
    }

    fun connectToDevice(device: MDeviceConfigBle) {
        // Stop scanning while connecting
        stopScan()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                MeoSdk.bleDiscoveryHandler().connectAndIdentifyDevice(device, object: RequestCallback<MDeviceInfo> {
                    override fun onSuccess(var1: MDeviceInfo, var2: String) {
                        viewModelScope.launch(Dispatchers.Main) {
                            _ui.value = _ui.value.copy(deviceId = var1.macAddress ?: "", error = null)
                        }
                    }

                    override fun onFailure(var1: Int, var2: String) {
                        viewModelScope.launch(Dispatchers.Main) {
                            _ui.value = _ui.value.copy(error = var2)
                        }
                    }
                })
            } catch (t: Throwable) {
                ILog.e(TAG, "connectToDevice error: ${t.message}")
                viewModelScope.launch(Dispatchers.Main) {
                    _ui.value = _ui.value.copy(error = t.message ?: "Connect error")
                }
            }
        }
    }

    /**
     * Provision device to wifi via BLE. This method calls underlying bleDiscoveryHandler.connectWifi
     * and updates UI provisioning state and error message based on callback.
     */
    fun connectWifi(ssid: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.Main) { _ui.value = _ui.value.copy(provisioning = true, error = null) }
            try {
                MeoSdk.bleDiscoveryHandler().connectWifi(ssid, password, object: RequestCallback<Boolean?> {
                    override fun onSuccess(var1: Boolean?, var2: String) {
                        viewModelScope.launch(Dispatchers.Main) {
                            _ui.value = _ui.value.copy(provisioning = false)
                            // onSuccess true indicates connected; we could clear deviceId or leave as-is
                        }
                    }

                    override fun onFailure(var1: Int, var2: String) {
                        viewModelScope.launch(Dispatchers.Main) {
                            _ui.value = _ui.value.copy(provisioning = false, error = var2)
                        }
                    }
                })
            } catch (t: Throwable) {
                ILog.e(TAG, "connectWifi error: ${t.message}")
                viewModelScope.launch(Dispatchers.Main) {
                    _ui.value = _ui.value.copy(provisioning = false, error = t.message ?: "Provisioning error")
                }
            }
        }
    }

    /**
     * Cancel any ongoing provisioning and reset provisioning state.
     */
    fun cancelProvisioning() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Try to cancel at handler level if available
                try {
                    MeoSdk.bleDiscoveryHandler().let { handler ->
                        // call cancelProvisioning if the implementation exposes it
                        try {
                            val method = handler::class.java.getMethod("cancelProvisioning")
                            method.invoke(handler)
                        } catch (_: NoSuchMethodException) {
                            // Method not available; ignore
                        }
                    }
                } catch (_: Throwable) {
                    // ignore
                }
            } finally {
                // Ensure UI state is updated on main thread
                viewModelScope.launch(Dispatchers.Main) {
                    _ui.value = _ui.value.copy(provisioning = false, deviceId = "")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try { stopScan() } catch (_: Throwable) {}
    }
}