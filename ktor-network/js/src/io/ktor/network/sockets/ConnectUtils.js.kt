/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.network.selector.*
import io.ktor.util.*
import kotlin.coroutines.*

internal actual suspend fun connect(
    selector: SelectorManager,
    remoteAddress: SocketAddress,
    socketOptions: SocketOptions.TCPClientSocketOptions
): Socket {
    checkIsNodeJs()
    check(remoteAddress is InetSocketAddress)

    val client = net.Socket()

    suspendCoroutine { proceed ->
        client.connect(remoteAddress.port, remoteAddress.hostname) {
            proceed.resume(Unit)
        }
    }
    return SocketJs(client)
}

internal actual fun bind(
    selector: SelectorManager,
    localAddress: SocketAddress?,
    socketOptions: SocketOptions.AcceptorOptions
): ServerSocket {
    checkIsNodeJs()
    check(localAddress is InetSocketAddress)

    return ServerSocketJs(localAddress, socketOptions)
}

internal fun checkIsNodeJs() {
    if (!PlatformUtils.IS_NODE) throw UnsupportedOperationException("Only supported on Node.js")
}
