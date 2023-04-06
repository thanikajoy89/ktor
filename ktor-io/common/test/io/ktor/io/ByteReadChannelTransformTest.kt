/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.test.dispatcher.*
import kotlin.test.*

class ByteReadChannelTransformTest {

    @Test
    fun testTransformEmpty() = testSuspend {
        val channel = ByteReadChannel.Empty
        val transformed = channel.transform { }

        assertEquals(0, transformed.availableForRead)
        assertFailsWith<EOFException> { transformed.readByte() }
    }

    @Test
    fun testTransformWithByteArray() = testSuspend {
        val channel = ByteReadChannel(byteArrayOf(1, 2, 3, 4, 5))
        val transformed = channel.map { it.incrementEachByte() }

        assertArrayEquals(byteArrayOf(2, 3, 4, 5, 6), transformed.readByteArray(5))
        assertFailsWith<EOFException> { transformed.readByte() }
    }

    @Test
    fun testWriteByteWithFlush() = testSuspend {
        val channel = writer {
            repeat(100) {
                writePacket {
                    writeByte(it.toByte())
                }
            }
        }

        val transformed = channel.map { it.incrementEachByte() }
        repeat(100) {
            assertEquals((it + 1).toByte(), transformed.readByte())
        }

        assertFailsWith<EOFException> { transformed.readByte() }
    }

    @Test
    fun testExceptionPropagatesIfRethrown() = testSuspend {
        val channel = writer {
            throw IOException("test")
        }

        var cause: Throwable? = null
        val transformed = channel.transform {
            onClose {
                cause = it
                it?.let { throw it }
            }
        }

        val exception = assertFailsWith<IOException> {
            transformed.readByte()
        }

        assertEquals("test", exception.message)
        assertEquals(exception, cause)
    }

    @Test
    fun testExceptionNotPropagates() = testSuspend {
        val channel = writer {
            throw IOException("test")
        }

        var cause: Throwable? = null
        val transformed = channel.transform {
            onClose {
                cause = it
            }
        }

        assertFailsWith<EOFException> {
            transformed.readByte()
        }

        assertNotNull(cause)
        assertEquals("test", cause?.message)
    }

    @Test
    fun testConsumeFailsWithoutConsuming() = testSuspend {
        val channel = writer {
            writePacket {
                writeByte(1)
            }
        }

        val cause = assertFailsWith<IllegalStateException> {
            val cause: Throwable? = channel.consume {
                onClose {
                    return@onClose it
                }
            }
        }

        assertEquals("Some bytes are left not consumed", cause.message)
    }
}
