package io.ktor.io

import io.ktor.io.internal.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

public interface ByteChannelConsumer {
    public suspend fun consume(block: suspend (Packet) -> Unit)
    public suspend fun consumeEach(block: suspend (Packet) -> Unit)
}

public fun CoroutineScope.reader(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend ByteChannelConsumer.() -> Unit
): ByteWriteChannel = WriteChannelConsumer(this.coroutineContext + coroutineContext, block)
