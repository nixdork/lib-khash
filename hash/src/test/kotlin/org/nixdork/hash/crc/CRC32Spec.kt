package org.nixdork.hash.crc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalUnsignedTypes::class)
class CRC32Spec : FunSpec({
    context("cyclic redundancy check") {
        test("CRC32 checks correctly") {
            val test = "The quick brown fox jumps over the lazy dog"
            val crc32: Long = test.toByteArray().crc32()
            crc32 shouldBe 0x414fa339
        }
    }
})
