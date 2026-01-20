package org.thingai.android.module.meo.handler.discovery

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.thingai.android.module.meo.ble.MBleClient
import org.thingai.android.module.meo.ble.MBleSession
import org.thingai.android.module.meo.util.ByteUtils
import org.thingai.base.log.ILog
import org.thingai.meo.common.ble.MBleUuid
import org.thingai.meo.common.callback.RequestCallback
import org.thingai.meo.common.define.MDeviceType
import org.thingai.meo.common.entity.device.MDevice
import org.thingai.meo.common.entity.device.MDeviceConfigBle
import org.thingai.meo.common.entity.device.MDeviceInfo
import org.thingai.meo.common.entity.info.MWifiInfo
import org.thingai.meo.common.handler.MDeviceDiscoveryHandlerBle

class MDeviceDiscoveryBleHandlerImpl(
    private val bleClient: MBleClient
): MDeviceDiscoveryHandlerBle() {
    private val TAG = "MDeviceDiscoveryBleHandler"

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var listDiscoveryDevice = mutableListOf<MDeviceConfigBle>()
    private var bleSession: MBleSession? = null
    private var scanJob: Job? = null

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
        scanJob = bleClient.scanForConfigDevices()
            .onEach { list ->
                listDiscoveryDevice.clear()
                listDiscoveryDevice.addAll(list)
                ILog.d(TAG, "discovery: $listDiscoveryDevice")
            }
            .catch { exception ->
                ILog.e(TAG, "discovery: $exception")
                return@catch
            }.let { flow ->
                scope.launch {
                    flow.collect {
                        listDiscoveryDevice.forEach { device ->
                            setupDeviceCallback.onDeviceFound(device)
                        }
                    }
                }
            }
        return true
    }

    override fun closeDiscovery(): Boolean {
        scanJob?.cancel()
        scanJob = null
        return true
    }

    override fun connectAndIdentifyDevice(
        p0: MDeviceConfigBle?,
        p1: RequestCallback<MDeviceInfo>?
    ) {
        scope.launch {
            bleSession?.close()
            bleSession = bleClient.connect(p0!!.bleAddress)
            bleSession!!.isConnected.collect { isConnected ->
                if (isConnected) {
                    _connected.value = true
                } else {
                    _connected.value = false
                    ILog.d(TAG, "connectAndIdentifyDevice: $isConnected")
                    p1?.onFailure(2, "Unable to connect to device")
                    setupDeviceCallback.onSetupFailed(2, "Unable to connect to device")
                    bleSession?.close()
                    bleSession = null
                    return@collect
                }
                val deviceInfo = MDeviceInfo()
                deviceInfo.deviceType = MDeviceType.CUSTOM
                deviceInfo.macAddress = ByteUtils.bytesToString(bleSession!!.read(MBleUuid.CH_UUID_MAC_ADDR))
                deviceInfo.buildInfo = ByteUtils.bytesToString(bleSession!!.read(MBleUuid.CH_UUID_BUILD_INFO))

                p1?.onSuccess(
                    deviceInfo,
                    "Connected to device"
                )
            }
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
