package org.thingai.android.module.meo.handler.discovery

import org.thingai.meo.common.callback.RequestCallback
import org.thingai.meo.common.callback.SetupDeviceCallback
import org.thingai.meo.common.entity.device.MDevice
import org.thingai.meo.common.entity.device.MDeviceConfigBle
import org.thingai.meo.common.entity.info.MWifiInfo
import org.thingai.meo.common.handler.MDeviceDiscoveryBleHandler

class MDeviceDiscoveryBleHandlerImpl: MDeviceDiscoveryBleHandler() {
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
        TODO("Not yet implemented")
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