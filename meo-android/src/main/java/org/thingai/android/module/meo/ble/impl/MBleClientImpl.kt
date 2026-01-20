package org.thingai.android.module.meo.ble.impl

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import org.thingai.android.module.meo.ble.MBleClient
import org.thingai.android.module.meo.ble.MBleSession
import org.thingai.base.log.ILog
import org.thingai.meo.common.ble.MBleUuid
import org.thingai.meo.common.entity.device.MDeviceConfigBle
import java.util.concurrent.ConcurrentHashMap

class MBleClientImpl(private val app: Context): MBleClient {
    private val TAG: String = "MBleClient"
    private val sessions = ConcurrentHashMap<String, MBleSessionImpl>()

    @SuppressLint("MissingPermission")
    override fun scanForConfigDevices(): Flow<List<MDeviceConfigBle>> {
        ILog.d(TAG, "scanForConfigDevices")
        val manager = app.getSystemService(BluetoothManager::class.java)
            ?: return callbackFlow { trySend(emptyList()); awaitClose {} }
        val adapter = manager.adapter ?: return callbackFlow { trySend(emptyList()); awaitClose {} }
        val scanner = adapter.bluetoothLeScanner
            ?: return callbackFlow { trySend(emptyList()); awaitClose {} }

        return callbackFlow {
            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(MBleUuid.MEO_BLE_PROV_SERV_UUID))
                .build()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            val devices = ConcurrentHashMap<String, MDeviceConfigBle>()

            val cb = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val d = result.device
                    val item = MDeviceConfigBle()
                    item.bleAddress = d.address
                    item.bleName = result.scanRecord?.deviceName ?: d.address
                    item.rssi = result.rssi

                    devices[d.address] = item

                    ILog.d(TAG, "scanForConfigDevices", "${item.isHasConfigService}")

                    trySend(devices.values.sortedBy { it.bleName ?: it.bleAddress })
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>) {
                    results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
                }

                override fun onScanFailed(errorCode: Int) {
                    ILog.d(TAG, "onScanFailed", "$errorCode")
                    trySend(emptyList())
                }
            }

            scanner.startScan(listOf(filter), settings, cb)
            awaitClose { scanner.stopScan(cb) }
        }
            .onStart { emit(emptyList()) }
            .flowOn(Dispatchers.IO)
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(address: String): MBleSession {
        ILog.d(TAG, "connect", address)
        // Reuse session if exists
        val existing = sessions[address]
        if (existing != null) {
            ILog.d(TAG, "reuse session", address)
            if (existing.isConnected.value) {
                existing.close()
            }
            existing.connect()
            return existing
        }

        ILog.d(TAG, "new session", address)
        val manager = app.getSystemService(BluetoothManager::class.java)
            ?: error("BluetoothManager not available")
        val device = manager.adapter?.getRemoteDevice(address)
            ?: error("Device $address not found")
        val session = MBleSessionImpl(app, device)
        sessions[address] = session
        // Initiate connection
        session.connect()
        return session
    }

    override suspend fun disconnect(address: String) {
        ILog.d(TAG, "disconnect", address)
        sessions.remove(address)?.close()
    }
}