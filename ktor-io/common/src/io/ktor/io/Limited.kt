/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import kotlinx.coroutines.*

public fun ByteReadChannel.limited(length: Long): ByteReadChannel = object : ByteReadChannel {
    var remaining = length

    override var closedCause: Throwable? = null
        private set

    override val readablePacket: Packet = Packet()

    override fun isClosedForRead(): Boolean {
        closedCause?.let { throw it }
        return readablePacket.isEmpty && remaining <= 0
    }

    override suspend fun awaitBytesWhile(predicate: () -> Boolean) {
        val source = this@limited
        try {
            while (!source.isClosedForRead() && predicate() && remaining > 0) {
                if (source.isEmpty) source.awaitBytes()
                if (remaining > source.availableForRead) {
                    remaining -= source.availableForRead
                    readablePacket.writePacket(source.readablePacket)
                } else {
                    val packet = source.readPacket(remaining.toInt())
                    remaining = 0
                    readablePacket.writePacket(packet)
                }
            }
        } catch (cause: Throwable) {
            closedCause = cause
            throw cause
        }
    }

    override fun cancel(cause: CancellationException?) {
        if (closedCause != null) return

        this@limited.cancel(cause)
        closedCause = cause
    }
}
