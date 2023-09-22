/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.*

internal class SocketJs(
    private val socket: dynamic, // net.Socket
    override val socketContext: Job = Job(),
) : Socket {
    override val coroutineContext: CoroutineContext = socketContext

    override val remoteAddress: SocketAddress
        get() = TODO()

    override val localAddress: SocketAddress
        get() = TODO()

    private val incoming = Channel<ByteArray>(Channel.UNLIMITED)

    init {
        println("Create socket $this")
        socket.on("close") {
            println("Close $this")
        }

        socket.on("error") { error: dynamic ->
            println("Error $error $this")
        }

        socket.on("data") { data ->
            incoming.trySend(data)
        }
    }

    override fun close() {
        socket.close()
    }

    override fun read(): ByteReadChannel = ReadChannel { packet ->
        val data = incoming.receive()
        packet.writeFully(data)
    }

    override fun write(): ByteWriteChannel = WriteChannel {
        socket.write(it.readBytes())
    }
}
