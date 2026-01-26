package org.thingai.android.module.meo.ble.impl

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeout
import org.thingai.android.module.meo.ble.MBleSession
import org.thingai.base.log.ILog
import org.thingai.meo.common.ble.MBleUuid
import java.util.UUID

class MBleSessionImpl(
    private val app: Context,
    private val device: BluetoothDevice
) : BluetoothGattCallback(), MBleSession {
    private val TAG = "MBleSession"

    private var gatt: BluetoothGatt? = null
    private val _connected = MutableStateFlow(false)
    override val isConnected = _connected

    override val address: String get() = device.address

    private val opMutex = Any()
    private var pendingRead: CompletableDeferred<ByteArray>? = null
    private var pendingWrite: CompletableDeferred<Unit>? = null
    private var servicesReady = CompletableDeferred<Unit>()

    private val notifyChannel = Channel<ByteArray>(Channel.UNLIMITED)

    @SuppressLint("MissingPermission")
    suspend fun connect() {
        ILog.d(TAG, "connect")
        gatt = device.connectGatt(app, false, this, BluetoothDevice.TRANSPORT_LE)
        // Wait until services discovered before allowing ops
        // Optional timeout to avoid deadlocks
        withTimeout(5_000) {
            ILog.d(TAG, "wait timeout")
            servicesReady.await()
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun close() {
        ILog.d(TAG, "close")
        try {
            gatt?.disconnect()
        } catch (e: Exception) {
            ILog.e(TAG, e.message)
        } finally {
            gatt?.close()
            gatt = null
            _connected.value = false
            pendingRead?.cancel()
            pendingWrite?.cancel()
            notifyChannel.close()
        }
    }

    override suspend fun open() {
        close()
        connect()
    }

    // Reads a characteristic as ByteArray
    @SuppressLint("MissingPermission")
    override suspend fun read(uuid: UUID): ByteArray {
        val ch = findCharacteristic(uuid) ?: error("Characteristic $uuid not found")
        val g = gatt ?: error("Not connected")
        val result = CompletableDeferred<ByteArray>()
        synchronized(opMutex) {
            pendingRead = result
            if (!g.readCharacteristic(ch)) {
                pendingRead = null
                error("readCharacteristic returned false for $uuid")
            }
        }
        return withTimeout(10_000) { result.await() }
    }

    // Writes a characteristic with or without response
    @SuppressLint("MissingPermission")
    override suspend fun write(uuid: UUID, value: ByteArray, withResponse: Boolean): ByteArray {
        val ch = findCharacteristic(uuid) ?: error("Characteristic $uuid not found")
        val g = gatt ?: error("Not connected")
        ch.writeType = if (withResponse)
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        else
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        val result = CompletableDeferred<Unit>()
        synchronized(opMutex) {
            pendingWrite = result
            if (g.writeCharacteristic(ch, value, ch.writeType) != BluetoothStatusCodes.SUCCESS) {
                pendingWrite = null
                error("writeCharacteristic returned false for $uuid")
            }
        }
        withTimeout(10_000) { result.await() }
        return value
    }

    // Notification flow for a characteristic
    @SuppressLint("MissingPermission")
    override fun notifications(uuid: UUID): Flow<ByteArray> {
        val ch = findCharacteristic(uuid) ?: error("Characteristic $uuid not found")
        val g = gatt ?: error("Not connected")
        // Enable notifications
        g.setCharacteristicNotification(ch, true)
        // Write CCCD to enable
        val cccd = ch.getDescriptor(CCCD_UUID)
        if (cccd != null) {
            g.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            g.setCharacteristicNotification(ch, false)
        }
        return notifyChannel.receiveAsFlow()
            .flowOn(Dispatchers.IO)
    }

    // BluetoothGattCallback implementations
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        ILog.d(TAG, "onConnectionStateChange", "$newState")
        _connected.value = (newState == BluetoothProfile.STATE_CONNECTED)
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices()
        } else {
            servicesReady.completeExceptionally(IllegalStateException("Disconnected"))
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            servicesReady.complete(Unit)
        } else {
            servicesReady.completeExceptionally(IllegalStateException("Service discovery failed: $status"))
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        pendingRead?.complete(value)
        pendingRead = null
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        pendingWrite?.complete(Unit)
        pendingWrite = null
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        notifyChannel.trySend(value)
    }

    private fun getConfigService(): BluetoothGattService? {
        return gatt?.getService(MBleUuid.MEO_BLE_PROV_SERV_UUID)
    }

    private fun findCharacteristic(uuid: UUID): BluetoothGattCharacteristic? {
        return getConfigService()?.getCharacteristic(uuid)
    }

    companion object {
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}