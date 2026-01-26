package org.thingai.android.module.meo.ble

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface MBleSession {
    val address: String
    val isConnected: Flow<Boolean>
    suspend fun read(uuid: UUID): ByteArray
    suspend fun write(uuid: UUID, value: ByteArray, withResponse: Boolean = true): ByteArray
    fun notifications(uuid: UUID): Flow<ByteArray>
    suspend fun close()
    suspend fun open()
}