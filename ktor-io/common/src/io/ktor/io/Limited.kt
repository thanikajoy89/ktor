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

    override suspend fun awaitWhile(predicate: () -> Boolean): Boolean {
        try {
            while (!predicate() && remaining > 0) {
                if (this@limited.readablePacket.isEmpty) this@limited.awaitWhile()
                if (remaining > this@limited.readablePacket.availableForRead) {
                    remaining -= this@limited.readablePacket.availableForRead
                    readablePacket.writePacket(this@limited.readablePacket)
                } else {
                    val packet = this@limited.readPacket(remaining.toInt())
                    remaining = 0
                    readablePacket.writePacket(packet)
                }
            }
        } catch (cause: Throwable) {
            closedCause = cause
            throw cause
        }

        return readablePacket.isNotEmpty
    }

    override fun cancel(cause: CancellationException?) {
        if (closedCause != null) return

        this@limited.cancel(cause)
        closedCause = cause
    }
}
