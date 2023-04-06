/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("ByteWriteChannelTransformKt")

package io.ktor.io

import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.jvm.*

public fun ByteWriteChannel.map(block: suspend (ReadablePacket) -> ReadablePacket): ByteWriteChannel = transform {
    onFlushBlock = block
}

public fun ByteWriteChannel.transform(
    context: CoroutineContext = Dispatchers.Default,
    block: suspend ByteWriteChannelTransformer.() -> Unit
): ByteWriteChannel = GlobalScope.reader(context) {
    val transformer = ByteWriteChannelTransformer()
    block(transformer)

    val onFlushBlock = transformer.onFlushBlock
    val onCloseBlock = transformer.onCloseBlock

    val exception = runCatching {
        consumeEach {
            val transformed = onFlushBlock(it)
            this@transform.writePacket(transformed)
            this@transform.flush()
        }
    }.exceptionOrNull()

    var failed = false
    try {
        onCloseBlock(exception)
    } catch (cause: Throwable) {
        failed = true
        this@transform.close(cause)
        throw cause
    } finally {
        if (!failed) this@transform.close()
    }
}

public class ByteWriteChannelTransformer {
    internal var onFlushBlock: suspend (ReadablePacket) -> ReadablePacket = { it }
    internal var onCloseBlock: suspend (Throwable?) -> Unit = { cause -> cause?.let { throw it } }
}
