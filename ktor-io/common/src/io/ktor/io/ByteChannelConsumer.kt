package io.ktor.io

import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.*

public interface ByteChannelConsumer {
    public suspend fun consume(block: suspend (Packet) -> Unit)
    public suspend fun consumeEach(block: suspend (Packet) -> Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
public fun CoroutineScope.reader(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend ByteChannelConsumer.() -> Unit
): ByteWriteChannel = object : ByteWriteChannel, ByteChannelConsumer {
    private val flushChannel = Channel<Packet>()
    private val consumeDone = Channel<Unit>()

    private val closed = atomic<ClosedToken?>(null)

    init {
        launch(coroutineContext) {
            try {
                block()
            } catch (cause: Throwable) {
                finishConsumer(cause)
            } finally {
                finishConsumer()
            }
        }
    }

    override val closedCause: Throwable?
        get() = closed.value?.cause

    override val writablePacket: Packet = Packet()

    override fun isClosedForWrite(): Boolean {
        closedCause?.let { throw it }
        return closed.value != null
    }

    override suspend fun flushAndClose(): Boolean {
        flush()
        closeFlushed()
        return true
    }

    override fun close(cause: Throwable?) {
        launch {
            if (cause == null) {
                flushAndClose()
            } else {
                closeFlushed(cause)
            }
        }
    }

    override suspend fun flush() {
        if (writablePacket.isEmpty) return

        try {
            flushChannel.send(writablePacket.steal())
            consumeDone.receiveCatching().exceptionOrNull()?.let { throw it }
        } catch (cause: Throwable) {
            closeFlushed(cause)
            throw cause
        }
    }

    private fun closeFlushed(cause: Throwable? = null) {
        closed.compareAndSet(null, ClosedToken(cause))
        flushChannel.close(cause)
        writablePacket.close()
    }

    // These methods are called only from reader.

    override suspend fun consumeEach(block: suspend (Packet) -> Unit) {
        while (!consumeDone.isClosedForSend) {
            consume(block)
        }
    }

    override suspend fun consume(block: suspend (Packet) -> Unit) {
        val result = flushChannel.receiveCatching()
        result.exceptionOrNull()?.let { throw it }
        val packet = result.getOrNull()
        if (packet != null) {
            block(packet)
            consumeDone.send(Unit)
        } else {
            consumeDone.close()
        }
    }

    fun finishConsumer(cause: Throwable? = null) {
        val cancelCause = if (cause == null) {
            null
        } else {
            CancellationException("Consumer has failed", cause)
        }

        if (!flushChannel.isClosedForReceive) {
            flushChannel.cancel(cancelCause)
        }

        if (!consumeDone.isClosedForSend) {
            consumeDone.close(cancelCause)
        }
    }
}

private class ClosedToken(val cause: Throwable?)
