/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.internal

import io.ktor.network.interop.*
import io.ktor.network.util.*
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.*

@Suppress("DEPRECATION")
@OptIn(ExperimentalStdlibApi::class, ExperimentalForeignApi::class)
@EagerInitialization
internal val init_sockets = Unit.apply {
    memScoped {
        val version = 0x0202.toUShort()
        val data: WSADATA = alloc<WSADATA>()
        val result = platform.windows.WSAStartup(version, data.ptr)
        if (result != 0) throw IOException("WSAStartup failed: $result")
    }
}

public actual val SHUT_RD: Int = SD_RECEIVE

public actual val SHUT_RDWR: Int = SD_BOTH

public actual val SHUT_WR: Int = SD_SEND

public actual val INET_ADDRSTRLEN: Int = platform.windows.INET_ADDRSTRLEN

public actual val INET6_ADDRSTRLEN: Int = platform.windows.INET6_ADDRSTRLEN

public actual val SO_REUSEPORT: Int = platform.windows.SO_REUSEADDR

@OptIn(ExperimentalForeignApi::class)
public actual var in_addr.interop_s_addr: in_addr_t
    get() = S_un.S_addr.convert()
    set(value) {
        S_un.S_addr = value.convert()
    }

@OptIn(ExperimentalForeignApi::class)
public actual var sockaddr_in.interop_sin_family: sa_family_t
    get() = sin_family.convert()
    set(value) {
        sin_family = value.convert()
    }

public actual typealias addrinfo = platform.windows.addrinfo

