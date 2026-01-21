package org.thingai.android.module.meo.ble

import android.bluetooth.le.ScanCallback
import kotlinx.coroutines.flow.Flow
import org.thingai.meo.common.entity.device.MDeviceConfigBle

interface MBleClient {
    suspend fun connect(address: String): MBleSession
    suspend fun disconnect(address: String)
    fun scan(callback: MBleScanCallback)
    fun stopScan()
}