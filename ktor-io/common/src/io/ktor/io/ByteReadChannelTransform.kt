/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import kotlinx.coroutines.*
import kotlin.coroutines.*

public fun ByteReadChannel.map(
    block: suspend (ReadablePacket) -> ReadablePacket
): ByteReadChannel = transform {
    onFlush(block)
}

@OptIn(DelicateCoroutinesApi::class)
public fun ByteReadChannel.transform(
    coroutineContext: CoroutineContext = Dispatchers.Default,
    block: suspend ByteReadChannelTransformer.() -> Unit
): ByteReadChannel = GlobalScope.writer(coroutineContext) {
    val consumer = ByteReadChannelTransformer()
    block(consumer)

    val consumerFlushBlock = consumer.onFlushBlock
    val consumerCloseBlock = consumer.onCloseBlock

    try {
        consume {
            onFlush { packet ->
                val transformed = consumerFlushBlock(packet)
                writePacket(transformed)
            }
            onClose {
                consumerCloseBlock(it)
            }
        }
    } catch (cause: Throwable) {
        if (closedCause == null) {
            cancel(CancellationException("Consumer failed", cause))
        }
        throw cause
    }
}

public class ByteReadChannelTransformer {
    internal var onFlushBlock: suspend (ReadablePacket) -> ReadablePacket = { packet: ReadablePacket -> packet }
    internal var onCloseBlock: suspend (Throwable?) -> Unit = { cause: Throwable? -> cause?.let { throw it } }

    public fun onFlush(block: suspend (ReadablePacket) -> ReadablePacket) {
        onFlushBlock = block
    }

    public fun onClose(block: suspend (Throwable?) -> Unit) {
        onCloseBlock = block
    }
}
