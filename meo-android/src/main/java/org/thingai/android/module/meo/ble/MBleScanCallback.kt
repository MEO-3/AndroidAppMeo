package org.thingai.android.module.meo.ble

import org.thingai.meo.common.entity.device.MDeviceConfigBle

interface MBleScanCallback {
    fun onDeviceFound(device: MDeviceConfigBle)
    fun onScanFailed(errorCode: Int, message: String)
}