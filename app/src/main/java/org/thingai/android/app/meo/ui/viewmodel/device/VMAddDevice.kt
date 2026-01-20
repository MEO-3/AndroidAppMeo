package org.thingai.android.app.meo.ui.viewmodel.device

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val deviceId: String = ""
    )

    private val _ui = MutableStateFlow(UiState())
    val ui = _ui.asStateFlow()

    fun startScan() {
        MeoSdk.bleDiscoveryHandler().setSetupDeviceCallback(object: SetupDeviceCallback<MDeviceConfigBle> {
            override fun onDeviceFound(p0: MDeviceConfigBle?) {

            }

            override fun onDeviceIdentifiedAndReady(p0: MDeviceInfo?) {

            }

            override fun onProgress(p0: Int, p1: String?) {

            }

            override fun onSetupFailed(p0: Int, p1: String?) {

            }

        })
        MeoSdk.bleDiscoveryHandler().discovery()
    }

    fun stopScan() {
        MeoSdk.bleDiscoveryHandler().closeDiscovery()
    }
}