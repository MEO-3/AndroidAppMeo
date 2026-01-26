package org.thingai.android.module.meo.util

class ByteUtils {
    companion object {
        fun byteToHexString(bytesArray: ByteArray): String {
            val stringBuilder = StringBuilder()
            for (byte in bytesArray) {
                stringBuilder.append(String.format("%02X", byte))
            }
            return stringBuilder.toString()
        }

        fun byteToUtf8String(bytesArray: ByteArray): String {
            return String(bytesArray)
        }
    }
}