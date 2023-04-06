/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.charsets.*
import kotlinx.coroutines.*

/**
 * Creates channel for reading from the specified byte array. Please note that it could use [content] directly
 * or copy its bytes depending on the platform.
 */
public fun ByteReadChannel(content: ByteArray, offset: Int = 0, length: Int = content.size): ByteReadChannel {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= content.size) {
        "offset + length shouldn't be greater than content size: ${content.size}"
    }

    return GlobalScope.writer {
        writePacket {
            writeByteArray(content, offset, length)
        }
    }
}

public fun ByteReadChannel(packet: Packet): ByteReadChannel = object : ByteReadChannel {
    override var closedCause: Throwable? = null
        private set

    override val readablePacket: Packet = packet

    override fun isClosedForRead(): Boolean {
        closedCause?.let { throw it }
        return packet.isEmpty
    }

    override suspend fun awaitBytesWhile(predicate: () -> Boolean) {
        closedCause?.let { throw it }
    }

    override fun cancel(cause: CancellationException?) {
        if (closedCause != null || packet.isEmpty) return
        readablePacket.close()
        closedCause = cause
    }
}

public fun ByteReadChannel(text: String, charset: Charset = Charsets.UTF_8): ByteReadChannel = GlobalScope.writer {
    writePacket {
        writeString(text, charset = charset)
    }
}
