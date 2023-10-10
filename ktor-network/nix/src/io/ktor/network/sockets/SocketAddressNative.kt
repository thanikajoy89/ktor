package io.ktor.network.sockets

public actual sealed class SocketAddress

public actual class InetSocketAddress actual constructor(
    public actual val hostname: String,
    public actual val port: Int
) : SocketAddress() {
    public actual operator fun component1(): String = hostname
    public actual operator fun component2(): Int = port
    public actual fun copy(hostname: String, port: Int): InetSocketAddress = InetSocketAddress(hostname, port)
    public override actual fun equals(other: Any?): Boolean = other is InetSocketAddress && (this === other || this.hostname == other.hostname && this.port == other.port)
    public override actual fun hashCode(): Int = toString().hashCode()
    public override actual fun toString(): String = "$hostname:$port"
}

public actual class UnixSocketAddress actual constructor(
    public actual val path: String
) : SocketAddress() {
    public actual operator fun component1(): String = path
    public actual fun copy(path: String): UnixSocketAddress = UnixSocketAddress(path)
    public override actual fun equals(other: Any?): Boolean = other is UnixSocketAddress && (this === other || this.path == other.path)
    public override actual fun hashCode(): Int = path.hashCode()
    public override actual fun toString(): String = path
}
