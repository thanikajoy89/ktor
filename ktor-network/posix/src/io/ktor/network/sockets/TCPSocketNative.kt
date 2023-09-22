/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.network.sockets

import io.ktor.network.internal.*
import io.ktor.network.selector.*
import io.ktor.utils.io.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

internal class TCPSocketNative(
    private val descriptor: Long,
    private val selector: SelectorManager,
    override val remoteAddress: SocketAddress,
    override val localAddress: SocketAddress,
    parent: CoroutineContext = EmptyCoroutineContext
) : Socket, CoroutineScope {
    private val _context: CompletableJob = Job(parent[Job])
    private val selectable: SelectableNative = SelectableNative(descriptor)

    override val coroutineContext: CoroutineContext = parent + Dispatchers.Unconfined + _context

    override val socketContext: Job
        get() = _context

    @Suppress("DEPRECATION")
    override fun attachForReading(channel: ByteChannel): WriterJob =
        attachForReadingImpl(channel, descriptor, selectable, selector)

    @Suppress("DEPRECATION")
    override fun attachForWriting(channel: ByteChannel): ReaderJob =
        attachForWritingImpl(channel, descriptor, selectable, selector)

    @OptIn(ExperimentalForeignApi::class)
    override fun close() {
        _context.complete()
        _context.invokeOnCompletion {
            shutdown(descriptor.convert(), SHUT_RDWR)
            // Descriptor is closed by the selector manager
            selector.notifyClosed(selectable)
        }
    }
}
