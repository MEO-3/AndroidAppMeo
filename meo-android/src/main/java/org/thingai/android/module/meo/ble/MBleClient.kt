package org.thingai.android.module.meo.ble

import kotlinx.coroutines.flow.Flow
import org.thingai.meo.common.entity.device.MDeviceConfigBle

interface MBleClient {
    fun scanForConfigDevices(): Flow<List<MDeviceConfigBle>>
    suspend fun connect(address: String): MBleSession
    suspend fun disconnect(address: String)
}