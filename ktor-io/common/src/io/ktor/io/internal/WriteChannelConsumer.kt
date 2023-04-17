/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.internal

import io.ktor.io.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

internal class WriteChannelConsumer(
    override val coroutineContext: CoroutineContext,
    private val block: suspend ByteChannelConsumer.() -> Unit
) : ByteWriteChannel, ByteChannelConsumer, CoroutineScope {

    private val state: AtomicRef<Any?> = atomic(null)

    init {
        launch {
            val cause = runCatching { block() }.exceptionOrNull()
            finishConsumer(cause)
        }
    }

    override val closedCause: Throwable?
        get() = state.value?.let { (it as? ClosedToken)?.cause }

    override val writablePacket: Packet = Packet()

    override fun isClosedForWrite(): Boolean {
        closedCause?.let { throw it }
        return state.value is ClosedToken
    }

    override suspend fun flushAndClose() {
        flush()
        close()
    }

    override fun close(cause: Throwable?) {
        check(cause != null || writablePacket.isEmpty) { "Cannot close channel with pending bytes. Use flush instead." }
        if (cause == null && writablePacket.isNotEmpty) {
            launch { flushAndClose() }
            return
        }

        while (true) {
            when (val value = state.value) {
                null -> {
                    // consumer can try consume
                    if (!state.compareAndSet(null, ClosedToken(cause))) continue
                    writablePacket.close()
                    return
                }

                is CancellableContinuation<*> -> {
                    // consumer is consuming
                    val continuation = value as CancellableContinuation<Unit>
                    if (state.compareAndSet(value, ClosedToken(cause))) {
                        writablePacket.close()
                        continuation.resume(Unit)
                        return
                    }
                }

                is ClosedToken -> return
                else -> error("Unexpected state: $value. Internal error. Please log an issue: https://youtrack.jetbrains.com/newIssue?project=KTOR")
            }
        }

    }

    override suspend fun flush() {
        if (writablePacket.isEmpty) return

        // Suspending until consumer come, consume [writablePacket] and resume us continuation.
        suspendCancellableCoroutine { continuation ->
            while (true) {
                when (val value = state.value) {
                    // Consumer is not waiting
                    null -> {
                        // Try to set our continuation to be resumed by consumer
                        if (!state.compareAndSet(null, continuation)) continue
                        break
                    }
                    // Consumer is waiting for a packet
                    is CancellableContinuation<*> -> {
                        // Set our continuation to be resumed by consumer
                        state.value = continuation

                        // Resume consumer to process our [writablePacket]
                        @Suppress("UNCHECKED_CAST")
                        (value as CancellableContinuation<Unit>).resume(Unit)
                        break
                    }
                    // Close token inside -> rethrow exception, no update needed
                    is ClosedToken -> {
                        value.cause?.let { throw it }
                        continuation.resume(Unit)
                        break
                    }

                    else -> error("Unexpected state: $value. Internal error. Please log an issue: https://youtrack.jetbrains.com/newIssue?project=KTOR")
                }
            }
        }
    }

    // These methods are called only from reader.
    override suspend fun consumeEach(block: suspend (Packet) -> Unit) {
        while (!isClosedForWrite()) {
            consume(block)
        }
    }

    override suspend fun consume(block: suspend (Packet) -> Unit) {
        while (true) {
            when (val value = state.value) {
                // No flush, we should wait.
                null -> {
                    // Try to set our continuation to be resumed by writer
                    suspendCancellableCoroutine<Unit> {
                        if (!state.compareAndSet(null, it)) {
                            it.resume(Unit)
                        }
                    }
                }
                // Writer is waiting until we consume [writablePacket].
                is CancellableContinuation<*> -> {
                    val cause = runCatching {
                        block(writablePacket)
                    }

                    if (cause.isFailure) {
                        val exception = cause.exceptionOrNull()!!
                        val result = ClosedToken(exception)
                        state.value = result
                        writablePacket.close()
                        value.resumeWithException(exception)
                        throw exception
                    }

                    check(writablePacket.isEmpty) { "Packet should be empty after consume. Remaining ${writablePacket.availableForRead} bytes" }

                    // Resume writer from flush.
                    state.value = null
                    (value as CancellableContinuation<Unit>).resume(Unit)
                    break
                }
                // Close token inside -> rethrow exception, no update needed
                is ClosedToken -> {
                    value.cause?.let { throw it }
                    break
                }

                else -> {
                    error("Unexpected state: $value. Internal error. Please long an issue: https://youtrack.jetbrains.com/issues/KTOR")
                }
            }
        }
    }

    // All operations in finishConsumer should be only through CAS and after CAS, because we're not holding the writer.
    private fun finishConsumer(cause: Throwable? = null) {
        while (true) {
            when (val value = state.value) {
                null -> {
                    // no writer is trying to flush.
                    if (!state.compareAndSet(null, ClosedToken(cause))) continue
                    writablePacket.close()
                    return
                }

                is CancellableContinuation<*> -> {
                    // resume writer with token
                    val continuation = value as CancellableContinuation<Unit>
                    if (!state.compareAndSet(value, ClosedToken(cause))) continue
                    writablePacket.close()
                    if (cause != null) {
                        continuation.resumeWithException(cause)
                    } else {
                        continuation.resume(Unit)
                    }
                }

                is ClosedToken -> return
                else -> error("Unexpected state: $value. Internal error. Please log an issue: https://youtrack.jetbrains.com/newIssue?project=KTOR")
            }
        }
    }
}
