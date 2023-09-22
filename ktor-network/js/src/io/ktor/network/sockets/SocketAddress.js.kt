/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

public actual sealed class SocketAddress

public actual class InetSocketAddress actual constructor(hostname: String, port: Int) : SocketAddress() {
    /**
     * The hostname of the socket address.
     *
     * Note that this may trigger a name service reverse lookup.
     */
    public actual val hostname: String = hostname

    /**
     * The port number of the socket address.
     */
    public actual val port: Int = port

    /**
     * The hostname of the socket address.
     *
     * Note that this may trigger a name service reverse lookup.
     */
    public actual operator fun component1(): String = hostname

    /**
     * The port number of the socket address.
     */
    public actual operator fun component2(): Int = port

    /**
     * Create a copy of [InetSocketAddress].
     *
     * Note that this may trigger a name service reverse lookup.
     */
    public actual fun copy(hostname: String, port: Int): InetSocketAddress = InetSocketAddress(hostname, port)
}

public actual class UnixSocketAddress actual constructor(path: String) : SocketAddress() {
    /**
     * The path of the socket address.
     */
    public actual val path: String = path

    /**
     * The path of the socket address.
     */
    public actual operator fun component1(): String = path

    /**
     * Create a copy of [UnixSocketAddress].
     */
    public actual fun copy(path: String): UnixSocketAddress = UnixSocketAddress(path)
}
