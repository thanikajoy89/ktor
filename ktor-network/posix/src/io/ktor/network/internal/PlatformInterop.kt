/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.internal

import io.ktor.network.util.NativeSocketAddress
import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
internal expect fun inetNtopBridge(type: Int, address: CPointer<*>, addressOf: CPointer<*>, size: Int)

public expect val SHUT_RD: Int

public expect val SHUT_RDWR: Int

public expect val SHUT_WR: Int

public expect val INET_ADDRSTRLEN: Int

public expect val INET6_ADDRSTRLEN: Int

public expect fun shutdown(descriptor: Long, flags: Int): Int

public expect fun muteSigpipe()

public expect val SO_REUSEPORT: Int

@OptIn(ExperimentalForeignApi::class)
public expect fun recv(
    fd: Long,
    buf: CValuesRef<*>?,
    n: Long,
    flags: Int
): Long

public expect fun interop_socket(domain: Int, type: Int, protocol: Int): Long

@OptIn(ExperimentalForeignApi::class)
public expect fun bind(descriptor: Long, pointer: CPointer<sockaddr>, size: Long): Long

@OptIn(ExperimentalForeignApi::class)
public expect fun connect(descriptor: Long, pointer: CPointer<sockaddr>, size: Long): Long

public expect fun nonBlocking(descriptor: Long)

public typealias sa_family_t = Short

public typealias socklen_t = Long

public typealias in_addr_t = Int

public expect var in_addr.interop_s_addr: in_addr_t

public expect var sockaddr_in.interop_sin_family: sa_family_t

@OptIn(ExperimentalForeignApi::class)
internal expect fun ktor_inet_ntop(
    family: Int,
    src: CValuesRef<*>?,
    dst: CValuesRef<ByteVar>?,
    size: Int
): CPointer<ByteVar>?

@OptIn(ExperimentalForeignApi::class)
internal expect fun pack_sockaddr_un(
    family: UShort,
    path: String,
    block: (address: CPointer<sockaddr>, size: socklen_t) -> Unit
)

internal expect fun <T> unpack_sockaddr_un(sockaddr: sockaddr, block: (family: UShort, path: String) -> T): T

@OptIn(ExperimentalForeignApi::class)
public expect class addrinfo : CStructVar

@OptIn(ExperimentalForeignApi::class)
public expect class in6_addr : CStructVar

public expect var sockaddr_in6.interop_sin6_scope_id: uint32_t

public expect var sockaddr_in6.interop_sin6_flowinfo: uint32_t

public expect var sockaddr_in6.interop_sin6_port: UShort

public expect val sockaddr_in6.interop_sin6_addr: in6_addr

public expect var sockaddr_in6.interop_sin6_family: sa_family_t

@OptIn(ExperimentalForeignApi::class)
public expect val addrinfo.interop_ai_next: CPointer<addrinfo>?

@OptIn(ExperimentalForeignApi::class)
public expect val addrinfo.interop_ai_addr: CPointer<sockaddr>?

@OptIn(ExperimentalForeignApi::class)
internal expect fun getpeername(
    __fd: Long,
    __addr: CValuesRef<sockaddr>?,
    __len: CValuesRef<UIntVarOf<UInt>>?
): Int

@OptIn(ExperimentalForeignApi::class)
public expect class sockaddr_in6 : CStructVar

@OptIn(ExperimentalForeignApi::class)
public expect fun getsockname(
    __fd: Int,
    __addr: CValuesRef<sockaddr>?,
    __len: CValuesRef<UIntVarOf<UInt>>?
): Int

internal expect fun getAddressInfo(
    hostname: String,
    portInfo: Int
): List<NativeSocketAddress>

@OptIn(ExperimentalForeignApi::class)
internal expect fun send(descriptor: Long, bufferStart: CPointer<ByteVarOf<Byte>>?, size: Long, flags: Int): Long

@OptIn(ExperimentalForeignApi::class)
public expect fun accept(
    descriptor: Long,
    clientAddress: CPointer<sockaddr>,
    clientAddressLength: CPointer<UIntVarOf<UInt>>
): Long

@OptIn(ExperimentalForeignApi::class)
public expect fun setsockopt(
    socket: Long,
    level: Int,
    optionName: Int,
    optionValue: CValuesRef<*>,
    optionLen: socklen_t
): Int

@OptIn(ExperimentalForeignApi::class)
public expect fun recvfrom(
    socket: Long,
    buffer: CValuesRef<ByteVar>?,
    length: Long,
    flags: Int,
    srcAddr: CValuesRef<sockaddr>?,
    srcLen: CValuesRef<UIntVarOf<UInt>>?
): Long

@OptIn(ExperimentalForeignApi::class)
public expect fun sendto(
    socket: Long,
    buffer: CValuesRef<ByteVar>?,
    length: Long,
    flags: Int,
    destAddr: CValuesRef<sockaddr>?,
    destLen: Long
): Long

@OptIn(ExperimentalForeignApi::class)
public expect fun listen(socket: Long, backlog: Int): Int
