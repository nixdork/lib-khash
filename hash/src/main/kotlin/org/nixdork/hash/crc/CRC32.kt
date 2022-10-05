@file:Suppress("UnusedPrivateMember", "MagicNumber")
package org.nixdork.hash.crc

private const val CRC_DEFAULT = 0xffffffff
private const val BIT_MASK_32 = CRC_DEFAULT

private const val POLYNOMIAL_CRC32_NORMAL = 0x1db710640

private val crc32Table: List<Long> = (0L until 0x100).map { i ->
    var rem = i
    for (j in 0 until 8) {
        if (rem and 1L == 1L)
            rem = POLYNOMIAL_CRC32_NORMAL xor rem
        rem = rem shr 1
    }
    rem
}

val CRC_32 = RedundancyCheck { input: ByteArray ->
    var idx: Long
    var crc: Long = CRC_DEFAULT

    input.forEach { byte ->
        idx = (crc xor byte.toLong()) and 0xFF
        crc = (crc shr 8) xor crc32Table[idx.toInt()]
    }

    return@RedundancyCheck crc xor BIT_MASK_32
}

fun ByteArray.crc32(): Long = CRC_32.check(this)