public actual typealias in6_addr = platform.windows.in6_addr

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
public actual var sockaddr_in6.interop_sin6_family: sa_family_t
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
        ai_family = platform.windows.AF_UNSPEC
        ai_socktype = platform.windows.SOCK_STREAM
        ai_flags = AI_PASSIVE or AI_NUMERICSERV
        ai_protocol = 0
    }

    val result = alloc<CPointerVar<addrinfo>>()
    val code = getaddrinfo(hostname, portInfo.toString(), hints, result.ptr)
    when (code) {
        0 -> {}
        EAI_NONAME -> throw IOException("Unknown host $hostname")
        else -> throw IOException("Failed to resolve $hostname: ${gai_strerror?.let { it(code) }?.toKString()}")
    }

    defer { freeaddrinfo(result.value) }
    return result.pointed.toIpList()
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun getpeername(
    __fd: Long,
    __addr: CValuesRef<sockaddr>?,
    __len: CValuesRef<UIntVarOf<UInt>>?
): Int {
    return platform.posix.getpeername(__fd.convert(), __addr, __len as CValuesRef<IntVarOf<Int>>?)
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun ktor_inet_ntop(
    family: Int,
    src: CValuesRef<*>?,
    dst: CValuesRef<ByteVar>?,
    size: Int
): CPointer<ByteVar>? {
    memScoped {
        return inet_ntop(family, src?.getPointer(this), dst?.getPointer(this), size.convert())
    }
}

internal actual fun <T> unpack_sockaddr_un(sockaddr: sockaddr, block: (family: UShort, path: String) -> T): T {
    throw IOException("Unix domain sockets are not supported on Windows")
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun pack_sockaddr_un(
    family: UShort,
    path: String,
    block: (address: CPointer<sockaddr>, size: socklen_t) -> Unit
) {
    throw IOException("Unix domain sockets are not supported on Windows")
}

public actual typealias sockaddr_in6 = platform.windows.sockaddr_in6

@OptIn(ExperimentalForeignApi::class)
public actual fun getsockname(
    __fd: Int,
    __addr: CValuesRef<sockaddr>?,
    __len: CValuesRef<UIntVarOf<UInt>>?
): Int {
    return platform.windows.getsockname(__fd.convert(), __addr, __len as CValuesRef<IntVarOf<Int>>?)
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun inetNtopBridge(
    type: Int,
    address: CPointer<*>,
    addressOf: CPointer<*>,
    size: Int
) {
    @Suppress("UNCHECKED_CAST")
    inet_ntop(
        type,
        address,
        addressOf as CPointer<ByteVarOf<Byte>>,
        size.convert()
    )
}

@OptIn(ExperimentalForeignApi::class)
public actual fun recv(
    fd: Long,
    buf: CValuesRef<*>?,
    n: Long,
    flags: Int
): Long {
    return platform.windows.recv(fd.convert(), buf as CValuesRef<ByteVar>, n.convert(), flags).convert()
}

public actual fun muteSigpipe() {
}

@OptIn(ExperimentalForeignApi::class)
public actual fun shutdown(descriptor: Long, flags: Int): Int = platform.windows.shutdown(descriptor.convert(), flags)

@OptIn(ExperimentalForeignApi::class)
public actual fun interop_socket(domain: Int, type: Int, protocol: Int): Long =
    platform.windows.socket(domain, type, protocol).convert()

@OptIn(ExperimentalForeignApi::class)
public actual fun bind(descriptor: Long, pointer: CPointer<sockaddr>, size: Long): Long =
    platform.windows.bind(descriptor.convert(), pointer.reinterpret(), size.convert()).convert()

@OptIn(ExperimentalForeignApi::class)
public actual fun connect(descriptor: Long, pointer: CPointer<sockaddr>, size: Long): Long =
    platform.windows.connect(descriptor.convert(), pointer.reinterpret(), size.convert()).convert()

@OptIn(ExperimentalForeignApi::class)
public actual fun nonBlocking(descriptor: Long) {
    memScoped {
        val nonBlocking = alloc<u_longVar>()
        nonBlocking.value = 1u

        platform.windows.ioctlsocket(descriptor.convert(), platform.posix.FIONBIO.convert(), nonBlocking.ptr)
            .check()
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun send(descriptor: Long, bufferStart: CPointer<ByteVarOf<Byte>>?, size: Long, flags: Int): Long {
    return send_win(descriptor.convert(), bufferStart, size.convert(), flags).convert()
}

@OptIn(ExperimentalForeignApi::class)
public actual fun accept(
    descriptor: Long,
    clientAddress: CPointer<sockaddr>,
    clientAddressLength: CPointer<UIntVarOf<UInt>>
): Long {
    return platform.windows.accept(
        descriptor.convert(),
        clientAddress.reinterpret(),
        clientAddressLength.reinterpret()
    ).convert()
}

@OptIn(ExperimentalForeignApi::class)
public actual fun setsockopt(
    socket: Long,
    level: Int,
    optionName: Int,
    optionValue: CValuesRef<*>,
    optionLen: socklen_t
): Int {
    return setsockopt_win(
        socket.convert(),
        level,
        optionName,
        optionValue,
        optionLen.convert()
    )
}

@OptIn(ExperimentalForeignApi::class)
public actual fun recvfrom(
    socket: Long,
    buffer: CValuesRef<ByteVar>?,
    length: Long,
    flags: Int,
    srcAddr: CValuesRef<sockaddr>?,
    srcLen: CValuesRef<UIntVarOf<UInt>>?
): Long = platform.windows.recvfrom(
    socket.convert(),
    buffer,
    length.convert(),
    flags,
    srcAddr,
    srcLen as CValuesRef<IntVarOf<Int>>?
).convert()

@OptIn(ExperimentalForeignApi::class)
public actual fun sendto(
    socket: Long,
    buffer: CValuesRef<ByteVar>?,
    length: Long,
    flags: Int,
    destAddr: CValuesRef<sockaddr>?,
    destLen: Long
): Long = sendto_win(
    socket.convert(),
    buffer,
    length.convert(),
    flags,
    destAddr,
    destLen.convert()
).convert()

@OptIn(ExperimentalForeignApi::class)
public actual fun listen(socket: Long, backlog: Int): Int {
    return platform.windows.listen(socket.convert(), backlog)
}
