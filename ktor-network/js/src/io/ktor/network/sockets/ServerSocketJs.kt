/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

internal val net: dynamic = js("eval('require')('net')")

internal class ServerSocketJs(
    override val localAddress: SocketAddress,
    socketOptions: SocketOptions.AcceptorOptions,
) : ServerSocket {
    private val channel = Channel<SocketJs>()
    private val server: dynamic = net.createServer { socket ->
        println("accepted")
        val socketJs = SocketJs(socket, Job(socketContext))

        CoroutineScope(socketContext).launch {
            channel.send(socketJs)
        }
    }

    override val socketContext: Job = Job()

    init {
        check(localAddress is InetSocketAddress)
        server.listen(localAddress.port, localAddress.hostname)
    }

    override suspend fun accept(): Socket = channel.receive()

    override fun close() {
        (socketContext as CompletableJob).complete()
        server.close()
    }
}
