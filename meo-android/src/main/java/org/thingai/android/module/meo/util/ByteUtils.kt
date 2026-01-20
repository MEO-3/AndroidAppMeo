package org.thingai.android.module.meo.util

class ByteUtils {
    companion object {
        fun bytesToString(bytesArray: ByteArray): String {
            val stringBuilder = StringBuilder()
            for (byte in bytesArray) {
                stringBuilder.append(String.format("%02X", byte))
            }
            return stringBuilder.toString()
        }
    }
}