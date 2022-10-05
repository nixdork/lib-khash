package org.nixdork.hash.crc

fun interface RedundancyCheck<T> {
    /**
     * Example:
     * ```kotlin
     * val crc32 = RedundancyCheck<Int> { input: ByteArray ->
     *     // check cyclic redundancy the CRC32 way
     * }
     * ```
     */
    fun check(input: ByteArray): T

    companion object {
        inline operator fun <T> invoke(crossinline block: (ByteArray) -> T): RedundancyCheck<T> =
            RedundancyCheck { input -> block(input) }
    }
}
