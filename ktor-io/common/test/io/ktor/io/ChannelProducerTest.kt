/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.test.dispatcher.*
import kotlinx.coroutines.*
import kotlin.test.*

@OptIn(DelicateCoroutinesApi::class)
class ChannelProducerTest {

    @Test
    fun testReadFromEmpty() = testSuspend {
        val channel = GlobalScope.writer { }

        with(channel) {
            assertFailsWith<EOFException> {
                readByte()
            }
            assertFailsWith<EOFException> {
                readShort()
            }
            assertFailsWith<EOFException> {
                readInt()
            }
            assertFailsWith<EOFException> {
                readLong()
            }

            assertEquals(ReadableBuffer.Empty, readBuffer())
        }
    }

    @Test
    fun testReadFromCancelled() = testSuspend {
        val channel = GlobalScope.writer {
            val exception = assertFailsWith<CancellationException> {
                writePacket { }
            }
            assertEquals("test", exception.message)
        }

        channel.cancel(CancellationException("test"))
    }

    @Test
    fun testReadByte() = testSuspend {
        fun channel() = GlobalScope.writer {
            writePacket {
                writeByte(1)
            }
        }

        with(channel()) {
            assertEquals(1, readByte().toInt())
            assertFailsWith<EOFException> {
                readByte()
            }
        }

        with(channel()) {
            assertFailsWith<EOFException> {
                readShort()
            }

            assertEquals(1, readByte().toInt())
        }
    }

    @Test
    fun testReadByteFromBuffer() = testSuspend {
        fun channel() = GlobalScope.writer {
            writePacket {
                writeByte(1)
            }
        }

        with(channel()) {
            assertEquals(1, readBuffer().readByte().toInt())
        }
    }

    @Test
    fun testWriteByteArray() = testSuspend {
        val channel = GlobalScope.writer {
            writePacket {
                writeByteArray(byteArrayOf(1, 2, 3))
            }
        }

        assertEquals(1, channel.readByte())
        assertEquals(2, channel.readByte())
        assertEquals(3, channel.readByte())

        assertFailsWith<EOFException> {
            channel.readByte()
        }
    }

    @Test
    fun testReadBuffer() = testSuspend {
        val channel = GlobalScope.writer {
            writePacket {
                writeByte(1)
            }
        }

        val buffer = channel.readBuffer()
        assertEquals(1, buffer.availableForRead)
        assertEquals(1, buffer.readByte().toInt())
    }

    @Test
    fun testExceptionDuringWriting() = testSuspend {
        val channel = writer {
            throw IOException("test")
        }

        val exception = assertFailsWith<IOException> {
            channel.readByte()
        }

        assertEquals("test", exception.message)
    }

    @Test
    fun testCancelReader() = testSuspend {
        val exception: CompletableDeferred<Throwable> = CompletableDeferred()

        val channel = writer {
            try {
                writePacket { }
            } catch (cause: Throwable) {
                exception.complete(cause)
            }
        }

        channel.cancel(CancellationException("test"))

        val cause = exception.await()
        assertTrue(cause is CancellationException)
        assertEquals("test", cause.message)
    }
}
