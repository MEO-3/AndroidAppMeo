package org.thingai.android.module.meo.handler.discovery

import org.thingai.meo.common.callback.RequestCallback
import org.thingai.meo.common.callback.SetupDeviceCallback
import org.thingai.meo.common.entity.device.MDevice
import org.thingai.meo.common.entity.device.MDeviceConfigLan
import org.thingai.meo.common.handler.MDeviceDiscoveryLanHandler

class MDeviceDiscoveryLanHandlerImpl: MDeviceDiscoveryLanHandler() {
    override fun discovery(): Boolean {
        TODO("Not yet implemented")
    }

    override fun closeDiscovery(): Boolean {
        TODO("Not yet implemented")
    }

    override fun onDeviceFound(p0: RequestCallback<MDeviceConfigLan?>?) {
        TODO("Not yet implemented")
    }

    override fun connectAndIdentifyDevice(
        p0: MDeviceConfigLan?,
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