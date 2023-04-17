/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.test.dispatcher.*
import kotlinx.coroutines.*
import kotlin.test.*

class ChannelConsumerTest {

    @Test
    fun testConsumeEmpty() = testSuspend {
        val channel = reader {
            consume {
                fail()
            }
        }

        channel.close()
    }

    @Test
    fun testWriteByteAndClose() = testSuspend {
        val channel = reader {
            consume {
                assertEquals(1, it.availableForRead)
                assertEquals(1, it.readByte())
            }

            consume {
                fail()
            }
        }

        channel.writeByte(1)
        channel.flushAndClose()
    }

    @Test
    fun testFlushBeforeClose() = testSuspend {
        val channel = reader {
            consume {
                assertEquals(1, it.availableForRead)
                assertEquals(1, it.readByte())
            }

            consume {
                fail()
            }
        }

        channel.writeByte(1)
        channel.flush()
        channel.close()
    }

    @Test
    fun testWriteMultipleTimes() = testSuspend {
        val channel = reader {
            consume {
                assertEquals(1, it.availableForRead)
                assertEquals(1, it.readByte())
            }

            consume {
                assertEquals(1, it.availableForRead)
                assertEquals(2, it.readByte())
            }

            consume {
                fail()
            }
        }

        channel.writeByte(1)
        channel.flush()
        channel.writeByte(2)
        channel.flushAndClose()
    }

    @Test
    fun testConsumeFailsWithException() = testSuspend {
        val channel = reader {
            consume {
                throw IOException("test")
            }

            consume {
                fail()
            }
        }

        channel.writeByte(1)
        val exception = assertFailsWith<IOException> {
            channel.flush()
        }

        assertEquals("test", exception.message)
        assertTrue(channel.writablePacket.isEmpty)
    }

    @Test
    fun testReaderFailsWithException() = testSuspend {
        val channel = reader {
            throw IOException("test")
        }

        channel.writeByte(1)
        val exception = assertFailsWith<IOException> {
            channel.flush()
        }

        assertEquals("test", exception.message)
        assertTrue(channel.writablePacket.isEmpty)
    }

    @Test
    fun testWriteChannelCancelled() = testSuspend {
        val exception = CompletableDeferred<Throwable?>()
        val channel = reader {
            try {
                consume {
                    fail()
                }
            } catch (cause: Throwable) {
                exception.complete(cause)
            }
        }

        channel.writeByte(1)
        channel.close(IOException("test"))

        val actual = exception.await()
        assertNotNull(actual)
        assertEquals("test", actual.message)
    }
}
