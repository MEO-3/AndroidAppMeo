package org.thingai.android.module.meo

import org.thingai.android.module.meo.handler.discovery.MDeviceDiscoveryBleHandlerImpl
import org.thingai.android.module.meo.handler.discovery.MDeviceDiscoveryLanHandlerImpl
import org.thingai.meo.common.handler.MDeviceDiscoveryBleHandler
import org.thingai.meo.common.handler.MDeviceDiscoveryLanHandler

class MeoSdk {
    val bleDiscoveryHandler: MDeviceDiscoveryBleHandler = MDeviceDiscoveryBleHandlerImpl()
    val lanDiscoveryHandler: MDeviceDiscoveryLanHandler = MDeviceDiscoveryLanHandlerImpl()
}