/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.test.dispatcher.*
import kotlinx.coroutines.*
import kotlin.test.*

class ConflatedChannelTest {

    @Test
    fun testWriteToClosedChannel() = testSuspend {
        val channel = ConflatedByteChannel()
        channel.close()

        assertFailsWith<IllegalStateException> {
            channel.writeByte(1)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeShort(1)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeInt(1)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeLong(1)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeDouble(1.0)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeFloat(1.0f)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeByteArray(ByteArray(0))
        }

        assertFailsWith<IllegalStateException> {
            channel.writeBuffer(Buffer.Empty)
        }

        assertFailsWith<IllegalStateException> {
            channel.writePacket(Packet())
        }

        assertFailsWith<IllegalStateException> {
            channel.writeString("")
        }

        Unit
    }

    @Test
    fun testCancelDuringFlush() = testSuspend {
        val channel = ConflatedByteChannel()
        val job = async {
            channel.writeByte(42)
            channel.flush()
        }

        delay(1000)
        channel.cancel()

        assertFailsWith<CancellationException> {
            job.await()
        }
    }
}
