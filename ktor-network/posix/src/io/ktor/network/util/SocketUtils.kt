/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.util

import io.ktor.network.internal.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.experimental.*

@OptIn(ExperimentalForeignApi::class)
internal fun getLocalAddress(descriptor: Long): NativeSocketAddress = memScoped {
    val address = alloc<sockaddr_storage>()
    val length: UIntVarOf<UInt> = alloc()
    length.value = sizeOf<sockaddr_storage>().convert()

    getsockname(descriptor.convert(), address.ptr.reinterpret(), length.ptr).check()

    return@memScoped address.reinterpret<sockaddr>().toNativeSocketAddress()
}

@OptIn(ExperimentalForeignApi::class)
internal fun getRemoteAddress(descriptor: Long): NativeSocketAddress = memScoped {
    val address = alloc<sockaddr_storage>()
    val length: UIntVarOf<UInt> = alloc()
    length.value = sizeOf<sockaddr_storage>().convert()

    getpeername(descriptor, address.ptr.reinterpret(), length.ptr).check()

    return@memScoped address.reinterpret<sockaddr>().toNativeSocketAddress()
}

@OptIn(ExperimentalForeignApi::class)
internal fun addrinfo?.toIpList(): List<NativeSocketAddress> {
    var current: addrinfo? = this
    val result = mutableListOf<NativeSocketAddress>()

    while (current != null) {
        result += current.interop_ai_addr?.pointed?.toNativeSocketAddress()
            ?: throw IOException("Failed to resolve address")
        current = current.interop_ai_next?.pointed
    }

    return result
}

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
internal fun sockaddr.toNativeSocketAddress(): NativeSocketAddress {
    return when (sa_family.toInt()) {
        AF_INET -> {
            val address: sockaddr_in = ptr.reinterpret<sockaddr_in>().pointed
            NativeIPv4SocketAddress(
                address.interop_sin_family,
                address.sin_addr,
                networkToHostOrder(address.sin_port).toInt()
            )
        }

        AF_INET6 -> {
            val address = ptr.reinterpret<sockaddr_in6>().pointed
            NativeIPv6SocketAddress(
                address.interop_sin6_family,
                address.interop_sin6_addr,
                networkToHostOrder(address.interop_sin6_port).toInt(),
                address.interop_sin6_flowinfo,
                address.interop_sin6_scope_id
            )
        }

        AF_UNIX -> {
            unpack_sockaddr_un(this) { family, path ->
                NativeUnixSocketAddress(family.convert(), path)
            }
        }

        else -> error("Unknown address family $sa_family")
    }
}

@OptIn(ExperimentalNativeApi::class)
internal fun networkToHostOrder(value: UShort): UShort {
    if (!Platform.isLittleEndian) return value
    return value.reverseByteOrder()
}

@OptIn(ExperimentalNativeApi::class)
internal fun hostToNetworkOrder(value: UShort): UShort {
    if (!Platform.isLittleEndian) return value
    return value.reverseByteOrder()
}
