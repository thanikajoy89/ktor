/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import kotlinx.coroutines.*

public suspend fun <T> ByteReadChannel.consume(block: suspend ByteReadChannelConsumer<T>.() -> Unit): T {
    val consumer = ByteReadChannelConsumer<T>()
    block(consumer)

    val onFlushBlock = consumer.onFlushBlock
    val onCloseBlock: suspend (Throwable?) -> T = consumer.onCloseBlock
        ?: throw IllegalStateException("onClose block is not specified")

    var failed = false

    try {
        while (!isClosedForRead()) {
            awaitWhile()
            onFlushBlock(readablePacket)

            if (!readablePacket.isEmpty) {
                throw IllegalStateException("Some bytes are left not consumed")
            }
        }
    } catch (cause: Throwable) {
        failed = true
        if (this@consume.closedCause == null) {
            this@consume.cancel(CancellationException("Consumer failed", cause))
        }

        val result =  onCloseBlock(cause)
        if (cause is IllegalStateException && cause.message == "Some bytes are left not consumed") throw cause
        return result
    } finally {
        if (!failed) {
            return onCloseBlock(null)
        }
    }

    error("Unreachable code. Please log an issue at https://youtrack.jetbrains.com/issues/KTOR")
}

public class ByteReadChannelConsumer<T> {
    internal var onFlushBlock: suspend (ReadablePacket) -> Unit = { }
    internal var onCloseBlock: (suspend (Throwable?) -> T)? = null

    public fun onFlush(block: suspend (ReadablePacket) -> Unit) {
        onFlushBlock = block
    }

    public fun onClose(block: suspend (Throwable?) -> T) {
        onCloseBlock = block
    }
}
