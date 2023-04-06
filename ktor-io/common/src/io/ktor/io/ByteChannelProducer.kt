/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.*


@OptIn(ExperimentalCoroutinesApi::class)
public fun CoroutineScope.writer(
    context: CoroutineContext = Dispatchers.Default,
    block: suspend ByteChannelProducer.() -> Unit
): ByteReadChannel = object : ByteReadChannel, ByteChannelProducer {
    private val data = Channel<ReadablePacket>()
    private val _closedCause = atomic<Throwable?>(null)

    init {
        launch(context) {
            try {
                block()
            } catch (cause: Throwable) {
                writerCancel(cause)
            } finally {
                writerClose()
            }
        }
    }

    override val closedCause: Throwable?
        get() = _closedCause.value

    override val readablePacket: Packet = Packet()

    override fun isClosedForRead(): Boolean {
        closedCause?.let { throw it }
        return readablePacket.isEmpty && data.isClosedForReceive
    }

    override suspend fun awaitBytesWhile(predicate: () -> Boolean) {
        try {
            while (predicate()) {
                val result = data.receiveCatching()
                result.exceptionOrNull()?.let { throw it }
                result.getOrNull()?.let { readablePacket.writePacket(it) }

                if (result.isClosed) break
            }
        } catch (cause: Throwable) {
            readablePacket.close()
            _closedCause.compareAndSet(null, cause)
            throw cause
        }
    }

    override fun cancel(cause: CancellationException?) {
        _closedCause.compareAndSet(null, cause)
        data.cancel(cause)
    }

    // Accessible only from writer coroutine.

    override suspend fun writePacket(packet: ReadablePacket) {
        data.send(packet)
    }

    private fun writerCancel(cause: Throwable) {
        _closedCause.compareAndSet(null, cause)
        data.close(cause)
    }

    private fun writerClose() {
        data.close()
    }
}

public interface ByteChannelProducer {
    public suspend fun writePacket(packet: ReadablePacket)
}

public suspend fun ByteChannelProducer.writeBuffer(buffer: ReadableBuffer) {
    val packet = buildPacket {
        writeBuffer(buffer)
    }

    writePacket(packet)
}

public suspend inline fun ByteChannelProducer.writePacket(block: Packet.() -> Unit) {
    val packet = buildPacket {
        block()
    }

    writePacket(packet)
}
