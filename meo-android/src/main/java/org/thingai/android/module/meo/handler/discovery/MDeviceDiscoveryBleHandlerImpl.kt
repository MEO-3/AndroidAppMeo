package org.thingai.android.module.meo.handler.discovery

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.thingai.android.module.meo.ble.MBleSession
import org.thingai.base.log.ILog
import org.thingai.meo.common.callback.RequestCallback
import org.thingai.meo.common.callback.SetupDeviceCallback
import org.thingai.meo.common.entity.device.MDevice
import org.thingai.meo.common.entity.device.MDeviceConfigBle
import org.thingai.meo.common.entity.info.MWifiInfo
import org.thingai.meo.common.handler.MDeviceDiscoveryBleHandler

class MDeviceDiscoveryBleHandlerImpl(
    private val bleConnector: suspend (String) -> MBleSession
): MDeviceDiscoveryBleHandler() {
    private val TAG = "MDeviceDiscoveryBleHandler"

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var bleSession: MBleSession? = null
    private var connectionJob: Job? = null

    private val _connected = MutableStateFlow(false)
    val isConnected: Flow<Boolean> = _connected.asStateFlow()

    override fun scanWifi(p0: RequestCallback<Array<out MWifiInfo?>?>?) {
        TODO("Not yet implemented")
    }

    override fun connectWifi(
        p0: String?,
        p1: String?,
        p2: RequestCallback<Boolean?>?
    ) {
        TODO("Not yet implemented")
    }

    override fun discovery(): Boolean {
        TODO("Not yet implemented")
    }

    override fun closeDiscovery(): Boolean {
        TODO("Not yet implemented")
    }

    override fun onDeviceFound(p0: RequestCallback<MDeviceConfigBle?>?) {
        TODO("Not yet implemented")
    }

    override fun connectAndIdentifyDevice(
        p0: MDeviceConfigBle?,
        p1: SetupDeviceCallback?
    ) {
        scope.launch {
            bleSession?.close()
            bleSession = bleConnector(p0!!.bleAddress)


        }

    }

    override fun setupAndSyncDeviceLocal(
        p0: String?,
        p1: RequestCallback<MDevice?>?
    ) {
        TODO("Not yet implemented")
    }

    override fun setupAndSyncDeviceToCloud(
        p0: String?,
        p1: RequestCallback<MDevice?>?
    ) {
        TODO("Not yet implemented")
    }
}
