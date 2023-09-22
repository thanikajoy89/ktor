/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import kotlin.math.*

private val MAX_PACKET_SIZE = 16 * 1024

internal fun WriteChannel(
    onFlush: suspend (buffer: ByteReadPacket) -> Unit
): ByteWriteChannel = object : ByteWriteChannel {
    private val closed = false
    val state = BytePacketBuilder()

    override val availableForWrite: Int get() = max(0, MAX_PACKET_SIZE - state.size)

    override val isClosedForWrite: Boolean get() = closed
    override val autoFlush: Boolean = false

    override var totalBytesWritten: Long = 0
        private set

    override var closedCause: Throwable? = null
        private set

    override suspend fun writeAvailable(src: ByteArray, offset: Int, length: Int): Int {
        if (availableForWrite == 0) {
            flush()
        }

        state.writeFully(src, offset, length)
        return length
    }

    override suspend fun writeAvailable(src: ChunkBuffer): Int {
        error("[ChunkBuffer] is not supported")
    }

    override suspend fun writeFully(src: ByteArray, offset: Int, length: Int) {
        if (availableForWrite < length) {
            flush()
        }

        state.writeFully(src, offset, length)
    }

    override suspend fun writeFully(src: Buffer) {
        error("[Buffer] is not supported")
    }

    override suspend fun writeFully(memory: Memory, startIndex: Int, endIndex: Int) {

    }

    override suspend fun writeSuspendSession(visitor: suspend WriterSuspendSession.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun writePacket(packet: ByteReadPacket) {
        if (availableForWrite < packet.remaining) {
            flush()
        }

        state.writePacket(packet)
    }

    override suspend fun writeLong(l: Long) {
        if (availableForWrite < 8) {
            flush()
        }

        state.writeLong(l)
    }

    override suspend fun writeInt(i: Int) {
        if (availableForWrite < 4) {
            flush()
        }

        state.writeInt(i)
    }

    override suspend fun writeShort(s: Short) {
        if (availableForWrite < 2) {
            flush()
        }

        state.writeShort(s)
    }

    override suspend fun writeByte(b: Byte) {
        if (availableForWrite < 1) {
            flush()
        }

        state.writeByte(b)
    }

    override suspend fun writeDouble(d: Double) {
        if (availableForWrite < 8) {
            flush()
        }

        state.writeDouble(d)
    }

    override suspend fun writeFloat(f: Float) {
        if (availableForWrite < 4) {
            flush()
        }

        state.writeFloat(f)
    }

    override suspend fun awaitFreeSpace() {
        flush()
    }

    override suspend fun close(cause: Throwable?): Boolean {
        if (closed) return false
        flush()
        closedCause = cause
        return true
    }

    override suspend fun flush() {
        if (state.isEmpty) return
        val packet = state.build()
        state.reset()
        totalBytesWritten += packet.remaining
        onFlush(packet)
    }
}

internal fun ReadChannel(onRead: suspend (buffer: BytePacketBuilder) -> Unit): ByteReadChannel {
    TODO()
}
