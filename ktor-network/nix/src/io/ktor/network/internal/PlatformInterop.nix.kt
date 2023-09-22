/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.internal

import io.ktor.network.util.*
import kotlinx.cinterop.*
import platform.posix.*

public actual val SHUT_RD: Int = platform.posix.SHUT_RD

public actual val SHUT_RDWR: Int = platform.posix.SHUT_RDWR

public actual val SHUT_WR: Int = platform.posix.SHUT_WR

public actual val INET_ADDRSTRLEN: Int = platform.posix.INET_ADDRSTRLEN

public actual val INET6_ADDRSTRLEN: Int = platform.posix.INET6_ADDRSTRLEN

public actual val SO_REUSEPORT: Int = platform.posix.SO_REUSEPORT

@OptIn(ExperimentalForeignApi::class)
public actual fun recv(
    fd: Long,
    buf: CValuesRef<*>?,
    n: Long,
    flags: Int
): Long {
    return platform.posix.recv(fd.convert(), buf, n.convert(), flags).convert()
}

@OptIn(ExperimentalForeignApi::class)
public actual fun shutdown(descriptor: Long, flags: Int): Int = platform.posix.shutdown(descriptor.convert(), flags)

@OptIn(ExperimentalForeignApi::class)
public actual fun muteSigpipe() {
    signal(SIGPIPE, SIG_IGN)
}

@OptIn(ExperimentalForeignApi::class)
public actual fun interop_socket(domain: Int, type: Int, protocol: Int): Long =
    platform.posix.socket(domain, type, protocol).convert()

@OptIn(ExperimentalForeignApi::class)
public actual fun bind(descriptor: Long, pointer: CPointer<sockaddr>, size: Long): Long =
    platform.posix.bind(descriptor.convert(), pointer.reinterpret(), size.convert()).convert()

@OptIn(ExperimentalForeignApi::class)
public actual fun connect(descriptor: Long, pointer: CPointer<sockaddr>, size: Long): Long =
    platform.posix.connect(descriptor.convert(), pointer.reinterpret(), size.convert()).convert()

@OptIn(ExperimentalForeignApi::class)
public actual fun nonBlocking(descriptor: Long) {
    platform.posix.fcntl(descriptor.convert(), F_SETFL, O_NONBLOCK)
}

@OptIn(ExperimentalForeignApi::class)
public actual var in_addr.interop_s_addr: io.ktor.network.internal.in_addr_t
    get() = s_addr.convert()
    set(value) {
        s_addr = value.convert()
    }

@OptIn(ExperimentalForeignApi::class)
public actual var sockaddr_in.interop_sin_family: io.ktor.network.internal.sa_family_t
    get() = sin_family.convert()
    set(value) {
        sin_family = value.convert()
    }

public actual typealias addrinfo = platform.posix.addrinfo

public actual typealias in6_addr = platform.posix.in6_addr

public actual var sockaddr_in6.interop_sin6_scope_id: uint32_t
    get() = this.sin6_scope_id
    set(value) {
        this.sin6_scope_id = value
    }

public actual var sockaddr_in6.interop_sin6_flowinfo: uint32_t
    get() = this.sin6_flowinfo
    set(value) {
        this.sin6_flowinfo = value
    }

public actual var sockaddr_in6.interop_sin6_port: UShort
    get() = this.sin6_port
    set(value) {
        this.sin6_port = value
    }

public actual val sockaddr_in6.interop_sin6_addr: in6_addr
    get() = this.sin6_addr

@OptIn(ExperimentalForeignApi::class)
public actual var sockaddr_in6.interop_sin6_family: io.ktor.network.internal.sa_family_t
    get() = this.sin6_family.convert()
    set(value) {
        this.sin6_family = value.convert()
    }

@OptIn(ExperimentalForeignApi::class)
public actual val addrinfo.interop_ai_next: CPointer<addrinfo>?
    get() = this.ai_next

@OptIn(ExperimentalForeignApi::class)
public actual val addrinfo.interop_ai_addr: CPointer<sockaddr>?
    get() = this.ai_addr

@OptIn(ExperimentalForeignApi::class)
internal actual fun getAddressInfo(
    hostname: String,
    portInfo: Int
): List<NativeSocketAddress> = memScoped {
    val hints: CValue<addrinfo> = cValue {
        ai_family = AF_UNSPEC
        ai_socktype = SOCK_STREAM
        ai_flags = AI_PASSIVE or AI_NUMERICSERV
        ai_protocol = 0
    }

    val result = alloc<CPointerVar<addrinfo>>()
    getaddrinfo(hostname, portInfo.toString(), hints, result.ptr)
        .check()

    defer { freeaddrinfo(result.value) }
    return result.pointed.toIpList()
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun getpeername(
    __fd: Long,
    __addr: CValuesRef<sockaddr>?,
    __len: CValuesRef<UIntVarOf<UInt>>?
): Int {
    return platform.posix.getpeername(__fd.convert(), __addr, __len)
}

public actual typealias sockaddr_in6 = platform.posix.sockaddr_in6

@OptIn(ExperimentalForeignApi::class)
public actual fun getsockname(
    __fd: Int,
    __addr: CValuesRef<sockaddr>?,
    __len: CValuesRef<UIntVarOf<UInt>>?
): Int {
    return platform.posix.getsockname(__fd, __addr, __len)
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun send(descriptor: Long, bufferStart: CPointer<ByteVarOf<Byte>>?, size: Long, flags: Int): Long {
    return platform.posix.send(descriptor.convert(), bufferStart, size.convert(), flags)
}

@OptIn(ExperimentalForeignApi::class)
public actual fun accept(
    descriptor: Long,
    clientAddress: CPointer<sockaddr>,
    clientAddressLength: CPointer<UIntVarOf<UInt>>
): Long {
    return platform.posix.accept(descriptor.convert(), clientAddress, clientAddressLength).convert()
}

@OptIn(ExperimentalForeignApi::class)
public actual fun setsockopt(
    socket: Long,
    level: Int,
    optionName: Int,
    optionValue: CValuesRef<*>,
    optionLen: socklen_t
): Int {
    return platform.posix.setsockopt(socket.convert(), level, optionName, optionValue, optionLen.convert())
}

@OptIn(ExperimentalForeignApi::class)
public actual fun recvfrom(
    socket: Long,
    buffer: CValuesRef<ByteVar>?,
    length: Long,
    flags: Int,
    srcAddr: CValuesRef<sockaddr>?,
    srcLen: CValuesRef<UIntVarOf<UInt>>?
): Long {
    return platform.posix.recvfrom(socket.convert(), buffer, length.convert(), flags, srcAddr, srcLen).convert()
}

@OptIn(ExperimentalForeignApi::class)
public actual fun sendto(
    socket: Long,
    buffer: CValuesRef<ByteVar>?,
    length: Long,
    flags: Int,
    destAddr: CValuesRef<sockaddr>?,
    destLen: Long
): Long = platform.posix.sendto(
    socket.convert(),
    buffer,
    length.convert(),
    flags,
    destAddr,
    destLen.convert()
).convert()

@OptIn(ExperimentalForeignApi::class)
public actual fun listen(socket: Long, backlog: Int): Int {
    return platform.posix.listen(socket.convert(), backlog)
}
