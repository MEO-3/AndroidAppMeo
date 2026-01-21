package org.thingai.android.module.meo.ble.impl

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import org.thingai.android.module.meo.ble.MBleClient
import org.thingai.android.module.meo.ble.MBleScanCallback
import org.thingai.android.module.meo.ble.MBleSession
import org.thingai.base.log.ILog
import org.thingai.meo.common.ble.MBleUuid
import org.thingai.meo.common.entity.device.MDeviceConfigBle
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class MBleClientImpl(private val app: Context): MBleClient {
    private val TAG: String = "MBleClient"
    private val sessions = ConcurrentHashMap<String, MBleSessionImpl>()
    // Keep a scanner reference only while actively scanning; clear ASAP for memory hygiene
    private var scanner: BluetoothLeScanner? = null
    private var scannerCallback: ScanCallback? = null

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
        val manager = app.applicationContext.getSystemService(BluetoothManager::class.java)
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

    /**
     * Start scanning. Memory-optimized:
     * - Use applicationContext to obtain the system service so we don't accidentally hold an Activity.
     * - Wrap external callback in WeakReference so if the caller is gone we won't keep it alive.
     * - Keep scanner cached only while scanning and clear it when stopping.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun scan(callback: MBleScanCallback) {
        ILog.d(TAG, "scan")
        val manager = app.applicationContext.getSystemService(BluetoothManager::class.java)
        scanner = manager?.adapter?.bluetoothLeScanner

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(MBleUuid.MEO_BLE_PROV_SERV_UUID))
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callbackRef = WeakReference(callback)

        // store callback to a field so stopScan can reference the same instance
        scannerCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val cb = callbackRef.get() ?: run {
                    safeStopScan()
                    return
                }

                val d = result.device
                val item = MDeviceConfigBle()
                item.bleAddress = d.address
                item.bleName = result.scanRecord?.deviceName ?: d.address
                item.rssi = result.rssi

                ILog.d(TAG, "scan", "${item.isHasConfigService}")

                cb.onDeviceFound(item)
            }

            override fun onScanFailed(errorCode: Int) {
                val cb = callbackRef.get()
                ILog.d(TAG, "onScanFailed", "$errorCode")
                if (cb == null) {
                    safeStopScan()
                } else {
                    cb.onScanFailed(errorCode, "Scan failed")
                }
            }
        }

        // Start scan if we have a scanner and a callback
        scanner?.startScan(listOf(scanFilter), scanSettings, scannerCallback)
    }

    /**
     * Stop scanning and free related references immediately to avoid retaining callers/contexts.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun stopScan() {
        ILog.d(TAG, "stopScan")
        safeStopScan()
    }

    /**
     * Internal safe stop that checks permission and handles exceptions. Not annotated with RequiresPermission
     * so it can be called from callbacks safely.
     */
    private fun safeStopScan() {
        try {
            val hasPermission = ContextCompat.checkSelfPermission(app, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                val s = scanner ?: app.applicationContext.getSystemService(BluetoothManager::class.java)
                    .adapter
                    .bluetoothLeScanner
                try {
                    s?.stopScan(scannerCallback)
                } catch (e: Exception) {
                    ILog.e(TAG, e.message)
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, e.message)
        } finally {
            // Always clear references to avoid leaks
            scannerCallback = null
            scanner = null
        }
    }

    /**
     * Explicit release to call from lifecycle end (e.g., Activity/Service onDestroy)
     */
    fun releaseScanner() {
        safeStopScan()
        scanner = null
    }
}