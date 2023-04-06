/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.test.dispatcher.*
import kotlinx.coroutines.*
import kotlin.test.*

class ByteWriteChannelTransformTest {

    @Test
    fun testTransformEmpty() = testSuspend {
        var counter = 0
        val output = reader {
            consumeEach {
                counter += 1
            }
        }

        val transformed = output.incrementEachByte()
        transformed.flushAndClose()
        assertEquals(0, counter)
    }

    @Test
    fun testTransform() = testSuspend {
        var counter = 0
        var data: ByteArray? = null
        val done = Job()
        val output = reader {
            consumeEach {
                counter += 1
                data = it.toByteArray()
            }

            done.complete()
        }

        val transformed = output.incrementEachByte()
        transformed.writeByte(1)
        transformed.writeByte(2)
        transformed.writeByte(3)
        transformed.flushAndClose()

        done.join()

        assertEquals(1, counter)
        assertNotNull(data)
        assertArrayEquals(byteArrayOf(2, 3, 4), data!!)
    }

    @Test
    fun testTransformFailedChannel() = testSuspend {
        val output = reader {
            throw IOException("test")
        }

        val transformed = output.incrementEachByte()
        val cause = assertFailsWith<CancellationException> {
            transformed.writeByte(1)
            transformed.flushAndClose()
        }

        assertEquals("test", cause.unwrapCancellation().message)
    }

    private fun ByteWriteChannel.incrementEachByte() = map {
        it.incrementEachByte()
    }
}
