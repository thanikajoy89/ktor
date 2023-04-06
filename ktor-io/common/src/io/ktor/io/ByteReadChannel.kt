package io.ktor.io

import io.ktor.io.internal.*
import kotlinx.coroutines.*

/**
 * Channel for asynchronous reading of sequences of bytes.
 * This is a **single-reader channel**.
 *
 * Operations on this channel cannot be invoked concurrently.
 */
public interface ByteReadChannel {
    /**
     * A closure causes exception or `null` if closed successfully or not yet closed
     */
    public val closedCause: Throwable?

    public val readablePacket: ReadablePacket

    /**
     * Return `true` if channel is closed for reading and there are no more bytes available for reading.
     *
     * if [closedCause] is not `null` it will be thrown.
     */
    public fun isClosedForRead(): Boolean

    /**
     * Wait more bytes from source until [predicate] returns true or channel is closed.
     *
     * @return `true` if there are bytes available for reading or `false` if EOF reached.
     * @throws closedCause if channel is closed with an exception.
     */
    public suspend fun awaitBytesWhile(predicate: () -> Boolean)

    public suspend fun awaitBytes() {
        awaitBytesWhile { isEmpty }
    }

    /**
     * Close channel with optional [cause] cancellation. Unlike [ByteWriteChannel.close] that could close channel
     * normally, cancel does always close with error so any operations on this channel will always fail
     * and all suspensions will be resumed with exception.
     *
     * Please note that if the channel has been provided by [reader] or [writer] then the corresponding owning
     * coroutine will be cancelled as well
     *
     * @see ByteWriteChannel.close
     */
    public fun cancel(cause: CancellationException? = null)

    public companion object {
        public val Empty: ByteReadChannel = EmptyByteReadChannel
    }
}
